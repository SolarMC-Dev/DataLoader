package gg.solarmc.loader.kitpvp;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataLoader;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import java.nio.file.Path;

public class KitPvpKey implements DataKey<KitPvp> {

    @Override
    public DataLoader<KitPvp> createLoader(FactoryOfTheFuture futuresFactory, Path configFolder) {
        return new KitPvpLoader();
    }
}
