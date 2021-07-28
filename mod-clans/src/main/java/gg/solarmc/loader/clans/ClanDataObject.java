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

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataObject;
import gg.solarmc.loader.schema.tables.records.ClansClanMembershipRecord;
import org.jooq.DSLContext;

import java.util.Optional;

import static gg.solarmc.loader.schema.tables.ClansClanMembership.CLANS_CLAN_MEMBERSHIP;


/**
 * Note to aurium - you don't have to put anything into the database when setting
 * the cached clan *because this doesn't exist in the database*
 *
 * (gg.solarmc.loader.clans.Clan membership is stored in the clan membership table, not alongside the dataobject,
 * and furthermore this dataobject isn't stored in a table)
 */
public abstract class ClanDataObject implements DataObject {

    private final int userId;
    private final ClanManager manager;


    public ClanDataObject(int userId, ClanManager manager) {
        this.userId = userId;
        this.manager = manager;
    }

    int getUserId() {
        return userId;
    }

    abstract void updateCachedClan(Clan clan);

    /**
     * Gets current clan the object belongs to accurately
     * @param transaction the tx
     * @return Optional containing clan player belongs to
     */
    public Optional<Clan> getClan(Transaction transaction) {


        Optional<Clan> clan = manager.getClanByUser(transaction, userId);

        clan.ifPresentOrElse(this::updateCachedClan, () -> updateCachedClan(null));

        return clan;
    }

    /**
     * Gets a gg.solarmc.loader.clans.ClanMember object from this object.
     * Note to Aurium from Aurium - Since clanmember should always be accurate to the gg.solarmc.loader.clans.Clan, you cannot get
     * one of these from cached information. Come back to this later if you have more memory loss :)
     * @param transaction the tx
     * @return gg.solarmc.loader.clans.ClanMember from table.
     */
    public ClanMember asClanMember(Transaction transaction) {
        return new ClanMember(userId);
    }

    /**
     * Checks if a gg.solarmc.loader.clans.ClanMember is similar (read: equal) to this object
     * @param member gg.solarmc.loader.clans.ClanMember to compare
     * @return whether they are similar or not
     */
    boolean isSimilar(ClanMember member) {
        return member.userId() == this.userId;
    }

}
