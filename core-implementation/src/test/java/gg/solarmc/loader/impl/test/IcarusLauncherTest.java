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

package gg.solarmc.loader.impl.test;

import gg.solarmc.loader.impl.IcarusLauncher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.util.concurrent.impl.IndifferentFactoryOfTheFuture;

import java.nio.file.Path;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class IcarusLauncherTest {

    @TempDir
    public Path tempDir;

    private IcarusLauncher launcher;

    @BeforeEach
    public void setup() {
        launcher = new IcarusLauncher(tempDir, new IndifferentFactoryOfTheFuture(),
                new DefaultOmnibus(), Executors::newFixedThreadPool);
    }

    @Test
    public void loadConfig() {
        assertDoesNotThrow(launcher::loadConfig);
    }

    @Test
    public void reloadConfig() {
        launcher.loadConfig();
        assertDoesNotThrow(launcher::loadConfig);
    }
}
