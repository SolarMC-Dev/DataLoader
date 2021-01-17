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

import gg.solarmc.loader.DataObject;
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;

import java.math.BigDecimal;

public class Credits implements DataObject {

	private volatile BigDecimal balance;

	Credits(BigDecimal balance) {
		this.balance = balance;
	}

	@Override
	public SolarPlayer getBoundPlayer() {
		return null;
	}

	BigDecimal currentBalance() {
		return balance;
	}

	/**
	 * Withdraw a value of currency
	 * @param transaction represents the Transaction
	 * @param withdrawAmount represents the amount being removed
	 * @return Result of the withdraw to the balance
	 *
	 * Result is failable
	 */

	WithdrawResult withdrawBalance(Transaction transaction, BigDecimal withdrawAmount) {
		return null;
	}

	/**
	 * Deposits a value of currency
	 * @param transaction
	 * @param depositAmount
	 * @return Result of the deposit to the balance
	 *
	 * Result cannot fail
	 */

	DepositResult depositBalance(Transaction transaction, BigDecimal depositAmount) {
		return null;
	}


}
