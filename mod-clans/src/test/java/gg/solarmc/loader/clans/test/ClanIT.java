package gg.solarmc.loader.clans.test;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.clans.Clan;
import gg.solarmc.loader.clans.ClanManager;
import gg.solarmc.loader.clans.ClanMember;
import gg.solarmc.loader.clans.ClansKey;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DatabaseExtension.class)
public class ClanIT {

    private DataCenterInfo dataCenterInfo;
    private ClanManager clanManager;

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).build();
        clanManager = dataCenterInfo.dataCenter().getDataManager(ClansKey.INSTANCE);
    }

    private void addMemberAssertSuccess(Clan clan, OnlineSolarPlayer member) {
        boolean addedMember = dataCenterInfo.transact((tx) -> clan.addClanMember(tx, member));
        assertTrue(addedMember, "New member");
    }

    private Clan createClan(OnlineSolarPlayer leader, String name) {
        Clan clan = dataCenterInfo.transact((tx) -> clanManager.createClan(tx, name, leader));
        assertEquals(1, dataCenterInfo.transact(clan::getClanSize), "Clan size starts at 1");
        assertEquals(Set.of(clanMember(leader)), dataCenterInfo.transact(clan::getClanMembers),
                "New clan's members should be only the leader");
        return clan;
    }

    private Clan createClan(OnlineSolarPlayer leader) {
        return createClan(leader, "clanName");
    }

    private Clan getClan(int clanId) {
        return dataCenterInfo.transact((tx) -> clanManager.getClan(tx, clanId));
    }

    private ClanMember clanMember(OnlineSolarPlayer member) {
        return new ClanMember(member.getUserId());
    }

    private void assertMembers(Set<ClanMember> members, Clan clan) {
        assertEquals(members.size(), dataCenterInfo.transact(clan::getClanSize), "Clan size");
        assertEquals(members, clan.currentMembers());
        assertEquals(members, dataCenterInfo.transact(clan::getClanMembers), "Re-retrieved members");
        assertEquals(members, clan.currentMembers(), "Cached members updated incorrectly");
    }

    @Test
    public void addClanMember() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer member = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);

        addMemberAssertSuccess(clan, member);
        assertMembers(Set.of(clanMember(leader), clanMember(member)), clan);
    }

    @Test
    public void addClanMemberAlreadyMember() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer member = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);
        addMemberAssertSuccess(clan, member);

        boolean addedMemberAgain = dataCenterInfo.transact((tx) -> clan.addClanMember(tx, member));
        assertFalse(addedMemberAgain, "Existing member is already a member");
    }

    @Test
    public void addClanMemberLeader() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);

        boolean addedLeader = dataCenterInfo.transact((tx) -> clan.addClanMember(tx, leader));
        assertFalse(addedLeader, "Leader is already a member");
        assertMembers(Set.of(clanMember(leader)), clan);
    }

    @Test
    public void removeClanMember() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer member = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);
        addMemberAssertSuccess(clan, member);

        boolean removedMember = dataCenterInfo.transact((tx) -> {
            return clan.removeClanMember(tx, member);
        });
        assertTrue(removedMember, "Should be able to remove member");
        assertMembers(Set.of(clanMember(leader)), clan);
    }

    @Test
    public void removeClanMemberSelf() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);
        dataCenterInfo.runTransact((tx) -> {
            assertThrows(IllegalArgumentException.class, () -> clan.removeClanMember(tx, leader));
        });
    }

    @Test
    public void removeClanMemberNotMember() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer member = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);

        boolean removedMember = dataCenterInfo.transact((tx) -> {
            return clan.removeClanMember(tx, member);
        });
        assertFalse(removedMember, "Not a member of the clan");
    }

    private void addAllyAssertSuccess(Clan clan, Clan ally) {
        boolean added = dataCenterInfo.transact((tx) -> clan.addClanAsAlly(tx, ally));
        assertTrue(added, "Clans should be allied");
    }

    @Test
    public void addClanAsAlly() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");

        addAllyAssertSuccess(clanOne, clanTwo);

        assertEquals(Optional.of(clanTwo), dataCenterInfo.transact(clanOne::getAlliedClan));
        assertEquals(Optional.of(clanOne), dataCenterInfo.transact(clanTwo::getAlliedClan));
    }

    @Test
    public void addClanAsAllyButSelfAlreadyAllied() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");
        Clan allyOfClanOne = createClan(dataCenterInfo.loginNewRandomUser(), "allyOfClanOne");

        addAllyAssertSuccess(clanOne, allyOfClanOne);

        boolean added = dataCenterInfo.transact((tx) -> clanOne.addClanAsAlly(tx, clanTwo));
        assertFalse(added, "Clan one is already allied");

        assertEquals(Optional.of(allyOfClanOne), dataCenterInfo.transact(clanOne::getAlliedClan));
        assertEquals(Optional.empty(), dataCenterInfo.transact(clanTwo::getAlliedClan));
    }

    @Test
    public void addClanAsAllyButAllyAlreadyAllied() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");
        Clan allyOfClanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "allyOfClanOne");

        addAllyAssertSuccess(clanTwo, allyOfClanTwo);

        boolean added = dataCenterInfo.transact((tx) -> clanOne.addClanAsAlly(tx, clanTwo));
        assertFalse(added, "Clan two is already allied");

        assertEquals(Optional.empty(), dataCenterInfo.transact(clanOne::getAlliedClan));
        assertEquals(Optional.of(allyOfClanTwo), dataCenterInfo.transact(clanTwo::getAlliedClan));
    }

    @Test
    public void addClanAsAllySelf() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser());
        Clan self = getClan(clan.getClanId());
        dataCenterInfo.runTransact((tx) -> {
            assertThrows(IllegalArgumentException.class, () -> clan.addClanAsAlly(tx, self));
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void revokeAlly(boolean revokeFromFirst) {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");

        boolean added = dataCenterInfo.transact((tx) -> clanOne.addClanAsAlly(tx, clanTwo));
        assertTrue(added, "Clans should be allied");

        assertEquals(Optional.of(clanTwo), dataCenterInfo.transact(clanOne::getAlliedClan));
        assertEquals(Optional.of(clanOne), dataCenterInfo.transact(clanTwo::getAlliedClan));

        Clan clanToCallRevokeOn = (revokeFromFirst) ? clanOne : clanTwo;
        assertTrue(dataCenterInfo.transact(clanToCallRevokeOn::revokeAlly), "Clans should be removed as allies");

        assertEquals(Optional.empty(), dataCenterInfo.transact(clanOne::getAlliedClan));
        assertEquals(Optional.empty(), dataCenterInfo.transact(clanTwo::getAlliedClan));
    }

    @Test
    public void revokeAllyNotAllied() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser());
        assertFalse(dataCenterInfo.transact(clan::revokeAlly));
    }

    private void addEnemyAssertSuccess(Clan clan, Clan enemy) {
        boolean added = dataCenterInfo.transact((tx) -> clan.addClanAsEnemy(tx, enemy));
        assertTrue(added, "Clans should now be enemies");
    }

    @Test
    public void addClanAsEnemy() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");

        addEnemyAssertSuccess(clanOne, clanTwo);

        assertEquals(Set.of(clanTwo), dataCenterInfo.transact(clanOne::getEnemyClans));
        assertEquals(Set.of(), dataCenterInfo.transact(clanTwo::getEnemyClans),
                "Enemies are a one-way relationship and need not be mutual");

        boolean addedAgain = dataCenterInfo.transact((tx) -> clanOne.addClanAsEnemy(tx, clanTwo));
        assertFalse(addedAgain, "Clans should already be enemies");
    }

    @Test
    public void addClanAsEnemyAllied() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");
        addAllyAssertSuccess(clanOne, clanTwo);
        dataCenterInfo.runTransact((tx) -> {
            assertThrows(IllegalArgumentException.class, () -> clanOne.addClanAsEnemy(tx, clanTwo));
        });
    }

    @Test
    public void addClanAsEnemySelf() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser());
        Clan self = getClan(clan.getClanId());
        dataCenterInfo.runTransact((tx) -> {
            assertThrows(IllegalArgumentException.class, () -> clan.addClanAsEnemy(tx, self));
        });
    }

    @Test
    public void removeClanAsEnemy() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");
        addEnemyAssertSuccess(clanOne, clanTwo);

        boolean removed = dataCenterInfo.transact((tx) -> {
            return clanOne.removeClanAsEnemy(tx, clanTwo);
        });
        assertTrue(removed, "Removed clan as enemy");

        assertEquals(Set.of(), dataCenterInfo.transact(clanOne::getEnemyClans), "Clan should have no more enemies");
    }

    @Test
    public void removeClanAsEnemyNotEnemy() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");

        boolean removed = dataCenterInfo.transact((tx) -> {
            return clanOne.removeClanAsEnemy(tx, clanTwo);
        });
        assertFalse(removed, "Clan is not an enemy");
    }

    @Test
    public void removeClanAsEnemySelf() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser());
        Clan self = getClan(clan.getClanId());
        dataCenterInfo.runTransact((tx) -> {
            assertThrows(IllegalArgumentException.class, () -> clan.removeClanAsEnemy(tx, self));
        });
    }
}
