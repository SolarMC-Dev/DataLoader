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
import gg.solarmc.loader.schema.tables.records.ClansClanInfoRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static gg.solarmc.loader.schema.Routines.clansAddAlly;
import static gg.solarmc.loader.schema.Routines.clansAddEnemy;
import static gg.solarmc.loader.schema.Routines.clansAddMember;

import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;
import static gg.solarmc.loader.schema.tables.ClansClanEnemies.CLANS_CLAN_ENEMIES;
import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;
import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;

/**
 * Note to reader of this source code: Come play SolarMC!
 */
public class Clan {

    private final int clanId;
    private volatile String clanName;

    private final AtomicReference<Set<ClanMember>> members;
    private final ClanManager manager;
    private volatile ClanMember leader;

    private volatile int clanKills;
    private volatile int clanDeaths;
    private volatile int clanAssists;

    Clan(ClanManager manager, int clanId,
         String clanName, ClanMember leader,
         int clanKills, int clanDeaths, int clanAssists,
         Set<ClanMember> members) {

        this.clanId = clanId;
        this.clanName = clanName;
        this.leader = leader;

        this.clanKills = clanKills;
        this.clanDeaths = clanDeaths;
        this.clanAssists = clanAssists;

        this.members = new AtomicReference<>(members);
        this.manager = manager;
    }

    /**
     * Gets this clan's ID
     *
     * @return the clan ID
     */
    public int getClanId() {
        return clanId;
    }

    /**
     * Gets this clan's ID
     *
     * @return the clan ID
     * @deprecated Use {@link #getClanId()} instead
     */
    @Deprecated
    public int getID() {
        return this.clanId;
    }

    public String currentClanName() {
        return this.clanName;
    }

    /**
     * Returns the clan's leader at the time of querying
     * @return the leader represented as an object (should not be relied on for correctness purposes)
     */
    public ClanMember currentLeader() {
        return this.leader;
    }

    /**
     * Returns the kills at the time of querying
     * @return kills (should not be relied on for correctness purposes)
     */
    public int currentKills() {
        return clanKills;
    }

    /**
     * Returns the deaths at the time of querying
     * @return deaths (should not be relied on for correctness purposes)
     */
    public int currentDeaths() {
        return clanDeaths;
    }

    /**
     * Returns the assists at the time of querying
     * @return assists (should not be relied on for correctness purposes)
     */
    public int currentAssists() {
        return clanAssists;
    }

    /**
     * Gets all current members currently in the clan.
     * @return all ClanMembers, unmodifiable.
     * (should not be relied on for correctness purposes)
     */
    public Set<ClanMember> currentMembers() {
        return Set.copyOf(members.getAcquire());
    }

    /**
     * Gets the current allied clan. Should not be relied upon for correctness
     * purposes.
     *
     * @return the alllied clan if there is one
     */
    public Optional<Clan> currentAllyClan() {
        return manager.cache().getCachedAlly(this);
    }

    /**
     * Gets the current enemy clans. Should not be relied upon for correctness
     * purposes.
     *
     * @return the clans which are marked as enemies of this one
     */
    public Set<Clan> currentEnemyClans() {
        return manager.cache().getCachedEnemies(this);
    }

    /**
     * Gets clan name accurately
     * @param transaction the transaction
     * @return the name of the clan
     * @throws IllegalStateException if this clan is not present in database
     */
    public String getClanName(Transaction transaction) {
        var returned =  transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_INFO.CLAN_NAME)
                .from(CLANS_CLAN_INFO)
                .where(CLANS_CLAN_INFO.CLAN_ID.eq(this.clanId))
                .fetchOne();

        if (returned == null) {
            throw new IllegalStateException("Clan name not present in database! Data violation!");
        }

        this.clanName = returned.value1();

