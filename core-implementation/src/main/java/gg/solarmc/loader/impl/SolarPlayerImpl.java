package gg.solarmc.loader.impl;

import gg.solarmc.loader.DataKey;
import gg.solarmc.loader.SolarPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SolarPlayerImpl implements SolarPlayer {

    private final Map<DataKey<?>,?> storedData = new ConcurrentHashMap<>();
    private final int userID;

    public SolarPlayerImpl(int userID) {
        this.userID = userID;
    }

    @Override
    public int getUserID() {
        return 0; //implement
    }

    @Override
    public UUID getMinecraftUUID() {

        //TODO implement

        return null;
    }

    @Override
    public <D> D getData(DataKey<D> key) {
        return (D) storedData.get(key);
    }

    public static class SolarPlayerImplBuilder {
        //figure this out
    }
}
