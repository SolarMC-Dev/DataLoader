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
import gg.solarmc.loader.schema.tables.records.KitpvpStatisticsRecord;
import org.jooq.DSLContext;

import java.util.Set;

import static gg.solarmc.loader.schema.tables.KitpvpKitsOwnership.KITPVP_KITS_OWNERSHIP;
import static gg.solarmc.loader.schema.tables.KitpvpStatistics.KITPVP_STATISTICS;

public abstract class KitPvp implements DataObject {

    private final int userID;
    private final KitPvpManager manager;

    KitPvp(int userID, KitPvpManager manager) {
        this.userID = userID;
        this.manager = manager;
    }

    private KitpvpStatisticsRecord getStatistics(Transaction transaction) {
        KitpvpStatisticsRecord record = transaction
                .getProperty(DSLContext.class)
                .fetchOne(KITPVP_STATISTICS,KITPVP_STATISTICS.USER_ID.eq(userID));

        assert record != null : "Data is missing from the stats!";

        return record;
    }

    abstract void updateKills(int i);
    abstract void updateDeaths(int i);
    abstract void updateAssists(int i);
    abstract void updateExperience(int i);
    abstract void updateHighestKillstreak(int i);
    abstract void updateCurrentKillstreak(int i);

    abstract void updateBounty(int i);

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
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        int existingValue = statisticsRecord.getKills();
        int newValue = existingValue + amount;

        statisticsRecord.setKills(newValue);
        statisticsRecord.store(KITPVP_STATISTICS.KILLS);
        this.updateKills(newValue);

        return new StatisticResult(newValue, existingValue);
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
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        int existingValue = statisticsRecord.getDeaths();
        int newValue = existingValue + amount;

        statisticsRecord.setKills(newValue);
        statisticsRecord.store(KITPVP_STATISTICS.DEATHS);
        this.updateDeaths(newValue);

        return new StatisticResult(newValue, existingValue);
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
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        int existingValue = statisticsRecord.getAssists();
        int newValue = existingValue + amount;

        statisticsRecord.setKills(newValue);
        statisticsRecord.store(KITPVP_STATISTICS.ASSISTS);

        this.updateAssists(newValue);

