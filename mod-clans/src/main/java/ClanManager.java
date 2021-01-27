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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.schema.tables.records.ClansClanInfoRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.time.Duration;

import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;

public class ClanManager implements DataManager {

    private final Cache<Integer,Clan> clans = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).build();

    public Clan getClan(Transaction transaction, Integer id) {
        return clans.get(id, i -> {

            ClansClanInfoRecord rec = transaction.getProperty(DSLContext.class).fetchOne(CLANS_CLAN_INFO,CLANS_CLAN_INFO.CLAN_ID.eq(i));

            if (rec == null) {
                throw new IllegalStateException("No such clan exists!");
            }

            return new Clan(rec.getClanId(), rec.getClanName(),rec.getClanKills(),rec.getClanDeaths(),rec.getClanAssists());

        });
    }

    /**
     * Creates an empty clan with given name. Note to implementer: You will need to manually
     * populate this Clan with players.
     * @param name
     * @param transaction
     * @return
     */
    public Clan createClan(Transaction transaction, String name) {
        ClansClanInfoRecord rec = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_INFO)
                .columns(CLANS_CLAN_INFO.CLAN_NAME,CLANS_CLAN_INFO.CLAN_KILLS,CLANS_CLAN_INFO.CLAN_DEATHS,CLANS_CLAN_INFO.CLAN_ASSISTS)
                .values(name,0,0,0)
                .returning()
                .fetchOne();

        if (rec == null) throw new IllegalStateException("Failed to insert new Clan by name " + name);

        Clan returned = new Clan(rec.getClanId(),rec.getClanName(),rec.getClanKills(),rec.getClanDeaths(),rec.getClanAssists());

        clans.put(returned.getID(),returned);

        return returned;
    }

    public void deleteClan(Transaction transaction, Clan clan) {
        transaction.getProperty(DSLContext.class)
                .delete(CLANS_CLAN_INFO)
                .where(CLANS_CLAN_INFO.CLAN_ID.eq(clan.getID()))
                .execute();
        clans.invalidate(clan);
    }


}
