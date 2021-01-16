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

import gg.solarmc.loader.data.DataLoader;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import java.nio.file.Path;

/**
 * A key used to retrieve certain player data
 *
 * @param <D> the data object type
 */
public interface DataKey<D> {

	DataLoader<D> createLoader(FactoryOfTheFuture futuresFactory, Path configFolder);

}
