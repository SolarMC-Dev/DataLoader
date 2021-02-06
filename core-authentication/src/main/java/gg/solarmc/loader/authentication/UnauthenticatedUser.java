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
import java.util.UUID;

public final class UnauthenticatedUser {

	private final UUID mcUuid;
	private final String username;

	public UnauthenticatedUser(UUID mcUuid, String username) {
		this.mcUuid = Objects.requireNonNull(mcUuid);
		this.username = Objects.requireNonNull(username);
	}

	boolean isPremium() {
		return UUIDOperations.isPremium(mcUuid);
	}

	public UUID mcUuid() {
		return mcUuid;
	}

	public String username() {
		return username;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UnauthenticatedUser that = (UnauthenticatedUser) o;
		return mcUuid.equals(that.mcUuid) && username.equals(that.username);
	}

	@Override
	public int hashCode() {
		int result = mcUuid.hashCode();
		result = 31 * result + username.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "UnauthenticatedUser{" +
				"mcUuid=" + mcUuid +
				", username='" + username + '\'' +
				'}';
	}
}
