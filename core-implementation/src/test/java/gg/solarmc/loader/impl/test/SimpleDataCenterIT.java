package gg.solarmc.loader.impl.test;

import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.SolarPlayer;
import gg.solarmc.loader.impl.PlayerTracker;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.UserDetails;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DataGenerator;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.util.concurrent.CentralisedFuture;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class SimpleDataCenterIT {

    private DataCenterInfo dataCenterInfo;
    private final PlayerTracker playerTracker;

    public SimpleDataCenterIT(@Mock PlayerTracker playerTracker) {
        this.playerTracker = playerTracker;
    }

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).playerTracker(playerTracker).build();
    }

    private DataCenter dataCenter() {
        return dataCenterInfo.dataCenter();
    }

    @Test
    public void lookupPlayerNonexistent() {
        when(playerTracker.getOnlinePlayerForName(any())).thenReturn(Optional.empty());
        when(playerTracker.getOnlinePlayerForUuid(any())).thenReturn(Optional.empty());
        when(playerTracker.getOnlinePlayerForUserId(anyInt())).thenReturn(Optional.empty());
        String name = "username";
        UUID uuid = UUID.randomUUID();
        int userId = 14;
        assertEquals(Optional.empty(), dataCenter().lookupPlayer(name).join());
        assertEquals(Optional.empty(), dataCenter().lookupPlayer(uuid).join());
        assertEquals(Optional.empty(), dataCenter().lookupPlayer(userId).join());
        assertEquals(Optional.empty(), dataCenterInfo.transact((tx) -> dataCenter().lookupPlayerUsing(tx, name)));
        assertEquals(Optional.empty(), dataCenterInfo.transact((tx) -> dataCenter().lookupPlayerUsing(tx, uuid)));
        assertEquals(Optional.empty(), dataCenterInfo.transact((tx) -> dataCenter().lookupPlayerUsing(tx, userId)));
    }

    @Test
    public void lookupPlayerByNameOnline(@Mock OnlineSolarPlayer player) {
        String name = "username";
        when(playerTracker.getOnlinePlayerForName(name)).thenReturn(Optional.of(player));
        assertEquals(Optional.of(player), dataCenter().lookupPlayer(name).join());
        assertEquals(Optional.of(player), dataCenterInfo.transact((tx) -> dataCenter().lookupPlayerUsing(tx, name)));
    }

    @Test
    public void lookupPlayerByUUIDOnline(@Mock OnlineSolarPlayer player) {
        UUID uuid = UUID.randomUUID();
        when(playerTracker.getOnlinePlayerForUuid(uuid)).thenReturn(Optional.of(player));
        assertEquals(Optional.of(player), dataCenter().lookupPlayer(uuid).join());
        assertEquals(Optional.of(player), dataCenterInfo.transact((tx) -> dataCenter().lookupPlayerUsing(tx, uuid)));
    }

    @Test
    public void lookupPlayerByUserIdOnline(@Mock OnlineSolarPlayer player) {
        int userId = 14;
        when(playerTracker.getOnlinePlayerForUserId(userId)).thenReturn(Optional.of(player));
        assertEquals(Optional.of(player), dataCenter().lookupPlayer(userId).join());
        assertEquals(Optional.of(player), dataCenterInfo.transact((tx) -> dataCenter().lookupPlayerUsing(tx, userId)));
    }

    private int loginUser(UserDetails userDetails) {
        return dataCenterInfo.loginUser(userDetails).getUserId();
    }

    private static void assertMatches(SolarPlayer player, UserDetails userDetails, int userId) {
        assertEquals(userDetails.mcUuid(), player.getMcUuid());
        assertEquals(userId, player.getUserId());
    }

    @Test
    public void lookupPlayerByNameStored() {
        UserDetails userDetails = DataGenerator.newRandomUser();
        int userId = loginUser(userDetails);
        when(playerTracker.getOnlinePlayerForName(any())).thenReturn(Optional.empty());
        SolarPlayer player = assertDoesNotThrow(() -> dataCenter().lookupPlayer(userDetails.username()).join().orElseThrow());
        assertMatches(player, userDetails, userId);
        SolarPlayer samePlayer = assertDoesNotThrow(() -> dataCenterInfo.transact(
                (tx) -> dataCenter().lookupPlayerUsing(tx, userDetails.username()).orElseThrow()));
        assertMatches(samePlayer, userDetails, userId);
    }

    @Test
    public void lookupPlayerByNameStoredDifferentCase() {
        UserDetails userDetails = new UserDetails(UUID.randomUUID(), "UsernameCase", DataGenerator.randomAddress());
        int userId = loginUser(userDetails);
        when(playerTracker.getOnlinePlayerForName(any())).thenReturn(Optional.empty());
        SolarPlayer player = assertDoesNotThrow(() -> dataCenter().lookupPlayer("UsernameCASE").join().orElseThrow());
        assertMatches(player, userDetails, userId);
        SolarPlayer samePlayer = assertDoesNotThrow(() -> dataCenterInfo.transact(
                (tx) -> dataCenter().lookupPlayerUsing(tx, "UsernameCASE").orElseThrow()));
        assertMatches(samePlayer, userDetails, userId);
    }

    @Test
    public void lookupPlayerByUUIDStored() {
        UserDetails userDetails = DataGenerator.newRandomUser();
        int userId = loginUser(userDetails);
        when(playerTracker.getOnlinePlayerForUuid(any())).thenReturn(Optional.empty());
        SolarPlayer player = assertDoesNotThrow(() -> dataCenter().lookupPlayer(userDetails.mcUuid()).join().orElseThrow());
        assertMatches(player, userDetails, userId);
        SolarPlayer samePlayer = assertDoesNotThrow(() -> dataCenterInfo.transact(
                (tx) -> dataCenter().lookupPlayerUsing(tx, userDetails.mcUuid()).orElseThrow()));
        assertMatches(samePlayer, userDetails, userId);
    }

    @Test
    public void lookupPlayerByUserIdStored() {
        UserDetails userDetails = DataGenerator.newRandomUser();
        int userId = loginUser(userDetails);
        when(playerTracker.getOnlinePlayerForUserId(anyInt())).thenReturn(Optional.empty());
        SolarPlayer player = assertDoesNotThrow(() -> dataCenter().lookupPlayer(userId).join().orElseThrow());
        assertMatches(player, userDetails, userId);
        SolarPlayer samePlayer = assertDoesNotThrow(() -> dataCenterInfo.transact(
                (tx) -> dataCenter().lookupPlayerUsing(tx, userId).orElseThrow()));
        assertMatches(samePlayer, userDetails, userId);
    }

}
