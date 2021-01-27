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

public class Clan {

    private final int clanID;
    private final String clanName;

    private volatile int clanKills;
    private volatile int clanDeaths;
    private volatile int clanAssists;

    public Clan(int clanID, String clanName, int clanKills, int clanDeaths, int clanAssists) {
        this.clanID = clanID;
        this.clanName = clanName;
        this.clanKills = clanKills;
        this.clanDeaths = clanDeaths;
        this.clanAssists = clanAssists;
    }

    public int getID() {
        return this.clanID;
    }

    public String getName() {
        return this.clanName;
    }

    /**
     * Returns the kills at the time of querying
     * @return kills
     */
    public int currentKills() {
        return clanKills;
    }

    /**
     * Returns the deaths at the time of querying
     * @return deaths
     */
    public int currentDeaths() {
        return clanDeaths;
    }

    /**
     * Returns the ass at the time of querying
     * @return ass
     */
    public int currentAssists() {
        return clanAssists;
    }







}
