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

package gg.solarmc.loader.impl;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataKeyInitializationContext;
import gg.solarmc.loader.data.DataKeySpi;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.data.DataObject;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

class DataGroupLoader {

	private final DataKeyInitializationContext context;

	DataGroupLoader(DataKeyInitializationContext context) {
		this.context = context;
	}

	Map<DataKey<?, ?>, DataGroup<?, ?>> loadGroups() {
		Map<DataKey<?, ?>, DataGroup<?, ?>> groups = new HashMap<>();
		ModuleLayer thisLayer = getClass().getModule().getLayer();
		if (thisLayer == null) {
			throw new IllegalStateException("Not in a named module");
		}
		for (DataKeySpi spi : ServiceLoader.load(thisLayer, DataKeySpi.class)) {
			for (DataKey<?, ?> key : spi.getKeys()) {
				groups.put(key, createGroup(key));
			}
		}
		return Map.copyOf(groups);
	}

	private <D extends DataObject, M extends DataManager> DataGroup<D, M> createGroup(DataKey<D, M> key) {
		M manager = key.createDataManager(context);
		return new DataGroup<>(key, manager, key.createLoader(manager, context));
	}

}
