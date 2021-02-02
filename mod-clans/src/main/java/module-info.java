module gg.solarmc.loader.clans {
    exports gg.solarmc.loader.clans;

    requires transitive gg.solarmc.loader;
    requires gg.solarmc.loader.impl;

    provides gg.solarmc.loader.data.DataKeySpi with gg.solarmc.loader.clans.ClansKeySpi;
}