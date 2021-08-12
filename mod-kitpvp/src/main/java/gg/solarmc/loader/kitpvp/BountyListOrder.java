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

import java.util.List;

/**
 * Builder for bounty list orders. Uses a series of steps for compile time safety. <br>
 * <br>
 * A bounty list is ordered by a certain currency, called the primary currency.
 * Additional currencies may be included, which will be available in the {@link Bounty}s
 * on the {@link BountyPage}.
 * <br>
 * Note that preconditions may be checked during {@link AlmostReadyStep#build()}
 *
 */
public final class BountyListOrder {

    private final int countPerPage;
    private List<BountyCurrency> includeCurrencies;

    private BountyListOrder(int countPerPage) {
        this.countPerPage = countPerPage;
    }

    /**
     * Begins creating a bounty list order by setting the count per page
     *
     * @param countPerPage the count per page
     * @return the first step
     * @throws IllegalArgumentException if the count per page is not positive
     */
    public static Step1 countPerPage(int countPerPage) {
        return new BountyListOrder(countPerPage).new Step1();
    }

    public final class Step1 {

        private Step1() {}

        /**
         * Sets the currencies to include. Bounties in all of these currencies will be available
         * in {@link Bounty}s listed. <br>
         * <br>
         * The bounty list will be ordered by the bounty value in descending order, primarily by
         * the value in the first currency, then by that in the second currency, and so on.
         *
         * @param includeCurrencies all the currencies to include
         * @return the next step
         * @throws IllegalArgumentException if {@code includeCurrencies} is empty
         */
        public AlmostReadyStep includeCurrencies(List<BountyCurrency> includeCurrencies) {
            BountyListOrder.this.includeCurrencies = List.copyOf(includeCurrencies);
            return new AlmostReadyStep();
        }

        /**
         * Sets the currencies to include. Bounties in all of these currencies will be available
         * in {@link Bounty}s listed. <br>
         * <br>
         * The bounty list will be ordered by the bounty value in descending order, primarily by
         * the value in the first currency, then by that in the second currency, and so on.
         *
         * @param includeCurrencies all the currencies to include
         * @return the next step
         * @throws IllegalArgumentException if {@code includeCurrencies} is empty
         */
        public AlmostReadyStep includeCurrencies(BountyCurrency...includeCurrencies) {
            return includeCurrencies(List.of(includeCurrencies));
        }

    }

    public final class AlmostReadyStep {

        private AlmostReadyStep() {}

        /**
         * Builds into a ready bounty list order. May be used repeatedly without side effects
         *
         * @return a built bounty list order
         */
        public Built build() {
            return new Built(countPerPage, includeCurrencies);
        }
    }

    public record Built(int countPerPage, List<BountyCurrency> includeCurrencies) {

        public Built {
            if (countPerPage <= 0) {
                throw new IllegalArgumentException("Count per page must be positive");
            }
            includeCurrencies = List.copyOf(includeCurrencies);
            if (includeCurrencies.isEmpty()) {
                throw new IllegalArgumentException("includeCurrencies must contain at least one currency");
            }
        }

    }
}
