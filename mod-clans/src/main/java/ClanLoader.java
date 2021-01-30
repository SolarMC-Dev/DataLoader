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

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.schema.tables.records.ClansClanMembershipRecord;
import org.jooq.DSLContext;

import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;

public class ClanLoader implements DataLoader<ClanDataObject> {

    private final ClanManager manager;

    ClanLoader(ClanManager manager) {
        this.manager = manager;
    }

    @Override
    public ClanDataObject createDefaultData(Transaction transaction, int userId) {
        return new ClanDataObject(userId,null,manager);
    }

    @Override
    public ClanDataObject loadData(Transaction transaction, int userId) {

        ClansClanMembershipRecord rec = transaction.getProperty(DSLContext.class).fetchOne(CLANS_CLAN_MEMBERSHIP,CLANS_CLAN_MEMBERSHIP.USER_ID.eq(userId));

        if (rec == null) return new ClanDataObject(userId,null,manager);

        return new ClanDataObject(userId,manager.getClan(transaction,userId),manager);
    }
}
