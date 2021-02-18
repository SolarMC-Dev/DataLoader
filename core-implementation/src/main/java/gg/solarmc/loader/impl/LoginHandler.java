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

package gg.solarmc.loader.impl;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.util.UUID;

/**
 * Entry point for platforms to obtain solar players
 *
 */
public interface LoginHandler {

    /**
     * Conducts the login of a user. <br>
     * <br>
     * <b>Should never be called for an unauthenticated user.</b> The user details are assumed to be accurate.
     *
     * @param userDetails the user details
     * @return a future which yields the solar player
     * @throws IllegalStateException if the user does not exist and the handler is configured to assume
     * that all users exist
     */
    CentralisedFuture<OnlineSolarPlayer> loginUser(UserDetails userDetails);

    /**
     * Conducts the login of a user. <br>
     * <br>
     * <b>Should never be called for an unauthenticated user.</b> The user details are assumed to be accurate.
     *
     * @param transaction the transaction
     * @param userId the user's ID
     * @param userDetails the user details
     * @return the solar player
     */
    OnlineSolarPlayer loginUserNow(Transaction transaction, int userId, UserDetails userDetails);

    /**
     * Creates an offline solar player
     *
     * @param userId the user ID
     * @param mcUuid the user's MC UUID
     * @return the offline solar player
     */
    SolarPlayer createOfflineUser(int userId, UUID mcUuid);

    /**
     * Builder of login handlers. All options on this builder are disabled by default.
     *
     */
    interface Builder {

        /**
         * Instructs that the handler will generate the user ID if no user ID exists
         * for the given user. <br>
         * <br>
         * Mostly useful for testing and development purposes.
         *
         * @return this builder
         */
        Builder createUserIfNotExists();

        /**
         * Instructs that the handler will update the user's name and address history.
         *
         * @return this builder
         */
        Builder updateNameAddressHistory();

        /**
         * Builds into a login handler
         *
         * @param playerTracker the player tracker
         * @return the login handler
         */
        LoginHandler build(PlayerTracker playerTracker);

    }
}
