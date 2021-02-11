module gg.solarmc.loader.clans {
    exports gg.solarmc.loader.clans;

    requires transitive gg.solarmc.loader;
    requires gg.solarmc.loader.impl;
    requires com.github.benmanes.caffeine;

    provides gg.solarmc.loader.data.DataKeySpi with gg.solarmc.loader.clans.ClansKeySpi;
}