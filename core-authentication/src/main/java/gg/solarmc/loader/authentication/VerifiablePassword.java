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

import java.util.Objects;

/**
 * A combination of hashed password, a salt used to help create the hash, and hashing instructions also used to create
 * the hash. <br>
 * <br>
 * Consider this a hashed password with instructions for how to recreate it given user input.
 */
public record VerifiablePassword(PasswordHash passwordHash,
								 PasswordSalt passwordSalt,
								 HashingInstructions instructions) {

	public VerifiablePassword {
		Objects.requireNonNull(passwordHash, "passwordHash");
		Objects.requireNonNull(passwordSalt, "passwordSalt");
		Objects.requireNonNull(instructions, "instructions");
	}

	/**
	 * Whether this password's hash matches another. Equivalent to {@code passwordHash().equals(other.passwordHash())}<br>
	 * <br>
	 * In practice, this will be {@code true} if {@code equals} is true, but note that {@code equals} also compares the
	 * salt and hashing instructions. Practically speaking, if the salt and hashing instructions differ, the password
	 * hash will also differ.
	 *
	 * @param other the other password
	 * @return true if matched, false otherwise
	 */
	public boolean matches(VerifiablePassword other) {
		return passwordHash.equals(other.passwordHash);
	}

}
