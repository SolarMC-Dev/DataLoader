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

import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.Transaction;

import java.math.BigDecimal;

public class Credits {

	private volatile BigDecimal balance;

	private SolarPlayer linkedPlayer; //not implemented

	Credits(BigDecimal balance) {
		this.balance = balance;
	}

	BigDecimal currentBalance() {
		return balance;
	}

	WithdrawResult withdrawBalance(Transaction transaction, BigDecimal withdrawAmount) {
		return null;
	}

	/**
	 * Deposits a value of currency
	 * @param transaction
	 * @param depositAmount
	 * @return
	 */

	DepositResult depositBalance(Transaction transaction, BigDecimal depositAmount) {
		return null;
	}
}
