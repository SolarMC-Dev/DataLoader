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

package gg.solarmc.loader.kitpvp;

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataObject;
import gg.solarmc.loader.impl.SQLTransaction;
import gg.solarmc.loader.schema.tables.records.KitpvpKitsOwnershipRecord;
import gg.solarmc.loader.schema.tables.records.KitpvpStatisticsRecord;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.util.HashSet;
import java.util.Set;

import static gg.solarmc.loader.schema.tables.KitpvpStatistics.*;
import static gg.solarmc.loader.schema.tables.KitpvpKitsOwnership.*;

public class KitPvp implements DataObject {

    private volatile Integer kills;
    private volatile Integer deaths;
    private volatile Integer assists;

    private final int userID;
    private final KitPvpManager manager;

    public KitPvp(int userID, Integer kills, Integer deaths, Integer assists, KitPvpManager manager) {
        this.userID = userID;

        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;

        this.manager = manager;
    }

    private KitpvpStatisticsRecord getStatistics(Transaction transaction) {
        DSLContext context = ((SQLTransaction) transaction).jooq();

        KitpvpStatisticsRecord record = context.fetchOne(KITPVP_STATISTICS,KITPVP_STATISTICS.USER_ID.eq(userID));
        assert record != null : "Data is missing!";

        return record;
    }

    private Result<KitpvpKitsOwnershipRecord> getOwnedKits(Transaction transaction) {
        DSLContext context = ((SQLTransaction) transaction).jooq();

        //cannot be null, is a list
        return context.fetch(KITPVP_KITS_OWNERSHIP,KITPVP_KITS_OWNERSHIP.USER_ID.eq(userID)); //return all entries where id = this.id
    }

    /**
     * Cached kills. Not reliable.
     * @return kills
     */
    public Integer currentKills() {
        return kills;
    }

    /**
     * Cached deaths. Not reliable.
     * @return deaths
     */
    public Integer currentDeaths() {
        return deaths;
    }

    /**
     * Cached assists. Not reliable
     * @return assists
     */
    public Integer currentAssists() {
        return assists;
    }

    /**
     * Adds kills to the user account. Infallible.
     * @param transaction represents the transaction
     * @param amount represents the amount of kills to add
     * @return a result with the new value of kills
     */
    public StatisticResult addKills(Transaction transaction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        KitpvpStatisticsRecord record = getStatistics(transaction);

        Integer existingValue = record.getKills();
        Integer newValue = existingValue + amount;

        record.setKills(newValue);
        record.store(KITPVP_STATISTICS.KILLS);
        this.kills = newValue;

        return new StatisticResult(newValue);
    }

    /**
     * Adds deaths to the user account. Infallible.
     * @param transaction represents the transaction
     * @param amount represents the amount of deaths to add
     * @return a result with the new value of deaths
     */
    public StatisticResult addDeaths(Transaction transaction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        KitpvpStatisticsRecord record = getStatistics(transaction);

        Integer existingValue = record.getDeaths();
        Integer newValue = existingValue + amount;

        record.setKills(newValue);
        record.store(KITPVP_STATISTICS.DEATHS);
        this.deaths = newValue;

        return new StatisticResult(newValue);
    }

    /**
     * Adds assists to the user account. Infallible.
     * @param transaction represents the transaction
     * @param amount represents the amount of assists to add
     * @return a result with the new value of assists
     */
    public StatisticResult addAssists(Transaction transaction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        KitpvpStatisticsRecord record = getStatistics(transaction);

        Integer existingValue = record.getAssists();
        Integer newValue = existingValue + amount;

        record.setKills(newValue);
        record.store(KITPVP_STATISTICS.ASSISTS);

        this.assists = newValue;

        return new StatisticResult(newValue);
    }

    //TODO verify if the record-counting part of this (res != 0) is accurate or needs to be flipped

    /**
     * Adds an existing kit to the player's kit list
     * @param transaction represents the transaction
     * @param kit represents the kit to add
     * @return whether the action was successful or not
     */
    public KitResult addKit(Transaction transaction, Kit kit) {
        var jooq = ((SQLTransaction)transaction).jooq();
        int res = jooq.insertInto(KITPVP_KITS_OWNERSHIP,KITPVP_KITS_OWNERSHIP.USER_ID,KITPVP_KITS_OWNERSHIP.KIT_ID)
                .values(userID,kit.getId())
                .onDuplicateKeyUpdate()
                .set(KITPVP_KITS_OWNERSHIP.KIT_ID, kit.getId())
                .execute();
        return new KitResult(res != 0);
    }

    //TODO verify if the record-counting part of this (res != 0) is accurate or needs to be flipped

    /**
     * Remove an existing kit from player's kit list
     * @param transaction represents the transaction
     * @param kit represents the kit to remove
     * @return whether the action was successful or not
     */
    public KitResult removeKit(Transaction transaction, Kit kit) {
        var jooq = ((SQLTransaction)transaction).jooq();
        int res = jooq.delete(KITPVP_KITS_OWNERSHIP)
                .where(KITPVP_KITS_OWNERSHIP.USER_ID.eq(userID),KITPVP_KITS_OWNERSHIP.KIT_ID.eq(kit.getId()))
                .execute();
        return new KitResult(res != 0);
    }

    /**
     * Gets all the kits the player owns
     * @param transaction represents a...
     * @return result containing all owned kits
     */
    public ListKitsResult getKits(Transaction transaction) {
        var jooq = ((SQLTransaction)transaction).jooq();
        Result<KitpvpKitsOwnershipRecord> result = jooq.fetch(KITPVP_KITS_OWNERSHIP,KITPVP_KITS_OWNERSHIP.USER_ID.eq(this.userID));

        Set<Kit> kits = new HashSet<>();

        for (KitpvpKitsOwnershipRecord record : result) {
            kits.add(manager.getKit(transaction,record.getKitId()));
        }

        return new ListKitsResult(kits);
    }




}
