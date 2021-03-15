/*
 * DataLoader
 * Copyright © 2021 SolarMC Developers
 *
 * DataLoader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DataLoader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DataLoader. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */

package gg.solarmc.loader.authentication;

import gg.solarmc.loader.Transaction;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Record6;
import org.slf4j.LoggerFactory;
import space.arim.omnibus.util.UUIDUtil;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static gg.solarmc.loader.schema.Routines.insertAutomaticAccountAndGetUserId;
import static gg.solarmc.loader.schema.Routines.migrateToPremiumAndGetUserId;
import static gg.solarmc.loader.schema.tables.AuthPasswords.*;
import static gg.solarmc.loader.schema.tables.LatestNames.*;
import static gg.solarmc.loader.schema.tables.UserIds.*;

/**
 * The center of authentication. There is a lot at play and at stake. Correct behavior
 * depends on both the caller and the implementor to carefully observe responsibilities. <br>
 * <br>
 * Authentication must support users joining multiple proxies concurrently, even users with the same
 * name. It must support new users and existing users, with a bias toward existing users. <br>
 * <br>
 * Note, in many cases, a "new user" can refer to a user who has a new name, but whose UUID
 * has been seen before, as can be the case for premium users. <br>
 * <br>
 * Implementors: keep in mind that MariaDB is case-insensitive.
 *
 */
public final class AuthenticationCenter {

	/*
	 * To understand the logic in this class, refer to its schema, which describes the multiple states
	 * a row in the passwords table may exist in.
	 * Authentication revolves around the concept of name ownership - who owns a name?
	 */

	private final PasswordHasher passwordHasher;

	private static final HashingInstructions DEFAULT_HASHING_INSTRUCTIONS = new HashingInstructions(10, 65536);

	AuthenticationCenter(PasswordHasher passwordHasher) {
		this.passwordHasher = passwordHasher;
	}

	/**
	 * Finds the UUID of a user who already exists and has the given name. <br>
	 * <br>
	 * The return type can indicate several possible states, which are explained
	 * in {@link AutoLoginPreparation.ResultType}. The name comparison is case
	 * insensitive when the query is performed, but as explained, there may be
	 * a user who has the same name ignoring case but a different name including case.
	 *
	 * @param transaction the transaction
	 * @param username the username, case insensitive to lookup
	 * @return the auto login preparation, indicating the existing user if found, and if
	 * the existing user is cracked, the cracked user's password
	 */
	public AutoLoginPreparation findExistingUserWithName(Transaction transaction, String username) {
		Objects.requireNonNull(username, "username");
		DSLContext context = transaction.getProperty(DSLContext.class);
		/*
		 * It is acceptable to rely on the fact that any entry in the auth passwords
		 * table will be matched by a corresponding one in the name history table.
		 * The reason is because as soon as an account is created, the user's data
		 * is also loaded, and their name history is catalogued, all within the
		 * same transaction.
		 *
		 * This query also relies on the case insensitivity of MariaDB
		 */
		Record6<byte[], String, Byte, Integer, byte[], byte[]> accountRecord = context
				.select(LATEST_NAMES.UUID, LATEST_NAMES.USERNAME,
						AUTH_PASSWORDS.ITERATIONS, AUTH_PASSWORDS.MEMORY,
						AUTH_PASSWORDS.PASSWORD_HASH, AUTH_PASSWORDS.PASSWORD_SALT)
				.from(LATEST_NAMES)
				.innerJoin(AUTH_PASSWORDS)
				.on(AUTH_PASSWORDS.USERNAME.eq(LATEST_NAMES.USERNAME))
				.where(LATEST_NAMES.USERNAME.eq(username))
				.fetchOne();
		if (accountRecord == null) {
			return AutoLoginPreparation.noneFound();
		}
		UserDetails userDetails = new UserDetails(
				UUIDUtil.fromByteArray(accountRecord.get(LATEST_NAMES.UUID)),
				accountRecord.get(LATEST_NAMES.USERNAME));
		if (userDetails.isPremium()) {
			assert accountRecord.get(AUTH_PASSWORDS.ITERATIONS) == 0 : "premium accounts use iterations=0";
			return AutoLoginPreparation.forExistingPremiumUser(userDetails);
		}
		if (!userDetails.username().equals(username)) {
			return AutoLoginPreparation.deniedCaseSensitivityOfName();
		}
		return AutoLoginPreparation.forExistingCrackedUser(
				userDetails,
				new VerifiablePassword(
						new PasswordHash(accountRecord.get(AUTH_PASSWORDS.PASSWORD_HASH)),
						new PasswordSalt(accountRecord.get(AUTH_PASSWORDS.PASSWORD_SALT)),
						new HashingInstructions(
								accountRecord.get(AUTH_PASSWORDS.ITERATIONS),
								accountRecord.get(AUTH_PASSWORDS.MEMORY))
				));
	}

