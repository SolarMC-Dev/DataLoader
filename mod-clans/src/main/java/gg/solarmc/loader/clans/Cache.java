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

package gg.solarmc.loader.clans;

import gg.solarmc.loader.Transaction;
import gg.solarmc.streamer.Streamer;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.Record6;
import org.jooq.Result;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;
import static gg.solarmc.loader.schema.tables.ClansClanEnemies.CLANS_CLAN_ENEMIES;
import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;

final class Cache {

    private final ConcurrentHashMap<Integer, Clan> clanCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Integer> allianceCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<Integer>> enemyCache = new ConcurrentHashMap<>();

    void cacheRefresh(Transaction transaction, ClanManager manager) {
        DSLContext context = transaction.getProperty(DSLContext.class);
        // Take a copy of the key set and refresh all cached clans
        for (int clanId : Set.copyOf(clanCache.keySet())) {
            // Use ifPresent to avoid race condition with map update and iteration
            Clan newClan = clanCache.computeIfPresent(clanId, (id, existingClan) -> {
                assert id == existingClan.getClanId();
                Record5<String, Integer, Integer, Integer, Integer> record = context
                        .select(CLANS_CLAN_INFO.CLAN_NAME, CLANS_CLAN_INFO.CLAN_LEADER,
                                CLANS_CLAN_INFO.CLAN_KILLS, CLANS_CLAN_INFO.CLAN_DEATHS, CLANS_CLAN_INFO.CLAN_ASSISTS)
                        .from(CLANS_CLAN_INFO)
                        .where(CLANS_CLAN_INFO.CLAN_ID.eq(id))
                        .fetchOne();
                if (record == null) {
                    return null;
                }
                return ClanBuilder.usingManager(manager)
                        .clanId(id)
                        .nameAndLeader(record.value1(), new ClanMember(record.value2()))
                        .killsDeathsAssists(record.value3(), record.value4(), record.value5())
                        .fetchMembers(transaction)
                        .build();
            });
            if (newClan != null) {
                findAndCacheAssociatedClans(manager, newClan, transaction);
            }
        }
    }

    interface ClanCreator {

        Clan createClan(int clanId);
    }

    Clan getOrCreateClan(int clanId, ClanCreator creator) {
        return clanCache.computeIfAbsent(clanId, creator::createClan);
    }

    void insertNewClan(Clan created) {
        clanCache.put(created.getClanId(), created);
    }

    void findAndCacheAssociatedClans(ClanManager manager, Clan clan, Transaction transaction) {
        Integer allyId = allianceCache.computeIfAbsent(clan.getClanId(), (clanId) -> {
            DSLContext context = transaction.getProperty(DSLContext.class);
            Record6<Integer, String, Integer, Integer, Integer, Integer> record = context
                    .select(CLANS_CLAN_INFO.CLAN_ID, CLANS_CLAN_INFO.CLAN_NAME, CLANS_CLAN_INFO.CLAN_LEADER,
                            CLANS_CLAN_INFO.CLAN_KILLS, CLANS_CLAN_INFO.CLAN_DEATHS, CLANS_CLAN_INFO.CLAN_ASSISTS)
                    .from(CLANS_CLAN_INFO)
                    .innerJoin(CLANS_CLAN_ALLIANCES)
                    .on(CLANS_CLAN_INFO.CLAN_ID.eq(CLANS_CLAN_ALLIANCES.ALLY_ID))
                    .where(CLANS_CLAN_ALLIANCES.CLAN_ID.eq(clan.getClanId()))
                    .fetchOne();
            if (record == null) {
                return null;
            }
            Clan ally = ClanBuilder.fromRecordAndFetchMembers(manager, record, transaction);
            clanCache.put(ally.getClanId(), ally);
            return ally.getClanId();
        });
        if (allyId != null) {
            allianceCache.put(allyId, clan.getClanId());
        }
        enemyCache.computeIfAbsent(clan.getClanId(), (clanId) -> {
            DSLContext context = transaction.getProperty(DSLContext.class);
            Result<Record6<Integer, String, Integer, Integer, Integer, Integer>> records = context
                    .select(CLANS_CLAN_INFO.CLAN_ID, CLANS_CLAN_INFO.CLAN_NAME, CLANS_CLAN_INFO.CLAN_LEADER,
                            CLANS_CLAN_INFO.CLAN_KILLS, CLANS_CLAN_INFO.CLAN_DEATHS, CLANS_CLAN_INFO.CLAN_ASSISTS)
                    .from(CLANS_CLAN_INFO)
                    .innerJoin(CLANS_CLAN_ENEMIES)
                    .on(CLANS_CLAN_INFO.CLAN_ID.eq(CLANS_CLAN_ENEMIES.ENEMY_ID))
                    .where(CLANS_CLAN_ENEMIES.CLAN_ID.eq(clan.getClanId()))
                    .fetch();
            if (records.isEmpty()) {
                return Set.of();
            }
            Set<Integer> enemyIds = new HashSet<>();
            for (Record6<Integer, String, Integer, Integer, Integer, Integer> record : records) {
                Clan enemy = ClanBuilder.fromRecordAndFetchMembers(manager, record, transaction);
                int enemyId = enemy.getClanId();
                clanCache.put(enemyId, enemy);
                enemyIds.add(enemyId);
            }
            return Set.copyOf(enemyIds);
        });
    }

