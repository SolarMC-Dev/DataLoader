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

import gg.solarmc.loader.DataCenter;

/**
 * Marker interface representing a container that caches mutable data. <br>
 * <br>
 * Usually, a data object contains some cached/displayable read-only data, values of which
 * should not be relied upon for correctness. This data is available via getters. <br>
 * <br>
 * A data object also exposes methods to manipulate the data itself via a transactional API.
 * {@link DataCenter} should be used to run such transactions.
 */
public interface DataObject {

}