	/**
	 * Determines whether the new user needs to enter a password. The user is deemed new on the
	 * basis that {@link #findExistingUserWithName(Transaction, String)} does not find an existing
	 * user. <br>
	 * <br>
	 *  By now, the caller should either have a premium user who has authenticated via the login protocol,
	 * or a cracked user. If the user is premium, they may still need to enter a password if there is
	 * a cracked account with the same name. If the user is cracked, they will need to enter a password,
	 * but it is also possible they are denied for other reasons.
	 *
	 * @param transaction the transaction
	 * @param user the unauthenticated user
	 * @return the login attempt result
	 */
	public LoginAttempt attemptLoginFromNewUsername(Transaction transaction, UserWithDataNotYetLoaded user) {
		DSLContext context = transaction.getProperty(DSLContext.class);

		Record5<String, Byte, Integer, byte[], byte[]> passwordRecord = context
				.select(AUTH_PASSWORDS.USERNAME,
						AUTH_PASSWORDS.ITERATIONS, AUTH_PASSWORDS.MEMORY,
						AUTH_PASSWORDS.PASSWORD_HASH, AUTH_PASSWORDS.PASSWORD_SALT)
				.from(AUTH_PASSWORDS)
				.where(AUTH_PASSWORDS.USERNAME.eq(user.username()))
				.fetchOne();
		if (passwordRecord == null) {
			if (user.isPremium()) {
				// New premium username, insert empty password and permit login
				insertAutomaticAccount(transaction, user);
				return LoginAttempt.premiumPermitted();
			}
			// New cracked username, request account creation
			return LoginAttempt.needsAccount();
		}
		LoggerFactory.getLogger(getClass()).info(
				"An incredible race condition occurred (or this is a bug). User {} logged in " +
				"while no one owned their username. Now, however, someone has taken their username.",
				user.username());
		int iterations = passwordRecord.get(AUTH_PASSWORDS.ITERATIONS);
		if (iterations == 0) { // 0 indicates a premium account
			/*
			Existing premium/automatic account
			 */
			if (!user.isPremium()) {
				// User is cracked, but is trying to use a premium user's name
				return LoginAttempt.deniedPremiumTookName();
			}
			/*
			 * The same premium user is involved with a race condition among themselves?
			 * What sorcery is this? Multiple people on the same account?
			 * In any case, we can actually permit this user to join. They may see
			 * outdated cached values for their user stats if they play at the same time;
			 * however, this is no concern for the reliability of their actual data
			 * stored in the database. We guarantee complete data reliability.
			 */
			int userId = fetchIdFromUuid(context, user);
			user.loadData(transaction, userId);
			return LoginAttempt.premiumPermitted();
		}
		// Existing cracked account
		if (!user.username().equals(passwordRecord.get(AUTH_PASSWORDS.USERNAME))) {
			return LoginAttempt.deniedCaseSensitivityOfName();
		}
		VerifiablePassword existingPassword = new VerifiablePassword(
				new PasswordHash(passwordRecord.get(AUTH_PASSWORDS.PASSWORD_HASH)),
				new PasswordSalt(passwordRecord.get(AUTH_PASSWORDS.PASSWORD_SALT)),
				new HashingInstructions(iterations, passwordRecord.get(AUTH_PASSWORDS.MEMORY))
		);
		return LoginAttempt.needsPassword(existingPassword);
	}

	private Integer fetchNullableIdFromUuid(DSLContext context, UserWithDataNotYetLoaded user) {
		byte[] uuidBytes = UUIDUtil.toByteArray(user.mcUuid());
		return context.select(USER_IDS.ID)
					  .from(USER_IDS)
					  .where(USER_IDS.UUID.eq(uuidBytes))
					  .fetchOne(USER_IDS.ID);
	}

	private int fetchIdFromUuid(DSLContext context, UserWithDataNotYetLoaded user) {
		Integer userId = fetchNullableIdFromUuid(context, user);
		if (userId == null) {
			throw new IllegalStateException("User " + user + " does not have an ID");
		}
		return userId;
	}

	private int userIdFromRecord(Record1<Integer> userIdRecord, UserWithDataNotYetLoaded user) {
		Integer userId;
		if (userIdRecord == null || (userId = userIdRecord.value1()) == null) {
			throw new IllegalStateException("Unable to get user ID for user " + user);
		}
		return userId;
	}

	/**
	 * Creates an automatic account. Called only for premium users
	 * @param transaction the transaction
	 * @param user the user
	 */
	private void insertAutomaticAccount(Transaction transaction, UserWithDataNotYetLoaded user) {
		DSLContext context = transaction.getProperty(DSLContext.class);
		byte[] uuidBytes = UUIDUtil.toByteArray(user.mcUuid());
		String username = user.username();
		Record1<Integer> userIdRecord = context
				.select(insertAutomaticAccountAndGetUserId(uuidBytes, username))
				.fetchOne();
		int userId = userIdFromRecord(userIdRecord, user);
		user.loadData(transaction, userId);
	}

