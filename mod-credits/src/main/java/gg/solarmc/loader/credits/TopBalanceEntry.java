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

import java.math.BigDecimal;
import java.util.Objects;

public final class TopBalanceEntry {

	private final int userId;
	private final String username;
	private final BigDecimal balance;

	public TopBalanceEntry(int userId, String username, BigDecimal balance) {
		this.userId = userId;
		this.username = Objects.requireNonNull(username);
		this.balance = Objects.requireNonNull(balance);
	}

	public int userId() {
		return userId;
	}

	public String username() {
		return username;
	}

	public BigDecimal balance() {
		return balance;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TopBalanceEntry that = (TopBalanceEntry) o;
		return userId == that.userId && username.equals(that.username) && balance.equals(that.balance);
	}

	@Override
	public int hashCode() {
		int result = userId;
		result = 31 * result + username.hashCode();
		result = 31 * result + balance.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "TopBalanceEntry{" +
				"userId=" + userId +
				", username='" + username + '\'' +
				", balance=" + balance +
				'}';
	}
}
