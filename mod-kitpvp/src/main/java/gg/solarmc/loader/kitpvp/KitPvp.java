package gg.solarmc.loader.kitpvp;

import gg.solarmc.loader.data.DataObject;

import java.math.BigDecimal;
import java.util.Set;

public class KitPvp implements DataObject {

    private volatile BigDecimal kills;
    private volatile BigDecimal deaths;
    private volatile BigDecimal assists;

    private Set<Kit> ownedKits;

    public KitPvp(BigDecimal kills, BigDecimal deaths, BigDecimal assists, Set<Kit> kits) {
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.ownedKits = kits;
    }

    public

}
