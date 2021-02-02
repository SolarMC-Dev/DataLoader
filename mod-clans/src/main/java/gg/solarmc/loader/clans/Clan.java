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

import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.schema.tables.records.ClansClanEnemiesRecord;
import gg.solarmc.loader.schema.tables.records.ClansClanInfoRecord;
import gg.solarmc.loader.schema.tables.records.ClansClanMembershipRecord;
import org.jooq.DSLContext;

import java.util.Optional;
import java.util.Set;

import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;
import static gg.solarmc.loader.schema.tables.ClansClanEnemies.CLANS_CLAN_ENEMIES;
import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;
import static gg.solarmc.loader.schema.tables.ClansClanInfo.CLANS_CLAN_INFO;

/**
 * Note to reader of this source code: Come play SolarMC!
 */
@SuppressWarnings("unused")
public class Clan {

    private final int clanID;
    private final String clanName;

    private volatile Integer alliedClan; //nullable

    private final Set<ClanMember> members;
    private final ClanManager manager;
    private final ClanMember leader;

    private volatile int clanKills;
    private volatile int clanDeaths;
    private volatile int clanAssists;

    public Clan(int clanID, String clanName, int clanKills, int clanDeaths, int clanAssists,
                Integer alliedClan, ClanManager manager, Set<ClanMember> members, ClanMember leader) {

        this.clanID = clanID;
        this.clanName = clanName;
        this.clanKills = clanKills;
        this.clanDeaths = clanDeaths;
        this.clanAssists = clanAssists;
        this.members = members;
        this.alliedClan = alliedClan;
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
     * @return all ClanMembers.
     */
    public Set<ClanMember> currentMembers() {
        return this.members;
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
    public Integer addKills(Transaction transaction, int toAdd) {
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
    public Integer addDeaths(Transaction transaction, int toAdd) {
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
    public Integer addAssists(Transaction transaction, int toAdd) {
        ClansClanInfoRecord rec = getInformation(transaction);

        int newValue = rec.getClanAssists() + toAdd;

        rec.setClanAssists(newValue);
        rec.store(CLANS_CLAN_INFO.CLAN_ASSISTS);

        this.clanAssists = newValue;

        return newValue;
    }

    /**
     * Returns the accurate allied clan to this object.
     * @param transaction
     * @return
     */
    public Optional<Clan> getAlliedClan(Transaction transaction) {
        if (this.alliedClan == null) return Optional.empty();

        return Optional.of(manager.getClan(transaction,alliedClan));
    }

    /**
     * Gets all cached clan members
     * @param transaction
     * @return
     */
    public Set<ClanMember> getClanMembers(Transaction transaction) {
        return transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_MEMBERSHIP.USER_ID)
                .from(CLANS_CLAN_MEMBERSHIP)
                .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID))
                .fetchSet((rec) -> new ClanMember(this.clanID,rec.value1(),manager));
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
     * @return Accurate set of ClanMembers post transaction
     * @throws IllegalStateException if member is already a member of another clan
     * @throws IllegalArgumentException if member is the leader of the clan
     */
    public Set<ClanMember> addClanMember(Transaction transaction, SolarPlayer player) {
        ClanDataObject receiver = player.getData(ClansKey.INSTANCE);

        if (receiver.isSimilar(leader)) throw new IllegalArgumentException("Tried to add leader as member");
        if (receiver.getClan(transaction).isPresent()) throw new IllegalStateException("Object is already a member of another clan");

        int sec = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_MEMBERSHIP)
                .columns(CLANS_CLAN_MEMBERSHIP.CLAN_ID,CLANS_CLAN_MEMBERSHIP.USER_ID)
                .values(this.clanID,receiver.getUserId())
                .execute();

        if (sec != 1) throw new IllegalStateException("Inserted gg.solarmc.loader.clans.ClanMember already belongs to clan!");

        receiver.setCachedClan(this);

        return getClanMembers(transaction);
    }

    /**
     * Removes a member from the clan.
     * @param transaction The tx
     * @param player player to remove from the clan
     * @return Accurate set of ClanMembers post transaction
     * @throws IllegalStateException if member already has a clan
     * @throws IllegalArgumentException if member tried to remove was the leader of the clan, or if they are not
     * present in the clan
     */
    public Set<ClanMember> removeClanMember(Transaction transaction, SolarPlayer player) {
        ClanDataObject receiver = player.getData(ClansKey.INSTANCE);
        if (receiver.getClan(transaction).isPresent()) throw new IllegalStateException("Receiver already has clan!");

        ClansClanMembershipRecord rec = transaction.getProperty(DSLContext.class)
                .fetchOne(CLANS_CLAN_MEMBERSHIP,CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID).and(CLANS_CLAN_MEMBERSHIP.USER_ID.eq(receiver.getUserId())));

        if (rec == null) throw new IllegalArgumentException("Member is not part of clan!");
        if (receiver.isSimilar(leader)) throw new IllegalArgumentException("Tried to remove leader of clan!");

        rec.delete();

        receiver.setCachedClan(null);

        return getClanMembers(transaction);
    }

