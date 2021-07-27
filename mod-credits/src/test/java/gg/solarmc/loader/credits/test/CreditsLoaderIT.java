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

package gg.solarmc.loader.credits.test;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.UserDetails;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DataGenerator;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class CreditsLoaderIT {

    private DataCenterInfo dataCenterInfo;

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).build();
    }

    @Test
    public void reloginUser() {
        UserDetails userDetails = DataGenerator.newRandomUser();
        OnlineSolarPlayer player = assertDoesNotThrow(() -> dataCenterInfo.loginUser(userDetails));
        assertDoesNotThrow(() -> dataCenterInfo.loginUser(userDetails));
    }

}
