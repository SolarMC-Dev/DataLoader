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

public class Icarus implements AutoCloseable {

	private final LoginHandler loginHandler;
	private final TransactionSource transactionSource;
	private final DataManagementCenter dataManagement;
	private final DataCenterLifecycle lifecycle;

	Icarus(LoginHandler loginHandler, TransactionSource transactionSource,
		   DataManagementCenter dataManagement, DataCenterLifecycle lifecycle) {
		this.loginHandler = loginHandler;
		this.transactionSource = transactionSource;
		this.dataManagement = dataManagement;
		this.lifecycle = lifecycle;
	}

	public LoginHandler loginHandler() {
		return loginHandler;
	}

	public TransactionSource transactionSource() {
		return transactionSource;
	}

	public DataManagementCenter dataManagement() {
		return dataManagement;
	}

	@Override
	public void close() {
		try {
			lifecycle.close();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to shut down properly", ex);
		}
	}
}
