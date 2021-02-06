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

public final class VerifiablePassword {

	private final PasswordHash passwordHash;
	private final PasswordSalt passwordSalt;
	private final HashingInstructions instructions;

	VerifiablePassword(PasswordHash passwordHash, PasswordSalt passwordSalt, HashingInstructions instructions) {
		this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
		this.passwordSalt = Objects.requireNonNull(passwordSalt, "passwordSalt");
		this.instructions = Objects.requireNonNull(instructions, "instructions");
	}

	public PasswordHash passwordHash() {
		return passwordHash;
	}

	public PasswordSalt passwordSalt() {
		return passwordSalt;
	}

	public HashingInstructions instructions() {
		return instructions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VerifiablePassword that = (VerifiablePassword) o;
		return passwordHash.equals(that.passwordHash) && passwordSalt.equals(that.passwordSalt) && instructions.equals(that.instructions);
	}

	@Override
	public int hashCode() {
		int result = passwordHash.hashCode();
		result = 31 * result + passwordSalt.hashCode();
		result = 31 * result + instructions.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "VerifiablePassword{" +
				"passwordHash=" + passwordHash +
				", passwordSalt=" + passwordSalt +
				", instructions=" + instructions +
				'}';
	}
}
