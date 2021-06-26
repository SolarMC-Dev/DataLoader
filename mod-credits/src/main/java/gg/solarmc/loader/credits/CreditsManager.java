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
import org.jooq.DSLContext;

import java.util.List;

import static gg.solarmc.loader.schema.tables.CreditsWithNames.CREDITS_WITH_NAMES;

public class CreditsManager implements DataManager {

	private final CreditsConfig configuration;

	CreditsManager(CreditsConfig config) {
		this.configuration = config;
	}

	public List<TopBalanceEntry> getTopBalances(Transaction transaction, int limit) {
		return transaction.getProperty(DSLContext.class).select().from(CREDITS_WITH_NAMES)
				.orderBy(CREDITS_WITH_NAMES.BALANCE.desc()).limit(limit)
				.fetch((rowRecord) -> new TopBalanceEntry(
						rowRecord.get(CREDITS_WITH_NAMES.USER_ID),
						rowRecord.get(CREDITS_WITH_NAMES.USERNAME),
						rowRecord.get(CREDITS_WITH_NAMES.BALANCE)));
	}

	public CreditsConfig getConfiguration() {
		return configuration;
	}

}
