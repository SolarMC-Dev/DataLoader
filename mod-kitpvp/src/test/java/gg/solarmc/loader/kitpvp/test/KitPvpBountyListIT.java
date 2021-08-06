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
import gg.solarmc.loader.kitpvp.BountyPage;
import gg.solarmc.loader.kitpvp.ItemSerializer;
import gg.solarmc.loader.kitpvp.KitPvpKey;
import gg.solarmc.loader.kitpvp.KitPvpManager;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Optional;

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

    private void assertEqualDecimals(BigDecimal expected, BigDecimal actual) {
        //noinspection SimplifiableAssertion
        assertTrue(expected.compareTo(actual) == 0, "Expected " + expected + " but got " + actual);
    }

    @Test
    public void noPages() {
        assertEquals(Optional.empty(), dataCenterInfo.transact((tx) -> manager.listBounties(tx, 3)));
    }

    private void assertPageValues(BountyPage page, BigDecimal...values) {
        List<Bounty> pageItems = page.itemsOnPage();
        assertEquals(values.length, pageItems.size());
        for (int n = 0; n < values.length; n++) {
            assertEqualDecimals(values[n], pageItems.get(n).amount());
        }
    }

    @Test
    public void multiplePages() {
        // Prefill data
        dataCenterInfo.runTransact((tx) -> {
            for (int n = 0; n < 5; n++) {
                OnlineSolarPlayer player = dataCenterInfo.loginNewRandomUser();
                player.getData(KitPvpKey.INSTANCE).addBounty(tx, BigDecimal.TEN.multiply(BigDecimal.valueOf(n + 1)));
            }
        });
        BountyPage pageOne;
        {
            Optional<BountyPage> optPageOne = dataCenterInfo.transact((tx) -> manager.listBounties(tx, 2));
            pageOne = assertDoesNotThrow((ThrowingSupplier<BountyPage>) optPageOne::orElseThrow, "Page 1");
            assertPageValues(pageOne, BigDecimal.valueOf(50), BigDecimal.valueOf(40));
        }
        BountyPage pageTwo;
        {
            Optional<BountyPage> optPageTwo = dataCenterInfo.transact(pageOne::nextPage);
            pageTwo = assertDoesNotThrow((ThrowingSupplier<BountyPage>) optPageTwo::orElseThrow, "Page 2");
            assertPageValues(pageTwo, BigDecimal.valueOf(30), BigDecimal.valueOf(20));
        }
        Optional<BountyPage> optPageThree = dataCenterInfo.transact(pageTwo::nextPage);
        BountyPage pageThree = assertDoesNotThrow((ThrowingSupplier<BountyPage>) optPageThree::orElseThrow, "Page 2");
        assertPageValues(pageThree, BigDecimal.TEN);

        assertEquals(Optional.empty(), dataCenterInfo.transact(pageThree::nextPage));
    }
}
