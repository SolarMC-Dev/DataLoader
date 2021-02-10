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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;
import static gg.solarmc.loader.schema.tables.ClansClanEnemies.CLANS_CLAN_ENEMIES;
import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;
import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;

/**
 * Note to reader of this source code: Come play SolarMC!
 */
public class Clan {

    private final int clanID;
    private final String clanName;

    private volatile ConcurrentHashMap.KeySetView<ClanMember,Boolean> members; //concurrentSet
    private final ClanManager manager;
    private final ClanMember leader;

    private volatile int clanKills;
    private volatile int clanDeaths;
    private volatile int clanAssists;

    Clan(int clanID, String clanName, int clanKills, int clanDeaths, int clanAssists,
         ClanManager manager, ConcurrentHashMap.KeySetView<ClanMember,Boolean> members, ClanMember leader) {

        this.clanID = clanID;
        this.clanName = clanName;
        this.clanKills = clanKills;
        this.clanDeaths = clanDeaths;
        this.clanAssists = clanAssists;
        this.members = members;
        this.manager = manager;
        this.leader = leader;
    }

    public int getID() {
        return this.clanID;
    }

    public String getName() {
        return this.clanName;
    }

    /**
     * Returns the kills at the time of querying (not accurate)
     * @return kills
     */
    public int currentKills() {
        return clanKills;
    }

    /**
     * Returns the deaths at the time of querying (not accurate)
     * @return deaths
     */
    public int currentDeaths() {
        return clanDeaths;
    }

    /**
     * Returns the ass at the time of querying (not accurate)
     * @return ass
     */
    public int currentAssists() {
        return clanAssists;
    }

    /**
     * Gets all current members currently in the clan. Not accurate.
     * @return all ClanMembers, unmodifiable.
     */
    public Set<ClanMember> currentMembers() {
        return Set.copyOf(this.members);
    }

    /**
     * Returns an optional representing allied clan IF it is present in the local cache.
     * Warning: Does not check the database for clan presensce, only the local cache.
     *
     * This operation is not likely to be accurate and as a result it is better to use
     * method getAlliedClan() for operations requiring accuracy.
     *
     * @return Optional containing allied can if present locally. The optional can be empty if the clan
     * is not present locally, or if the clan present locally has no ally.
     */
    public Optional<Clan> currentAllyClan() {
       Optional<Integer> s = manager.getAllyFromCache(this.getID());

       if (s.isEmpty()) {
           return Optional.empty();
       }

       return manager.getCachedClanOnly(s.orElseThrow());
    }

    /**
     * Gets this object's currently allied clan accurately
     * @param transaction the tx
     * @return optional holding currently allied clan
     */
    public Optional<Clan> getAlliedClan(Transaction transaction) {
        Record1<Integer> rec1 = transaction
                .getProperty(DSLContext.class)
                .select(CLANS_CLAN_ALLIANCES.ALLY_ID)
                .where(CLANS_CLAN_ALLIANCES.CLAN_ID.eq(this.clanID))
                .fetchOne();



        if (rec1 == null) { 
            return Optional.empty();
        }

        manager.insertAllianceCache(this.clanID,rec1.value1());

        return Optional.of(manager.getClan(transaction,rec1.value1()));
    }

    /**
     * Returns a record of info
     * @param transaction the tx
     * @return the record
     */
    private ClansClanInfoRecord getInformation(Transaction transaction) {
        ClansClanInfoRecord rec =  transaction.getProperty(DSLContext.class)
                .fetchOne(CLANS_CLAN_INFO,CLANS_CLAN_INFO.CLAN_ID.eq(this.clanID));

        assert rec != null : "nullity check failed for record get!";

        return rec;
    }

