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

import java.util.Optional;

public class OnlineClanDataObject extends ClanDataObject {

    private volatile Clan cachedClan;

    OnlineClanDataObject(int userId, ClanManager manager, Clan cached) {
        super(userId, manager);
        this.cachedClan = cached;
    }

    /**
     * Gets current cached clan. Not accurate.
     * @return Optional containing cached value
     */
    public Optional<Clan> currentClan() {
        if (cachedClan != null && cachedClan.isInvalid()) {
            cachedClan = null;
        }
        return Optional.ofNullable(cachedClan);
    }

    @Override
    void updateCachedClan(Clan clan) {
        this.cachedClan = clan;
    }
}
