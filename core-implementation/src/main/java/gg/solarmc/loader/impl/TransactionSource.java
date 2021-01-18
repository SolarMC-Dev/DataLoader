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

import space.arim.omnibus.util.concurrent.CentralisedFuture;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Produces {@link SQLTransaction}s as well as allows for Async Futures to be produced using the contained executor.
 */
class TransactionSource {

	private final FactoryOfTheFuture futuresFactory;
	private final Executor executor;
	private final DataSource dataSource;

	TransactionSource(FactoryOfTheFuture futuresFactory, Executor executor, DataSource dataSource) {
		this.futuresFactory = futuresFactory;
		this.executor = executor;
		this.dataSource = dataSource;
	}

	SQLTransaction openTransaction() throws SQLException {
		return new SQLTransaction(dataSource.getConnection());
	}

	CentralisedFuture<?> runAsync(Runnable action) {
		return futuresFactory.runAsync(action, executor);
	}

	<T> CentralisedFuture<T> supplyAsync(Supplier<T> supplier) {
		return futuresFactory.supplyAsync(supplier, executor);
	}

}