    /**
     * Adds kills to a clan
     * @param transaction the tx
     * @param toAdd how much to add
     * @return accurate amount of kills post transaction
     */
    public int addKills(Transaction transaction, int toAdd) {
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
     */
    public int addDeaths(Transaction transaction, int toAdd) {
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
     */
    public int addAssists(Transaction transaction, int toAdd) {
        ClansClanInfoRecord rec = getInformation(transaction);

        int newValue = rec.getClanAssists() + toAdd;

        rec.setClanAssists(newValue);
        rec.store(CLANS_CLAN_INFO.CLAN_ASSISTS);

        this.clanAssists = newValue;

        return newValue;
    }

    /**
     * Gets all clan members accurately
     * @param transaction the tx
     * @return An immutable set containing the members of the clan, unmodifiable. immutable or readonly.
     */
    public Set<ClanMember> getClanMembers(Transaction transaction) {
        Set<ClanMember> bruh = transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_MEMBERSHIP.USER_ID)
                .from(CLANS_CLAN_MEMBERSHIP)
                .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID))
                .fetchSet((rec) -> new ClanMember(this.clanID,rec.value1(),manager));

        //this feels like a clumsy way of doing this but i can't figure out any others

        ConcurrentHashMap.KeySetView<ClanMember,Boolean> view = ConcurrentHashMap.newKeySet();
        view.addAll(bruh);
        this.members = view;

