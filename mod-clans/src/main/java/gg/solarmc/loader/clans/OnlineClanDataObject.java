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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public class OnlineClanDataObject extends ClanDataObject {

    private volatile Integer cachedClan;

    OnlineClanDataObject(int userId, ClanManager manager, @Nullable Clan clan) {
        super(userId, manager);
        this.cachedClan = (clan == null) ? null : clan.getClanId();
    }

    /**
     * Gets current cached clan. Should not be relied upon for correctness
     *
     * @return optional containing cached clan
     */
    public Optional<Clan> currentClan() {
        Integer cachedClan = this.cachedClan;
        if (cachedClan == null) {
            return Optional.empty();
        }
        Optional<Clan> result = manager().cache().getCachedClan(cachedClan);
        if (result.isEmpty()) {
            this.cachedClan = null;
        }
        return result;
    }

    @Override
    void updateCachedClan(@Nullable Clan clan) {
        this.cachedClan = (clan == null) ? null : clan.getClanId();
    }
}
