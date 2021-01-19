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
import gg.solarmc.loader.kitpvp.kit.Kit;
import gg.solarmc.loader.schema.tables.records.KitpvpKitsOwnershipRecord;
import gg.solarmc.loader.schema.tables.records.KitpvpStatisticsRecord;
import org.jooq.DSLContext;
import org.jooq.Result;

import java.util.List;
import java.util.Set;

import static gg.solarmc.loader.schema.tables.KitpvpStatistics.*;
import static gg.solarmc.loader.schema.tables.KitpvpKitsOwnership.*;

public class KitPvp implements DataObject {

    private volatile Integer kills;
    private volatile Integer deaths;
    private volatile Integer assists;

    private final int userID;
    private final KitPvpManager manager;

    private final Set<Integer> ownedKits;

    public KitPvp(int userID, Integer kills, Integer deaths, Integer assists, Set<Integer> kits, KitPvpManager manager) {
        this.userID = userID;

        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.ownedKits = kits;

        this.manager = manager;
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

    private KitpvpStatisticsRecord getStatistics(Transaction transaction) {
        DSLContext context = ((SQLTransaction) transaction).jooq();

        KitpvpStatisticsRecord record = context.fetchOne(KITPVP_STATISTICS,KITPVP_STATISTICS.USER_ID.eq(userID));
        assert record != null : "Data is missing!";

        return record;
    }

    private Result<KitpvpKitsOwnershipRecord> getOwnedKits(Transaction transaction) {
        DSLContext context = ((SQLTransaction) transaction).jooq();

        //cannot be null, is a list
        return context.fetch(KITPVP_KITS_OWNERSHIP,KITPVP_KITS_OWNERSHIP.USER_ID.eq(userID));
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

    /**
     * Adds an existing kit to the player's kit list based on ID
     * @param transaction represents the transaction
     * @param kitID represents the kit to add (by ID)
     * @return resulting kits the player owns by ID
     */
    public KitResult addKit(Transaction transaction, int kitID) {
        if (!manager.verifyKitID(kitID)) {
            throw new IllegalArgumentException("Kit does not exist locally!");
        }

        var kits = getOwnedKits(transaction).intoMap(KITPVP_KITS_OWNERSHIP.USER_ID,KITPVP_KITS_OWNERSHIP.KIT_ID);

        if (kits.containsValue(kitID)) {
            return new KitResult(kits.values(),false);
        }

        ((SQLTransaction) transaction).jooq().insertInto(KITPVP_KITS_OWNERSHIP)
                .columns(KITPVP_KITS_OWNERSHIP.USER_ID,KITPVP_KITS_OWNERSHIP.KIT_ID)
                .values(userID,kitID);

        this.ownedKits.add(kitID);
        kits.values().add(kitID);

        return new KitResult(kits.values(),true);
    }

    /**
     * Adds an existing kit to the player's kit list based on the Name.
     * Recommended to use addKit by ID instead due to case sensitivity.
     * @param transaction represents the transaction
     * @param kitName represents the name of the kit
     * @return resulting kits the player owns by ID
     */
    public KitResult addKit(Transaction transaction, String kitName) {
        return addKit(transaction,manager.getKitIDFromString(kitName));
    }

    /**
     * Remove a kit from the player based on kit ID. Can fail.
     * @param transaction represents the transaction
     * @param kitID represents the ID of the kit
     */
    public RemoveKitResult removeKit(Transaction transaction, int kitID) {
        return null;
    }

}
