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
import gg.solarmc.loader.Transaction;
import org.jooq.DSLContext;
import space.arim.omnibus.util.concurrent.CentralisedFuture;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Produces {@link Transaction}s as well as allows for Async Futures to be produced using the contained executor.
 * <br> <br>
 * The transactions provided will have properties available for at least {@link Connection} and {@link DSLContext}
 * and objects, accessible via {@link Transaction#getProperty(Class)}
 */
public class TransactionSource {

	private final FactoryOfTheFuture futuresFactory;
	private final Executor executor;
	private final DataSource dataSource;

	TransactionSource(FactoryOfTheFuture futuresFactory, Executor executor, DataSource dataSource) {
		this.futuresFactory = futuresFactory;
		this.executor = executor;
		this.dataSource = dataSource;
	}

	public CentralisedFuture<?> runTransact(DataCenter.TransactionRunner runner) {
		Objects.requireNonNull(runner, "runner");
		return runAsync(() -> {
			try (Connection connection = dataSource.getConnection()) {
				SQLTransaction transaction = new SQLTransaction(connection);

				try {
					runner.runTransactUsing(transaction);
				} catch (RuntimeException ex) {
					try {
						connection.rollback();
					} catch (SQLException suppressed) { ex.addSuppressed(suppressed); }
					throw ex;
				}
				connection.commit();

			} catch (SQLException ex) {
				throw new UncheckedSQLException(ex);
			}
		});
	}

	public <R> CentralisedFuture<R> transact(DataCenter.TransactionActor<R> actor) {
		Objects.requireNonNull(actor, "actor");
		return supplyAsync(() -> {
			try (Connection connection = dataSource.getConnection()) {
				SQLTransaction transaction = new SQLTransaction(connection);

				R value;
				try {
					value = actor.transactUsing(transaction);
				} catch (RuntimeException ex) {
					try {
						connection.rollback();
					} catch (SQLException suppressed) { ex.addSuppressed(suppressed); }
					throw ex;
				}
				connection.commit();
				return value;

			} catch (SQLException ex) {
				throw new UncheckedSQLException(ex);
			}
		});
	}

	private CentralisedFuture<?> runAsync(Runnable action) {
		return futuresFactory.runAsync(action, executor);
	}

	private <T> CentralisedFuture<T> supplyAsync(Supplier<T> supplier) {
		return futuresFactory.supplyAsync(supplier, executor);
	}

}