    void removeDeletedClan(Clan clan) {
        int clanId = clan.getClanId();
        Clan removed = clanCache.remove(clanId);
        assert removed != null : "Deleted clan should have been cached";
        assert clan == removed : "Removed clan should be same deleted clan";
        Integer allyId = allianceCache.remove(clanId);
        if (allyId != null) {
            allianceCache.remove(allyId);
        }
        enemyCache.remove(clanId);
    }

    Optional<Clan> getCachedClan(int id) {
        return Optional.ofNullable(clanCache.get(id));
    }

    private Clan getCachedClanAssertExists(int id) {
        return getCachedClan(id).orElseThrow(() -> new IllegalDataStateException("Clan does not exist in cache"));
    }

    Optional<Clan> getCachedAlly(Clan clan) {
        return Optional.ofNullable(allianceCache.get(clan.getClanId())).map(this::getCachedClanAssertExists);
    }

    Set<Clan> getCachedEnemies(Clan clan) {
        Set<Integer> enemyIds = enemyCache.get(clan.getClanId());
        if (enemyIds == null || enemyIds.isEmpty()) {
            return Set.of();
        }
        return Streamer.stream(enemyIds)
                .map(this::getCachedClanAssertExists)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Inserts 2 rows into the alliance cache, not order sensitive
     * @param clan1 the first clan, ally of the second
     * @param clan2Id the second clan, ally of the first
     */
    void linkAlliance(Clan clan1, int clan2Id) {
        int clan1Id = clan1.getClanId();
        allianceCache.put(clan1Id, clan2Id);
        allianceCache.put(clan2Id, clan1Id);
    }

    /**
     * Invalidates an alliance, not order sensitive
     *
     * @param clan1 the first clan
     * @param clan2Id the second clan's ID
     */
    void revokeAlliance(Clan clan1, int clan2Id) {
        int clan1Id = clan1.getClanId();
        if (allianceCache.remove(clan1Id, clan2Id)) {
            allianceCache.remove(clan2Id, clan1Id);
        }
    }

    void createEnemy(Clan clan, Clan enemy) {
        enemyCache.compute(clan.getClanId(), (id, existingEnemies) -> {
            int enemyId = enemy.getClanId();
            if (existingEnemies == null) {
                return Set.of(enemyId);
            }
            Set<Integer> newEnemies = new HashSet<>(existingEnemies);
            newEnemies.add(enemyId);
            return Set.copyOf(newEnemies);
        });
    }

    void removeEnemy(Clan clan, Clan enemy) {
        enemyCache.computeIfPresent(clan.getClanId(), (id, existingEnemies) -> {
            int enemyId = enemy.getClanId();
            Set<Integer> newEnemies = new HashSet<>(existingEnemies);
            newEnemies.remove(enemyId);
            return Set.copyOf(newEnemies);
        });
    }

    void linkAllEnemies(Clan clan, Set<Integer> enemies) {
        enemyCache.put(clan.getClanId(), enemies);
    }

    void clearAll() {
        clanCache.clear();
        allianceCache.clear();
        enemyCache.clear();
    }

}
