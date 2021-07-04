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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(DatabaseExtension.class)
public class ClanStatisticsIT {

    private DataCenterInfo dataCenterInfo;
    private Clan clan;

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).build();
        ClanManager clanManager = dataCenterInfo.dataCenter().getDataManager(ClansKey.INSTANCE);
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        clan = dataCenterInfo.transact((tx) -> clanManager.createClan(tx, "clan", leader));
    }

    @Test
    public void addKills() {
        int newKills = dataCenterInfo.transact((tx) -> clan.addKills(tx, 1));
        assertEquals(1, newKills);
        assertEquals(newKills, clan.currentKills());
        newKills = dataCenterInfo.transact((tx) -> clan.addKills(tx, 2));
        assertEquals(3, newKills);
    }

    @Test
    public void addDeaths() {
        int newDeaths = dataCenterInfo.transact((tx) -> clan.addDeaths(tx, 1));
        assertEquals(1, newDeaths);
        assertEquals(newDeaths, clan.currentDeaths());
        newDeaths = dataCenterInfo.transact((tx) -> clan.addDeaths(tx, 2));
        assertEquals(3, newDeaths);
    }

    @Test
    public void addAssists() {
        int newAssists = dataCenterInfo.transact((tx) -> clan.addAssists(tx, 1));
        assertEquals(1, newAssists);
        assertEquals(newAssists, clan.currentAssists());
        newAssists = dataCenterInfo.transact((tx) -> clan.addAssists(tx, 2));
        assertEquals(3, newAssists);
    }

    @Test
    public void addNonPositiveKillsDeathsAssists() {
        dataCenterInfo.runTransact((tx) -> {
            assertThrows(IllegalArgumentException.class, () -> clan.addKills(tx, -2));
            assertThrows(IllegalArgumentException.class, () -> clan.addDeaths(tx, -2));
            assertThrows(IllegalArgumentException.class, () -> clan.addAssists(tx, -2));
            assertThrows(IllegalArgumentException.class, () -> clan.addKills(tx, 0));
            assertThrows(IllegalArgumentException.class, () -> clan.addDeaths(tx, 0));
            assertThrows(IllegalArgumentException.class, () -> clan.addAssists(tx, 0));
        });
    }

}
