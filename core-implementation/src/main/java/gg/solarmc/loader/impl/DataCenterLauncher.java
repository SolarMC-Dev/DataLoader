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

import com.zaxxer.hikari.HikariDataSource;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.data.DataKey;
import org.flywaydb.core.Flyway;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.registry.RegistryPriorities;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataCenterLauncher {

	private final Path folder;
	private final FactoryOfTheFuture futuresFactory;
	private final Omnibus omnibus;

	public DataCenterLauncher(Path folder, FactoryOfTheFuture futuresFactory, Omnibus omnibus) {
		this.folder = folder;
		this.futuresFactory = futuresFactory;
		this.omnibus = omnibus;
	}

	private SolarDataConfig loadConfig() {
		try {
			return new ConfigurationHelper<>(folder, "credentials.yml",
					new SnakeYamlConfigurationFactory<>(SolarDataConfig.class, ConfigurationOptions.defaults(),
							new SnakeYamlOptions.Builder().useCommentingWriter(true).build())).reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			throw new RuntimeException("Fix the configuration and restart", ex);
		}
	}

	public OperationalSolarDataControl launch() {
		HikariDataSource dataSource = new DatabaseSettings(loadConfig()).createDataSource();

		Flyway flyway = Flyway.configure(getClass().getClassLoader())
				.dataSource(dataSource)
				.locations("classpath:sql-schema")
				.validateMigrationNaming(true).group(true)
				.load();
		flyway.migrate();

		ExecutorService executor = Executors.newFixedThreadPool(dataSource.getMaximumPoolSize());

		TransactionSource transactionSource = new TransactionSource(futuresFactory, executor, dataSource);

		Map<DataKey<?, ?>, DataGroup<?, ?>> groupsMap;
		try (SQLTransaction transaction = transactionSource.openTransaction()) {
			transaction.markReadOnly();

			groupsMap = new DataGroupLoader(
					new DataKeyInitializationContextImpl(omnibus, futuresFactory, folder, transaction)).loadGroups();
		} catch (SQLException ex) {
			throw new UncheckedSQLException(ex);
		}
		Set<DataGroup<?, ?>> groupsSet = Set.copyOf(groupsMap.values());

		DataCenter dataCenter = new CoreDataCenter(transactionSource, groupsMap);
		omnibus.getRegistry().register(DataCenter.class, RegistryPriorities.LOWEST, dataCenter, "Main DataCenter");

		return new OperationalSolarDataControl(
				new LoginHandler(transactionSource, groupsSet),
				new DataCenterLifecycle(executor, dataSource, groupsSet));
	}

}
