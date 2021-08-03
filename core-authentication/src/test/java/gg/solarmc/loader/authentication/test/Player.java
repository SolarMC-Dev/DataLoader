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

package gg.solarmc.loader.authentication.test;

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.authentication.UserWithDataNotYetLoaded;
import gg.solarmc.loader.authentication.User;

import java.util.UUID;

public final class Player implements UserWithDataNotYetLoaded {

    private final User user;
    private Integer dataLoadUserId;

    public Player(User user) {
        this.user = user;
    }

    /**
     * Gets a duplicate player whose data loading is performed separately than this one
     *
     * @return a duplicate player
     */
    public Player duplicate() {
        return new Player(user);
    }

    public User user() {
        return user;
    }

    public boolean isDataLoaded() {
        return dataLoadUserId != null;
    }

    public int getLoadedUserId() {
        if (dataLoadUserId == null) {
            throw new IllegalStateException("No data loaded");
        }
        return dataLoadUserId;
    }

    @Override
    public UUID mcUuid() {
        return user.mcUuid();
    }

    @Override
    public String mcUsername() {
        return user.mcUsername();
    }

    @Override
    public void loadData(Transaction transaction, int userId) {
        dataLoadUserId = userId;
    }

    @Override
    public String toString() {
        return "Player{" +
                "user=" + user +
                ", dataLoadUserId=" + dataLoadUserId +
                '}';
    }
}