    /**
     * Adds clan as an ally
     * @param transaction the tx
     * @param receiver the receiver
     * @return whether record was inserted or not.
     * @throws IllegalArgumentException if receiver is the same gg.solarmc.loader.clans.Clan as this object
     * @throws IllegalStateException if receiver or this object already has an ally
     */
    public boolean addClanAsAlly(Transaction transaction, Clan receiver) {
        if (receiver.getID() == this.clanID) throw new IllegalArgumentException("Tried to mark clan ally as same clan!");
        if (this.getAlliedClan(transaction).isPresent()) throw new IllegalStateException("Sender already have an ally!");
        if (receiver.getAlliedClan(transaction).isPresent()) throw new IllegalStateException("Selected clan already has an ally!");

        int res = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_ALLIANCES)
                .columns(CLANS_CLAN_ALLIANCES.CLAN_ID,CLANS_CLAN_ALLIANCES.ALLY_ID)
                .values(this.clanID,receiver.getID())
                .execute();

        this.alliedClan = receiver.getID();
        receiver.alliedClan = this.getID();

        return res == 1;
    }

    /**
     * Revokes ally. Can be called from either clan. Revokes alliance for both.
     * @param transaction the tx
     * @return true if ally removed, false if this clan had no ally
     */
    public boolean revokeAlly(Transaction transaction) {

        int i = transaction.getProperty(DSLContext.class)
                .deleteFrom(CLANS_CLAN_ALLIANCES)
                .where(CLANS_CLAN_ALLIANCES.CLAN_ID.eq(this.clanID))
                .or(CLANS_CLAN_ALLIANCES.ALLY_ID.eq(this.clanID))
                .execute();

        this.getAlliedClan(transaction).ifPresent(c -> c.alliedClan = null);
        this.alliedClan = null;

        return i == 1;
    }

    /**
     * Marks clan as an enemy
     * @param transaction twix bar
     * @param receiver the clan to mark as an enemy.
     * @return whether action was successful or not (failure represents that they are already enemies)
     * @throws IllegalArgumentException if the presented clan is an ally or is the same object (what?)
     */
    public boolean addClanAsEnemy(Transaction transaction, Clan receiver) {
        if (receiver.getID() == this.clanID) throw new IllegalArgumentException("Tried to mark clan enemy as same clan!");

        this.getAlliedClan(transaction).ifPresent(ally -> {
            if (this.alliedClan.equals(ally.getID())) throw new IllegalArgumentException("Tried to add allied clan as enemy!");
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
     */
    public void removeClanAsEnemy(Transaction transaction, Clan clan) {
        if (clan.getID() == this.clanID) throw new IllegalArgumentException("Tried to unmark self as enemy??");

        this.getAlliedClan(transaction).ifPresent(ally -> {
            if (clan.getID() == ally.getID()) throw new IllegalArgumentException("Tried to unmark allied clan as enemy!");
        });

        ClansClanEnemiesRecord rec = transaction.getProperty(DSLContext.class)
                .fetchOne(CLANS_CLAN_ENEMIES,CLANS_CLAN_ENEMIES.CLAN_ID.eq(this.clanID).and(CLANS_CLAN_ENEMIES.ENEMY_ID.eq(clan.getID())));

        if (rec == null) {
            throw new IllegalStateException("Method executing clan is not enemies with provided clan!");
        }

        rec.delete();
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
