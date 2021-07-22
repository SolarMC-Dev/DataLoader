/*
 *
 *  * dataloader
 *  * Copyright © 2021 SolarMC Developers
 *  *
 *  * dataloader is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as
 *  * published by the Free Software Foundation, either version 3 of the
 *  * License, or (at your option) any later version.
 *  *
 *  * dataloader is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with dataloader. If not, see <https://www.gnu.org/licenses/>
 *  * and navigate to version 3 of the GNU Affero General Public License.
 *
 */

package gg.solarmc.loader.clans;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.schema.tables.records.ClansClanMembershipRecord;
import org.jooq.DSLContext;

import java.util.Optional;

import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;
import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;
import static gg.solarmc.loader.schema.tables.ClansClanEnemies.CLANS_CLAN_ENEMIES;
import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;

public class ClanLoader implements DataLoader<OnlineClanDataObject,ClanDataObject> {

    private final ClanManager manager;

    ClanLoader(ClanManager manager) {
        this.manager = manager;
    }

    @Override
    public OnlineClanDataObject loadData(Transaction transaction, int userId) {

        Optional<Clan> clan = manager.getClanByUser(transaction,userId);

        return new OnlineClanDataObject(userId, manager, clan.orElse(null));
    }

    @Override
    public OfflineClanDataObject createOfflineData(int userId) {
        return new OfflineClanDataObject(userId,manager);
    }

    @Override
    public void wipeAllData(Transaction transaction) {
        DSLContext dsl = transaction.getProperty(DSLContext.class);
        dsl.deleteFrom(CLANS_CLAN_MEMBERSHIP).execute();
        dsl.deleteFrom(CLANS_CLAN_ALLIANCES).execute();
        dsl.deleteFrom(CLANS_CLAN_ENEMIES).execute();
        dsl.deleteFrom(CLANS_CLAN_INFO).execute();
    }
}
