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
 * The total bounty on a certain user
 *
 */
public class Bounty {

    private final String target;
    private final BigDecimal amount;

    Bounty(String target, BigDecimal amount) {
        this.target = Objects.requireNonNull(target, "target");
        this.amount = Objects.requireNonNull(amount, "amount");
    }

    /**
     * The target user's username
     *
     * @return the target username
     */
    public String target() {
        return target;
    }

    /**
     * The total bounty value
     *
     * @return the total bounty on the target user
     */
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bounty bounty = (Bounty) o;
        return target.equals(bounty.target) && amount.equals(bounty.amount());
    }

    @Override
    public int hashCode() {
        int result = target.hashCode();
        result = 31 * result + amount.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Bounty{" +
                "target='" + target + '\'' +
                ", amount=" + amount +
                '}';
    }
}
