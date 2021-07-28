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
import gg.solarmc.loader.schema.routines.KitpvpAddKillstreak;
import org.jooq.DSLContext;

import java.util.Set;

import static gg.solarmc.loader.kitpvp.KitOwnership.ADDED_KIT;
import static gg.solarmc.loader.kitpvp.KitOwnership.NO_CHANGE;
import static gg.solarmc.loader.kitpvp.KitOwnership.REMOVED_KIT;
import static gg.solarmc.loader.schema.Routines.kitpvpAddAssists;
import static gg.solarmc.loader.schema.Routines.kitpvpAddBounty;
import static gg.solarmc.loader.schema.Routines.kitpvpAddDeaths;
import static gg.solarmc.loader.schema.Routines.kitpvpAddExperience;
import static gg.solarmc.loader.schema.Routines.kitpvpAddKills;
import static gg.solarmc.loader.schema.Routines.kitpvpResetBounty;
import static gg.solarmc.loader.schema.Routines.kitpvpResetCurrentKillstreak;
import static gg.solarmc.loader.schema.tables.KitpvpKitsOwnership.KITPVP_KITS_OWNERSHIP;
import static gg.solarmc.loader.schema.tables.KitpvpStatistics.KITPVP_STATISTICS;

public abstract class KitPvp implements DataObject {

    private final int userId;
    private final KitPvpManager manager;

    KitPvp(int userId, KitPvpManager manager) {
        this.userId = userId;
        this.manager = manager;
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
        int newValue = transaction.getProperty(DSLContext.class)
                .select(kitpvpAddKills(userId, amount))
                .fetchSingle().value1();
        this.updateKills(newValue);

        return new StatisticResult(newValue, newValue - amount);
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
        int newValue = transaction.getProperty(DSLContext.class)
                .select(kitpvpAddDeaths(userId, amount))
                .fetchSingle().value1();

        this.updateDeaths(newValue);
        return new StatisticResult(newValue, newValue - amount);
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
        int newValue = transaction.getProperty(DSLContext.class)
                .select(kitpvpAddAssists(userId, amount))
                .fetchSingle().value1();

        this.updateAssists(newValue);
        return new StatisticResult(newValue, newValue - amount);
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
        int newValue = transaction.getProperty(DSLContext.class)
                .select(kitpvpAddExperience(userId, amount))
                .fetchSingle().value1();

        this.updateExperience(newValue);
        return new StatisticResult(newValue, newValue - amount);
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
        KitpvpAddKillstreak killstreakProcedure = new KitpvpAddKillstreak();
        killstreakProcedure.setUserIdentifier(userId);
        killstreakProcedure.setAmount(amount);
        killstreakProcedure.execute(transaction.getProperty(DSLContext.class).configuration());

        int newCurrentKillstreak = killstreakProcedure.getNewCurrentKillstreak();
        int newHighestKillstreak = killstreakProcedure.getNewHighestKillstreak();

        this.updateCurrentKillstreak(newCurrentKillstreak);
        this.updateHighestKillstreak(newHighestKillstreak);

        return new StatisticResult(newCurrentKillstreak, newCurrentKillstreak - amount);
    }

    /**
     * Resets the current killstreak amouont. Infallible
     *
     * Used to reset the current killstreak
     *
     * @param transaction represents the current transaction
     * @return the amount they had before
     */
    public int resetCurrentKillstreaks(Transaction transaction) {
        int old = transaction.getProperty(DSLContext.class)
                .select(kitpvpResetCurrentKillstreak(userId))
                .fetchSingle().value1();

        this.updateCurrentKillstreak(0);
        return old;
    }

    /**
     * Gets the bounty
     * @param transaction the transaction
     * @return the current bounty
     */
    public int getBounty(Transaction transaction) {
        int existingValue = transaction.getProperty(DSLContext.class)
                .select(KITPVP_STATISTICS.BOUNTY)
                .from(KITPVP_STATISTICS)
                .where(KITPVP_STATISTICS.USER_ID.eq(userId))
                .fetchSingle().value1();

        this.updateBounty(existingValue);

        return existingValue;
    }

    /**
     * Adds bounty currency to the player. Infallible.
     *
     * @param transaction the transaction
     * @param amount amount to add
     * @throws IllegalArgumentException if the amount is negative
     * @return the new bounty on the player
     */
    public int addBounty(Transaction transaction, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be negative!");
        }
        int newValue = transaction.getProperty(DSLContext.class)
                .select(kitpvpAddBounty(userId, amount))
                .fetchSingle().value1();

        this.updateBounty(newValue);
        return newValue;
    }

    /**
     * Resets the bounty. infallible.
     *
     * @param transaction the transaction
     * @return the previous bounty on the player
     */
    public int resetBounty(Transaction transaction) {
        int previousBounty = transaction.getProperty(DSLContext.class)
                .select(kitpvpResetBounty(userId))
                .fetchSingle().value1();

        this.updateBounty(0);
        return previousBounty;
    }

    /**

     * Adds an existing kit to the player's kit list
     * @param transaction represents the transaction
     * @param kit represents the kit to add
     * @return whether the action was successful or not
     */
    public KitOwnership addKit(Transaction transaction, Kit kit) {
        int updateCount = transaction.getProperty(DSLContext.class)
                .insertInto(KITPVP_KITS_OWNERSHIP)
                .columns(KITPVP_KITS_OWNERSHIP.USER_ID, KITPVP_KITS_OWNERSHIP.KIT_ID)
                .values(userId, kit.getId())
                .onConflictDoNothing()
                .execute();
        return updateCount != 0 ? ADDED_KIT : NO_CHANGE;
    }

    /**
     * Remove an existing kit from player's kit list
     * @param transaction represents the transaction
     * @param kit represents the kit to remove
     * @return whether the action was successful or not
     */
    public KitOwnership removeKit(Transaction transaction, Kit kit) {
        int updateCount = transaction.getProperty(DSLContext.class)
                .delete(KITPVP_KITS_OWNERSHIP)
                .where(KITPVP_KITS_OWNERSHIP.USER_ID.eq(userId))
                .and(KITPVP_KITS_OWNERSHIP.KIT_ID.eq(kit.getId()))
                .execute();
        return updateCount != 0 ? REMOVED_KIT : NO_CHANGE;
    }

    /**
     * Gets all the kits the player owns
     * @param transaction the transaction
     * @return result containing all owned kits
     */
    public Set<Kit> getKits(Transaction transaction) {
        Set<Kit> kits = transaction.getProperty(DSLContext.class)
                .select(KITPVP_KITS_OWNERSHIP.KIT_ID).from(KITPVP_KITS_OWNERSHIP)
                .where(KITPVP_KITS_OWNERSHIP.USER_ID.eq(this.userId))
                .fetchSet((ownershipRecord) -> {
                    return manager.getKitById(transaction, ownershipRecord.get(KITPVP_KITS_OWNERSHIP.KIT_ID))
                            .orElseThrow(() -> new IllegalStateException("Player owns kit which does not exist"));
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
                .where(KITPVP_KITS_OWNERSHIP.USER_ID.eq(this.userId))
                .and(KITPVP_KITS_OWNERSHIP.KIT_ID.eq(kit.getId())));
    }

}
