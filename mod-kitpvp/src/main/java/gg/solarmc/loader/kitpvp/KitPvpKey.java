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

package gg.solarmc.loader.kitpvp;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataKeyInitializationContext;
import gg.solarmc.loader.data.DataLoader;

import java.util.ServiceLoader;

public class KitPvpKey implements DataKey<OnlineKitPvp, KitPvp, KitPvpManager> {

    public static final KitPvpKey INSTANCE = new KitPvpKey();

    KitPvpKey() {}

    @Override
    public DataLoader<OnlineKitPvp,KitPvp> createLoader(KitPvpManager dataManager, DataKeyInitializationContext context) {
        return new KitPvpLoader(dataManager);
    }

    @Override
    public KitPvpManager createDataManager(DataKeyInitializationContext context) {
        return new KitPvpManager(context.omnibus().getRegistry().getProvider(ItemSerializer.class)
                .orElseGet(this::serviceLoadItemSerializer));
    }

    private ItemSerializer serviceLoadItemSerializer() {
        ModuleLayer layer = getClass().getModule().getLayer();
        if (layer == null) {
            throw new IllegalStateException("Not in a module layer. For classpath-based testing, " +
                    "adding a provider for ItemSerializer to the Omnibus Registry is supported");
        }
        return ServiceLoader.load(layer, ItemSerializer.class).findFirst()
                .orElseThrow(() -> new IllegalStateException("No ItemSerializer SPI available"));
    }

}
