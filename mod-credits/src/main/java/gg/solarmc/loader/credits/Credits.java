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
import gg.solarmc.loader.data.DataObject;
import gg.solarmc.loader.schema.tables.records.CreditsRecord;
import org.jooq.DSLContext;

import java.math.BigDecimal;

import static gg.solarmc.loader.schema.tables.Credits.CREDITS;

public class Credits implements DataObject {

	private final int userId;
	private volatile BigDecimal currentBalance;

	Credits(int userId, BigDecimal currentBalance) {
		this.userId = userId;
		this.currentBalance = currentBalance;
	}

	/**
	 * The user's cached current balance. Should not be relied upon for correctness; see {@link DataObject}
	 *
	 * @return the cached current balance
	 */
	public BigDecimal currentBalance() {
		return currentBalance;
	}

	private CreditsRecord getBalance(Transaction transaction) {
		CreditsRecord creditsRecord = transaction.getProperty(DSLContext.class).fetchOne(CREDITS, CREDITS.USER_ID.eq(userId));

		assert creditsRecord != null : "User data disappeared";
		return creditsRecord;
	}

	/**
	 * Withdraws from the user's account. Only succeeds if the user has enough balance.
	 *
	 * @param transaction the transaction
	 * @param withdrawAmount the amount withdrawn
	 * @return a withdraw result indicating success or failure, as well as the new balance
	 * @throws IllegalArgumentException if {@code withdrawAmount} is negative or zero
	 */
	public WithdrawResult withdrawBalance(Transaction transaction, BigDecimal withdrawAmount) {
		if (withdrawAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("withdrawAmount must be positive");
		}
		CreditsRecord creditsRecord = getBalance(transaction);
		BigDecimal existingBalance = creditsRecord.getBalance();
		BigDecimal newBalance = existingBalance.subtract(withdrawAmount);
		if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
			return new WithdrawResult(existingBalance, false);
		}
		creditsRecord.setBalance(newBalance);
		creditsRecord.store(CREDITS.BALANCE);
		currentBalance = newBalance;
		return new WithdrawResult(newBalance, true);
	}

	/**
	 * Deposits into the user's account. Cannot fail
	 * @param transaction the transaction
	 * @param depositAmount the amount to deposit
	 * @return a deposit result indicating the new balance
	 * @throws IllegalArgumentException if {@code depositAmount} is negative or zero
	 */
	public DepositResult depositBalance(Transaction transaction, BigDecimal depositAmount) {
		if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("depositAmount must be positive");
		}
		CreditsRecord creditsRecord = getBalance(transaction);
		BigDecimal existingBalance = creditsRecord.getBalance();
		BigDecimal newBalance = existingBalance.add(depositAmount);
		creditsRecord.setBalance(newBalance);
		creditsRecord.store(CREDITS.BALANCE);
		currentBalance = newBalance;
		return new DepositResult(newBalance);
	}

	/**
	 * Sets the user's account balance. Cannot fail
	 * @param transaction the transaction
	 * @param newAmount amount to set to
	 */
	public void setBalance(Transaction transaction, BigDecimal newAmount) {
		if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("newAmount must be positive");
		}

		CreditsRecord creditsRecord = getBalance(transaction);
		creditsRecord.setBalance(newAmount);
		creditsRecord.store(CREDITS.BALANCE);
		currentBalance = newAmount;
	}

}
