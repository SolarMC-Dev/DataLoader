/*
 *
 *  * dataloader
 *  * Copyright © $DateInfo.year SolarMC Developers
 *  *
 *  * dataloader is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as
 *  * published by the Free Software Foundation, either version 3 of the
 *  * License, or (at your option) any later version.
 *  *
 *  * dataloader is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with dataloader. If not, see <https://www.gnu.org/licenses/>
 *  * and navigate to version 3 of the GNU Affero General Public License.
 *
 */

package gg.solarmc.loader.credits;

import java.math.BigDecimal;

public class EconomyResult {

	private final BigDecimal newBalance;

	EconomyResult(BigDecimal newBalance) {
		this.newBalance = newBalance;
	}

	/**
	 * Gets the balance after the operation. If the operation somehow failed,
	 * this will stay as the existing balance.
	 *
	 * @return the balance after the operation
	 */
	public BigDecimal newBalance() {
		return newBalance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EconomyResult that = (EconomyResult) o;
		return newBalance.equals(that.newBalance);
	}

	@Override
	public int hashCode() {
		return newBalance.hashCode();
	}
}
