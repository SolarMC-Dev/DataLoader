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

import java.util.Arrays;
import java.util.Objects;

public final class PasswordSalt {

	private final byte[] salt;

	public PasswordSalt(byte[] salt) {
		this.salt = Objects.requireNonNull(salt);
	}

	public byte[] salt() {
		return salt.clone();
	}

	byte[] saltUncloned() {
		return salt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PasswordSalt that = (PasswordSalt) o;
		return Arrays.equals(salt, that.salt);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(salt);
	}

	@Override
	public String toString() {
		return "PasswordSalt{" +
				"salt=" + Arrays.toString(salt) +
				'}';
	}
}