	/**
	 * Creates the user's account. Should only be called when {@link LoginAttempt.ResultType#NEEDS_ACCOUNT}
	 * is encountered
	 *
	 * @param transaction the transaction
	 * @param user the unauthenticated user
	 * @param password the verifiable password
	 * @return a create account result indicating success or failure
	 */
	public CreateAccountResult createAccount(Transaction transaction,
											 UserWithDataNotYetLoaded user, VerifiablePassword password) {
		DSLContext context = transaction.getProperty(DSLContext.class);
		HashingInstructions instructions = password.instructions();

		int updateCount = context
				.insertInto(AUTH_PASSWORDS)
				.columns(AUTH_PASSWORDS.USERNAME,
						AUTH_PASSWORDS.ITERATIONS, AUTH_PASSWORDS.MEMORY,
						AUTH_PASSWORDS.PASSWORD_HASH, AUTH_PASSWORDS.PASSWORD_SALT)
				.values(user.username(),
						(byte) instructions.iterations(), instructions.memory(),
						password.passwordHash().hashUncloned(), password.passwordSalt().saltUncloned())
				.onDuplicateKeyIgnore()
				.execute();
		if (updateCount == 0) {
			return CreateAccountResult.CONFLICT;
		}
		Record1<Integer> userIdRecord = context
				.insertInto(USER_IDS)
				.columns(USER_IDS.UUID)
				.values(UUIDUtil.toByteArray(user.mcUuid()))
				.returningResult(USER_IDS.ID)
				.fetchOne();
		int userId = userIdFromRecord(userIdRecord, user);
		user.loadData(transaction, userId);
		return CreateAccountResult.CREATED;
	}

	/**
	 * Assuming the user has entered the password correctly, complete their login. <br>
	 * <br>
	 * The user may in fact be premium or cracked. If the joining user is premium,
	 * their data will be migrated
	 *
	 * @param transaction the transaction
	 * @param user the user
	 * @return a login result indicating whether migration was performend
	 */
	public CompleteLoginResult completeLoginAndPossiblyMigrate(Transaction transaction, UserWithDataNotYetLoaded user) {
		DSLContext context = transaction.getProperty(DSLContext.class);
		if (user.isPremium()) {
			// Migrate previous cracked account to premium
			String username = user.username();
			UUID offlineUuid = computeOfflineUuid(username);
			byte[] offlineUuidBytes = UUIDUtil.toByteArray(offlineUuid);
			byte[] onlineUuidBytes = UUIDUtil.toByteArray(user.mcUuid());

			Record1<Integer> userIdRecord = context
					.select(migrateToPremiumAndGetUserId(offlineUuidBytes, onlineUuidBytes, username))
					.fetchOne();

			int userId = userIdFromRecord(userIdRecord, user);
			user.loadData(transaction, userId);
			return CompleteLoginResult.MIGRATED_TO_PREMIUM;
		}
		assert user.mcUuid().equals(computeOfflineUuid(user.username()))
				: "user is cracked";

		Integer userId = fetchNullableIdFromUuid(context, user);
		if (userId == null) {
			return CompleteLoginResult.USER_ID_MISSING;
		}
		user.loadData(transaction, userId);
		return CompleteLoginResult.NORMAL;
	}

	/**
	 * Calculates the offline uuid for a username. Case sensitive with regards
	 * to the username.
	 *
	 * @param username the username
	 * @return the offline uuid
	 */
	public UUID computeOfflineUuid(String username) {
		return UUIDOperations.computeOfflineUuid(username);
	}

	/**
	 * Hashes a user password, use this for creating a new account. <br>
	 * Recommended to run on the common pool - {@link ForkJoinPool#commonPool()}.
	 *
	 * @param password the password
	 * @return the verifiable password
	 */
	public VerifiablePassword hashNewPassword(String password) {
		// Create a new salt and use it to hash this password
		return hashPassword(password, passwordHasher.generateSalt(), DEFAULT_HASHING_INSTRUCTIONS);
	}

	/**
	 * Hashes a user password. Recommended to run on the common pool - {@link ForkJoinPool#commonPool()}.
	 *
	 * @param password the password
	 * @param salt the password salt
	 * @param instructions the hashing instructions
	 * @return the verifiable password
	 */
	public VerifiablePassword hashPassword(String password, PasswordSalt salt, HashingInstructions instructions) {
		var hash = passwordHasher.hashPassword(password, salt, instructions);
		return new VerifiablePassword(hash, salt, instructions);
	}

}
