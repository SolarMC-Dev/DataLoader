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

package gg.solarmc.loader.impl.player;

import java.util.Objects;
import java.util.UUID;

public final class SolarPlayerId {

	private final int userId;
	private final UUID mcUuid;

	public SolarPlayerId(int userId, UUID mcUuid) {
		this.userId = userId;
		this.mcUuid = Objects.requireNonNull(mcUuid);
	}

	public int userId() {
		return userId;
	}

	public UUID mcUuid() {
		return mcUuid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SolarPlayerId that = (SolarPlayerId) o;
		return userId == that.userId && mcUuid.equals(that.mcUuid);
	}

	@Override
	public int hashCode() {
		int result = userId;
		result = 31 * result + mcUuid.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "SolarPlayerId{" +
				"userId=" + userId +
				", mcUuid=" + mcUuid +
				'}';
	}
}
