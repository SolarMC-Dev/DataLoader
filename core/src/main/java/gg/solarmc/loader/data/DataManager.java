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
 * Marker interface for data managers. A data manager is not associated with any
 * specific player. One such data manager exists for its data key. <br>
 * <br>
 * Data managers aggregate data for all players. For example, an economy system's
 * data manager might provide a method to find the top balances.
 */
public interface DataManager extends AutoCloseable {

	/**
	 * Refreshes cached in-memory data. <br>
	 * <br>
	 * The exact behavior of this method depends on the implementing data manager.
	 * No cached data is guaranteed be refreshed unless specifically documented.
	 *
	 * @param transaction a transaction with which to refresh
	 * @implSpec The default implementation does nothing
	 */
	default void refreshCaches(Transaction transaction) { }

	/**
	 * Clears cached in-memory data. This is primarily intended for testing purposes. <br>
	 * <br>
	 * The exact behavior of this method depends on the implementing data manager.
	 * No cached data is guaranteed be cleared unless specifically documented.
	 *
	 * @implSpec The default implementation does nothing
	 */
	default void clearCaches() { }

	@Override
	default void close() throws Exception { }
}
