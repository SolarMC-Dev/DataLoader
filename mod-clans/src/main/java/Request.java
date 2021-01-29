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
import org.jooq.DSLContext;
import org.jooq.impl.UpdatableRecordImpl;

import static gg.solarmc.loader.schema.tables.ClansAllianceRequests.CLANS_ALLIANCE_REQUESTS;
import static gg.solarmc.loader.schema.tables.ClansClanAlliances.CLANS_CLAN_ALLIANCES;

public class Request {

    private final Clan sender;
    private final Clan receiver;

    public Request(Clan sender, Clan receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    /**
     * Approves this clan alliance request
     * Usually called on a request from request receiver.
     * @param transaction da tx
     * @return whether the operation was a success or not
     */
    public void approve(Transaction transaction) {

        if (receiver.getEnemyClans(transaction).contains(sender)) throw new IllegalArgumentException("Tried to accept ally request from enemy!");
        if (receiver.currentAlliedClan().isPresent()) throw new IllegalStateException("Tried to accept ally request while posessing ally!");
        if (sender.currentAlliedClan().isPresent()) throw new IllegalStateException("Sender is already allied with a clan!");

        var jooq = transaction.getProperty(DSLContext.class);

        //cancels all of sender's existing requests (including this)
        //cancels all of receiver's existing requests

        jooq.fetch(CLANS_ALLIANCE_REQUESTS,CLANS_ALLIANCE_REQUESTS.REQUESTER_ID.eq(receiver.getID()))
                .forEach(UpdatableRecordImpl::delete);

        jooq.fetch(CLANS_ALLIANCE_REQUESTS,CLANS_ALLIANCE_REQUESTS.REQUESTER_ID.eq(sender.getID()))
                .forEach(UpdatableRecordImpl::delete);

        jooq.insertInto(CLANS_CLAN_ALLIANCES)
                .columns(CLANS_CLAN_ALLIANCES.CLAN_ID,CLANS_CLAN_ALLIANCES.ALLY_ID)
                .values(sender.getID(),receiver.getID());
    }



}
