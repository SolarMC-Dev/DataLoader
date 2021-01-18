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

import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataManager;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.sql.SQLException;
import java.util.Map;

class CoreDataCenter implements DataCenter {

	private final TransactionManager transactionManager;
	private final Map<DataKey<?, ?>, DataGroup<?, ?>> groups;

	CoreDataCenter(TransactionManager transactionManager, Map<DataKey<?, ?>, DataGroup<?, ?>> groups) {
		this.transactionManager = transactionManager;
		this.groups = Map.copyOf(groups);
	}

	@Override
	public CentralisedFuture<?> runTransact(TransactionRunner runner) {
		return transactionManager.runAsync(() -> {
			try (SQLTransaction transaction = transactionManager.openTransaction()) {
				runner.runTransactUsing(transaction);
			} catch (SQLException ex) {
				throw new UncheckedSQLException(ex);
			}
		});
	}

	@Override
	public <R> CentralisedFuture<R> transact(TransactionActor<R> actor) {
		return transactionManager.supplyAsync(() -> {
			try (SQLTransaction transaction = transactionManager.openTransaction()) {
				return actor.transactUsing(transaction);
			} catch (SQLException ex) {
				throw new UncheckedSQLException(ex);
			}
		});
	}

	@Override
	public <M extends DataManager> M getDataManager(DataKey<?, M> key) {
		@SuppressWarnings("unchecked")
		DataGroup<?, M> group = (DataGroup<?, M>) groups.get(key);
		return group.manager();
	}
}
