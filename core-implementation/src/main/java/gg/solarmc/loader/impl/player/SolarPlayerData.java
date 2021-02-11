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

package gg.solarmc.loader.impl.player;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataObject;

import java.util.Map;

public final class SolarPlayerData {

	private final Map<DataKey<?, ?, ?>, DataObject> storedData;

	private static final SolarPlayerData EMPTY = new SolarPlayerData(Map.of());

	public SolarPlayerData(Map<DataKey<?, ?, ?>, DataObject> storedData) {
		this.storedData = Map.copyOf(storedData);
	}

	public static SolarPlayerData empty() {
		return EMPTY;
	}

	/**
	 * Gets data, asserting it to be online data
	 *
	 * @param key the key
	 * @param <D> the data object type
	 * @param <O> the offline data object type
	 * @return the online data
	 */
	public <D extends O, O extends DataObject> D getDataOnline(DataKey<D, O, ?> key) {
		return getData(key);
	}

	/**
	 * Gets data
	 *
	 * @param key the key
	 * @param <D> the data object type
	 * @param <O> the offline data object type
	 * @return the offline data
	 */
	public <D extends O, O extends DataObject> O getDataOffline(DataKey<D, O, ?> key) {
		return getData(key);
	}

	private <T> T getData(DataKey<?, ?, ?> key) {
		@SuppressWarnings("unchecked")
		T casted = (T) storedData.get(key);
		if (casted == null) {
			throw new IllegalStateException("No data found for key " + key);
		}
		return casted;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SolarPlayerData that = (SolarPlayerData) o;
		return storedData.equals(that.storedData);
	}

	@Override
	public int hashCode() {
		return storedData.hashCode();
	}

	@Override
	public String toString() {
		return "SolarPlayerData{" +
				"storedData=" + storedData +
				'}';
	}
}
