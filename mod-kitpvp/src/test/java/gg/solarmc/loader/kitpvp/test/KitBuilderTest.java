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

import gg.solarmc.loader.kitpvp.ItemInSlot;
import gg.solarmc.loader.kitpvp.KitBuilder;
import gg.solarmc.loader.kitpvp.KitItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class KitBuilderTest {

    @Test
    public void buildKit() {
        Set<ItemInSlot> contents = Set.of(new ItemInSlot(1, mock(KitItem.class)));
        KitBuilder.Built ready = new KitBuilder()
                .name("Kit Name")
                .contents(contents)
                .build();
        assertEquals("Kit Name", ready.name());
        assertEquals(contents, ready.contents());
        assertEquals(Duration.ZERO, ready.cooldown());
    }

    @Test
    public void buildKitWithCooldown() {
        Set<ItemInSlot> contents = Set.of(new ItemInSlot(1, mock(KitItem.class)));
        Duration cooldown = Duration.ofHours(1L);
        KitBuilder.Built ready = new KitBuilder()
                .name("Kit Name")
                .contents(contents)
                .cooldown(cooldown)
                .build();
        assertEquals("Kit Name", ready.name());
        assertEquals(contents, ready.contents());
        assertEquals(cooldown, ready.cooldown());
    }
}
