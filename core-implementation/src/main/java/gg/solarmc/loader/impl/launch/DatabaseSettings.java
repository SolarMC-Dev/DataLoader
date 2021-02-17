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

package gg.solarmc.loader.impl.launch;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.solarmc.loader.impl.SolarDataConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseSettings {

	private final SolarDataConfig.DatabaseCredentials config;

	private static final AtomicInteger instanceNumber = new AtomicInteger();

	public DatabaseSettings(SolarDataConfig.DatabaseCredentials config) {
		this.config = config;
	}

	private HikariConfig setUnderlyingDataSource(String connectionProperties) {
		HikariConfig hikariConf = new HikariConfig();
		// Credentials
		hikariConf.setUsername(config.username());
		hikariConf.setPassword(config.password());

		// JDBC url and data source class
		String database = config.database();
		hikariConf.addDataSourceProperty("databaseName", database);
		String jdbcUrl = "jdbc:mariadb://" + config.host() + ":" + config.port() + "/" + database + connectionProperties;
		hikariConf.addDataSourceProperty("url", jdbcUrl);
		hikariConf.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");

		// JMX
		hikariConf.setRegisterMbeans(true);

		// Other settings
		hikariConf.setAutoCommit(false);
		hikariConf.setTransactionIsolation("TRANSACTION_REPEATABLE_READ");
		hikariConf.setPoolName("SolarData-" + instanceNumber.incrementAndGet());
		int poolSize = config.poolSize();
		hikariConf.setMinimumIdle(poolSize);
		hikariConf.setMaximumPoolSize(poolSize);

		// Timeouts
		Duration connectionTimeout = Duration.ofSeconds(config.connectionTimeoutSeconds());
		Duration maxLifetime = Duration.ofMinutes(config.maxLifetimeMinutes());
		hikariConf.setConnectionTimeout(connectionTimeout.toMillis());
		hikariConf.setMaxLifetime(maxLifetime.toMillis());
		return hikariConf;
	}

	private String getConnectionProperties() {
		Map<String, Object> properties = new HashMap<>();
		// Set default connection settings
		properties.put("autocommit", false);
		properties.put("defaultFetchSize", 1000);

		// Help debug in case of deadlock
		properties.put("includeInnodbStatusInDeadlockExceptions", true);
		properties.put("includeThreadDumpInDeadlockExceptions", true);

		// https://github.com/brettwooldridge/HikariCP/wiki/Rapid-Recovery#mysql
		properties.put("socketTimeout", Duration.ofSeconds(30L).toMillis());

		// User-defined additional properties
		properties.putAll(config.connectionProperties());

		return new ConnectionProperties('?', '&').formatProperties(properties);
	}

	public HikariDataSource createDataSource() {
		String connectionProperties = getConnectionProperties();
		HikariConfig hikariConf = setUnderlyingDataSource(connectionProperties);
		return new HikariDataSource(hikariConf);
	}
}