        return returned.value1();
    }

    /**
     * Gets this object's currently allied clan accurately
     * @param transaction the tx
     * @return optional holding currently allied clan
     */
    public Optional<Clan> getAllyClan(Transaction transaction) {
        Record1<Integer> record = transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_ALLIANCES.ALLY_ID)
                .from(CLANS_CLAN_ALLIANCES)
                .where(CLANS_CLAN_ALLIANCES.CLAN_ID.eq(this.clanId))
                .fetchOne();

        if (record == null) {
            return Optional.empty();
        }
        int allyId = record.value1();
        manager.cache().linkAlliance(this, allyId);
        return manager.getClanById(transaction, allyId);
    }

    /**
     * Returns a record of info
     * @param transaction the tx
     * @return the record
     */
    private ClansClanInfoRecord getInformation(Transaction transaction) {
        ClansClanInfoRecord rec =  transaction.getProperty(DSLContext.class)
                .fetchOne(CLANS_CLAN_INFO,CLANS_CLAN_INFO.CLAN_ID.eq(this.clanId));

        assert rec != null : "nullity check failed for record get!";

        return rec;
    }

    /**
     * Adds kills to a clan
     * @param transaction the tx
     * @param toAdd how much to add
     * @return accurate amount of kills post transaction
     * @throws IllegalArgumentException if {@code toAdd} is not positive
     */
    public int addKills(Transaction transaction, int toAdd) {
        if (toAdd <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        ClansClanInfoRecord rec = getInformation(transaction);

        int newValue = rec.getClanKills() + toAdd;

        rec.setClanKills(newValue);
        rec.store(CLANS_CLAN_INFO.CLAN_KILLS);

        this.clanKills = newValue;

        return newValue;
    }

    /**
     * Adds deaths to a gg.solarmc.loader.clans.Clan
     * @param transaction the tx
     * @param toAdd amount of deaths to add
     * @return accurate count of deaths post transaction
     * @throws IllegalArgumentException if {@code toAdd} is not positive
     */
    public int addDeaths(Transaction transaction, int toAdd) {
        if (toAdd <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        ClansClanInfoRecord rec = getInformation(transaction);

        int newValue = rec.getClanDeaths() + toAdd;

        rec.setClanDeaths(newValue);
        rec.store(CLANS_CLAN_INFO.CLAN_DEATHS);

        this.clanDeaths = newValue;

        return newValue;
    }

    /**
     * Adds assists to a gg.solarmc.loader.clans.Clan.
     * @param transaction tx
     * @param toAdd the amount you want to add
     * @return Accurate assists after transaction
     * @throws IllegalArgumentException if {@code toAdd} is not positive
     */
    public int addAssists(Transaction transaction, int toAdd) {
        if (toAdd <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        ClansClanInfoRecord rec = getInformation(transaction);

        int newValue = rec.getClanAssists() + toAdd;

        rec.setClanAssists(newValue);
        rec.store(CLANS_CLAN_INFO.CLAN_ASSISTS);

        this.clanAssists = newValue;

        return newValue;
    }

    /**
     * Gets all clan members
     *
     * @param transaction the transaction
     * @return An immutable set containing the members of the clan. May be immutable or
     * may be an unmodifiable view.
     */
    public Set<ClanMember> getClanMembers(Transaction transaction) {
        Set<ClanMember> oldMembers = this.members.get();
        Set<ClanMember> newMembers = ClanMember.fetchMembers(transaction, clanId);
        // If unsuccessful, the old members are more likely accurate and will be used
        boolean cas = this.members.compareAndSet(oldMembers, newMembers);
        return Set.copyOf((cas) ? newMembers : oldMembers);
    }

    /**
     * Method that exists because a248 told me to
     *
     * Gets size of clan members from table accurately
     * @param transaction the tx
     * @return size of clan
     */
    public int getClanSize(Transaction transaction) {
        return transaction.getProperty(DSLContext.class)
                .fetchCount(CLANS_CLAN_MEMBERSHIP, CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanId));
    }

    /**
     * Adds a member to the clan. Note that this does not have a built in limit, API implementer needs to implement
     * limit if we desire a limit on members
     *
     * Note to API implementer: This handles nothing regarding invites. If you want an invite system, it needs to come
     * before this method.
     *
     * @param transaction The tx
     * @param member player to add
     * @return true if it was added, false if the member is already in this or another clan
     */
    public boolean addClanMember(Transaction transaction, SolarPlayer member) {
        ClanDataObject receiver = member.getData(ClansKey.INSTANCE);

        if (receiver.isSimilar(leader)) {
            return false;
        }
        boolean added = transaction.getProperty(DSLContext.class)
                .select(clansAddMember(clanId, receiver.getUserId()))
                .fetchSingle().value1();

        if (added) {
            ClanMember memberAdded = new ClanMember(receiver.getUserId());
            this.members.getAndUpdate((members) -> {
                Set<ClanMember> newMembers = new HashSet<>(members);
                newMembers.add(memberAdded);
                return newMembers;
            });
            receiver.updateCachedClan(this);
        }
        return added;
    }

    /**
     * Removes a member from the clan.
     * @param transaction The tx
     * @param player player to remove from the clan
     * @return return true if added, false if member is not part of clan or already in a clan.
     * @throws IllegalArgumentException if member tried to remove was the leader of the clan
     */
    public boolean removeClanMember(Transaction transaction, SolarPlayer player) {
        ClanDataObject receiver = player.getData(ClansKey.INSTANCE);
        if (receiver.isSimilar(leader))  {
            throw new IllegalArgumentException("Tried to remove leader of clan!");
        }

        int updateCount = transaction.getProperty(DSLContext.class)
                .deleteFrom(CLANS_CLAN_MEMBERSHIP)
                .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanId))
                .and(CLANS_CLAN_MEMBERSHIP.USER_ID.eq(receiver.getUserId()))
                .execute();

        if (updateCount != 1)  {
            return false;
        }
        ClanMember memberRemoved = new ClanMember(receiver.getUserId());
        this.members.getAndUpdate((members) -> {
            Set<ClanMember> newMembers = new HashSet<>(members);
            newMembers.remove(memberRemoved);
            return newMembers;
        });
        receiver.updateCachedClan(null);
        return true;
    }

    /**
     * Adds clan as an ally
     *
     * @param transaction the tx
     * @param receiver the receiver
     * @throws IllegalArgumentException if provided clan was this clan or is an enemy of this clan
     * @return true if added, false if clans are already allied or already have separate allies in the table.
     */
    public boolean addClanAsAlly(Transaction transaction, Clan receiver) {
        int allyId = receiver.getClanId();
        if (allyId == this.clanId) throw new IllegalArgumentException("Tried to mark clan ally as this clan!");

        //note to aurium - these are now okay to do becaue we run the checks previous
        byte addStatus = transaction.getProperty(DSLContext.class)
                .select(clansAddAlly(clanId, allyId))
                .fetchSingle().value1();
        return switch (addStatus) {
            case 0 -> {
                manager.cache().linkAlliance(this, allyId);
                yield true;
            }
            case 1 -> false;
            case 2 -> throw new IllegalArgumentException("Tried to add enemy clan as ally");
            default -> throw new IllegalStateException("Unknown status");
        };
    }

    /**
     * Revokes ally. Can be called from either clan. Revokes alliance for both.
     *
     * @param transaction the transaction
     * @return true if revoked, false if the clans are not allied
     */
    public boolean revokeAlly(Transaction transaction) {
        Result<Record2<Integer, Integer>> deletions = transaction.getProperty(DSLContext.class)
                .deleteFrom(CLANS_CLAN_ALLIANCES)
                .where(CLANS_CLAN_ALLIANCES.CLAN_ID.eq(this.clanId))
                .or(CLANS_CLAN_ALLIANCES.ALLY_ID.eq(this.clanId))
                .returningResult(CLANS_CLAN_ALLIANCES.CLAN_ID, CLANS_CLAN_ALLIANCES.ALLY_ID)
                .fetch();
        if (deletions.size() == 0) {
            return false;
        }
        // Determine the ally ID from the RETURNING result set
        Integer otherClanId = null;
        for (Record2<Integer, Integer> deletedRow : deletions) {
            int clanId = deletedRow.value1();
            if (clanId != this.clanId) {
                otherClanId = clanId;
                break;
            }
            int allyId = deletedRow.value2();
            if (allyId != this.clanId) {
                otherClanId = allyId;
                break;
            }
        }
        if (otherClanId == null) {
            throw new IllegalStateException("Could not determine ID of removed ally");
        }
        manager.cache().revokeAlliance(this, otherClanId);
        return true;
    }

    /**
     * Marks clan as an enemy of this one
     *
     * @param transaction the transaction
     * @param enemy the clan to mark as an enemy.
     * @return true if the enemy was added, false if the given clan is already an enemy of this one
     * @throws IllegalArgumentException if the specified clan is an ally of this clan
     */
    public boolean addClanAsEnemy(Transaction transaction, Clan enemy) {
        if (enemy.getClanId() == this.clanId) {
            throw new IllegalArgumentException("Tried to mark clan enemy as same clan!");
        }

        byte addStatus = transaction.getProperty(DSLContext.class)
                .select(clansAddEnemy(clanId, enemy.getClanId()))
                .fetchSingle().value1();

        return switch (addStatus) {
        case 0 -> {
            manager.cache().createEnemy(this, enemy);
            yield true;
        }
        case 1 -> false;
        case 2 -> throw new IllegalArgumentException("Tried to add allied clan as enemy");
        default -> throw new IllegalStateException("Unknown status");
        };
    }

    /**
     * Marks clan enemy as no longer an enemy
     *
     * @param transaction the tx
     * @param enemy the enemy clan
     * @throws IllegalArgumentException if passed self
     * @return true if removed, false if the given clan is not an enemy of this one
     */
    public boolean removeClanAsEnemy(Transaction transaction, Clan enemy) {
        if (enemy.getClanId() == this.clanId) {
            throw new IllegalArgumentException("Tried to unmark self as enemy??");
        }

        int updateCount = transaction.getProperty(DSLContext.class)
                .deleteFrom(CLANS_CLAN_ENEMIES)
                .where(CLANS_CLAN_ENEMIES.CLAN_ID.eq(this.clanId))
                .and(CLANS_CLAN_ENEMIES.ENEMY_ID.eq(enemy.getClanId()))
                .execute();
        boolean removed = updateCount == 1;
        if (removed) {
            manager.cache().removeEnemy(this, enemy);
        }
        return removed;
    }

    /**
     * Gets all the enemies of this clan
     *
     * @param transaction the tx
     * @return All clans this clan has currently marked as enemies
     */
    public Set<Clan> getEnemyClans(Transaction transaction) {
        Set<Integer> enemyIds = Set.copyOf(transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_ENEMIES.ENEMY_ID)
                .from(CLANS_CLAN_ENEMIES)
                .where(CLANS_CLAN_ENEMIES.CLAN_ID.eq(this.clanId))
                .fetchSet(Record1::value1));
        Set<Clan> enemies = new HashSet<>(enemyIds.size());
        for (int enemyId : enemyIds) {
            enemies.add(manager.getClanById(transaction, enemyId).orElseThrow(IllegalDataStateException::new));
        }
        manager.cache().linkAllEnemies(this, enemyIds);
        return enemies;
    }

    /**
     * Sets the owner of the clan to the user provided
     *
     * The old owner remains a member of the clan.
     *
     * @param transaction tx
     * @param user user id of the new clan leader
     * @throws IllegalStateException if the new clam leader is not a member of the clan
     */
    public void setLeader(Transaction transaction, SolarPlayer user) {

        var context = transaction.getProperty(DSLContext.class);

        var alreadyMember = context.select(CLANS_CLAN_MEMBERSHIP.CLAN_ID)
                .from(CLANS_CLAN_MEMBERSHIP)
                .where(CLANS_CLAN_MEMBERSHIP.USER_ID.eq(user.getUserId()))
                .fetchOne();

        if (alreadyMember == null) {
            throw new IllegalStateException("User is not a member of any clan!");
        }
        if (!alreadyMember.value1().equals(this.clanId)) {
            throw new IllegalStateException("User is not member of this clan!");
        }

        context
                .update(CLANS_CLAN_INFO)
                .set(CLANS_CLAN_INFO.CLAN_LEADER, user.getUserId())
                .where(CLANS_CLAN_INFO.CLAN_ID.eq(this.clanId))
                .execute();

        this.leader = new ClanMember(user.getUserId());

    }

    /**
     * Sets the name of the clan
     *
     * @param transaction transaction
     * @param name name
     */
    public void setName(Transaction transaction, String name){
        transaction.getProperty(DSLContext.class)
                .update(CLANS_CLAN_INFO)
                .set(CLANS_CLAN_INFO.CLAN_NAME, name)
                .where(CLANS_CLAN_INFO.CLAN_ID.eq(this.clanId))
                .execute();

        this.clanName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clan clan = (Clan) o;
        return clanId == clan.clanId;
    }

    @Override
    public int hashCode() {
        return clanId;
    }
}












//wow
//secret source code tic tac toe :)
/*

        |     |     |
        |  -  |  -  |  -
        ______|_____|_____
        |     |
        |  -  |  -  |  -
        ______|_____|_____
        |     |     |
        |  -  |  -  |  -
        |     |     |
*/
//if you found this through commits youre a nerd
