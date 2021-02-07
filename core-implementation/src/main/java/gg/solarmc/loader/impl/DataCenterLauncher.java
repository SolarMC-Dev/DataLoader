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
import gg.solarmc.loader.data.DataKey;
import org.flywaydb.core.Flyway;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.util.concurrent.FactoryOfTheFuture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class DataCenterLauncher {

	private final Path folder;
	private final FactoryOfTheFuture futuresFactory;
	private final Omnibus omnibus;
	private final ExecutorServiceFactory executorServiceFactory;

	public DataCenterLauncher(Path folder, FactoryOfTheFuture futuresFactory,
							  Omnibus omnibus, ExecutorServiceFactory executorServiceFactory) {
		this.folder = folder;
		this.futuresFactory = futuresFactory;
		this.omnibus = omnibus;
		this.executorServiceFactory = executorServiceFactory;
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

	public Icarus launch() {
		HikariDataSource dataSource = new DatabaseSettings(loadConfig()).createDataSource();

		Flyway flyway = Flyway.configure(getClass().getClassLoader())
				.dataSource(dataSource)
				.locations("classpath:sql-schema")
				.validateMigrationNaming(true).group(true)
				.load();
		flyway.migrate();

		ExecutorService executor = executorServiceFactory.newFixedThreadPool(dataSource.getMaximumPoolSize());

		TransactionSource transactionSource = new TransactionSource(futuresFactory, executor, dataSource);

		Map<DataKey<?, ?>, DataGroup<?, ?>> groupsMap = transactionSource.transact((transaction) -> {
			return new DataGroupLoader(
					new DataKeyInitializationContextImpl(omnibus, futuresFactory, folder, transaction)
			).loadGroups();
		}).join();
		Set<DataGroup<?, ?>> groupsSet = Set.copyOf(groupsMap.values());

		return new Icarus(
				new LoginHandler(transactionSource, groupsSet),
				transactionSource,
				new DataManagementCenter(groupsMap),
				new DataCenterLifecycle(executor, dataSource, groupsSet));
	}

}
