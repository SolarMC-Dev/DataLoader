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

import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.impl.SQLTransaction;
import gg.solarmc.loader.kitpvp.kit.Kit;

import java.util.HashSet;

import static gg.solarmc.loader.schema.tables.KitpvpStatistics.*;

// TODO all of this
public class KitPvpLoader implements DataLoader<KitPvp> {

    private final KitPvpManager manager;

    public KitPvpLoader(KitPvpManager manager) {
        this.manager = manager;
    }

    @Override
    public KitPvp createDefaultData(Transaction transaction, int userId) {
        //TODO: implement configuration for default kits

        HashSet<Kit> defaultKits = new HashSet<>();

        var jooq = ((SQLTransaction) transaction).jooq();
        jooq.insertInto(KITPVP_STATISTICS)
                .columns(KITPVP_STATISTICS.USER_ID, KITPVP_STATISTICS.KILLS,KITPVP_STATISTICS.DEATHS,KITPVP_STATISTICS.ASSISTS)
                .values(userId,0,0,0)
                .execute();

        return new KitPvp(userId,0,0,0,new HashSet<>(),manager);
    }

    @Override
    public KitPvp loadData(Transaction transaction, int userId) {
        // SELECT * FROM pvpstats WHERE user_id = ?
        //TODO improve query

        return null;
    }

}
