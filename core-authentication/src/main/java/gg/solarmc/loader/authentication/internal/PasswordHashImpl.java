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

import gg.solarmc.loader.authentication.PasswordHash;

import java.util.Arrays;
import java.util.Objects;

public final class PasswordHashImpl implements PasswordHash {

	private final byte[] hash;

	public PasswordHashImpl(byte[] hash) {
		this.hash = Objects.requireNonNull(hash);
	}

	@Override
	public byte[] hash() {
		return hash.clone();
	}

	public static byte[] hashUncloned(PasswordHash hash) {
		return ((PasswordHashImpl) hash).hash;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PasswordHashImpl that = (PasswordHashImpl) o;
		return Arrays.equals(hash, that.hash);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(hash);
	}

	@Override
	public String toString() {
		return "HashedPassword{" +
				"hash=" + Arrays.toString(hash) +
				'}';
	}
}
