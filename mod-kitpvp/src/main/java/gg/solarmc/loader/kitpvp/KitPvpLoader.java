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
import org.jooq.DSLContext;
import org.jooq.Record2;

import java.math.BigDecimal;
import java.util.Map;

import static gg.solarmc.loader.schema.tables.KitpvpBounties.KITPVP_BOUNTIES;
import static gg.solarmc.loader.schema.tables.KitpvpBountyLogs.KITPVP_BOUNTY_LOGS;
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
        var kitpvpRecord = context
                .select(KITPVP_STATISTICS.KILLS, KITPVP_STATISTICS.DEATHS, KITPVP_STATISTICS.ASSISTS,
                        KITPVP_STATISTICS.EXPERIENCE, KITPVP_STATISTICS.CURRENT_KILLSTREAK, KITPVP_STATISTICS.HIGHEST_KILLSTREAK)
                .from(KITPVP_STATISTICS)
                .where(KITPVP_STATISTICS.USER_ID.eq(userId))
                .fetchOne();
        if (kitpvpRecord != null) {
            Map<BountyCurrency, BigDecimal> bounties = context
                    .select(KITPVP_BOUNTIES.BOUNTY_CURRENCY, KITPVP_BOUNTIES.BOUNTY_AMOUNT)
                    .from(KITPVP_BOUNTIES)
                    .where(KITPVP_BOUNTIES.USER_ID.eq(userId))
                    .fetchMap(record -> BountyCurrency.deserialize(record.value1()), Record2::value2);
            return new OnlineKitPvp(
                    userId, manager,
                    kitpvpRecord.value1(), kitpvpRecord.value2(), kitpvpRecord.value3(),
                    kitpvpRecord.value4(), kitpvpRecord.value5(), kitpvpRecord.value6(),
                    bounties.getOrDefault(BountyCurrency.CREDITS, BigDecimal.ZERO),
                    bounties.getOrDefault(BountyCurrency.PLAIN_ECO, BigDecimal.ZERO));
        }
        context.insertInto(KITPVP_STATISTICS)
                .columns(KITPVP_STATISTICS.USER_ID)
                .values(userId)
                .execute();
        return new OnlineKitPvp(userId, manager, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO);
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
        context.deleteFrom(KITPVP_BOUNTIES).execute();
        context.deleteFrom(KITPVP_BOUNTY_LOGS).execute();
    }

}
