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
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataObject;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LoginHandler {

	private final TransactionManager transactionManager;
	private final Set<DataGroup<?, ?>> groups;

	LoginHandler(TransactionManager transactionManager, Set<DataGroup<?, ?>> groups) {
		this.transactionManager = transactionManager;
		this.groups = Set.copyOf(groups);
	}

	public CentralisedFuture<SolarPlayer> login(UUID mcUUID) {
		return transactionManager.supplyAsync(() -> {
			try {
				return runLogin(mcUUID);
			} catch (SQLException ex) {
				throw new UncheckedSQLException("Unable to log in user", ex);
			}
		});
	}

	private SolarPlayer runLogin(UUID mcUUID) throws SQLException {
		Map<DataKey<?, ?>, DataObject> storedData = new HashMap<>();
		int userId = 0;
		try (SQLTransaction transaction = transactionManager.openTransaction()) {
			// if user exists
			if (true) {
				// New user
				for (DataGroup<?, ?> group : groups) {
					storedData.put(group.key(), group.loader().createDefaultData());
				}
			} else {
				// Existing user
			}
		}
		return new SolarPlayerImpl(storedData, userId, mcUUID);
	}

}
