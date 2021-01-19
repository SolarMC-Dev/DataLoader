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

package gg.solarmc.loader.credits;

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataManager;
import gg.solarmc.loader.impl.SQLTransaction;
import gg.solarmc.loader.impl.SolarDataConfig;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import static gg.solarmc.loader.schema.tables.CreditsWithNames.CREDITS_WITH_NAMES;

public class CreditsManager implements DataManager {

	private CreditsConfig configuration;

	public CreditsManager(Path path) {
		loadConfig(path);
	}

	private CreditsConfig loadConfig(Path path) {
		try {
			return new ConfigurationHelper<>(path, "credits.yml",
					new SnakeYamlConfigurationFactory<>(CreditsConfig.class, ConfigurationOptions.defaults(),
							new SnakeYamlOptions.Builder().useCommentingWriter(true).build())).reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InvalidConfigException ex) {
			throw new RuntimeException("Fix the configuration and restart", ex);
		}
	}

	public List<TopBalanceEntry> getTopBalances(Transaction transaction, int limit) {
		var jooq = ((SQLTransaction) transaction).jooq();

		return jooq.select().from(CREDITS_WITH_NAMES)
				.orderBy(CREDITS_WITH_NAMES.BALANCE.desc()).limit(limit)
				.fetch((rowRecord) -> {
					return new TopBalanceEntry(
							rowRecord.get(CREDITS_WITH_NAMES.USER_ID),
							rowRecord.get(CREDITS_WITH_NAMES.USERNAME),
							rowRecord.get(CREDITS_WITH_NAMES.BALANCE));
				});
	}

	public CreditsConfig getConfiguration() { return configuration; }

}
