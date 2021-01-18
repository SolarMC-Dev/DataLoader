package gg.solarmc.loader.kitpvp;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataKeyInitializationContext;
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.data.DataManager;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import java.nio.file.Path;

public class KitPvpKey implements DataKey<KitPvp, KitPvpManager> {

    @Override
    public DataLoader<KitPvp> createLoader(KitPvpManager dataManager, DataKeyInitializationContext context) {
        return new KitPvpLoader();
    }

    @Override
    public KitPvpManager createDataManager(DataKeyInitializationContext context) {
        return null;
    }
}
