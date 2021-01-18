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

package gg.solarmc.loader;

import gg.solarmc.loader.data.DataKey;
import gg.solarmc.loader.data.DataManager;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

public interface DataCenter {

	/**
	 * Runs a transaction which does not return a result. Helper method
	 * for {@link #transact(TransactionActor)}
	 *
	 * @param runner the transaction body
	 * @return a future completed once the transaction is complete
	 */
	CentralisedFuture<?> runTransact(TransactionRunner runner);

	/**
	 * Transactor which does not return a result
	 */
	@FunctionalInterface
	interface TransactionRunner {

		void runTransactUsing(Transaction transaction);
	}

	/**
	 * Runs a transaction, i.e. a group of batched actions executed together
	 * for consistency
	 *
	 * @param actor the transaction body
	 * @param <R> the result type
	 * @return a future completed once the transaction is complete, yielding the transaction result
	 */
	<R> CentralisedFuture<R> transact(TransactionActor<R> actor);

	/**
	 * Transactor returning a result
	 *
	 * @param <R> the result type
	 */
	@FunctionalInterface
	interface TransactionActor<R> {

		R transactUsing(Transaction transaction);
	}

	/**
	 * Gets a specific data manager
	 *
	 * @param key the data key
	 * @param <M> the manager type
	 * @return the data manager
	 */
	<M extends DataManager> M getDataManager(DataKey<?, M> key);

}
