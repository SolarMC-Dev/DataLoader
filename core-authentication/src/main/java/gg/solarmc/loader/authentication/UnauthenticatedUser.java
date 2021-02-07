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

import java.util.UUID;

public interface UnauthenticatedUser {

	UUID mcUuid();

	String username();

	default boolean isPremium() {
		return UUIDOperations.isPremium(mcUuid());
	}

	/**
	 * Loads the data for the user, with the assumption that they have logged in before
	 *
	 * @param userId the user ID
	 * @param transaction the transaction
	 */
	void loadExistingData(int userId, Transaction transaction);

	/**
	 * Loads data for a brand new user
	 *
	 * @param userId the user ID
	 * @param transaction the transaction
	 */
	void loadNewData(int userId, Transaction transaction);

	/**
	 * Whether this user is equal to another
	 *
	 * @param o the object to determine equality with
	 * @return true if equal, false otherwise
	 */
	@Override
	boolean equals(Object o);

}
