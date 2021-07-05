package gg.solarmc.loader.credits.test;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.credits.CreditsKey;
import gg.solarmc.loader.credits.DepositResult;
import gg.solarmc.loader.credits.OnlineCredits;
import gg.solarmc.loader.credits.WithdrawResult;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.file.Path;

import static gg.solarmc.loader.impl.test.extension.DataGenerator.randomNegativeInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class CreditsIT {

    private DataCenterInfo dataCenterInfo;
    private OnlineCredits data;

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials) {
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).build();
        OnlineSolarPlayer player = dataCenterInfo.loginNewRandomUser();
        data = player.getData(CreditsKey.INSTANCE);
    }

    private static void assertEqualDecimals(BigDecimal value1, BigDecimal value2) {
        //noinspection SimplifiableAssertion
        assertTrue(value1.compareTo(value2) == 0, () -> "Expected " + value1 + " but got " + value2);
    }

    @Test
    public void withdrawBalance() {
        BigDecimal previousBalance = data.currentBalance();
        BigDecimal withdrawal = BigDecimal.TEN;
        assertTrue(previousBalance.compareTo(withdrawal) > 0);

        WithdrawResult withdrawResult = dataCenterInfo.transact((tx) -> {
            return data.withdrawBalance(tx, withdrawal);
        });
        assertTrue(withdrawResult.isSuccessful());
        assertEqualDecimals(previousBalance.subtract(withdrawal), withdrawResult.newBalance());
        assertEquals(withdrawResult.newBalance(), data.currentBalance());
    }

    @Test
    public void withdrawBalanceUnsuccessful() {
        BigDecimal previousBalance = data.currentBalance();
        BigDecimal withdrawal = BigDecimal.valueOf(Integer.MAX_VALUE);
        assertTrue(withdrawal.compareTo(previousBalance) > 0);

        WithdrawResult withdrawResult = dataCenterInfo.transact((tx) -> {
            return data.withdrawBalance(tx, withdrawal);
        });
        assertFalse(withdrawResult.isSuccessful());
        assertEqualDecimals(previousBalance, withdrawResult.newBalance());
        assertEquals(withdrawResult.newBalance(), data.currentBalance());
    }

    @Test
    public void withdrawBalancePreconditions(@Mock Transaction tx) {
        assertThrows(IllegalArgumentException.class,
                () -> data.withdrawBalance(tx, BigDecimal.valueOf(randomNegativeInteger())));
    }

    @Test
    public void depositBalance() {
        BigDecimal previousBalance = data.currentBalance();
        BigDecimal deposit = BigDecimal.TEN;

        DepositResult depositResult = dataCenterInfo.transact((tx) -> {
            return data.depositBalance(tx, deposit);
        });
        assertEqualDecimals(previousBalance.add(deposit), depositResult.newBalance());
        assertEquals(depositResult.newBalance(), data.currentBalance());
    }

    @Test
    public void depositBalancePreconditions(@Mock Transaction tx) {
        assertThrows(IllegalArgumentException.class,
                () -> data.depositBalance(tx, BigDecimal.valueOf(randomNegativeInteger())));
    }

    @Test
    public void setBalance() {
        BigDecimal balance = BigDecimal.valueOf(13.312);
        dataCenterInfo.runTransact((tx) -> {
            data.setBalance(tx, balance);
        });
        assertEquals(balance, data.currentBalance());
    }

    @Test
    public void setBalancePreconditions(@Mock Transaction tx) {
        assertThrows(IllegalArgumentException.class,
                () -> data.setBalance(tx, BigDecimal.valueOf(randomNegativeInteger())));
    }

}
