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

package gg.solarmc.loader;

public interface DataLoader<D> {

	/**
	 * Performs the initial data loader for a new user.
	 *
	 * @return the data object with default values
	 */
	D createDefaultData();

	/**
	 * Performs the initial data load for a specific user ID. Usually this is used
	 * to load cachable, read-only data. Writable data should use transaction/future
	 * based methods on the data object.
	 *
	 * @param transaction the enclosing transaction
	 * @param userId the user ID
	 * @return the loaded data
	 */
	D loadData(Transaction transaction, int userId);

}
