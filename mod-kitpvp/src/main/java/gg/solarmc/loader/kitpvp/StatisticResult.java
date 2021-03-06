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

public class StatisticResult {

    private final int newStat;
    private final int oldStat;

    StatisticResult(int newStat, int oldStat) {
        this.newStat = newStat;
        this.oldStat = oldStat;
    }

    /**
     * The updated value of the statistic
     *
     * @return the new value
     */
    public int newValue() {
        return newStat;
    }

    /**
     * The original value of the statistic
     * @return the old value
     */
    public int oldValue() {
        return oldStat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticResult that = (StatisticResult) o;
        return newStat == that.newStat;
    }

    @Override
    public int hashCode() {
        return newStat;
    }
}
