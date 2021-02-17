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

package gg.solarmc.loader.impl.launch;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.data.DataObject;

public class DataGroup<D extends O, O extends DataObject, M extends DataManager> {

	private final DataKey<D, O, ?> key;
	private final M manager;
	private final DataLoader<D, O> loader;

	public DataGroup(DataKey<D, O, ?> key, M manager, DataLoader<D, O> loader) {
		this.key = key;
		this.manager = manager;
		this.loader = loader;
	}

	public DataKey<D, O, ?> key() {
		return key;
	}

	public M manager() {
		return manager;
	}

	public DataLoader<D, O> loader() {
		return loader;
	}
}
