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

import java.util.Objects;
import java.util.Optional;

/**
 * Represents something that can be in a clan. Always compare with #equals
 *
 * Think of this as a final object - it simply holds information for you to interact with
 * that is accurate at the time - aurium, telling aurium how his own code works (2021)
 */
public class ClanMember {

    private final Integer clanId; //nullable
    private final int userId;
    private final ClanManager manager;

    public ClanMember(Integer clanId, int userId, ClanManager manager) {
        this.clanId = clanId;
        this.userId = userId;
        this.manager = manager;
    }

    public int getUserId() {
        return userId;
    }

    /**
     * Checks if a ClanDataObject is similar to this
     * @param object the object to compare
     * @return whether they are similar or not
     */
    public boolean isSimilar(ClanDataObject object) {
        return object.isSimilar(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClanMember that = (ClanMember) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }




}
