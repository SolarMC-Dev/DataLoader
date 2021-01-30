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
import gg.solarmc.loader.schema.tables.records.ClansClanEnemiesRecord;
import gg.solarmc.loader.schema.tables.records.ClansClanMembershipRecord;
import org.jooq.DSLContext;

import java.util.Optional;
import java.util.Set;

import static gg.solarmc.loader.schema.tables.ClansAllianceRequests.*;
import static gg.solarmc.loader.schema.tables.ClansClanEnemies.*;
import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;

/**
 * Note to reader of this source code: Come play SolarMC!
 */
public class Clan {

    private final int clanID;
    private final String clanName;

    private final Clan alliedClan;
    private final Set<ClanMember> members;
    private final ClanManager manager;
    private final ClanMember leader;

    private volatile int clanKills;
    private volatile int clanDeaths;
    private volatile int clanAssists;

    public Clan(int clanID, String clanName, int clanKills, int clanDeaths, int clanAssists,
                Clan alliedClan, ClanManager manager, Set<ClanMember> members, ClanMember leader) {

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
     * Returns the current allied clan. Not accurate.
     * @return current allied clan
     */
    public Optional<Clan> currentAlliedClan() { return Optional.ofNullable(alliedClan); }

    /**
     * Gets all current members currently in the clan. Not accurate.
     * @return all ClanMembers.
     */
    public Set<ClanMember> currentMembers() {
        return this.members;
    }

    /**
     * Adds a member to the clan. Note that this does not have a built in limit, API implementer needs to implement
     * limit if we desire a limit on members
     *
     * Note to API implementer: This handles nothing regarding invites. If you want an invite system, it needs to come
     * before this method.
     *
     * @param transaction The tx
     * @param object ClanDataObject to add.
     * @return Accurate set of ClanMembers post transaction
     * @throws IllegalStateException if member is already a member of the clan.
     * @throws IllegalArgumentException if member is the leader of the clan
     */
    public Set<ClanMember> addClanMember(Transaction transaction, ClanDataObject object) {
        if (object.isSimilar(leader)) throw new IllegalArgumentException("Tried to add leader as member");
        int sec = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_MEMBERSHIP)
                .columns(CLANS_CLAN_MEMBERSHIP.CLAN_ID,CLANS_CLAN_MEMBERSHIP.USER_ID)
                .values(this.clanID,object.getUserId())
                .execute();

        if (sec != 1) throw new IllegalStateException("Inserted ClanMember already belongs to clan!");

        object.setCachedClan(this);

        return transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_MEMBERSHIP.USER_ID)
                .from(CLANS_CLAN_MEMBERSHIP)
                .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID))
                .fetchSet((rec) -> {
                    return new ClanMember(this.clanID,rec.value1(),manager);
                });
    }

    /**
     * Removes a member from the clan.
     * @param transaction The tx
     * @param object ClanDataObject to remove
     * @return Accurate set of ClanMembers post transaction
     * @throws IllegalStateException if member is not present in the clan
     * @throws IllegalArgumentException if member tried to remove was the leader of the clan
     */
    public Set<ClanMember> removeClanMember(Transaction transaction, ClanDataObject object) {
        ClansClanMembershipRecord rec = transaction.getProperty(DSLContext.class)
                .fetchOne(CLANS_CLAN_MEMBERSHIP,CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID).and(CLANS_CLAN_MEMBERSHIP.USER_ID.eq(object.getUserId())));

        if (rec == null) throw new IllegalStateException("Member is not part of clan!");
        if (object.isSimilar(leader)) throw new IllegalArgumentException("Tried to remove leader of clan!");

        rec.delete();

        object.setCachedClan(null);

        return transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_MEMBERSHIP.USER_ID)
                .from(CLANS_CLAN_MEMBERSHIP)
                .where(CLANS_CLAN_MEMBERSHIP.CLAN_ID.eq(this.clanID))
                .fetchSet((rec1) -> {
                    return new ClanMember(this.clanID,rec1.value1(),manager);
                });
    }

    /**
     * Requests clan to be ally. Note that the clan will have to approve this request.
     * @param transaction the tx
     * @param receiver represents the clan to add as an ally
     * @return whether action was successful or not (failure implying request already exists)
     */
    public boolean requestClanAsAlly(Transaction transaction, Clan receiver) {
        if (receiver.getID() == this.clanID) throw new IllegalArgumentException("Tried to mark clan ally as same clan!");
        if (this.currentAlliedClan().isPresent()) throw new IllegalStateException("Tried to ally with alternate clan while having an ally!");
        if (receiver.currentAlliedClan().isPresent()) throw new IllegalStateException("Selected clan already has an ally!");

        int res = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_ALLIANCE_REQUESTS)
                .columns(CLANS_ALLIANCE_REQUESTS.REQUESTER_ID,CLANS_ALLIANCE_REQUESTS.REQUESTED_ID)
                .values(this.clanID,receiver.getID())
                .execute();

        return res == 1;
    }

    /**
     * Marks clan as an enemy
     * @param transaction twix bar
     * @param clan the clan to mark as an enemy.
     * @return whether action was successful or not (failure represents that they are already enemies)
     * @throws IllegalArgumentException if the presented clan is an ally or is the same object (what?)
     */
    public boolean addClanAsEnemy(Transaction transaction, Clan clan) {
        if (clan.getID() == this.clanID) throw new IllegalArgumentException("Tried to mark clan enemy as same clan!");
        if (clan.equals(this.alliedClan)) throw new IllegalArgumentException("Tried to add allied clan as enemy!");

        int res = transaction.getProperty(DSLContext.class)
                .insertInto(CLANS_CLAN_ENEMIES)
                .columns(CLANS_CLAN_ENEMIES.CLAN_ID,CLANS_CLAN_ENEMIES.ENEMY_ID)
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
        if (clan.equals(this.alliedClan)) throw new IllegalArgumentException("Tried to unmark allied clan!");

        ClansClanEnemiesRecord rec = transaction.getProperty(DSLContext.class)
                .fetchOne(CLANS_CLAN_ENEMIES,CLANS_CLAN_ENEMIES.CLAN_ID.eq(this.clanID).and(CLANS_CLAN_ENEMIES.ENEMY_ID.eq(clan.getID())));

        assert rec != null : "Method executing clan is not enemies with provided clan!";

        rec.delete();
    }

    /**
     * Gets all clans enemies with this current clan
     * @param transaction
     * @return All clans this clan has currently marked as enemies
     */
    public Set<Clan> getEnemyClans(Transaction transaction) {
        return transaction.getProperty(DSLContext.class)
                .select(CLANS_CLAN_ENEMIES.ENEMY_ID).from(CLANS_CLAN_ENEMIES)
                .where(CLANS_CLAN_ENEMIES.CLAN_ID.eq(this.clanID))
                .fetchSet((record) -> {
                    return manager.getClan(transaction,record.get(CLANS_CLAN_ENEMIES.ENEMY_ID));
                });
    }

    public Set<Request> getAllyRequests(Transaction transaction) {
        return transaction.getProperty(DSLContext.class)
                .select(CLANS_ALLIANCE_REQUESTS.REQUESTER_ID).from(CLANS_ALLIANCE_REQUESTS)
                .where(CLANS_ALLIANCE_REQUESTS.REQUESTED_ID.eq(this.clanID))
                .fetchSet((record) -> {
                   return new Request(this, manager.getClan(transaction,record.get(CLANS_ALLIANCE_REQUESTS.REQUESTER_ID)));
                });
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
