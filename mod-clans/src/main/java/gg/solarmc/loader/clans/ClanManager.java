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

import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.schema.tables.records.ClansClanInfoRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Record6;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;
import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;

public class ClanManager implements DataManager {

    private final Cache cache;

    ClanManager(Cache cache) {
        this.cache = cache;
    }

    Cache cache() {
        return cache;
    }

    /**
     * Gets a clan by the id number of a user
     * @param transaction the transaction
     * @param userId the ID of the user
     * @return empty if no clan was found, or the clan that the user is present in
     */
    Optional<Clan> getClanByUser(Transaction transaction, int userId) {
        DSLContext context = transaction.getProperty(DSLContext.class);
        Record6<Integer, String, Integer, Integer, Integer, Integer> record = context
                .select(CLANS_CLAN_INFO.CLAN_ID, CLANS_CLAN_INFO.CLAN_NAME, CLANS_CLAN_INFO.CLAN_LEADER,
                        CLANS_CLAN_INFO.CLAN_KILLS, CLANS_CLAN_INFO.CLAN_DEATHS, CLANS_CLAN_INFO.CLAN_ASSISTS)
                .from(CLANS_CLAN_INFO)
                .innerJoin(CLANS_CLAN_MEMBERSHIP)
                .on(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(CLANS_CLAN_INFO.CLAN_ID))
                .where(CLANS_CLAN_MEMBERSHIP.USER_ID.eq(userId))
                .fetchOne();

        if (record == null) return Optional.empty();

        Clan clan = cache.getOrCreateClan(record.value1(), (clanId) -> {
            return ClanBuilder.fromRecordAndFetchMembers(this, record, transaction);
        });
        cache.findAndCacheAssociatedClans(this, clan, transaction);
        return Optional.of(clan);
    }

    /**
     * Gets a clan by its ID
     *
     * @param transaction the transaction
     * @param id the clan's id
     * @return the clan if present, empty if not.
     */
    public Optional<Clan> getClanById(Transaction transaction, int id) {
        Optional<Clan> optClan = Optional.ofNullable(cache.getOrCreateClan(id, (clanId) -> {
            ClansClanInfoRecord record = transaction.getProperty(DSLContext.class)
                    .fetchOne(CLANS_CLAN_INFO, CLANS_CLAN_INFO.CLAN_ID.eq(clanId));
            if (record == null) {
                return null;
            }
            return getClan(transaction, record);
        }));
        optClan.ifPresent((clan) -> cache.findAndCacheAssociatedClans(this, clan, transaction));
        return optClan;
    }

    /**
     * Gets a clan by the name of the clan.
     *
     * @param transaction the transaction
     * @param name name of the clan
     * @return an empty optional if the clan is not present, a clan if it is.
     */
    public Optional<Clan> getClanByName(Transaction transaction, String name) {
        ClansClanInfoRecord record = transaction.getProperty(DSLContext.class)
                .fetchOne(CLANS_CLAN_INFO, CLANS_CLAN_INFO.CLAN_NAME.eq(name));
        if (record == null) {
            return Optional.empty();
        }
        Clan clan = cache.getOrCreateClan(record.getClanId(), (clanId) -> getClan(transaction, record));
        cache.findAndCacheAssociatedClans(this, clan, transaction);
        return Optional.of(clan);
    }

    Clan getClan(Transaction transaction, ClansClanInfoRecord record) {
        int clanId = record.getClanId();
        return ClanBuilder.usingManager(this)
                .clanId(clanId)
                .nameAndLeader(record.getClanName(), new ClanMember(record.getClanLeader()))
                .killsDeathsAssists(record.getClanKills(), record.getClanDeaths(), record.getClanAssists())
                .fetchMembers(transaction)
                .build();
    }

    /**
     * Creates a new clan with given name.
     * @param name Name of the Clan to add (must be unique)
     * @param transaction the tx
     * @param leader the player to be owner of the clan
     * @throws IllegalStateException if name is not unique
     * @return created clan.
     */
    public Clan createClan(Transaction transaction, String name, SolarPlayer leader) {
        DSLContext context = transaction.getProperty(DSLContext.class);
        ClanDataObject owner = leader.getData(ClansKey.INSTANCE);

        Record1<Integer> rec = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_INFO)
                .columns(CLANS_CLAN_INFO.CLAN_NAME, CLANS_CLAN_INFO.CLAN_LEADER, CLANS_CLAN_INFO.CLAN_KILLS, CLANS_CLAN_INFO.CLAN_DEATHS, CLANS_CLAN_INFO.CLAN_ASSISTS)
                .values(name, owner.getUserId(), 0, 0, 0)
                .returningResult(CLANS_CLAN_INFO.CLAN_ID)
                .fetchOne();

        if (rec == null) throw new IllegalStateException("Failed to insert new gg.solarmc.loader.clans.Clan by name " + name + "! Either ID was not correctly incremented or name provided was not unique!");

        int clanId = rec.value1();

        int updateCount = context
                .insertInto(CLANS_CLAN_MEMBERSHIP)
                .columns(CLANS_CLAN_MEMBERSHIP.CLAN_ID, CLANS_CLAN_MEMBERSHIP.USER_ID)
                .values(clanId, owner.getUserId())
                .execute();
        assert updateCount == 1;

