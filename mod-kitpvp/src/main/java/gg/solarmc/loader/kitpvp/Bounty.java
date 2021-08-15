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
import java.util.Map;

/**
 * The bounties on a certain user. An instance of this interface will
 * include information for some, but not all, {@link BountyCurrency}s.
 *
 */
public interface Bounty {

    /**
     * The target user's username
     *
     * @return the target username
     */
    String target();

    /**
     * The bounty value for a certain currency
     *
     * @param currency the currency
     * @return the bounty on the target user in the currency
     * @throws IllegalStateException if this bounty has no information for the given currency
     */
    BountyAmount amount(BountyCurrency currency);

    /**
     * Gets all the bounty amounties
     *
     * @return an immutable map of all the bounty amounts
     */
    Map<BountyCurrency, BigDecimal> allAmounts();

}
