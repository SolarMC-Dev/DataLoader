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

package gg.solarmc.loader.kitpvp;

import java.math.BigDecimal;

public class OnlineKitPvp extends KitPvp {
    private volatile int kills;
    private volatile int deaths;
    private volatile int assists;
    private volatile int experience;
    private volatile int currentKillstreaks;
    private volatile int highestKillstreaks;
    private volatile BigDecimal creditsBounty;
    private volatile BigDecimal plainEcoBounty;

    public OnlineKitPvp(int userID, KitPvpManager manager,
                        int kills, int deaths, int assists, int experience,
                        int currentKillstreaks, int highestKillstreaks,
                        BigDecimal creditsBounty, BigDecimal plainEcoBounty) {
        super(userID, manager);
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.experience = experience;
        this.currentKillstreaks = currentKillstreaks;
        this.highestKillstreaks = highestKillstreaks;
        this.creditsBounty = creditsBounty;
        this.plainEcoBounty = plainEcoBounty;
    }

    @Override
    void updateKills(int i) {
        this.kills = i;
    }

    @Override
    void updateDeaths(int i) {
        this.deaths = i;
    }

    @Override
    void updateAssists(int i) {
        this.assists = i;
    }

    @Override
    void updateExperience(int i) {
        this.experience = i;
    }

    @Override
    void updateHighestKillstreak(int i) {
        this.highestKillstreaks = i;
    }

    @Override
    void updateCurrentKillstreak(int i) {
        this.currentKillstreaks = i;
    }

    @Override
    void updateBounty(BountyCurrency currency, BigDecimal bounty) {
        switch (currency) {
        case CREDITS -> creditsBounty = bounty;
        case PLAIN_ECO -> plainEcoBounty = bounty;
        }
    }

    /**
     * Cached kills. Not reliable.
     * @return kills
     */
    public int currentKills() {
        return kills;
    }

    /**
     * Cached deaths. Not reliable.
     * @return deaths
     */
    public int currentDeaths() {
        return deaths;
    }

    /**
     * Cached assists. Not reliable
     * @return assists
     */
    public int currentAssists() {
        return assists;
    }

    public int currentCurrentKillstreaks() {
        return currentKillstreaks;
    }

    public int currentHighestKillstreaks() {
        return highestKillstreaks;
    }

    public int currentExperience() {
        return experience;
    }

    /**
     * The cached bounty value. Should not be relied upon for correctness
     *
     * @param currency the bounty currency
     * @return the bounty
     */
    public BigDecimal currentBounty(BountyCurrency currency) {
        return switch (currency) {
            case CREDITS -> creditsBounty;
            case PLAIN_ECO -> plainEcoBounty;
        };
    }
}
