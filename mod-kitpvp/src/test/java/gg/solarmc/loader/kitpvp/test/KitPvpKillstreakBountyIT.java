package gg.solarmc.loader.kitpvp.test;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import gg.solarmc.loader.kitpvp.ItemSerializer;
import gg.solarmc.loader.kitpvp.KitPvpKey;
import gg.solarmc.loader.kitpvp.OnlineKitPvp;
import gg.solarmc.loader.kitpvp.StatisticResult;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.Omnibus;

import java.math.BigDecimal;
import java.nio.file.Path;

import static gg.solarmc.loader.impl.test.extension.DataGenerator.randomIntegerBetween;
import static gg.solarmc.loader.impl.test.extension.DataGenerator.randomPositiveBigDecimal;
import static gg.solarmc.loader.impl.test.extension.DataGenerator.randomPositiveInteger;
import static gg.solarmc.loader.schema.tables.KitpvpStatistics.KITPVP_STATISTICS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class KitPvpKillstreakBountyIT {

    private DataCenterInfo dataCenterInfo;
    private int userId;
    private OnlineKitPvp data;

    @BeforeEach
    public void setDataCenter(@TempDir Path folder, SolarDataConfig.DatabaseCredentials credentials,
                              @Mock ItemSerializer itemSerializer) {
        Omnibus omnibus = new DefaultOmnibus();
        omnibus.getRegistry().register(ItemSerializer.class, (byte) 0, itemSerializer, "Serializer");
        dataCenterInfo = DataCenterInfo.builder(folder, credentials).omnibus(omnibus).build();
        OnlineSolarPlayer user = dataCenterInfo.loginNewRandomUser();
        userId = user.getUserId();
        data = user.getData(KitPvpKey.INSTANCE);
    }

    private void assertKillstreaks(int current, int highest) {
        Integer actualCurrentKillstreak = dataCenterInfo.transact((tx) -> {
            return tx.getProperty(DSLContext.class)
                    .select(KITPVP_STATISTICS.CURRENT_KILLSTREAK)
                    .from(KITPVP_STATISTICS)
                    .where(KITPVP_STATISTICS.USER_ID.eq(userId))
                    .fetchSingle().value1();
        });
        Integer actualHighestKillstreak = dataCenterInfo.transact((tx) -> {
            return tx.getProperty(DSLContext.class)
                    .select(KITPVP_STATISTICS.HIGHEST_KILLSTREAK)
                    .from(KITPVP_STATISTICS)
                    .where(KITPVP_STATISTICS.USER_ID.eq(userId))
                    .fetchSingle().value1();
        });
        assertEquals(current, data.currentCurrentKillstreaks());
        assertEquals(current, actualCurrentKillstreak);
        assertEquals(highest, data.currentHighestKillstreaks());
        assertEquals(highest, actualHighestKillstreak);
    }

    private void assertEqualDecimals(BigDecimal expected, BigDecimal actual) {
        assertTrue(expected.subtract(actual).compareTo(BigDecimal.ONE) < 0,
                "Expected " + expected + " but got " + actual);
    }

    @Test
    public void addKillstreak() {
        assertKillstreaks(0, 0);

        int amount = randomPositiveInteger();
        StatisticResult statResult = dataCenterInfo.transact((tx) -> {
            return data.addKillstreaks(tx, amount);
        });
        assertEquals(amount, statResult.newValue());
        assertEquals(0, statResult.oldValue());
        assertKillstreaks(amount, amount);
    }

    @Test
    public void doubleAddKillstreak() {
        assertKillstreaks(0, 0);

        int amount = randomPositiveInteger();
        StatisticResult statResult = dataCenterInfo.transact((tx) -> {
            return data.addKillstreaks(tx, amount);
        });
        int total = amount;
        assertEquals(total, statResult.newValue());
        assertEquals(0, statResult.oldValue());
        assertKillstreaks(total, total);

        int secondAmount = randomPositiveInteger();
        StatisticResult secondStatResult = dataCenterInfo.transact((tx) -> {
            return data.addKillstreaks(tx, secondAmount);
        });
        total += secondAmount;
        assertEquals(total, secondStatResult.newValue());
        assertEquals(amount, secondStatResult.oldValue());
        assertKillstreaks(total, total);
    }

    private static final int SHORTFALL = 2;
    @Test
    public void addThenResetThenReaddKillstreak() {
        int initialAmount = SHORTFALL + randomPositiveInteger();
        dataCenterInfo.runTransact((tx) -> {
            data.addKillstreaks(tx, initialAmount);
        });
        assertKillstreaks(initialAmount, initialAmount);

        int previousKillstreak = dataCenterInfo.transact((tx) -> {
            return data.resetCurrentKillstreaks(tx);
        });
        assertEquals(initialAmount, previousKillstreak);
        assertKillstreaks(0, initialAmount);

        int nextAmount = initialAmount - SHORTFALL;
        dataCenterInfo.runTransact((tx) -> {
            data.addKillstreaks(tx, nextAmount);
        });
        assertKillstreaks(nextAmount, initialAmount);

        int furtherAmount = randomIntegerBetween(SHORTFALL + 1, Integer.MAX_VALUE - nextAmount);
        dataCenterInfo.runTransact((tx) -> {
            data.addKillstreaks(tx, furtherAmount);
        });
        int newTotal = nextAmount + furtherAmount;
        assertKillstreaks(newTotal, newTotal);
    }

    private void assertBounty(BigDecimal bounty) {
        BigDecimal actualBounty = dataCenterInfo.transact((tx) -> {
            return tx.getProperty(DSLContext.class)
                    .select(KITPVP_STATISTICS.BOUNTY)
                    .from(KITPVP_STATISTICS)
                    .where(KITPVP_STATISTICS.USER_ID.eq(userId))
                    .fetchSingle().value1();
        });
        assertEqualDecimals(bounty, data.currentBounty());
        assertEqualDecimals(bounty, actualBounty);
    }

    @Test
    public void addBounty() {
        assertBounty(BigDecimal.ZERO);

        BigDecimal amount = randomPositiveBigDecimal();
        BigDecimal currentBounty = dataCenterInfo.transact((tx) -> {
            return data.addBounty(tx, amount);
        });
        assertEqualDecimals(amount, currentBounty);
        assertBounty(currentBounty);

        BigDecimal secondAmount = randomPositiveBigDecimal();
        currentBounty = dataCenterInfo.transact((tx) -> {
            return data.addBounty(tx, secondAmount);
        });
        assertEqualDecimals(amount.add(secondAmount), currentBounty);
        assertBounty(currentBounty);
    }

    @Test
    public void resetBounty() {
        assertBounty(BigDecimal.ZERO);

        BigDecimal amount = randomPositiveBigDecimal();
        BigDecimal currentBounty = dataCenterInfo.transact((tx) -> {
            return data.addBounty(tx, amount);
        });
        assertEqualDecimals(amount, currentBounty);
        assertBounty(currentBounty);

        BigDecimal previousBounty = dataCenterInfo.transact((tx) -> {
            return data.resetBounty(tx);
        });
        assertEqualDecimals(amount, previousBounty);
        assertBounty(BigDecimal.ZERO);
    }

    @Test
    public void getBounty() {
        assertBounty(BigDecimal.ZERO);

        BigDecimal bounty = randomPositiveBigDecimal();
        dataCenterInfo.runTransact((tx) -> {
            tx.getProperty(DSLContext.class)
                    .update(KITPVP_STATISTICS)
                    .set(KITPVP_STATISTICS.BOUNTY, bounty)
                    .where(KITPVP_STATISTICS.USER_ID.eq(userId))
                    .execute();
        });
        assertEqualDecimals(bounty, dataCenterInfo.transact(data::getBounty));
        assertBounty(bounty);
    }

}
