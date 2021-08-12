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
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
        assertEquals(Optional.of(clan), member.getData(ClansKey.INSTANCE).currentClan(), "New member's cached membership");
    }

    private Clan createClan(OnlineSolarPlayer leader, String name) {
        Clan clan = dataCenterInfo.transact((tx) -> clanManager.createClan(tx, name, leader));
        assertEquals(1, dataCenterInfo.transact(clan::getClanSize), "Clan size starts at 1");
        assertEquals(Set.of(clanMember(leader)), dataCenterInfo.transact(clan::getClanMembers),
                "New clan's members should be only the leader");
        assertEquals(Optional.of(clan), leader.getData(ClansKey.INSTANCE).currentClan(), "Leader's cached membership'");
        return clan;
    }

    private Clan createClan(OnlineSolarPlayer leader) {
        return createClan(leader, "clanName");
    }

    private Clan getClan(int clanId) {
        return dataCenterInfo.transact((tx) -> clanManager.getClanById(tx, clanId)).orElseThrow();
    }

    private ClanMember clanMember(OnlineSolarPlayer member) {
        return new ClanMember(member.getUserId());
    }

    private void assertMembers(Set<OnlineSolarPlayer> playerMembers, Clan clan) {
        for (OnlineSolarPlayer member : playerMembers) {
            assertEquals(Optional.of(clan), member.getData(ClansKey.INSTANCE).currentClan());
        }
        assertEquals(playerMembers.size(), dataCenterInfo.transact(clan::getClanSize), "Clan size");
        Set<ClanMember> members = playerMembers.stream().map(this::clanMember).collect(Collectors.toUnmodifiableSet());
        assertEquals(members, clan.currentMembers());
        assertEquals(members, dataCenterInfo.transact(clan::getClanMembers), "Re-retrieved members");
        assertEquals(members, clan.currentMembers(), "Cached members updated incorrectly");
    }

    @Test
    public void setClanLeader() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer usurper = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);
        addMemberAssertSuccess(clan, usurper);

        dataCenterInfo.runTransact(tx -> clan.setLeader(tx, usurper));

        assertEquals(usurper.getUserId(), clan.currentLeader().userId(), "Cached leader is not the same as usurper");
        assertEquals(true,dataCenterInfo.transact(tx -> clan.getClanMembers(tx).contains(new ClanMember(leader.getUserId()))), "Doesn't contain old leader as member!");
        assertEquals(true, dataCenterInfo.transact(tx -> clan.getClanMembers(tx).contains(new ClanMember(usurper.getUserId()))), "Doesn't contain usurper as member!");


    }

    @Test
    public void setClanLeaderAsOutsider() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer usurper = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);

        dataCenterInfo.runTransact(tx -> {
            assertThrows(IllegalStateException.class, () -> clan.setLeader(tx, usurper));
        });
    }

    @Test
    public void setClanName() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);

        String returned = dataCenterInfo.transact(tx -> {
            clan.setName(tx, "can_we_merge_yet");

            return clan.getClanName(tx);
        });

        assertEquals("can_we_merge_yet", clan.currentClanName(), "Cached return invalid!");
        assertEquals("can_we_merge_yet", returned, "Transactional return not set!");

    }

    @Test
    public void addClanMember() {
        OnlineSolarPlayer leader = dataCenterInfo.loginNewRandomUser();
        OnlineSolarPlayer member = dataCenterInfo.loginNewRandomUser();
        Clan clan = createClan(leader);

        addMemberAssertSuccess(clan, member);
        assertMembers(Set.of(leader, member), clan);
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
        assertMembers(Set.of(leader), clan);
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
        assertMembers(Set.of(leader), clan);
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

    private void assertBeforeAndAfterCacheRefresh(Executable assertion) {
        assertDoesNotThrow(assertion, "Before cache refresh");
        dataCenterInfo.runTransact(clanManager::refreshCaches);
        assertDoesNotThrow(assertion, "After cache refresh");
    }

    private void addAllyAssertSuccess(Clan clan, Clan ally) {
        boolean added = dataCenterInfo.transact((tx) -> clan.addClanAsAlly(tx, ally));
        assertTrue(added, "Clans should be allied");
    }

    @Test
    public void noAllies() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser(), "clanName");

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Optional.empty(), clan.currentAllyClan());
            assertEquals(Optional.empty(), dataCenterInfo.transact(clan::getAllyClan));
        });
    }

    @Test
    public void addClanAsAlly() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan ally = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");

        addAllyAssertSuccess(clan, ally);

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Optional.of(ally), clan.currentAllyClan(), "Cached ally (clan->ally)");
            assertEquals(Optional.of(clan), ally.currentAllyClan(), "Cached ally (ally->clan)");
            assertEquals(Optional.of(ally), dataCenterInfo.transact(clan::getAllyClan));
            assertEquals(Optional.of(clan), dataCenterInfo.transact(ally::getAllyClan));
        });
    }

    @Test
    public void addClanAsAllyButSelfAlreadyAllied() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");
        Clan allyOfClanOne = createClan(dataCenterInfo.loginNewRandomUser(), "allyOfClanOne");

        addAllyAssertSuccess(clanOne, allyOfClanOne);

        boolean added = dataCenterInfo.transact((tx) -> clanOne.addClanAsAlly(tx, clanTwo));
        assertFalse(added, "Clan one is already allied");

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Optional.of(allyOfClanOne), clanOne.currentAllyClan());
            assertEquals(Optional.empty(), clanTwo.currentAllyClan());
            assertEquals(Optional.of(allyOfClanOne), dataCenterInfo.transact(clanOne::getAllyClan));
            assertEquals(Optional.empty(), dataCenterInfo.transact(clanTwo::getAllyClan));
        });
    }

    @Test
    public void addClanAsAllyButAllyAlreadyAllied() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");
        Clan allyOfClanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "allyOfClanOne");

        addAllyAssertSuccess(clanTwo, allyOfClanTwo);

        boolean added = dataCenterInfo.transact((tx) -> clanOne.addClanAsAlly(tx, clanTwo));
        assertFalse(added, "Clan two is already allied");

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Optional.empty(), clanOne.currentAllyClan());
            assertEquals(Optional.of(allyOfClanTwo), clanTwo.currentAllyClan());
            assertEquals(Optional.empty(), dataCenterInfo.transact(clanOne::getAllyClan));
            assertEquals(Optional.of(allyOfClanTwo), dataCenterInfo.transact(clanTwo::getAllyClan));
        });
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

        addAllyAssertSuccess(clanOne, clanTwo);

        Clan clanToCallRevokeOn = (revokeFromFirst) ? clanOne : clanTwo;
        assertTrue(dataCenterInfo.transact(clanToCallRevokeOn::revokeAlly), "Clans should be removed as allies");

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Optional.empty(), clanOne.currentAllyClan());
            assertEquals(Optional.empty(), clanTwo.currentAllyClan());
            assertEquals(Optional.empty(), dataCenterInfo.transact(clanOne::getAllyClan));
            assertEquals(Optional.empty(), dataCenterInfo.transact(clanTwo::getAllyClan));
        });
    }

    @Test
    public void revokeAllyNotAllied() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser());
        assertFalse(dataCenterInfo.transact(clan::revokeAlly));
        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Optional.empty(), clan.currentAllyClan());
            assertEquals(Optional.empty(), dataCenterInfo.transact(clan::getAllyClan));
        });
    }

    private void addEnemyAssertSuccess(Clan clan, Clan enemy) {
        boolean added = dataCenterInfo.transact((tx) -> clan.addClanAsEnemy(tx, enemy));
        assertTrue(added, "Clans should now be enemies");
    }

    @Test
    public void noEnemies() {
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser(), "clanName");

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Set.of(), clan.currentEnemyClans());
            assertEquals(Set.of(), dataCenterInfo.transact(clan::getEnemyClans));
        });
    }

    @Test
    public void addClanAsEnemy() {
        Clan clanOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan clanTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");

        addEnemyAssertSuccess(clanOne, clanTwo);

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Set.of(clanTwo), clanOne.currentEnemyClans());
            assertEquals(Set.of(), clanTwo.currentEnemyClans(),
                    "Enemies are a one-way relationship and need not be mutual");
            assertEquals(Set.of(clanTwo), dataCenterInfo.transact(clanOne::getEnemyClans));
            assertEquals(Set.of(), dataCenterInfo.transact(clanTwo::getEnemyClans),
                    "Enemies are a one-way relationship and need not be mutual");
        });

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
        Clan clan = createClan(dataCenterInfo.loginNewRandomUser(), "clanOneName");
        Clan enemyOne = createClan(dataCenterInfo.loginNewRandomUser(), "clanTwoName");
        Clan enemyTwo = createClan(dataCenterInfo.loginNewRandomUser(), "clanThreeName");
        addEnemyAssertSuccess(clan, enemyOne);
        addEnemyAssertSuccess(clan, enemyTwo);

        {
            boolean removedOne = dataCenterInfo.transact((tx) -> {
                return clan.removeClanAsEnemy(tx, enemyOne);
            });
            assertTrue(removedOne, "Removed clan as enemy");

            assertBeforeAndAfterCacheRefresh(() -> {
                assertEquals(Set.of(enemyTwo), clan.currentEnemyClans(),
                        "Clan should still have one enemy remaining");
                assertEquals(Set.of(enemyTwo), dataCenterInfo.transact(clan::getEnemyClans),
                        "Clan should still have one enemy remaining");
            });
        }

        boolean removedTwo = dataCenterInfo.transact((tx) -> {
            return clan.removeClanAsEnemy(tx, enemyTwo);
        });
        assertTrue(removedTwo, "Removed clan as enemy");

        assertBeforeAndAfterCacheRefresh(() -> {
            assertEquals(Set.of(), clan.currentEnemyClans(),
                    "Clan should have no more enemies");
            assertEquals(Set.of(), dataCenterInfo.transact(clan::getEnemyClans),
                    "Clan should have no more enemies");
        });
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
