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

package gg.solarmc.loader.impl.login;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.data.DataObject;
import gg.solarmc.loader.impl.launch.DataGroup;
import gg.solarmc.loader.impl.LoginHandler;
import gg.solarmc.loader.impl.PlayerTracker;
import gg.solarmc.loader.impl.TransactionSource;
import gg.solarmc.loader.impl.UserDetails;
import gg.solarmc.loader.impl.player.DelegatingSolarPlayer;
import gg.solarmc.loader.impl.player.OnlineSolarPlayerImpl;
import gg.solarmc.loader.impl.player.SolarPlayerData;
import gg.solarmc.loader.impl.player.SolarPlayerId;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class LoginHandlerImpl implements LoginHandler {

    private final TransactionSource transactionSource;
    private final Set<DataGroup<?, ?, ?>> groups;
    private final PlayerTracker playerTracker;
    private final IdRetrieval idRetrieval;
    private final NameAddressHistoryUpdate nameAddressHistoryUpdate;

    LoginHandlerImpl(TransactionSource transactionSource, Set<DataGroup<?, ?, ?>> groups, PlayerTracker playerTracker,
                            IdRetrieval idRetrieval, NameAddressHistoryUpdate nameAddressHistoryUpdate) {
        this.transactionSource = transactionSource;
        this.groups = groups;
        this.playerTracker = playerTracker;
        this.idRetrieval = idRetrieval;
        this.nameAddressHistoryUpdate = nameAddressHistoryUpdate;
    }

    @Override
    public CentralisedFuture<OnlineSolarPlayer> loginUser(UserDetails userDetails) {
        return transactionSource.transact((transaction) -> {
            int userId = idRetrieval.retrieveUserId(transaction, userDetails);
            return loginUserNow(transaction, userId, userDetails);
        });
    }

    @Override
    public OnlineSolarPlayer loginUserNow(Transaction transaction, int userId, UserDetails userDetails) {
        nameAddressHistoryUpdate.update(transaction, userDetails);
        SolarPlayerData data = loadDataWith(userId, (loader, id) -> loader.loadData(transaction, id));
        return new OnlineSolarPlayerImpl(
                new SolarPlayerId(userId, userDetails.mcUuid(), userDetails.mcUsername()),
                data);
    }

    @Override
    public SolarPlayer createOfflineUser(int userId, UUID mcUuid, String mcUsername) {
        SolarPlayerData data = loadDataWith(userId, DataLoader::createOfflineData);
        return new DelegatingSolarPlayer(
                new SolarPlayerId(userId, mcUuid, mcUsername),
                data,
                playerTracker);
    }

    private SolarPlayerData loadDataWith(int userId, LoadDataFunction function) {
        if (groups.isEmpty()) {
            return SolarPlayerData.empty();
        }
        Map<DataKey<?, ?, ?>, DataObject> storedData = new HashMap<>();
        for (DataGroup<?, ?, ?> group : groups) {
            storedData.put(group.key(), function.loadData(group.loader(), userId));
        }
        return new SolarPlayerData(storedData);
    }

    private interface LoadDataFunction {
        DataObject loadData(DataLoader<?, ?> loader, int userId);
    }
}
