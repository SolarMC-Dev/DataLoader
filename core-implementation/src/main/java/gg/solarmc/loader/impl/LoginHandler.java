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

import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataObject;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LoginHandler {

	private final TransactionSource transactionSource;
	private final Set<DataGroup<?, ?>> groups;

	LoginHandler(TransactionSource transactionSource, Set<DataGroup<?, ?>> groups) {
		this.transactionSource = transactionSource;
		this.groups = Set.copyOf(groups);
	}

	/**
	 * Loads a solar player
	 *
	 * @param userId the user ID
	 * @param mcUuid the user's MC UUID
	 * @return a future which yields the solar player
	 */
	public CentralisedFuture<SolarPlayer> loginUser(int userId, UUID mcUuid) {
		return transactionSource.transact((transaction) -> loginUserNow(transaction, userId, mcUuid));
	}

	/**
	 * Loads a solar player
	 *
	 * @param transaction the transaction
	 * @param userId the user ID
	 * @param mcUuid the user's MC UUID
	 * @return the solar player
	 */
	public SolarPlayer loginUserNow(Transaction transaction, int userId, UUID mcUuid) {
		Map<DataKey<?, ?>, DataObject> storedData = new HashMap<>();
		for (DataGroup<?, ?> group : groups) {
			storedData.put(group.key(), group.loader().loadData(transaction, userId));
		}
		return new SolarPlayerImpl(storedData, userId, mcUuid);
	}

}
