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
import java.util.Objects;

record BountyDoubleCurrency(int userId,
                            String target, BigDecimal creditsAmount,
                            BigDecimal plainEcoAmount) implements BountyInternal {

    BountyDoubleCurrency {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(creditsAmount, "creditsAmount");
        Objects.requireNonNull(plainEcoAmount, "plainEcoAmount");
    }

    static <S> BountyInternal from(int userId, String target, S bountyDataSource, AmountFetcher<S> amountFetcher) {
        return new BountyDoubleCurrency(userId, target,
                amountFetcher.getAmount(bountyDataSource, BountyCurrency.CREDITS),
                amountFetcher.getAmount(bountyDataSource, BountyCurrency.PLAIN_ECO));
    }

    interface AmountFetcher<S> {

        BigDecimal getAmount(S bounyDataSource, BountyCurrency currency);
    }

    @Override
    public BountyAmount amount(BountyCurrency currency) {
        return currency.createAmount(switch (currency) {
            case CREDITS -> creditsAmount;
            case PLAIN_ECO -> plainEcoAmount;
        });
    }

    @Override
    public Map<BountyCurrency, BigDecimal> allAmounts() {
        return Map.of(
                BountyCurrency.CREDITS, creditsAmount,
                BountyCurrency.PLAIN_ECO, plainEcoAmount);
    }

}
