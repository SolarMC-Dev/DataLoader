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
import gg.solarmc.loader.kitpvp.Bounty;
import gg.solarmc.loader.kitpvp.BountyAmount;
import gg.solarmc.loader.kitpvp.BountyCurrency;
import gg.solarmc.loader.kitpvp.BountyListOrder;
import gg.solarmc.loader.kitpvp.BountyPage;
import gg.solarmc.loader.kitpvp.ItemSerializer;
import gg.solarmc.loader.kitpvp.KitPvpKey;
import gg.solarmc.loader.kitpvp.KitPvpManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.Omnibus;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static gg.solarmc.loader.kitpvp.BountyCurrency.CREDITS;
import static gg.solarmc.loader.kitpvp.BountyCurrency.PLAIN_ECO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class KitPvpBountyListIT {

    private DataCenterInfo dataCenterInfo;
    private KitPvpManager manager;

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials,
                              @Mock ItemSerializer itemSerializer) {
        Omnibus omnibus = new DefaultOmnibus();
        omnibus.getRegistry().register(ItemSerializer.class, (byte) 0, itemSerializer, "Serializer");
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).omnibus(omnibus).build();
        manager = dataCenterInfo.dataCenter().getDataManager(KitPvpKey.INSTANCE);
    }

    private void assertEqualDecimals(BigDecimal expected, BountyAmount actual, Supplier<String> message) {
        //noinspection SimplifiableAssertion
        assertTrue(expected.compareTo(actual.value()) == 0, () -> "Expected " + expected + " but got " + actual + ". More information: " + message.get());
    }

    @Test
    public void noPages() {
        assertEquals(Optional.empty(), dataCenterInfo.transact((tx) -> {
            return manager.listBounties(tx,
                    BountyListOrder.countPerPage(3).includeCurrencies(CREDITS).build());
        }));
    }

    private void assertPageValues(BountyPage page, BountyCurrency currency, BigDecimal...values) {
        List<? extends Bounty> pageItems = page.itemsOnPage();
        assertEquals(values.length, pageItems.size());
        for (int n = 0; n < values.length; n++) {
            assertEqualDecimals(values[n], pageItems.get(n).amount(currency),
                    () -> "Wanted " + Arrays.toString(values) + " but got page " + page);
        }
    }

    @Test
    public void multiplePages() {
        // Prefill data
        dataCenterInfo.runTransact((tx) -> {
            for (int n = 0; n < 5; n++) {
                OnlineSolarPlayer player = dataCenterInfo.loginNewRandomUser();
                player.getData(KitPvpKey.INSTANCE).addBounty(tx,
                        CREDITS.createAmount(BigDecimal.TEN.multiply(BigDecimal.valueOf(n + 1))));
            }
        });
        BountyPage pageOne;
        {
            Optional<BountyPage> optPageOne = dataCenterInfo.transact((tx) -> {
                return manager.listBounties(tx, BountyListOrder.countPerPage(2).includeCurrencies(CREDITS).build());
            });
            pageOne = assertDoesNotThrow((ThrowingSupplier<BountyPage>) optPageOne::orElseThrow, "Page 1");
            assertPageValues(pageOne, CREDITS, BigDecimal.valueOf(50), BigDecimal.valueOf(40));
        }
        BountyPage pageTwo;
        {
            Optional<BountyPage> optPageTwo = dataCenterInfo.transact(pageOne::nextPage);
            pageTwo = assertDoesNotThrow((ThrowingSupplier<BountyPage>) optPageTwo::orElseThrow, "Page 2");
            assertPageValues(pageTwo, CREDITS, BigDecimal.valueOf(30), BigDecimal.valueOf(20));
        }
        Optional<BountyPage> optPageThree = dataCenterInfo.transact(pageTwo::nextPage);
        BountyPage pageThree = assertDoesNotThrow((ThrowingSupplier<BountyPage>) optPageThree::orElseThrow, "Page 2");
        assertPageValues(pageThree, CREDITS, BigDecimal.TEN);

        assertEquals(Optional.empty(), dataCenterInfo.transact(pageThree::nextPage));
    }

    @Test
    public void multipleCurrencies() {
        OnlineSolarPlayer playerOne = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerTwo = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerThree = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerFour = dataCenterInfo.loginNewRandomUser();
        /*OnlineSolarPlayer playerFive =*/ dataCenterInfo.loginNewRandomUser();
        dataCenterInfo.runTransact((tx) -> {
            // 10 credits on player1
            playerOne.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.TEN));
            // 2 credit 100 plain eco on player2
            playerTwo.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.valueOf(2)));
            playerTwo.getData(KitPvpKey.INSTANCE).addBounty(tx, PLAIN_ECO.createAmount(BigDecimal.valueOf(100)));
            // 1 credit 8 plain eco on player3
            playerThree.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.ONE));
            playerThree.getData(KitPvpKey.INSTANCE).addBounty(tx, PLAIN_ECO.createAmount(BigDecimal.valueOf(8)));
            // 9 plain eco on player4
            playerFour.getData(KitPvpKey.INSTANCE).addBounty(tx, PLAIN_ECO.createAmount(BigDecimal.valueOf(9)));
            // Nothing on player5
        });
        BountyPage pageOne = dataCenterInfo.transact((tx) -> {
            return manager.listBounties(tx, BountyListOrder.countPerPage(10).includeCurrencies(BountyCurrency.values()).build());
        }).orElseThrow(AssertionError::new);
        List<? extends Bounty> bounties = pageOne.itemsOnPage();
        {
            assertEquals(4, bounties.size(), "Bounty list: expected 4 in size but was " + bounties);
            List<OnlineSolarPlayer> players = List.of(playerOne, playerTwo, playerThree, playerFour);
            for (int n = 0; n < 4; n++) {
                assertEquals(bounties.get(n).target(), players.get(n).getMcUsername());
            }
        }
        assertPageValues(
                pageOne, CREDITS, BigDecimal.TEN, BigDecimal.valueOf(2), BigDecimal.ONE, BigDecimal.ZERO);
        assertPageValues(
                pageOne, PLAIN_ECO, BigDecimal.ZERO, BigDecimal.valueOf(100), BigDecimal.valueOf(8), BigDecimal.valueOf(9));
    }

    @Test
    public void multipleCurrenciesMultiplePages() {
        // A variation on the last test using a smaller countPerPage
        OnlineSolarPlayer playerOne = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerTwo = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerThree = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerFour = dataCenterInfo.loginNewRandomUser();
        dataCenterInfo.runTransact((tx) -> {
            // 10 credits on player1
            playerOne.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.TEN));
            // 2 credit 100 plain eco on player2
            playerTwo.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.valueOf(2)));
            playerTwo.getData(KitPvpKey.INSTANCE).addBounty(tx, PLAIN_ECO.createAmount(BigDecimal.valueOf(100)));
            // 1 credit 8 plain eco on player3
            playerThree.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.ONE));
            playerThree.getData(KitPvpKey.INSTANCE).addBounty(tx, PLAIN_ECO.createAmount(BigDecimal.valueOf(8)));
            // 9 plain eco on player4
            playerFour.getData(KitPvpKey.INSTANCE).addBounty(tx, PLAIN_ECO.createAmount(BigDecimal.valueOf(9)));
        });
        BountyPage pageOne = dataCenterInfo.transact((tx) -> {
            return manager.listBounties(tx, BountyListOrder.countPerPage(3).includeCurrencies(BountyCurrency.values()).build());
        }).orElseThrow(AssertionError::new);
        List<? extends Bounty> bounties = pageOne.itemsOnPage();
        assertEquals(3, bounties.size(), "Bounty list: expected 3 in size but was " + bounties);
        assertPageValues(
                pageOne, CREDITS, BigDecimal.TEN, BigDecimal.valueOf(2), BigDecimal.ONE);
        assertPageValues(
                pageOne, PLAIN_ECO, BigDecimal.ZERO, BigDecimal.valueOf(100), BigDecimal.valueOf(8));
        BountyPage pageTwo = dataCenterInfo.transact(pageOne::nextPage).orElseThrow(AssertionError::new);
        assertPageValues(pageTwo, CREDITS, BigDecimal.ZERO);
        assertPageValues(pageTwo, PLAIN_ECO, BigDecimal.valueOf(9));
    }

    @Disabled("temporarily, if you see this during review, please remove")
    @Test
    public void paginateAcrossSameBountyValue() {
        // Check behavior when page splits across users with the same bounty
        OnlineSolarPlayer playerOne = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerTwo = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerThree = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer playerFour = dataCenterInfo.loginNewRandomUser();
        dataCenterInfo.runTransact((tx) -> {
            // 10 credits on player1
            playerOne.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.TEN));
            // 1 credit on player2
            playerTwo.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.ONE));
            // 1 credit on player3
            playerThree.getData(KitPvpKey.INSTANCE).addBounty(tx, CREDITS.createAmount(BigDecimal.ONE));
            // 1 plain eco on player4
            playerFour.getData(KitPvpKey.INSTANCE).addBounty(tx, PLAIN_ECO.createAmount(BigDecimal.ONE));
        });
        BountyPage pageOne = dataCenterInfo.transact((tx) -> {
            return manager.listBounties(tx, BountyListOrder.countPerPage(2).includeCurrencies(BountyCurrency.values()).build());
        }).orElseThrow(AssertionError::new);
        assertPageValues(pageOne, CREDITS, BigDecimal.TEN, BigDecimal.ONE);

        BountyPage pageTwo = dataCenterInfo.transact((tx) -> {
            return manager.listBounties(tx, BountyListOrder.countPerPage(2).includeCurrencies(BountyCurrency.values()).build());
        }).orElseThrow(AssertionError::new);
        assertPageValues(pageTwo, CREDITS, BigDecimal.ONE, BigDecimal.ZERO);
    }
}
