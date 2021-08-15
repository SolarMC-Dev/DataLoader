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
import gg.solarmc.loader.schema.tables.KitpvpBounties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static gg.solarmc.loader.schema.tables.KitpvpBounties.KITPVP_BOUNTIES;
import static gg.solarmc.loader.schema.tables.LatestNames.LATEST_NAMES;

/**
 * Implementation of bounty listing
 *
 */
record BountyList(KitPvpManager manager, BountyListOrder.Built listOrder) {

    Optional<BountyPage> beginToListBounties(Transaction tx) {
        return listBounties(tx, null);
    }

    private KitpvpBounties aliasedBounties(BountyCurrency currency) {
        return KITPVP_BOUNTIES.as("bounties_" + currency.name().toLowerCase(Locale.ROOT));
    }

    /*
    Paginate according to currencies first, then use user ID to break ties
    Use the keyset pagination method
     */

    private record PaginationCursor(Map<BountyCurrency, BigDecimal> afterAmounts, int userId) {
    }

    private Optional<BountyPage> listBounties(Transaction tx,
                                              @Nullable PaginationCursor cursor) {
        DSLContext context = tx.getProperty(DSLContext.class);
        SelectJoinStep<Record> step1;
        {
            // SELECT user_id, username, and all bounty values
            List<SelectFieldOrAsterisk> selections = new ArrayList<>(2 + listOrder.includeCurrencies().size());
            selections.add(LATEST_NAMES.USER_ID);
            selections.add(LATEST_NAMES.USERNAME);
            for (BountyCurrency currency : listOrder.includeCurrencies()) {
                selections.add(aliasedBounties(currency).BOUNTY_AMOUNT);
            }
            step1 = context.select(selections).from(LATEST_NAMES);
        }
        // LEFT JOIN bounties table for each currency
        for (BountyCurrency currency : listOrder.includeCurrencies()) {
            KitpvpBounties bounties = aliasedBounties(currency);
            step1 = step1.leftJoin(bounties)
                    .on(bounties.USER_ID.eq(LATEST_NAMES.USER_ID))
                    .and(bounties.BOUNTY_CURRENCY.eq(currency.serialize()));
        }
        // WHERE any bounty value is non-null
        SelectConditionStep<? extends Record> step2 = step1
                .where(anyBountyValueIsNonNull());
        ResultQuery<? extends Record> step3;
        {
            // ORDER BY each currency DESC NULLS LAST
            List<OrderField<?>> orderBy = new ArrayList<>(1 + listOrder.includeCurrencies().size());
            for (BountyCurrency currency : listOrder.includeCurrencies()) {
                orderBy.add(aliasedBounties(currency).BOUNTY_AMOUNT.desc().nullsLast());
            }
            // Break ties with user ID
            orderBy.add(LATEST_NAMES.USER_ID);
            if (cursor == null) {
                step3 = step2
                        .orderBy(orderBy).limit(listOrder.countPerPage());
            } else {
                step3 = step2
                        .and(cursorSeekCondition(cursor))
                        .orderBy(orderBy).limit(listOrder.countPerPage());
            }
        }
        List<BountyInternal> bounties = step3.fetch((record) -> {
            int userId = record.get(LATEST_NAMES.USER_ID);
            String target = record.get(LATEST_NAMES.USERNAME);
            return switch (listOrder.includeCurrencies().size()) {
                case 1 -> {
                    BountyCurrency currency = listOrder.includeCurrencies().get(0);
                    BigDecimal bountyValue = record.get(aliasedBounties(currency).BOUNTY_AMOUNT);
                    yield new BountySingleCurrency(userId, target, currency.createAmount(bountyValue));
                }
                case 2 -> BountyDoubleCurrency.from(userId, target, record, (rec, currency) -> {
                    BigDecimal bountyAmount = rec.get(aliasedBounties(currency).BOUNTY_AMOUNT);
                    return Objects.requireNonNullElse(bountyAmount, BigDecimal.ZERO);
                });
                default -> throw new UnsupportedOperationException("Not implemented for more than 2 currencies");
            };
        });
        if (bounties.isEmpty()) {
            return Optional.empty();
        }
        record BountyPageImpl(BountyList bountyList, List<BountyInternal> itemsOnPage)
                implements BountyPage {

            @Override
            public Optional<BountyPage> nextPage(Transaction tx) {
                BountyInternal lastBounty = itemsOnPage.get(itemsOnPage.size() - 1);
                return bountyList.listBounties(tx,
                        new PaginationCursor(lastBounty.allAmounts(), lastBounty.userId()));
            }
        }
        return Optional.of(new BountyPageImpl(this, bounties));
    }

