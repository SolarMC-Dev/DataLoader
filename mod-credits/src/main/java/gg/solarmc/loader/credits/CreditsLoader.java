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

import gg.solarmc.loader.data.DataLoader;
import gg.solarmc.loader.Transaction;
import org.jooq.DSLContext;

import java.math.BigDecimal;

import static gg.solarmc.loader.schema.tables.Credits.CREDITS;

class CreditsLoader implements DataLoader<Credits> {

	private final BigDecimal defaultBigDecimalBalance;

	CreditsLoader(BigDecimal defaultValue) {
		defaultBigDecimalBalance = defaultValue;
	}

	@Override
	public Credits loadData(Transaction transaction, int userId) {
		DSLContext context = transaction.getProperty(DSLContext.class);
		BigDecimal balance = context
				.select(CREDITS.BALANCE)
				.from(CREDITS)
				.where(CREDITS.USER_ID.eq(userId))
				.fetchOne((rowRecord) -> rowRecord.get(CREDITS.BALANCE));
		if (balance == null) {
			context.insertInto(CREDITS)
					.columns(CREDITS.USER_ID, CREDITS.BALANCE)
					.values(userId, defaultBigDecimalBalance)
					.execute();
			balance = defaultBigDecimalBalance;
		}
		return new Credits(userId, balance);
	}

	@Override
	public void wipeAllData(Transaction transaction) {
		DSLContext context = transaction.getProperty(DSLContext.class);
		context.deleteFrom(CREDITS).execute();
	}

}
