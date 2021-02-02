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

package gg.solarmc.loader.clans;/*
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
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.schema.tables.records.ClansClanAlliancesRecord;
import gg.solarmc.loader.schema.tables.records.ClansClanInfoRecord;
import org.jooq.DSLContext;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;
import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;
import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;

@SuppressWarnings("unused")
public class ClanManager implements DataManager {

    private final Cache<Integer,Clan> clans = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).build();

    /**
     * Gets a clan from memory cache or from table if not present.
     * @param transaction the tx
     * @param id ID of clan requested
     * @return the clan you asked for idiot
     * @throws IllegalStateException if the clan isn't present in table
     */
    public Clan getClan(Transaction transaction, Integer id) {
        return clans.get(id, i -> {

            var jooq = transaction.getProperty(DSLContext.class);

            ClansClanInfoRecord rec = jooq.fetchOne(CLANS_CLAN_INFO,CLANS_CLAN_INFO.CLAN_ID.eq(i));

            if (rec == null) {
                throw new IllegalStateException("No such clan exists!");
            }

            Set<ClanMember> members = jooq.select(CLANS_CLAN_MEMBERSHIP.USER_ID).from(CLANS_CLAN_MEMBERSHIP)
                    .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(id)).fetchSet((rec1) -> new ClanMember(i,rec1.value1(),this));

            ClansClanAlliancesRecord rec1 = jooq.fetchOne(CLANS_CLAN_ALLIANCES,CLANS_CLAN_ALLIANCES.CLAN_ID.eq(i));

            ClanMember owner = new ClanMember(i,rec.getClanLeader(),this);

            //Yes, i know i could :probably: put rec1#getAllyId in the alliedClan slot and save 2 lines of code
            //However i don't want to risk any bugs and nullpointerexceptions
            //i hate null you were right a248

            if (rec1 == null) {
                return new Clan(rec.getClanId(), rec.getClanName(),rec.getClanKills(),
                        rec.getClanDeaths(),rec.getClanAssists(),null,this,members,owner);
            } else {
                return new Clan(rec.getClanId(), rec.getClanName(),rec.getClanKills(),
                        rec.getClanDeaths(),rec.getClanAssists(),rec1.getAllyId(),this,members,owner);
            }

        });
    }

    /**
     * Creates an empty clan with given name.
     * @param name Name of the gg.solarmc.loader.clans.Clan to add
     * @param transaction the tx
     * @param owner the to be owner of the clan
     * @return created clan.
     */
    public Clan createClan(Transaction transaction, String name, ClanDataObject owner) {
        ClansClanInfoRecord rec = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_INFO)
                .columns(CLANS_CLAN_INFO.CLAN_NAME,CLANS_CLAN_INFO.CLAN_LEADER,CLANS_CLAN_INFO.CLAN_KILLS,CLANS_CLAN_INFO.CLAN_DEATHS,CLANS_CLAN_INFO.CLAN_ASSISTS)
                .values(name,owner.getUserId(),0,0,0)
                .returning()
                .fetchOne();

        if (rec == null) throw new IllegalStateException("Failed to insert new gg.solarmc.loader.clans.Clan by name " + name);

        ClanMember ownerAsMember = owner.asClanMember(transaction);
        Set<ClanMember> memberSet = new HashSet<>();
        memberSet.add(ownerAsMember);

        Clan returned = new Clan(rec.getClanId(),rec.getClanName(),rec.getClanKills(),rec.getClanDeaths(),rec.getClanAssists(),null,this,memberSet,ownerAsMember);

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