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
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.data.DataObject;

import java.util.Map;
import java.util.UUID;

class SolarPlayerImpl implements SolarPlayer {

    private final Map<DataKey<?, ?>, DataObject> storedData;
    private final int userID;
    private final UUID mcUuid;

    SolarPlayerImpl(Map<DataKey<?, ?>, DataObject> storedData, int userID, UUID mcUuid) {
        this.storedData = Map.copyOf(storedData);
        this.userID = userID;
        this.mcUuid = mcUuid;
    }

    @Override
    public int getUserID() {
        return userID;
    }

    @Override
    public UUID getMinecraftUUID() {
        return mcUuid;
    }

    @Override
    public <D extends DataObject> D getData(DataKey<D, ?> key) {
        @SuppressWarnings("unchecked")
        D data = (D) storedData.get(key);
        assert data != null : "No data loaded at " + key;
        return data;
    }

}
