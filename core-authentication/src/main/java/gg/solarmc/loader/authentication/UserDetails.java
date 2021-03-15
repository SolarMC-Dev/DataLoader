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

package gg.solarmc.loader.authentication;

import java.util.Objects;
import java.util.UUID;

public record UserDetails(UUID mcUuid, String username) {

    public UserDetails {
        Objects.requireNonNull(mcUuid, "mcUuid");
        Objects.requireNonNull(username, "username");
    }

    /**
     * Determines whether the UUID is premium
     *
     * @return true if the uuid is premium, false otherwise
     */
    public boolean isPremium() {
        return UUIDOperations.isPremium(mcUuid());
    }

}