        return new StatisticResult(newValue, existingValue);
    }

    /**

     * Adds experience to the user account. Infallible.
     * @param transaction represents the transaction
     * @param amount represents the amount of xp to add
     * @return a result with the new value of xp
     */
    public StatisticResult addExperience(Transaction transaction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        int existingValue = statisticsRecord.getExperience();
        int newValue = existingValue + amount;

        statisticsRecord.setExperience(newValue);
        statisticsRecord.store(KITPVP_STATISTICS.EXPERIENCE);

        this.updateExperience(newValue);

        return new StatisticResult(newValue, existingValue);
    }

    /**
     * Adds current killstreaks to the user account. Infallible.
     *
     * This will increment both current and highest killstreaks.
     *
     * @param transaction represents the current killstreak
     * @param amount represents the amount of killstreak to add
     * @return a result with the new value of killstreak
     */
    public StatisticResult addKillstreaks(Transaction transaction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        int existingValue = statisticsRecord.getCurrentKillstreak();
        int newValue = existingValue + amount;

        int existingTwo = statisticsRecord.getHighestKillstreak();

        if (newValue > existingTwo) {
            int newTwo = existingTwo + amount;
            statisticsRecord.setHighestKillstreak(newTwo);
            this.updateHighestKillstreak(newTwo);
        }

        statisticsRecord.setCurrentKillstreak(newValue);
        statisticsRecord.store(KITPVP_STATISTICS.HIGHEST_KILLSTREAK,KITPVP_STATISTICS.CURRENT_KILLSTREAK);

        this.updateCurrentKillstreak(newValue);

        return new StatisticResult(newValue, existingValue);
    }

    /**
     * Resets the current killstreak amouont. Infallible
     *
     * Used to reset the current killstreak
     *
     * @param transaction represents the current transaction
     */
    public void resetCurrentKillstreaks(Transaction transaction) {
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        statisticsRecord.setCurrentKillstreak(0);
        statisticsRecord.store(KITPVP_STATISTICS.CURRENT_KILLSTREAK);

        this.updateCurrentKillstreak(0);
    }

    /**
     * Gets the bounty
     * @param transaction the transaction
     * @return bounty
     */
    public int getBounty(Transaction transaction) {
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        int existingValue = statisticsRecord.getBounty();

        this.updateBounty(existingValue);

        return existingValue;
    }

    /**
     * Adds bounty currency to the player. Infallible.
     *
     * @param transaction the transaction
     * @param amount amount to add
     * @throws IllegalArgumentException if the amount is negative
     */
    public void addBounty(Transaction transaction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative!");
        }

        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        int existingValue = statisticsRecord.getBounty();
        int newValue = existingValue + amount;

        statisticsRecord.setBounty(newValue);
        statisticsRecord.store(KITPVP_STATISTICS.BOUNTY);

        this.updateBounty(amount);
    }

    /**
     * Resets the bounty. infallible.
     *
     * @param transaction the transaction
     */
    public void resetBounty(Transaction transaction) {
        KitpvpStatisticsRecord statisticsRecord = getStatistics(transaction);

        statisticsRecord.setBounty(0);
        statisticsRecord.store(KITPVP_STATISTICS.BOUNTY);

        this.updateBounty(0);
    }

    /**

     * Adds an existing kit to the player's kit list
     * @param transaction represents the transaction
     * @param kit represents the kit to add
     * @return whether the action was successful or not
     */
    public KitOwnershipResult addKit(Transaction transaction, Kit kit) {
        int res = transaction.getProperty(DSLContext.class)
                .insertInto(KITPVP_KITS_OWNERSHIP)
                .columns(KITPVP_KITS_OWNERSHIP.USER_ID, KITPVP_KITS_OWNERSHIP.KIT_ID)
                .values(userID, kit.getId())
                .onDuplicateKeyIgnore()
                .execute();
        return new KitOwnershipResult(res != 0);
    }

    /**
     * Remove an existing kit from player's kit list
     * @param transaction represents the transaction
     * @param kit represents the kit to remove
     * @return whether the action was successful or not
     */
    public KitOwnershipResult removeKit(Transaction transaction, Kit kit) {
        int res = transaction.getProperty(DSLContext.class)
                .delete(KITPVP_KITS_OWNERSHIP)
                .where(KITPVP_KITS_OWNERSHIP.USER_ID.eq(userID))
                .and(KITPVP_KITS_OWNERSHIP.KIT_ID.eq(kit.getId()))
                .execute();
        return new KitOwnershipResult(res != 0);
    }

    /**
     * Gets all the kits the player owns
     * @param transaction the transaction
     * @return result containing all owned kits
     */
    public Set<Kit> getKits(Transaction transaction) {
        Set<Kit> kits = transaction.getProperty(DSLContext.class)
                .select(KITPVP_KITS_OWNERSHIP.KIT_ID).from(KITPVP_KITS_OWNERSHIP)
                .where(KITPVP_KITS_OWNERSHIP.USER_ID.eq(this.userID))
                .fetchSet((ownershipRecord) -> {
                    return manager.getKit(transaction, ownershipRecord.get(KITPVP_KITS_OWNERSHIP.KIT_ID));
                });
        return Set.copyOf(kits);
    }

    /**
     * Determines if the user owns the specified kit
     *
     * @param transaction the transaction
     * @param kit the kit
     * @return true if the user owns the kit, false otherwise
     */
    public boolean ownsKit(Transaction transaction, Kit kit) {
        var context = transaction.getProperty(DSLContext.class);
        return context.fetchExists(
                context.select(KITPVP_KITS_OWNERSHIP.KIT_ID).from(KITPVP_KITS_OWNERSHIP)
                .where(KITPVP_KITS_OWNERSHIP.USER_ID.eq(this.userID))
                .and(KITPVP_KITS_OWNERSHIP.KIT_ID.eq(kit.getId())));
    }

}
