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

package gg.solarmc.loader.kitpvp;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A quantity of a bounty and an associated currency
 *
 * @param currency the bounty currency
 * @param value the bounty value
 */
public record BountyAmount(BountyCurrency currency, BigDecimal value) {

    public BountyAmount {
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(value, "value");
    }

    /**
     * Determines whether this amount is zero
     *
     * @return true if zero, false otherwise
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Determines whether this amount is not zero
     *
     * @return false if zero, true otherwise
     */
    public boolean isNonZero() {
        return !isZero();
    }

    /**
     * Adds the given amount to this one
     *
     * @param augend the amount to add
     * @return the new bounty amount
     * @throws IllegalArgumentException if the currencies do not match
     */
    public BountyAmount add(BountyAmount augend) {
        if (currency != augend.currency) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return currency.createAmount(value.add(augend.value));
    }

    /**
     * Subtracts the given amount from this one
     *
     * @param subtrahend the amount to subtract
     * @return the new bounty amount
     * @throws IllegalArgumentException if the currencies do not match
     */
    public BountyAmount subtract(BountyAmount subtrahend) {
        if (currency != subtrahend.currency) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return currency.createAmount(value.subtract(subtrahend.value));
    }
}
