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

package gg.solarmc.loader.authentication.internal;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import gg.solarmc.loader.authentication.HashingInstructions;
import gg.solarmc.loader.authentication.PasswordHash;
import gg.solarmc.loader.authentication.PasswordSalt;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class PasswordHasher {

	private final Argon2Advanced argon2;

	public static final int HASH_LENGTH = 64;
	public static final int SALT_LENGTH = 32;

	private PasswordHasher(Argon2Advanced argon2) {
		this.argon2 = Objects.requireNonNull(argon2, "argon2");
	}

	public static PasswordHasher create() {
		return new PasswordHasher(Argon2Factory.createAdvanced(
				Argon2Factory.Argon2Types.ARGON2i, PasswordHasher.SALT_LENGTH, PasswordHasher.HASH_LENGTH));
	}

	public PasswordHash hashPassword(String password, PasswordSalt salt, HashingInstructions instructions) {
		byte[] hash = argon2.rawHash(
					instructions.iterations(), instructions.memory(), 1,
					password.getBytes(StandardCharsets.UTF_8), PasswordSaltImpl.saltUncloned(salt));
		assert hash.length == HASH_LENGTH : hash.length;
		return new PasswordHashImpl(hash);
	}

	public PasswordSalt generateSalt() {
		byte[] salt = argon2.generateSalt();
		assert salt.length == SALT_LENGTH;
		return new PasswordSaltImpl(salt);
	}

}
