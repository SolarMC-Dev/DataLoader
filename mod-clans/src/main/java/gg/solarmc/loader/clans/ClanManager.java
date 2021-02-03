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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.schema.tables.records.ClansClanAlliancesRecord;
import gg.solarmc.loader.schema.tables.records.ClansClanInfoRecord;
import org.jooq.DSLContext;

import java.time.Duration;
import java.util.*;

import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;
import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;
import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;


public class ClanManager implements DataManager {

    private final Cache<Integer,Clan> clans = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).build();
    private final Cache<Integer,Integer> allianceCache = Caffeine.newBuilder().build();

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
                    .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(id)).fetchSet((rec1) -> new ClanMember(id,rec1.value1(),this));

            ClansClanAlliancesRecord rec1 = jooq.fetchOne(CLANS_CLAN_ALLIANCES,CLANS_CLAN_ALLIANCES.CLAN_ID.eq(id));

            ClanMember owner = new ClanMember(id,rec.getClanLeader(),this);

            Clan returned = new Clan(rec.getClanId(), rec.getClanName(),rec.getClanKills(),
                    rec.getClanDeaths(),rec.getClanAssists(),this,members,owner);

            if (rec1 != null) {
                this.insertAllianceCache(rec1.getClanId(),rec1.getAllyId());
            }

            return returned;

        });
    }

    public Optional<Integer> getAllyFromCache(Integer clanId) { return Optional.ofNullable(allianceCache.getIfPresent(clanId)); }

    /**
     * Returns an optional representing the allied clan
     * @param clan The clan you want to check for alliance
     * @return the optional containing allied clan.
     */

    /*public Optional<Clan> s(Clan clan) {
        return Optional.ofNullable(alliance.getIfPresent(clan));
    }*/

    /**
     * Invalidates an alliance, not order sensitive
     * Information for api users - this invalidates two rows in the cache.
     * @param clan1 the first clan id, ally of the second
     * @param clan2 the second clan id, ally of the first
     */
    public void invalidateAllianceCache(Integer clan1, Integer clan2) {
        this.allianceCache.invalidate(clan1);
        this.allianceCache.invalidate(clan2);
    }

    /**
     * (a248 please review this method, i'm not sure if inserting two rows here is an issue
     * because it will be called on clan generation for the first and for the second clan,
     * and i'm not sure if this will lead to 2 rows {cache ignores duplicate} or to 4 rows { cache accepts duplicate} )
     *
     * Inserts 2 rows into the alliance cache, not order sensitive
     * @param clan1 the first clan, ally of the second
     * @param clan2 the second clan, ally of the first
     */
    public void insertAllianceCache(Integer clan1, Integer clan2) {
        this.allianceCache.put(clan1,clan2);
        this.allianceCache.put(clan2,clan1);
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
                .columns(CLANS_CLAN_INFO.CLAN_NAME, CLANS_CLAN_INFO.CLAN_LEADER, CLANS_CLAN_INFO.CLAN_KILLS, CLANS_CLAN_INFO.CLAN_DEATHS, CLANS_CLAN_INFO.CLAN_ASSISTS)
                .values(name,owner.getUserId(),0,0,0)
                .returning()
                .fetchOne();

        if (rec == null) throw new IllegalStateException("Failed to insert new gg.solarmc.loader.clans.Clan by name " + name);

        ClanMember ownerAsMember = owner.asClanMember(transaction);
        Set<ClanMember> memberSet = new HashSet<>();
        memberSet.add(ownerAsMember);

        Clan returned = new Clan(rec.getClanId(),rec.getClanName(),rec.getClanKills(),rec.getClanDeaths(),rec.getClanAssists(),this,memberSet,ownerAsMember);

        clans.put(returned.getID(),returned);

        return returned;
    }

    /**
     * Deletes a clan from the cache and table, and deletes alliances
     * if the clan is in an alliance.
     *
     * @param transaction the tx
     * @param clan the clan to delete
     * @throws IllegalStateException if the clan does not exist in the table.
     */
    public void deleteClan(Transaction transaction, Clan clan) {
        int i = transaction.getProperty(DSLContext.class)
                .delete(CLANS_CLAN_INFO)
                .where(CLANS_CLAN_INFO.CLAN_ID.eq(clan.getID()))
                .execute();

        assert i == 1 : "Clan does not exist in table!";

        clan.currentlyAlliedClan().ifPresent(allyId -> {
            this.invalidateAllianceCache(clan.getID(),allyId);
        });

        clans.invalidate(clan);
    }


}