    private Condition anyBountyValueIsNonNull() {
        // (currency1 IS NOT NULL OR currency2 IS NOT NULL OR ...)
        Condition anyBountyValueNotNull = null;
        for (BountyCurrency currency : listOrder.includeCurrencies()) {
            Condition thisCurrencyValueNotNull = aliasedBounties(currency).BOUNTY_AMOUNT.isNotNull();
            if (anyBountyValueNotNull == null) {
                anyBountyValueNotNull = thisCurrencyValueNotNull;
            } else {
                anyBountyValueNotNull = anyBountyValueNotNull.or(thisCurrencyValueNotNull);
            }
        }
        return anyBountyValueNotNull;
    }

    private Condition cursorSeekCondition(PaginationCursor cursor) {
        /*
		Dynamically build a WHERE clause, e.g.
		(currency1 < afterAmount1) OR
		(currency1 = afterAmount1 AND currency2 < afterAmount2) OR
		(currency1 = afterAmount1 AND currency2 = afterAmount2 AND currency3 < afterAmount3)

		OR all currencies are equal and user ID is greater than cursor user ID
        */
        Condition seekCondition = null;
        Condition currenciesSoFarAreEqual = null;
        for (BountyCurrency currency : listOrder.includeCurrencies()) {
            KitpvpBounties bounties = aliasedBounties(currency);
            BigDecimal bountyValue = cursor.afterAmounts().get(currency);

            // currencyX < afterAmountX
            Condition valueLessThanThisCurrencyValue = nullSafeLessThan(bounties.BOUNTY_AMOUNT, bountyValue);
            // currencyX = afterAmountX
            Condition valueEqualToThisCurrencyValue = nullSafeEqual(bounties.BOUNTY_AMOUNT, bountyValue);

            if (seekCondition == null) {
                // Primary currency
                seekCondition = valueLessThanThisCurrencyValue;
                currenciesSoFarAreEqual = valueEqualToThisCurrencyValue;
            } else {
                // (...) OR (currency1 = afterAmount1 AND currency2 < afterAmount2)
                seekCondition = seekCondition.or(
                        currenciesSoFarAreEqual.and(valueLessThanThisCurrencyValue));
                currenciesSoFarAreEqual = currenciesSoFarAreEqual.and(valueEqualToThisCurrencyValue);
            }
        }
        assert seekCondition != null : "at least one currency";
        return seekCondition.or(
                currenciesSoFarAreEqual.and(LATEST_NAMES.USER_ID.greaterThan(cursor.userId())));
    }

    // Helpers for treating NULL values as zero
    // This is the same as NULLS LAST except NULLS LAST is only usable in ORDER BY

    private Condition nullSafeLessThan(Field<BigDecimal> column, BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            // Nothing can be less than zero
            return DSL.falseCondition();
        }
        // If the column value is non-null, it has to pass the lessThan
        // If the column value is null, it is included
        return column.lessThan(value).or(column.isNull());
    }

    private Condition nullSafeEqual(Field<BigDecimal> field, BigDecimal value) {
        Condition equality = field.eq(value);
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return equality.or(field.isNull());
        }
        return equality;
    }
}
