package gg.solarmc.loader.kitpvp.test;

import gg.solarmc.loader.OnlineSolarPlayer;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.impl.SolarDataConfig;
import gg.solarmc.loader.impl.test.extension.DataCenterInfo;
import gg.solarmc.loader.impl.test.extension.DatabaseExtension;
import gg.solarmc.loader.kitpvp.ItemSerializer;
import gg.solarmc.loader.kitpvp.KitPvp;
import gg.solarmc.loader.kitpvp.KitPvpKey;
import gg.solarmc.loader.kitpvp.OnlineKitPvp;
import gg.solarmc.loader.kitpvp.StatisticResult;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.DefaultOmnibus;
import space.arim.omnibus.Omnibus;

import java.nio.file.Path;

import static gg.solarmc.loader.impl.test.extension.DataGenerator.randomNegativeInteger;
import static gg.solarmc.loader.impl.test.extension.DataGenerator.randomPositiveInteger;
import static gg.solarmc.loader.schema.tables.KitpvpStatistics.KITPVP_STATISTICS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(DatabaseExtension.class)
@ExtendWith(MockitoExtension.class)
public class KitPvpStatisticsIT {

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
        data = user.getData(KitPvpKey.INSTANCE);
        userId = user.getUserId();
    }

    @ParameterizedTest
    @EnumSource(SimpleStatType.class)
    public void addStatistic(SimpleStatType simpleStatType) {
        int currentAmount = 0;
        assertEquals(currentAmount, simpleStatType.cachedValue(data));
        assertEquals(currentAmount, simpleStatType.fetchValue(dataCenterInfo, userId));

        for (int n = 0; n < 3; n++) {
            int previousAmount = currentAmount;
            int delta = randomPositiveInteger();
            currentAmount += delta;

            StatisticResult statResult = simpleStatType.addAmount(data, dataCenterInfo, delta);
            assertEquals(currentAmount, statResult.newValue());
            assertEquals(previousAmount, statResult.oldValue());
            assertEquals(currentAmount, simpleStatType.cachedValue(data));
            assertEquals(currentAmount, simpleStatType.fetchValue(dataCenterInfo, userId));
        }
    }

    @ParameterizedTest
    @EnumSource(SimpleStatType.class)
    public void addStatisticPreconditions(SimpleStatType simpleStatType) {
        int amount = randomNegativeInteger();
        dataCenterInfo.runTransact((tx) -> {
            assertThrows(IllegalArgumentException.class, () -> simpleStatType.addAmount(data, tx, amount));
        });
    }

    private enum SimpleStatType {
        KILLS,
        DEATHS,
        ASSISTS,
        EXPERIENCE;

        StatisticResult addAmount(KitPvp data, DataCenterInfo dataCenterInfo, int amount) {
            return dataCenterInfo.transact((tx) -> {
                return addAmount(data, tx, amount);
            });
        }

        StatisticResult addAmount(KitPvp data, Transaction tx, int amount) {
            return switch (this) {
                case KILLS -> data.addKills(tx, amount);
                case DEATHS -> data.addDeaths(tx, amount);
                case ASSISTS -> data.addAssists(tx, amount);
                case EXPERIENCE -> data.addExperience(tx, amount);
            };
        }

        int cachedValue(OnlineKitPvp data) {
            return switch (this) {
                case KILLS -> data.currentKills();
                case DEATHS -> data.currentDeaths();
                case ASSISTS -> data.currentAssists();
                case EXPERIENCE -> data.currentExperience();
            };
        }

        int fetchValue(DataCenterInfo dataCenterInfo, int userId) {
            return dataCenterInfo.transact((tx) -> fetchValue(tx, userId));
        }

        private int fetchValue(Transaction tx, int userId) {
            Field<Integer> columnField = switch (this) {
                case KILLS -> KITPVP_STATISTICS.KILLS;
                case DEATHS -> KITPVP_STATISTICS.DEATHS;
                case ASSISTS -> KITPVP_STATISTICS.ASSISTS;
                case EXPERIENCE -> KITPVP_STATISTICS.EXPERIENCE;
            };
            return tx.getProperty(DSLContext.class)
                    .select(columnField).from(KITPVP_STATISTICS)
                    .where(KITPVP_STATISTICS.USER_ID.eq(userId))
                    .fetchSingle().value1();
        }
    }

}
