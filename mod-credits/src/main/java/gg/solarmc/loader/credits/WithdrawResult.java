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

public class WithdrawResult extends EconomyResult {

	private final boolean success;

	WithdrawResult(BigDecimal newBalance, boolean success) {
		super(newBalance);
		this.success = success;
	}

	public boolean isSuccessful() {
		return success;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		WithdrawResult that = (WithdrawResult) o;
		return success == that.success;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (success ? 1 : 0);
		return result;
	}
}
