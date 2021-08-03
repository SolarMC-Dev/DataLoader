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

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.schema.tables.records.KitpvpStatisticsRecord;
import org.jooq.DSLContext;

import static gg.solarmc.loader.schema.tables.KitpvpKitsContents.KITPVP_KITS_CONTENTS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsCooldowns.KITPVP_KITS_COOLDOWNS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsIds.KITPVP_KITS_IDS;
import static gg.solarmc.loader.schema.tables.KitpvpKitsOwnership.KITPVP_KITS_OWNERSHIP;
import static gg.solarmc.loader.schema.tables.KitpvpStatistics.KITPVP_STATISTICS;

class KitPvpLoader implements DataLoader<OnlineKitPvp, KitPvp> {

    private final KitPvpManager manager;

    KitPvpLoader(KitPvpManager manager) {
        this.manager = manager;
    }

    @Override
    public OnlineKitPvp loadData(Transaction transaction, int userId) {
        DSLContext context = transaction.getProperty(DSLContext.class);
        KitpvpStatisticsRecord kitpvpRecord = context
                .fetchOne(KITPVP_STATISTICS, KITPVP_STATISTICS.USER_ID.eq(userId));
        if (kitpvpRecord != null) {
            return new OnlineKitPvp(
                    userId, manager,
                    kitpvpRecord.getKills(), kitpvpRecord.getDeaths(),
                    kitpvpRecord.getAssists(), kitpvpRecord.getExperience(),
                    kitpvpRecord.getCurrentKillstreak(), kitpvpRecord.getHighestKillstreak(),
                    kitpvpRecord.getBounty());
        }
        context.insertInto(KITPVP_STATISTICS)
                .columns(KITPVP_STATISTICS.USER_ID)
                .values(userId)
                .execute();
        return new OnlineKitPvp(userId, manager, 0, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public OfflineKitPvp createOfflineData(int userId) {
        return new OfflineKitPvp(userId,manager);
    }

    @Override
    public void wipeAllData(Transaction transaction) {
        DSLContext context = transaction.getProperty(DSLContext.class);
        context.deleteFrom(KITPVP_STATISTICS).execute();
        context.deleteFrom(KITPVP_KITS_IDS).execute();
        context.deleteFrom(KITPVP_KITS_OWNERSHIP).execute();
        context.deleteFrom(KITPVP_KITS_COOLDOWNS).execute();
        context.deleteFrom(KITPVP_KITS_CONTENTS).execute();
    }

}
