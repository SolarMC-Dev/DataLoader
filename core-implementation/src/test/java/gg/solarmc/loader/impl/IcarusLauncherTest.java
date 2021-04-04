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

package gg.solarmc.loader.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("Bug in DazzleConf on the module path. " +
            "See https://github.com/A248/DazzleConf/commit/dbaa4defb07199b138c5d8834ef6209f2f89a304 " +
            "Will be fixed in DazzleConf 1.2.0")
    public void loadConfig() {
        assertDoesNotThrow(launcher::loadConfig);
    }

    @Test
    @Disabled("same as prior")
    public void reloadConfig() {
        launcher.loadConfig();
        assertDoesNotThrow(launcher::loadConfig);
    }
}
