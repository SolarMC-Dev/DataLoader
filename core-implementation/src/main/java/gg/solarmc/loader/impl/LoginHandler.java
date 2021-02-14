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
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.data.DataObject;
import gg.solarmc.loader.impl.player.DelegatingSolarPlayer;
import gg.solarmc.loader.impl.player.OnlineSolarPlayerImpl;
import gg.solarmc.loader.impl.player.SolarPlayerData;
import gg.solarmc.loader.impl.player.SolarPlayerId;
import org.jooq.DSLContext;
import space.arim.omnibus.util.UUIDUtil;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static gg.solarmc.loader.schema.tables.UserIds.USER_IDS;

public final class LoginHandler {

	private final TransactionSource transactionSource;
	private final Set<DataGroup<?, ?, ?>> groups;
	private final PlayerTracker playerTracker;

	LoginHandler(TransactionSource transactionSource, Set<DataGroup<?, ?, ?>> groups, PlayerTracker playerTracker) {
		this.transactionSource = transactionSource;
		this.groups = Set.copyOf(groups);
		this.playerTracker = playerTracker;
	}

	/**
	 * Loads an online solar player. First looks up their user ID.
	 *
	 * @param mcUuid the user's MC UUID
	 * @return a future which yields the solar player
	 */
	public CentralisedFuture<OnlineSolarPlayer> loginUser(UUID mcUuid) {
		return transactionSource.transact((transaction) -> {
			Integer userId = transaction.getProperty(DSLContext.class)
					.select(USER_IDS.ID)
					.from(USER_IDS)
					.where(USER_IDS.UUID.eq(UUIDUtil.toByteArray(mcUuid)))
					.fetchOne(USER_IDS.ID);
			if (userId == null) {
				throw new IllegalStateException("Unable to find user ID for " + mcUuid);
			}
			return loginUserNow(transaction, userId, mcUuid);
		});
	}

	/**
	 * Loads an online solar player
	 *
	 * @param userId the user ID
	 * @param mcUuid the user's MC UUID
	 * @return a future which yields the solar player
	 */
	public CentralisedFuture<OnlineSolarPlayer> loginUser(int userId, UUID mcUuid) {
		return transactionSource.transact((transaction) -> loginUserNow(transaction, userId, mcUuid));
	}

	/**
	 * Loads an online solar player
	 *
	 * @param transaction the transaction
	 * @param userId the user ID
	 * @param mcUuid the user's MC UUID
	 * @return the solar player
	 */
	public OnlineSolarPlayer loginUserNow(Transaction transaction, int userId, UUID mcUuid) {
		SolarPlayerData data = loadDataWith(userId, (loader, id) -> loader.loadData(transaction, id));
		return new OnlineSolarPlayerImpl(
				new SolarPlayerId(userId, mcUuid),
				data);
	}

	/**
	 * Creates an offline solar player
	 *
	 * @param userId the user ID
	 * @param mcUuid the user's MC UUID
	 * @return the offline solar player
	 */
	public SolarPlayer createOfflineUser(int userId, UUID mcUuid) {
		SolarPlayerData data = loadDataWith(userId, DataLoader::createOfflineData);
		return new DelegatingSolarPlayer(
				new SolarPlayerId(userId, mcUuid),
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
