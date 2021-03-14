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

import gg.solarmc.loader.impl.launch.DataGroup;
import gg.solarmc.loader.impl.login.LoginHandlerBuilderImpl;

import java.util.Set;

public class Icarus implements AutoCloseable {

	private final TransactionSource transactionSource;
	private final DataManagementCenter dataManagement;
	private final Set<DataGroup<?, ?, ?>> groups;
	private final DataCenterLifecycle lifecycle;

	Icarus(TransactionSource transactionSource, DataManagementCenter dataManagement,
		   Set<DataGroup<?, ?, ?>> groups, DataCenterLifecycle lifecycle) {
		this.transactionSource = transactionSource;
		this.dataManagement = dataManagement;
		this.groups = groups;
		this.lifecycle = lifecycle;
	}

	public TransactionSource transactionSource() {
		return transactionSource;
	}

	public DataManagementCenter dataManagement() {
		return dataManagement;
	}

	/**
	 * Obtains a login handler builder using the configuration
	 *
	 * @param loginConfig the configuration
	 * @return a preconfigured login handler builder
	 */
	public LoginHandler.Builder loginHandlerBuilder(SolarDataConfig.Logins loginConfig) {
		LoginHandler.Builder loginHandlerBuilder = new LoginHandlerBuilderImpl(transactionSource, groups);
		loginConfig.configureLoginHandlerBuilder(loginHandlerBuilder);
		return loginHandlerBuilder;
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
