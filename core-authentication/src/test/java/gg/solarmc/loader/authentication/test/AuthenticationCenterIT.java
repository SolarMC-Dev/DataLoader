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

package gg.solarmc.loader.authentication.test;

import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.authentication.AuthenticationCenter;
import gg.solarmc.loader.authentication.AutoLoginResult;
import gg.solarmc.loader.authentication.CompleteLoginResult;
import gg.solarmc.loader.authentication.CreateAccountResult;
import gg.solarmc.loader.authentication.LoginAttempt;
import gg.solarmc.loader.authentication.User;
import gg.solarmc.loader.authentication.VerifiablePassword;
import gg.solarmc.loader.authentication.internal.UUIDOperations;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class AuthenticationCenterIT {

    private AuthenticationCenter authCenter;
    private DataCenterInfo dataCenterInfo;

    @BeforeEach
    public void setupCenters(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        authCenter = AuthenticationCenter.create();
        dataCenterInfo = DataCenterInfo.create(folder, credentials);
    }

    private Player premiumPlayer(String name) {
        return new Player(new User(UUID.randomUUID(), name));
    }

    private User crackedUser(String name) {
        return new User(UUIDOperations.computeOfflineUuid(name), name);
    }

    private Player crackedPlayer(String name) {
        return new Player(crackedUser(name));
    }

    private <T> T transact(DataCenter.TransactionActor<T> actor) {
        return dataCenterInfo.dataCenter().transact(actor).join();
    }

    private void runTransact(DataCenter.TransactionRunner runner) {
        dataCenterInfo.dataCenter().runTransact(runner).join();
    }

    // Completely new user
    @Test
    public void newUserNotFound() {
        AutoLoginResult autoLoginResult = transact((tx) -> {
            return authCenter.findExistingUserWithName(tx, "A248");
        });
        assertEquals(AutoLoginResult.ResultType.NONE_FOUND, autoLoginResult.resultType());
        assertThrows(IllegalStateException.class, autoLoginResult::userDetails);
        assertThrows(IllegalStateException.class, autoLoginResult::verifiablePassword);
    }

    // New user who is premium according to the Mojang API
    @Test
    public void finishLoginPremium() {
        Player player = premiumPlayer("A248");
        LoginAttempt loginAttempt = transact((tx) -> {
            return authCenter.attemptLoginOfIdentifiedUser(tx, player);
        });
        assertEquals(LoginAttempt.ResultType.PREMIUM_PERMITTED, loginAttempt.resultType());
        assertThrows(IllegalStateException.class, loginAttempt::verifiablePassword);
        assertFalse(player.isDataLoaded());
    }

    // New user who is not premium according to the Mojang API
    @Test
    public void finishLoginCracked() {
        Player crackedPlayer = crackedPlayer("A248");
        LoginAttempt loginAttempt = transact((tx) ->  authCenter.attemptLoginOfIdentifiedUser(tx, crackedPlayer));
        assertEquals(LoginAttempt.ResultType.NEEDS_ACCOUNT, loginAttempt.resultType());
        assertThrows(IllegalStateException.class, loginAttempt::verifiablePassword);
    }

    // Premium user who logs in successfully and then re-joins
    @Test
    public void premiumLoginAndRelogin() {
        String username = "A248";
        Player player = premiumPlayer(username);
        LoginAttempt loginAttempt = transact((tx) -> {
            return authCenter.attemptLoginOfIdentifiedUser(tx, player);
        });
        assertEquals(LoginAttempt.ResultType.PREMIUM_PERMITTED, loginAttempt.resultType());

        AutoLoginResult autoLoginResult = transact((tx) -> authCenter.findExistingUserWithName(tx, username));
        assertEquals(AutoLoginResult.ResultType.PREMIUM, autoLoginResult.resultType());
        assertEquals(player.user(), autoLoginResult.userDetails());
        assertThrows(IllegalStateException.class, autoLoginResult::verifiablePassword);
    }

    // Premium user whose name, during the login process, is taken by a cracked user
    // Premium user subsequently migrates cracked user's account
    // The cracked user then cannot log in
    // This test covers race conditions galore
    @Test
    public void premiumLoginWithExistingNameOfCrackedUser() {
        String username = "A248";
        Player player = premiumPlayer(username);
        Player crackedPlayer = crackedPlayer(username);
        VerifiablePassword password = authCenter.hashNewPassword("passcode");
        assertEquals(
                CreateAccountResult.CREATED,
                transact((tx) -> authCenter.createAccount(tx, crackedPlayer, password)));

        LoginAttempt loginAttempt = transact((tx) -> authCenter.attemptLoginOfIdentifiedUser(tx, player));
        assertEquals(LoginAttempt.ResultType.NEEDS_PASSWORD, loginAttempt.resultType());
        assertEquals(password, loginAttempt.verifiablePassword());

        // Now that the premium user has entered the password correctly, it is technically
        // possible for them to migrate the cracked account to premium
        CompleteLoginResult premiumLoginResult = transact((tx) -> {
            return authCenter.completeLoginAndPossiblyMigrate(tx, player);
        });
        assertEquals(CompleteLoginResult.MIGRATED_TO_PREMIUM, premiumLoginResult);

        // Assume cracked player now quits. While re-joining, their account is migrated by the premium user
        CompleteLoginResult crackedLoginResult= transact((tx) -> {
            return authCenter.completeLoginAndPossiblyMigrate(tx, crackedPlayer);
        });
        assertEquals(CompleteLoginResult.USER_ID_MISSING, crackedLoginResult);
    }

    // Premium user whose name, during the login process, is taken by a cracked user
    // with a name which is different in case
    @Test
    public void premiumLoginWithExistingCaseInsensitiveNameOfCrackedUser() {
        String username = "A248";
        Player player = premiumPlayer(username);
        Player crackedPlayer = crackedPlayer("a248");
        VerifiablePassword password = authCenter.hashNewPassword("passcode");
        assertEquals(
                CreateAccountResult.CREATED,
                transact((tx) -> authCenter.createAccount(tx, crackedPlayer, password)));

        LoginAttempt loginAttempt = transact((tx) -> {
            return authCenter.attemptLoginOfIdentifiedUser(tx, player);
        });
        assertEquals(LoginAttempt.ResultType.DENIED_CASE_SENSITIVITY_OF_NAME, loginAttempt.resultType());
    }

    // Cracked user's name is taken, during the Mojang API lookup, by a premium user
    @Test
    public void crackedNameTakenByExistingPremium() {
        String username = "A248";
        Player player = premiumPlayer(username);
        Player crackedPlayer = crackedPlayer(username);

        // Premium player logs in before cracked player can finish login
        LoginAttempt premiumLoginAttempt = transact((tx) -> authCenter.attemptLoginOfIdentifiedUser(tx, player));
        assertEquals(LoginAttempt.ResultType.PREMIUM_PERMITTED, premiumLoginAttempt.resultType());

        LoginAttempt crackedLoginAttempt = transact((tx) -> authCenter.attemptLoginOfIdentifiedUser(tx, crackedPlayer));
        assertEquals(LoginAttempt.ResultType.DENIED_PREMIUM_TOOK_NAME, crackedLoginAttempt.resultType());
    }

    // Cracked players with same names ignoring case but different case
    @Test
    public void caseSensitivityOfCrackedNames() {
        String usernameOne = "a248";
        String usernameTwo = "A248";
        Player playerOne = crackedPlayer(usernameOne);
        VerifiablePassword password = authCenter.hashNewPassword("passcode");
        assertEquals(
                CreateAccountResult.CREATED,
                transact((tx) -> authCenter.createAccount(tx, playerOne, password)));

        AutoLoginResult autoLoginResult = transact((tx) -> authCenter.findExistingUserWithName(tx, usernameTwo));
        assertEquals(AutoLoginResult.ResultType.DENIED_CASE_SENSITIVITY_OF_NAME, autoLoginResult.resultType());
    }

    // Cracked user who creates an account and then re-joins
    @Test
    public void crackedUserCreateAccountAndRelogin() {
        String username = "Aesthetik";
        VerifiablePassword password = authCenter.hashNewPassword("passcode");
        int userId;
        {
            Player crackedPlayerOriginal = crackedPlayer(username);
            assertEquals(
                    CreateAccountResult.CREATED,
                    transact((tx) -> authCenter.createAccount(tx, crackedPlayerOriginal, password)));
            assertTrue(crackedPlayerOriginal.isDataLoaded());
            userId = crackedPlayerOriginal.getLoadedUserId();
        }

        // Re-login
        AutoLoginResult autoLoginResult = transact((tx) -> authCenter.findExistingUserWithName(tx, username));
        assertEquals(AutoLoginResult.ResultType.CRACKED, autoLoginResult.resultType());
        assertEquals(crackedUser(username), autoLoginResult.userDetails());
        assertEquals(password, autoLoginResult.verifiablePassword());
        // Assume password entered correctly
        Player crackedPlayerRelogin = crackedPlayer(username);
        CompleteLoginResult loginResult = transact((tx) -> {
            return authCenter.completeLoginAndPossiblyMigrate(tx, crackedPlayerRelogin);
        });
        assertEquals(CompleteLoginResult.NORMAL, loginResult);
        assertTrue(crackedPlayerRelogin.isDataLoaded());
        assertEquals(userId, crackedPlayerRelogin.getLoadedUserId());
    }

    // Cracked user who desires migration then re-joins as premium
    @Test
    public void crackedUserCreateAccountDesiresMigrateAndReloginPremiumMigration() {
        String username = "Aesthetik";
        VerifiablePassword password = authCenter.hashNewPassword("passcode");
        int userId;
        {
            Player crackedPlayerOriginal = crackedPlayer(username);
            assertEquals(
                    CreateAccountResult.CREATED,
                    transact((tx) -> authCenter.createAccount(tx, crackedPlayerOriginal, password)));
            assertTrue(crackedPlayerOriginal.isDataLoaded());
            userId = crackedPlayerOriginal.getLoadedUserId();
            runTransact((tx) -> authCenter.desiresAccountMigration(tx, crackedPlayerOriginal.user()));
        }

        // Re-login
        AutoLoginResult autoLoginResult = transact((tx) -> authCenter.findExistingUserWithName(tx, username));
        assertEquals(AutoLoginResult.ResultType.CRACKED_BUT_DESIRES_MIGRATION, autoLoginResult.resultType());
        assertEquals(crackedUser(username), autoLoginResult.userDetails());
        assertEquals(password, autoLoginResult.verifiablePassword());
        // Assume password entered correctly
        Player crackedPlayerNowPremium = premiumPlayer(username);
        CompleteLoginResult loginResult = transact((tx) -> {
            return authCenter.completeLoginAndPossiblyMigrate(tx, crackedPlayerNowPremium);
        });
        assertEquals(CompleteLoginResult.MIGRATED_TO_PREMIUM, loginResult);
        assertTrue(crackedPlayerNowPremium.isDataLoaded());
        assertEquals(userId, crackedPlayerNowPremium.getLoadedUserId());
    }

}
