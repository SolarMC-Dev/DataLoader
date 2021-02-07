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
import gg.solarmc.loader.impl.SQLExceptionHandler;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record5;
import space.arim.omnibus.util.UUIDUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static gg.solarmc.loader.schema.Tables.AUTH_PASSWORDS;
import static gg.solarmc.loader.schema.Tables.USER_IDS;

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
	 * Attempts the first step in the login process. Will auto-login premium users
	 *
	 * @param transaction the transaction
	 * @param user the unauthenticated user
	 * @return the login attempt result
	 */
	public InitialLoginAttempt attemptInitialLogin(Transaction transaction, UnauthenticatedUser user) {
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
				// New premium user, insert empty password and permit login
				insertAutomaticAccount(transaction, user);
				return InitialLoginAttempt.premiumPermitted();
			}
			// New cracked user, request account creation
			return InitialLoginAttempt.needsAccount();
		}
		int iterations = passwordRecord.get(AUTH_PASSWORDS.ITERATIONS);
		if (iterations == 0) { // 0 indicates a premium account
			/*
			Existing premium/automatic account
			 */
			if (!user.isPremium()) {
				// User is cracked, but is trying to use a premium user's name
				return InitialLoginAttempt.deniedPremiumTookName();
			}
			// User must be permitted. This has to be the same premium user
			int userId = fetchIdFromUuid(context, user);
			user.loadExistingData(userId, transaction);
			return InitialLoginAttempt.premiumPermitted();
		}
		// Existing cracked account

		if (!user.username().equals(passwordRecord.get(AUTH_PASSWORDS.USERNAME))) {
			/*
			MariaDB is case-insensitive, which is why we were able to select this password.
			However, we cannot permit multiple cracked users to have the same name ignoring case.
			For premium users, it's OK because all we need to do is check if the UUID is premium.
			For cracked users, it's not okay because the cracked users will have different UUIDs.
			 */
			return InitialLoginAttempt.deniedCaseSensitivityOfName();
		}
		VerifiablePassword existingPassword = new VerifiablePassword(
				new PasswordHash(passwordRecord.get(AUTH_PASSWORDS.PASSWORD_HASH)),
				new PasswordSalt(passwordRecord.get(AUTH_PASSWORDS.PASSWORD_SALT)),
				new HashingInstructions(iterations, passwordRecord.get(AUTH_PASSWORDS.MEMORY))
		);
		return InitialLoginAttempt.needsPassword(existingPassword);
	}

	private int fetchIdFromUuid(DSLContext context, UnauthenticatedUser user) {
		return fetchIdFromUuid(context, UUIDUtil.toByteArray(user.mcUuid()), user);
	}

	private int fetchIdFromUuid(DSLContext context, byte[] uuidBytes, UnauthenticatedUser user) {
		Integer userId = context.select(USER_IDS.ID)
				.from(USER_IDS)
				.where(USER_IDS.UUID.eq(uuidBytes))
				.fetchOne(USER_IDS.ID);
		if (userId == null) {
			throw new IllegalStateException("User " + user + " does not have an ID");
		}
		return userId;
	}

	/**
	 * Creates an automatic account. Called only for premium users
	 * @param transaction the transaction
	 * @param user the user
	 */
	private void insertAutomaticAccount(Transaction transaction, UnauthenticatedUser user) {
		DSLContext context = transaction.getProperty(DSLContext.class);
		context.insertInto(AUTH_PASSWORDS)
				// iterations and memory, being omitted, will be set to 0
				.columns(AUTH_PASSWORDS.USERNAME, AUTH_PASSWORDS.PASSWORD_HASH, AUTH_PASSWORDS.PASSWORD_SALT)
				.values(user.username(), passwordHasher.emptyHash(), passwordHasher.emptySalt())
				.execute();
		byte[] uuidBytes = UUIDUtil.toByteArray(user.mcUuid());
		int updateCount;
		int userId;
		/*
		JDBC is the only way to retrieve both the update count and the generated ID
		at the same time
		 */
		try (PreparedStatement preparedStatement = transaction.getProperty(Connection.class).prepareStatement(
				"INSERT IGNORE INTO user_ids (uuid) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setBytes(1, uuidBytes);
			updateCount = preparedStatement.executeUpdate();
			if (updateCount == 0) {
				preparedStatement.close(); // early release
				userId = fetchIdFromUuid(context, uuidBytes, user);
			} else {
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (!generatedKeys.next()) {
						throw new IllegalStateException("Unable to generate ID for inserted user " + user);
					}
					userId = generatedKeys.getInt("id");
				}
			}
		} catch (SQLException ex) {
			throw transaction.getProperty(SQLExceptionHandler.class).handle(ex);
		}
		if (updateCount == 0) {
			// Premium user has joined before, with a different name
			user.loadExistingData(userId, transaction);
		} else {
			// Totally new user
			user.loadNewData(userId, transaction);
		}
	}

	/**
	 * Creates the user's account. Should only be called when {@link InitialLoginAttempt.ResultType#NEEDS_ACCOUNT}
	 * is encountered
	 *
	 * @param transaction the transaction
	 * @param user the unauthenticated user
	 * @param password the verifiable password
	 * @return a create account result indicating success or failure
	 */
	public CreateAccountResult createAccount(Transaction transaction,
											 UnauthenticatedUser user, VerifiablePassword password) {
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
		Integer userId;
		Record1<Integer> userIdRecord = context.insertInto(USER_IDS)
				.columns(USER_IDS.UUID)
				.values(UUIDUtil.toByteArray(user.mcUuid()))
				.returningResult(USER_IDS.ID)
				.fetchOne();
		if (userIdRecord == null || (userId = userIdRecord.value1()) == null) {
			throw new IllegalStateException("Unable to generate user ID for user " + user);
		}
		user.loadNewData(userId, transaction);
		return CreateAccountResult.CREATED;
	}

	/**
	 * Assuming the user has entered the password correctly, complete their login. <br>
	 * <br>
	 * Will migrate the user's previous data if they are now premium, but used to be cracked
	 *
	 * @param transaction the transaction
	 * @param user the user
	 * @return a login result indicating whether migration was performend
	 */
	public CompleteLoginResult completeLoginAndPossiblyMigrate(Transaction transaction, UnauthenticatedUser user) {
		DSLContext context = transaction.getProperty(DSLContext.class);
		if (user.isPremium()) {
			// Migrate previous cracked account to premium
			byte[] currentUuidBytes = UUIDUtil.toByteArray(user.mcUuid());
			{
				UUID offlineUuid = UUIDOperations.computeOfflineUuid(user.username());
				int updateCountUserIds = context
						.update(USER_IDS)
						.set(USER_IDS.UUID, currentUuidBytes)
						.where(USER_IDS.UUID.eq(UUIDUtil.toByteArray(offlineUuid)))
						.execute();
				assert updateCountUserIds == 1;
			}
			{
				int updateCountAuthPasswords = context
						.update(AUTH_PASSWORDS)
						.set(AUTH_PASSWORDS.ITERATIONS, (byte) 0)
						.set(AUTH_PASSWORDS.MEMORY, 0)
						.set(AUTH_PASSWORDS.PASSWORD_HASH, passwordHasher.emptyHash())
						.set(AUTH_PASSWORDS.PASSWORD_SALT, passwordHasher.emptySalt())
						.where(AUTH_PASSWORDS.USERNAME.eq(user.username()))
						.execute();
				assert updateCountAuthPasswords == 1;
			}
			int userId = fetchIdFromUuid(context, currentUuidBytes, user);
			user.loadExistingData(userId, transaction);

			return CompleteLoginResult.MIGRATED_TO_PREMIUM;
		}
		assert user.mcUuid().equals(UUIDOperations.computeOfflineUuid(user.username()))
				: "user is cracked";
		int userId = fetchIdFromUuid(context, user);
		user.loadExistingData(userId, transaction);
		return CompleteLoginResult.NORMAL;
	}

	/**
	 * Hashes a user password, use this for creating a new account. <br>
	 * Recommended to run on the common pool {@link ForkJoinPool#commonPool()}.
	 *
	 * @param password the password
	 * @return the verifiable password
	 */
	public VerifiablePassword hashNewPassword(String password) {
		// Create a new salt and use it to hash this password
		return hashPassword(password, passwordHasher.generateSalt(), DEFAULT_HASHING_INSTRUCTIONS);
	}

	/**
	 * Hashes a user password. Recommended to run on the common pool {@link ForkJoinPool#commonPool()}.
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
