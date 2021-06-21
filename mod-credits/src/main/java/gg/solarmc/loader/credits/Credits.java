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
import gg.solarmc.loader.schema.routines.CreditsWithdrawBalance;
import org.jooq.DSLContext;
import org.jooq.Record1;

import java.math.BigDecimal;

import static gg.solarmc.loader.schema.Routines.creditsDepositBalance;
import static gg.solarmc.loader.schema.tables.Credits.CREDITS;

public abstract class Credits implements DataObject {

	private final int userId;

	Credits(int userId) {
		this.userId = userId;
	}

	abstract void updateBalance(BigDecimal newBalance);

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
		CreditsWithdrawBalance withdrawProcedure = new CreditsWithdrawBalance();
		withdrawProcedure.setUserIdentifier(userId);
		withdrawProcedure.setWithdrawAmount(withdrawAmount);
		withdrawProcedure.execute(transaction.getProperty(DSLContext.class).configuration());
		// New balance is always balance after operation
		BigDecimal newBalance = withdrawProcedure.getNewBalance();
		assert newBalance != null : "Remote routine returned null balance";
		updateBalance(newBalance);
		return new WithdrawResult(newBalance, withdrawProcedure.getSuccessful() == 1);
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
		Record1<BigDecimal> newBalanceRecord = transaction.getProperty(DSLContext.class)
				.select(creditsDepositBalance(userId, depositAmount))
				.fetchOne();
		assert newBalanceRecord != null : "Remote routine returned null balance record";
		BigDecimal newBalance = newBalanceRecord.value1();
		updateBalance(newBalance);
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
		transaction.getProperty(DSLContext.class)
				.update(CREDITS)
				.set(CREDITS.BALANCE, newAmount)
				.where(CREDITS.USER_ID.eq(userId))
				.execute();
		updateBalance(newAmount);
	}

}
