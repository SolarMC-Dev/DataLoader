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
import gg.solarmc.loader.impl.launch.DataGroup;
import gg.solarmc.loader.impl.launch.DataGroupLoader;
import gg.solarmc.loader.impl.launch.DataKeyInitializationContextImpl;
import gg.solarmc.loader.impl.launch.DataLoaderThreadFactory;
import gg.solarmc.loader.impl.launch.DatabaseSettings;
import org.flywaydb.core.Flyway;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.CommentMode;
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

public class IcarusLauncher {

	private final Path folder;
	private final FactoryOfTheFuture futuresFactory;
	private final Omnibus omnibus;
	private final ExecutorServiceFactory executorServiceFactory;

	/**
	 * Creates the launcher
	 *
	 * @param folder the launch directory used for configuration
	 * @param futuresFactory the futures factory
	 * @param omnibus the omnibus
	 * @param executorServiceFactory the thread pool factory
	 */
	public IcarusLauncher(Path folder, FactoryOfTheFuture futuresFactory,
						  Omnibus omnibus, ExecutorServiceFactory executorServiceFactory) {
		this.folder = folder;
		this.futuresFactory = futuresFactory;
		this.omnibus = omnibus;
		this.executorServiceFactory = executorServiceFactory;
	}

	/**
	 * Loads the configuration from a "dataloader.yml" in the same directory
	 * given to this launcher
	 *
	 * @return the configuration
	 */
	public SolarDataConfig loadConfig() {
		try {
			return new ConfigurationHelper<>(folder, "dataloader.yml",
					SnakeYamlConfigurationFactory.create(SolarDataConfig.class, ConfigurationOptions.defaults(),
							new SnakeYamlOptions.Builder().commentMode(CommentMode.fullComments()).build())
			).reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			throw new RuntimeException("Fix the configuration and restart", ex);
		}
	}

	/**
	 * Launches using the given credentials
	 *
	 * @param credentials the database credentials
	 * @return the icarus
	 */
	public Icarus launch(SolarDataConfig.DatabaseCredentials credentials) {
		HikariDataSource dataSource = new DatabaseSettings(credentials).createDataSource();

		Flyway flyway = Flyway.configure(getClass().getClassLoader())
				.dataSource(dataSource)
				.locations("classpath:sql-schema")
				.validateMigrationNaming(true).group(true)
				.baselineOnMigrate(true).baselineVersion("0.0")
				.load();
		flyway.migrate();

		ExecutorService executor = executorServiceFactory.newFixedThreadPool(
				dataSource.getMaximumPoolSize(), new DataLoaderThreadFactory());

		TransactionSource transactionSource = new TransactionSource(futuresFactory, executor, dataSource);

		Map<DataKey<?, ?, ?>, DataGroup<?, ?, ?>> groupsMap = transactionSource.transact((transaction) -> {
			return new DataGroupLoader(
					new DataKeyInitializationContextImpl(omnibus, futuresFactory, folder, transaction)
			).loadGroups();
		}).join();
		Set<DataGroup<?, ?, ?>> groupsSet = Set.copyOf(groupsMap.values());

		return new Icarus(
				transactionSource,
				new DataManagementCenter(groupsMap),
				groupsSet,
				new DataCenterLifecycle(executor, dataSource, groupsSet));
	}

}
