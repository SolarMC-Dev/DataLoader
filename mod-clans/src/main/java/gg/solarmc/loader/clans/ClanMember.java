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

/**
 * Represents something that can be in a clan. Always compare with #equals
 *
 * Think of this as a final object - it simply holds information for you to interact with
 * that is accurate at the time - aurium, telling aurium how his own code works (2021)
 *
 * @param userId the user ID of the member
 */
public record ClanMember(int userId) {

    /**
     * Gets the user ID of the clan member
     *
     * @return the user ID
     * @deprecated Use {@link #userId()}
     */
    @Deprecated
    public int getUserId() {
        return userId;
    }

    /**
     * Checks if a gg.solarmc.loader.clans.ClanDataObject is similar to this
     * @param object the object to compare
     * @return whether they are similar or not
     */
    public boolean isSimilar(ClanDataObject object) {
        return object.isSimilar(this);
    }

}
