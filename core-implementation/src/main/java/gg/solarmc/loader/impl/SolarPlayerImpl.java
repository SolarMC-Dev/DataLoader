package gg.solarmc.loader.impl;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.data.DataObject;

import java.util.Map;
import java.util.UUID;

class SolarPlayerImpl implements SolarPlayer {

    private final Map<DataKey<?, ?>, DataObject> storedData;
    private final int userID;
    private final UUID mcUUID;

    SolarPlayerImpl(Map<DataKey<?, ?>, DataObject> storedData, int userID, UUID mcUUID) {
        this.storedData = Map.copyOf(storedData);
        this.userID = userID;
        this.mcUUID = mcUUID;
    }

    @Override
    public int getUserID() {
        return userID;
    }

    @Override
    public UUID getMinecraftUUID() {
        return mcUUID;
    }

    @Override
    public <D extends DataObject> D getData(DataKey<D, ?> key) {
        @SuppressWarnings("unchecked")
        D data = (D) storedData.get(key);
        assert data != null : key;
        return data;
    }

}
