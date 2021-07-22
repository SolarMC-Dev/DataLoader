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

package gg.solarmc.loader.impl.test.extension;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.impl.PlayerTracker;

import java.util.Optional;
import java.util.UUID;

final class EmptyPlayerTracker implements PlayerTracker {

    @Override
    public Optional<OnlineSolarPlayer> getOnlinePlayerForUuid(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<OnlineSolarPlayer> getOnlinePlayerForName(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<OnlineSolarPlayer> getOnlinePlayerForUserId(int userId) {
        return Optional.empty();
    }
}
