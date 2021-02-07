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
import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.data.DataObject;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.sql.SQLException;
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
	 * Loads a solar player who has already logged in before
	 *
	 * @param userId the user ID
	 * @param mcUuid the user's MC UUID
	 * @return a future which yields the solar player
	 */
	public CentralisedFuture<SolarPlayer> loginExistingUser(int userId, UUID mcUuid) {
		return runLogin(userId, mcUuid, DataLoader::loadData);
	}

	/**
	 * Loads a new solar player
	 *
	 * @param userId the user ID
	 * @param mcUuid the user's MC UUID
	 * @return a future which yields the solar player
	 */
	public CentralisedFuture<SolarPlayer> loginNewUser(int userId, UUID mcUuid) {
		return runLogin(userId, mcUuid, DataLoader::createDefaultData);
	}

	private interface LoadDataFunction {
		<D> D loadData(DataLoader<D> loader, Transaction transaction, int userId);
	}

	private CentralisedFuture<SolarPlayer> runLogin(int userId, UUID mcUuid, LoadDataFunction function) {
		return transactionSource.supplyAsync(() -> {
			Map<DataKey<?, ?>, DataObject> storedData = new HashMap<>();
			try (SQLTransaction transaction = transactionSource.openTransaction()) {
				transaction.markReadOnly();

				for (DataGroup<?, ?> group : groups) {
					storedData.put(group.key(), function.loadData(group.loader(), transaction, userId));
				}
				// It is okay that the transaction is not committed, because data loading is read-only
			} catch (SQLException ex) {
				throw new UncheckedSQLException("Unable to log in user", ex);
			}
			return new SolarPlayerImpl(storedData, userId, mcUuid);
		});
	}

}