        return Set.copyOf(bruh);
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
                .fetchCount(CLANS_CLAN_MEMBERSHIP,CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID));
    }

    /**
     * Adds a member to the clan. Note that this does not have a built in limit, API implementer needs to implement
     * limit if we desire a limit on members
     *
     * Note to API implementer: This handles nothing regarding invites. If you want an invite system, it needs to come
     * before this method.
     *
     * @param transaction The tx
     * @param player player to add
     * @return true if it was added, false if the member is already in this or another clan
     */
    public boolean addClanMember(Transaction transaction, SolarPlayer player) {
        ClanDataObject receiver = player.getData(ClansKey.INSTANCE);

        if (receiver.isSimilar(leader)) {
            return false;
        }

        int sec = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_MEMBERSHIP)
                .columns(CLANS_CLAN_MEMBERSHIP.CLAN_ID,CLANS_CLAN_MEMBERSHIP.USER_ID)
                .values(this.clanID,receiver.getUserId())
                .onDuplicateKeyIgnore()
                .execute();

        this.members.add(new ClanMember(this.clanID,receiver.getUserId(),manager));

        if (sec != 1) {
            return false;
        } else {
            receiver.setCachedClan(this);
            return true;
        }
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

        int res = transaction.getProperty(DSLContext.class)
                .deleteFrom(CLANS_CLAN_MEMBERSHIP)
                .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID).and(CLANS_CLAN_MEMBERSHIP.USER_ID.eq(receiver.getUserId())))
                .execute();

        this.members.removeIf(receiver::isSimilar);

        if (res != 1)  {
            return false;
        } else {
            receiver.setCachedClan(null);
            return true;
        }
    }

    /**
     * Adds clan as an ally
     *
     * @param transaction the tx
     * @param receiver the receiver
     * @throws IllegalArgumentException if provided clan was this clan
     * @return true if added, false if clans are already allied or already have separate allies in the table.
     */
    public boolean addClanAsAlly(Transaction transaction, Clan receiver) {
        if (receiver.getID() == this.clanID) throw new IllegalArgumentException("Tried to mark clan ally as this clan!");

        //note to aurium - these are now okay to do becaue we run the checks previous
        int res = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_ALLIANCES)
                .columns(CLANS_CLAN_ALLIANCES.CLAN_ID,CLANS_CLAN_ALLIANCES.ALLY_ID)
                .values(this.clanID,receiver.getID())
                .values(receiver.getID(),this.clanID)
                .onDuplicateKeyIgnore()
                .execute();

        if (res != 2) {
            return false;
        } else {
            manager.insertAllianceCache(this.getID(),receiver.getID());
            return true;
        }
    }

    /**
     * Revokes ally. Can be called from either clan. Revokes alliance for both.
     * @param transaction the tx
     * @return true if revoked, false if one of the clans or both of the clans has no ally
     */
    public boolean revokeAlly(Transaction transaction) {
        Optional<Clan> localClan = this.currentAllyClan();

        if (localClan.isEmpty()) return false;

        //haha, it turns out you were right - we would need to use returningResult here, just
        //not for what i thought we would @A248
        Result<Record2<Integer,Integer>> res = transaction.getProperty(DSLContext.class)
                .deleteFrom(CLANS_CLAN_ALLIANCES)
                .where(CLANS_CLAN_ALLIANCES.CLAN_ID.eq(this.clanID))
                .or(CLANS_CLAN_ALLIANCES.ALLY_ID.eq(this.clanID))
                .returningResult(CLANS_CLAN_ALLIANCES.CLAN_ID,CLANS_CLAN_ALLIANCES.ALLY_ID).fetch();

        if (res.size() != 2) {
            return false;
        } else {

            res.forEach(rec -> {
                //calling this for both is redundant, but i don't care (shouldn't cause any issues as
                // the values should simply be removed). Also, the alternative is to manually
                //search through the two records and select one randomly OR select the one that starts with
                //this object's clanID, so in the end i'm just gonna do this and see what happens.

                manager.invalidateAllianceCache(rec.value1(),rec.value2());
            });

            return true;
        }
    }

    /**
     * Marks clan as an enemy
     * @param transaction twix bar
     * @param receiver the clan to mark as an enemy.
     * @return true if the enemy was added, false if the clans are already enemies
     * @throws IllegalArgumentException if the specified clan is an ally or this clan
     */
    public boolean addClanAsEnemy(Transaction transaction, Clan receiver) {
        if (receiver.getID() == this.clanID) throw new IllegalArgumentException("Tried to mark clan enemy as same clan!");

        this.getAlliedClan(transaction).ifPresent(ally -> {
            if (receiver.getID() == ally.getID()) throw new IllegalArgumentException("Tried to remove allied clan as enemy");
        });

        int res = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_ENEMIES)
                .columns(CLANS_CLAN_ENEMIES.CLAN_ID,CLANS_CLAN_ENEMIES.ENEMY_ID)
                .values(this.clanID,receiver.getID())
                .onDuplicateKeyIgnore()
                .execute();

        return res == 1;
    }

    /**
     * Marks clan enemy as no longer an enemy
     * @param transaction the tx
     * @param clan clan provided
     * @throws IllegalArgumentException if passed self or if passed ally
     * @return true if went ok, false if method executing clan is not enemies with provided clan
     */
    public boolean removeClanAsEnemy(Transaction transaction, Clan clan) {
        if (clan.getID() == this.clanID) throw new IllegalArgumentException("Tried to unmark self as enemy??");

        this.getAlliedClan(transaction).ifPresent(ally -> {
            if (clan.getID() == ally.getID()) throw new IllegalArgumentException("Tried to add allied clan as enemy");
        });

        int res = transaction.getProperty(DSLContext.class)
                .deleteFrom(CLANS_CLAN_ENEMIES)
                .where(CLANS_CLAN_ENEMIES.CLAN_ID.eq(this.clanID))
                .and(CLANS_CLAN_ENEMIES.ENEMY_ID.eq(clan.getID()))
                .execute();

        return res != 1;
    }

    /**
     * Gets all clans enemies with this current clan
     * @param transaction the tx
     * @return All clans this clan has currently marked as enemies
     */
    public Set<Clan> getEnemyClans(Transaction transaction) {
        return transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_ENEMIES.ENEMY_ID).from(CLANS_CLAN_ENEMIES)
                .where(CLANS_CLAN_ENEMIES.CLAN_ID.eq(this.clanID))
                .fetchSet((record) -> manager.getClan(transaction,record.get(CLANS_CLAN_ENEMIES.ENEMY_ID)));
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
