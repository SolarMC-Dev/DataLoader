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

import de.mkammerer.argon2.Argon2Advanced;

import java.nio.charset.StandardCharsets;

class PasswordHasher {

	private final Argon2Advanced argon2;

	static final int HASH_LENGTH = 64;
	static final int SALT_LENGTH = 32;

	PasswordHasher(Argon2Advanced argon2) {
		this.argon2 = argon2;
	}

	PasswordHash hashPassword(String password, PasswordSalt salt, HashingInstructions instructions) {
		byte[] hash = argon2.rawHash(
					instructions.iterations(), instructions.memory(), 1,
					password.getBytes(StandardCharsets.UTF_8), salt.saltUncloned());
		assert hash.length == HASH_LENGTH : hash.length;
		return new PasswordHash(hash);
	}

	byte[] emptyHash() {
		return new byte[HASH_LENGTH];
	}

	PasswordSalt generateSalt() {
		byte[] salt = argon2.generateSalt();
		assert salt.length == SALT_LENGTH;
		return new PasswordSalt(salt);
	}

	byte[] emptySalt() {
		return new byte[SALT_LENGTH];
	}

}
