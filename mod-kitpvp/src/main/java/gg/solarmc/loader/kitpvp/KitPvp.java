package gg.solarmc.loader.kitpvp;

import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.data.DataObject;
import gg.solarmc.loader.impl.SQLTransaction;
import gg.solarmc.loader.schema.tables.records.KitpvpStatisticsRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import static gg.solarmc.loader.schema.tables.KitpvpStatistics.*;

public class KitPvp implements DataObject {

    private volatile BigDecimal kills;
    private volatile BigDecimal deaths;
    private volatile BigDecimal assists;

    private final int userID;

    private Set<Kit> ownedKits;

    public KitPvp(int userID, BigDecimal kills, BigDecimal deaths, BigDecimal assists, Set<Kit> kits) {
        this.userID = userID;

        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.ownedKits = kits;
    }

    /**
     * Cached kills. Not reliable.
     * @return kills
     */
    public BigDecimal currentKills() {
        return kills;
    }

    /**
     * Cached deaths. Not reliable.
     * @return deaths
     */
    public BigDecimal currentDeaths() {
        return deaths;
    }

    /**
     * Cached assists. Not reliable
     * @return assists
     */
    public BigDecimal currentAssists() {
        return assists;
    }

    private KitpvpStatisticsRecord getStatistics(Transaction transaction) {
        DSLContext context = ((SQLTransaction) transaction).jooq();

        KitpvpStatisticsRecord record = context.fetchOne(KITPVP_STATISTICS,KITPVP_STATISTICS.USER_ID.eq(userID));
        Objects.requireNonNull(record,"kitPVP data is null! Call the police!");

        return record;
    }

}
