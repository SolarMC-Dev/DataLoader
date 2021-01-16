package gg.solarmc.loader.kitpvp;

import java.math.BigDecimal;
import java.util.HashSet;

public class KitPvp {

    private volatile BigDecimal kills;
    private volatile BigDecimal deaths;
    private volatile BigDecimal assists;

    private HashSet<Kit> ownedKits; //Correct me if i'm wrong here

    public KitPvp(BigDecimal kills, BigDecimal deaths, BigDecimal assists, HashSet<Kit> kits) {
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.ownedKits = kits;
    }

}