        ClanMember ownerAsMember = owner.asClanMember(transaction);
        Clan created = ClanBuilder.usingManager(this)
                .clanId(clanId)
                .nameAndLeader(name, ownerAsMember)
                .killsDeathsAssists(0, 0, 0)
                .members(Set.of(ownerAsMember))
                .build();

        owner.updateCachedClan(created);
        cache.insertNewClan(created);

        return created;
    }

    /**
     * Deletes a clan from the cache and table, and deletes alliances
     * if the clan is in an alliance.
     *
     * @param transaction the tx
     * @param clan the clan to delete
     * @throws IllegalStateException if the clan does not exist.
     */
    public void deleteClan(Transaction transaction, Clan clan) {
        int updateCount = transaction.getProperty(DSLContext.class)
                .delete(CLANS_CLAN_INFO)
                .where(CLANS_CLAN_INFO.CLAN_ID.eq(clan.getClanId()))
                .execute();
        if (updateCount == 0) {
            throw new IllegalStateException("Clan does not exist in table!");
        }
        cache.removeDeletedClan(clan);
    }

    public record TopClanResult(int clanId, int statisticValue, String clanName) {}

    /**
     * Gets x amount of the highest ranked clans by kills
     * Ordered by highest to lowest.
     * @param amount the amount of clans to fetch
     * @return an ordered list (highest first, lowest last) of
     * clans ranked by stat type.
     */
    public List<TopClanResult> getTopClanKills(Transaction transaction, int amount) {
        Result<Record3<Integer,Integer,String>> result = transaction.getProperty(DSLContext.class).select(CLANS_CLAN_INFO.CLAN_ID, CLANS_CLAN_INFO.CLAN_KILLS, CLANS_CLAN_INFO.CLAN_NAME)
                .from(CLANS_CLAN_INFO)
                .orderBy(CLANS_CLAN_INFO.CLAN_KILLS.desc())
                .limit(amount)
                .fetch();

        List<TopClanResult> results = new ArrayList<>();

        result.forEach(rec -> {
            results.add(new TopClanResult(rec.value1(), rec.value2(), rec.value3()));
        });

        return results;
    }

    /**
     * Gets x amount of the highest ranked clans by deaths
     * Ordered by highest to lowest.
     * @param amount the amount of clans to fetch
     * @return an ordered list (highest first, lowest last) of
     * clans ranked by stat type.
     */
    public List<TopClanResult> getTopClanDeaths(Transaction transaction, int amount) {
        Result<Record3<Integer,Integer,String>> result = transaction.getProperty(DSLContext.class).select(CLANS_CLAN_INFO.CLAN_ID, CLANS_CLAN_INFO.CLAN_DEATHS, CLANS_CLAN_INFO.CLAN_NAME)
                .from(CLANS_CLAN_INFO)
                .orderBy(CLANS_CLAN_INFO.CLAN_DEATHS.desc())
                .limit(amount)
                .fetch();

        List<TopClanResult> results = new ArrayList<>();

        result.forEach(rec -> {
            results.add(new TopClanResult(rec.value1(), rec.value2(), rec.value3()));
        });

        return results;
    }

    /**
     * Gets x amount of the highest ranked clans by assists
     * Ordered by highest to lowest.
     * @param amount the amount of clans to fetch
     * @return an ordered list (highest first, lowest last) of
     * clans ranked by stat type.
     */
    public List<TopClanResult> getTopClanAssists(Transaction transaction, int amount) {
        Result<Record3<Integer,Integer,String>> result = transaction.getProperty(DSLContext.class).select(CLANS_CLAN_INFO.CLAN_ID, CLANS_CLAN_INFO.CLAN_ASSISTS, CLANS_CLAN_INFO.CLAN_NAME)
                .from(CLANS_CLAN_INFO)
                .orderBy(CLANS_CLAN_INFO.CLAN_ASSISTS.desc())
                .limit(amount)
                .fetch();

        List<TopClanResult> results = new ArrayList<>();

        result.forEach(rec -> {
            results.add(new TopClanResult(rec.value1(), rec.value2(), rec.value3()));
        });

        return results;
    }

    public List<TopClanResult> getTopClanUsingMembers(Transaction transaction, int amount, Function<Set<ClanMember>, Integer> calculateValue) {
        Result<Record2<Integer, String>> result = transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_INFO.CLAN_ID, CLANS_CLAN_INFO.CLAN_NAME)
                .from(CLANS_CLAN_INFO)
                .fetch();
        List<TopClanResult> results = new ArrayList<>();

        result.forEach(rec -> {
            Integer value = calculateValue.apply(ClanMember.fetchMembers(transaction, rec.value1()));
            results.add(new TopClanResult(rec.value1(), value, rec.value2()));
        });

        results.sort(Comparator.comparingInt(TopClanResult::statisticValue));
        return results.subList(0, amount);
    }

    @Override
    public void refreshCaches(Transaction transaction) {
        cache.cacheRefresh(transaction, this);
    }

    @Override
    public void clearCaches() {
        cache.clearAll();
    }

}
