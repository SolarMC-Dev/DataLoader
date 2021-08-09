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

package gg.solarmc.loader.kitpvp.test;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import gg.solarmc.loader.kitpvp.BountyAmount;
import gg.solarmc.loader.kitpvp.BountyCurrency;
import gg.solarmc.loader.kitpvp.ItemSerializer;
import gg.solarmc.loader.kitpvp.KitPvpKey;
import gg.solarmc.loader.kitpvp.OnlineKitPvp;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.Omnibus;

import java.math.BigDecimal;
import java.nio.file.Path;

import static gg.solarmc.loader.impl.test.extension.DataGenerator.randomPositiveBigDecimal;
import static gg.solarmc.loader.kitpvp.BountyCurrency.CREDITS;
import static gg.solarmc.loader.kitpvp.BountyCurrency.PLAIN_ECO;
import static gg.solarmc.loader.schema.tables.KitpvpBounties.KITPVP_BOUNTIES;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class KitPvpBountyIT {

    private DataCenterInfo dataCenterInfo;
    private int userId;
    private OnlineKitPvp data;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.TEN.multiply(BigDecimal.TEN);

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials,
                              @Mock ItemSerializer itemSerializer) {
        Omnibus omnibus = new DefaultOmnibus();
        omnibus.getRegistry().register(ItemSerializer.class, (byte) 0, itemSerializer, "Serializer");
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).omnibus(omnibus).build();
        OnlineSolarPlayer user = dataCenterInfo.loginNewRandomUser();
        userId = user.getUserId();
        data = user.getData(KitPvpKey.INSTANCE);
    }

    private void assertEqualDecimals(BigDecimal expected, BigDecimal actual) {
        assertTrue(expected.subtract(actual).compareTo(BigDecimal.ONE) < 0,
                "Expected " + expected + " but got " + actual);
    }

    private void assertEqualAmounts(BountyAmount expected, BountyAmount actual) {
        assertEqualDecimals(expected.value(), actual.value());
        assertEquals(expected.currency(), actual.currency(), "Currencies should match");
    }

    private void assertBounty(BountyAmount bounty) {
        BountyCurrency currency = bounty.currency();
        BigDecimal actualBounty = dataCenterInfo.transact((tx) -> {
            return tx.getProperty(DSLContext.class)
                    .select(KITPVP_BOUNTIES.BOUNTY_AMOUNT)
                    .from(KITPVP_BOUNTIES)
                    .where(KITPVP_BOUNTIES.USER_ID.eq(userId))
                    .and(KITPVP_BOUNTIES.BOUNTY_CURRENCY.eq(currency.serialize()))
                    .fetchOptional(KITPVP_BOUNTIES.BOUNTY_AMOUNT).orElse(BigDecimal.ZERO);
        });
        BigDecimal rawValue = bounty.value();
        assertEqualDecimals(rawValue, actualBounty);
        assertEqualDecimals(rawValue, data.currentBounty(currency));
    }

    @ParameterizedTest
    @EnumSource(BountyCurrency.class)
    public void addBounty(BountyCurrency currency) {
        assertBounty(currency.zero());

        BountyAmount firstAmount = currency.createAmount(randomPositiveBigDecimal());
        BountyAmount currentBounty = dataCenterInfo.transact((tx) -> {
            return data.addBounty(tx, firstAmount);
        });
        assertEqualAmounts(firstAmount, currentBounty);
        assertBounty(currentBounty);

        BountyAmount secondAmount = currency.createAmount(randomPositiveBigDecimal());
        currentBounty = dataCenterInfo.transact((tx) -> {
            return data.addBounty(tx, secondAmount);
        });
        assertEqualAmounts(
                currency.createAmount(firstAmount.value().add(secondAmount.value())), currentBounty);
        assertBounty(currentBounty);
    }

    @Test
    public void addBountyDifferentCurrencies() {
        assertBounty(CREDITS.zero());
        assertBounty(PLAIN_ECO.zero());

        BountyAmount creditsBounty = dataCenterInfo.transact((tx) -> {
            return data.addBounty(tx, CREDITS.createAmount(BigDecimal.TEN));
        });
        assertEquals(CREDITS, creditsBounty.currency());
        assertBounty(creditsBounty);
        assertBounty(PLAIN_ECO.zero());
    }

    @ParameterizedTest
    @EnumSource(BountyCurrency.class)
    public void resetBounty(BountyCurrency currency) {
        assertBounty(currency.zero());

        BountyAmount amount = currency.createAmount(randomPositiveBigDecimal());
        BountyAmount currentBounty = dataCenterInfo.transact((tx) -> {
            return data.addBounty(tx, amount);
        });
        assertEqualAmounts(amount, currentBounty);
        assertBounty(currentBounty);

        BountyAmount previousBounty = dataCenterInfo.transact((tx) -> {
            return data.resetBounty(tx, currency);
        });
        assertEqualAmounts(amount, previousBounty);
        assertBounty(currency.zero());
    }

    @Test
    public void resetBountyNone() {
        BountyAmount previousBounty = dataCenterInfo.transact((tx) -> {
            return data.resetBounty(tx, CREDITS);
        });
        assertEqualAmounts(CREDITS.zero(), previousBounty);
    }

    @Test
    public void resetBountyDifferentCurrencies() {
        assertBounty(CREDITS.zero());
        assertBounty(PLAIN_ECO.zero());

        dataCenterInfo.runTransact((tx) -> {
            data.addBounty(tx, CREDITS.createAmount(BigDecimal.TEN));
            data.addBounty(tx, PLAIN_ECO.createAmount(ONE_HUNDRED));
        });
        BountyAmount creditsBounty = dataCenterInfo.transact((tx) -> {
            return data.resetBounty(tx, CREDITS);
        });
        assertEquals(CREDITS, creditsBounty.currency());
        assertEqualAmounts(creditsBounty, CREDITS.createAmount(BigDecimal.TEN));
        assertBounty(CREDITS.zero());
        assertBounty(PLAIN_ECO.createAmount(ONE_HUNDRED));
    }

    @ParameterizedTest
    @EnumSource(BountyCurrency.class)
    public void getBounty(BountyCurrency currency) {
        assertBounty(currency.zero());

        BountyAmount bounty = currency.createAmount(randomPositiveBigDecimal());
        dataCenterInfo.runTransact((tx) -> {
            tx.getProperty(DSLContext.class)
                    .insertInto(KITPVP_BOUNTIES)
                    .columns(KITPVP_BOUNTIES.USER_ID, KITPVP_BOUNTIES.BOUNTY_AMOUNT, KITPVP_BOUNTIES.BOUNTY_CURRENCY)
                    .values(userId, bounty.value(), currency.serialize())
                    .execute();
        });
        assertEqualAmounts(bounty, dataCenterInfo.transact((tx) -> data.getBounty(tx, currency)));
        assertBounty(bounty);
    }

    @Test
    public void getBountyDifferentCurrencies() {
        assertBounty(CREDITS.zero());
        assertBounty(PLAIN_ECO.zero());

        dataCenterInfo.runTransact((tx) -> {
            tx.getProperty(DSLContext.class)
                    .insertInto(KITPVP_BOUNTIES)
                    .columns(KITPVP_BOUNTIES.USER_ID, KITPVP_BOUNTIES.BOUNTY_AMOUNT, KITPVP_BOUNTIES.BOUNTY_CURRENCY)
                    .values(userId, BigDecimal.TEN, CREDITS.serialize())
                    .values(userId, ONE_HUNDRED, PLAIN_ECO.serialize())
                    .execute();
        });
        assertEqualAmounts(CREDITS.zero(), dataCenterInfo.transact((tx) -> data.getBounty(tx, CREDITS)));
        assertEqualAmounts(CREDITS.zero(), dataCenterInfo.transact((tx) -> data.getBounty(tx, CREDITS)));
    }

    @Test
    public void logBounty() {
        OnlineSolarPlayer killer = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer victim = dataCenterInfo.loginNewRandomUser();
        assertDoesNotThrow(() -> dataCenterInfo.runTransact((tx) -> {
            dataCenterInfo.dataCenter().getDataManager(KitPvpKey.INSTANCE)
                    .logBounty(tx, killer, victim, CREDITS.createAmount(BigDecimal.TEN));
        }));
    }

}
