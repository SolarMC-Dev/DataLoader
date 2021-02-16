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

public class OnlineKitPvp extends KitPvp {
    private volatile int kills;
    private volatile int deaths;
    private volatile int assists;

    public OnlineKitPvp(int userID, KitPvpManager manager, Integer kills, Integer deaths, Integer assists) {
        super(userID, manager);

        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;

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
}