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

public final class HashingInstructions {

	private final int iterations;
	private final int memory;

	HashingInstructions(int iterations, int memory) {
		this.iterations = iterations;
		this.memory = memory;
	}

	/**
	 * Gets the amount of algorithm iterations
	 *
	 * @return the iteration
	 */
	public int iterations() {
		return iterations;
	}

	/**
	 * Gets the KB memory value used in the hashing algorithm
	 *
	 * @return the kb of memory to use
	 */
	public int memory() {
		return memory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HashingInstructions that = (HashingInstructions) o;
		return iterations == that.iterations && memory == that.memory;
	}

	@Override
	public int hashCode() {
		int result = iterations;
		result = 31 * result + memory;
		return result;
	}

	@Override
	public String toString() {
		return "PasswordDetails{" +
				"iterations=" + iterations +
				", memory=" + memory +
				'}';
	}
}
