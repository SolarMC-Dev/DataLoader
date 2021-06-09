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
import gg.solarmc.loader.authentication.internal.UUIDOperations;

import java.util.UUID;

/**
 * A user whose data has not been loaded yet. The user may not be authenticated either
 *
 */
public interface UserWithDataNotYetLoaded {

	UUID mcUuid();

	String username();

	default boolean isPremium() {
		return UUIDOperations.isPremium(mcUuid());
	}

	/**
	 * Loads the data for the user
	 *
	 * @param transaction the transaction
	 * @param userId the user ID
	 */
	void loadData(Transaction transaction, int userId);

	/**
	 * Whether this user is equal to another
	 *
	 * @param o the object to determine equality with
	 * @return true if equal, false otherwise
	 */
	@Override
	boolean equals(Object o);

}
