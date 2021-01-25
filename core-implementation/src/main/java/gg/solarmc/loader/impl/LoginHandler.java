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
import org.jooq.DSLContext;
import space.arim.omnibus.util.concurrent.CentralisedFuture;
import space.arim.omnibus.util.UUIDUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static gg.solarmc.loader.schema.tables.UserIds.USER_IDS;

public class LoginHandler {

	private final TransactionSource transactionSource;
	private final Set<DataGroup<?, ?>> groups;

	LoginHandler(TransactionSource transactionSource, Set<DataGroup<?, ?>> groups) {
		this.transactionSource = transactionSource;
		this.groups = Set.copyOf(groups);
	}

	public CentralisedFuture<SolarPlayer> login(UUID mcUUID) {
		return transactionSource.supplyAsync(() -> {
			try {
				return runLogin(mcUUID);
			} catch (SQLException ex) {
				throw new UncheckedSQLException("Unable to log in user", ex);
			}
		});
	}

	private SolarPlayer runLogin(UUID mcUuid) throws SQLException {
		Map<DataKey<?, ?>, DataObject> storedData = new HashMap<>();
		byte[] mcUuidBytes = UUIDUtil.toByteArray(mcUuid);
		int userId;
		try (SQLTransaction transaction = transactionSource.openTransaction()) {
			transaction.markReadOnly();

			var idRecord = transaction.getProperty(DSLContext.class)
					.select(USER_IDS.ID).from(USER_IDS)
					.where(USER_IDS.UUID.eq(mcUuidBytes)).fetchOne();
			if (idRecord == null) {
				throw new IllegalStateException("User ID does not exist for " + mcUuid);
			}
			userId = idRecord.get(USER_IDS.ID);
			for (DataGroup<?, ?> group : groups) {
				storedData.put(group.key(), group.loader().loadData(transaction, userId));
			}
			// It is okay that the transaction is not committed, because data loading is read-only
		}
		return new SolarPlayerImpl(storedData, userId, mcUuid);
	}

}
