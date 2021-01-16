package gg.solarmc.loader.kitpvp;

import gg.solarmc.loader.DataLoader;
import gg.solarmc.loader.Transaction;

import java.math.BigDecimal;
import java.util.HashSet;

public class KitPvpLoader implements DataLoader<KitPvp> {

    @Override
    public KitPvp createDefaultData() {
        return new KitPvp(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,new HashSet<>());
    }

    @Override
    public KitPvp loadData(Transaction transaction, int userId) {
        // SELECT * FROM pvpstats WHERE user_id = ?
        //TODO improve query

        return null;
    }

}
