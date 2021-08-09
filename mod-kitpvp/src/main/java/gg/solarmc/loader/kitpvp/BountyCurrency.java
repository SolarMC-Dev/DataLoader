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

/**
 * The medium of exchange for bounties. <br>
 * <br>
 * Strictly speaking, the names of the enum entries are purely informative
 * and do not impose requirements on the API user.
 *
 */
public enum BountyCurrency {

    /**
     * Solar credits
     *
     */
    CREDITS,
    /**
     * Plain economy, usually handled by Vault
     *
     */
    PLAIN_ECO,

    ;

    /**
     * Deserializes from a byte
     *
     * @param storedValue the serialized value
     * @return the deserialized currency
     * @throws IllegalArgumentException if the stored value is not recognized as a currency
     */
    public static BountyCurrency deserialize(byte storedValue) {
        return switch (storedValue) {
            case 0 -> CREDITS;
            case 1 -> PLAIN_ECO;
            default -> throw new IllegalArgumentException("Received unknown value " + storedValue);
        };
    }

    /**
     * Serializes to a byte
     *
     * @return the byte
     */
    public byte serialize() {
        return (byte) ordinal();
    }

    /**
     * Creates a bounty amount using this currency
     *
     * @param value the value of the amount
     * @return the bounty amount
     */
    public BountyAmount createAmount(BigDecimal value) {
        return new BountyAmount(this, value);
    }

    /**
     * Creates a zero-valued amount using this currency
     *
     * @return the bounty amount
     */
    public BountyAmount zero() {
        return createAmount(BigDecimal.ZERO);
    }
}
