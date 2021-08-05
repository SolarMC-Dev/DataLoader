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

package gg.solarmc.loader.clans.test;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.clans.Clan;
import gg.solarmc.loader.clans.ClanManager;
import gg.solarmc.loader.clans.ClansKey;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DatabaseExtension.class)
public class ClanManagerIT {

    private DataCenterInfo dataCenterInfo;
    private ClanManager clanManager;

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).build();
        clanManager = dataCenterInfo.dataCenter().getDataManager(ClansKey.INSTANCE);
    }

    @Test
    public void deleteClan() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        Clan clan = dataCenterInfo.transact((tx) -> clanManager.createClan(tx, "MyClan", leader));
        assertEquals(Optional.of(clan), leader.getData(ClansKey.INSTANCE).currentClan());
        dataCenterInfo.runTransact((tx) -> clanManager.deleteClan(tx, clan));
        assertEquals(Optional.empty(), leader.getData(ClansKey.INSTANCE).currentClan());
    }

    @Test
    public void deleteClanWithAlly() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer allyLeader = dataCenterInfo.loginNewRandomUser();
        Clan ally = dataCenterInfo.transact((tx) -> {
            Clan createdClan = clanManager.createClan(tx, "MyClan", leader);
            Clan createdAlly = clanManager.createClan(tx, "AllyClan", allyLeader);
            clanManager.deleteClan(tx, createdClan);
            return createdAlly;
        });
        assertEquals(Optional.empty(), ally.currentAllyClan());
    }

}
