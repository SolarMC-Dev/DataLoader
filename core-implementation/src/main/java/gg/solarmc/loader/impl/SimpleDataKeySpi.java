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
import gg.solarmc.loader.data.DataKeySpi;

import java.util.Set;

/**
 * Simple {@link DataKeySpi} implementation for use by dependent data modules
 *
 */
public final class SimpleDataKeySpi implements DataKeySpi {

	private final DataKey<?, ?> key;

	/**
	 * Creates from a single key
	 *
	 * @param key the key
	 */
	public SimpleDataKeySpi(DataKey<?, ?> key) {
		this.key = key;
	}

	@Override
	public Iterable<DataKey<?, ?>> getKeys() {
		return Set.of(key);
	}
}
