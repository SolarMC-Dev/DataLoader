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

package gg.solarmc.loader.data;

import gg.solarmc.loader.Transaction;

/**
 * Interface for data modules to implement; used by the framework
 *
 * @param <D> the data object
 * @param <O> the offline data object
 */
public interface DataLoader<D extends O, O extends DataObject> {

	/**
	 * Performs the data load for a specific user. Implementations should accomodate
	 * whether this is the first login or an existing one, and set default values
	 * if necessary.
	 *
	 * @param transaction the enclosing transaction
	 * @param userId the user's ID
	 * @return the online data object
	 */
	D loadData(Transaction transaction, int userId);

	/**
	 * Creates offline data for a specific user.
	 *
	 * @param userId the user's ID
	 * @return the offline data object
	 */
	O createOfflineData(int userId);

	/**
	 * Drops any and all data relating to this loader for all users. Used for testing purposes. <br>
	 * <br>
	 * A logical implementation is to truncate all tables.
	 *
	 * @param transaction the transaction
	 */
	void wipeAllData(Transaction transaction);

}
