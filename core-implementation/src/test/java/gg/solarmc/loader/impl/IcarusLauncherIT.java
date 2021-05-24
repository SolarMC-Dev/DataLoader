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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.util.concurrent.impl.IndifferentFactoryOfTheFuture;

import java.nio.file.Path;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class IcarusLauncherIT {

    @TempDir
    public Path tempDir;

    @Test
    public void launch(SolarDataConfig.DatabaseCredentials credentials) {
        IcarusLauncher launcher = new IcarusLauncher(tempDir, new IndifferentFactoryOfTheFuture(),
                new DefaultOmnibus(), Executors::newFixedThreadPool);
        Icarus icarus = assertDoesNotThrow(() -> launcher.launch(credentials));
        assertDoesNotThrow(icarus::close);
    }

}
