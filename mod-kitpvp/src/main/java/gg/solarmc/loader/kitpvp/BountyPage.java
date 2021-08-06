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

import gg.solarmc.loader.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * A page of bounties
 *
 */
public interface BountyPage {

    /**
     * Gets the bounties on this page. Will never be empty.
     *
     * @return the items on this page
     */
    List<Bounty> itemsOnPage();

    /**
     * Navigates to the next page and yields a bounty page for it.
     * The next page will not contain any of the bounties in this one.
     *
     * @param tx the transaction
     * @return the next page, or an empty optional if no more pages exist
     */
    Optional<BountyPage> nextPage(Transaction tx);

}
