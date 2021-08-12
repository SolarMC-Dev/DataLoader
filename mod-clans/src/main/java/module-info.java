module gg.solarmc.loader.clans {
    requires transitive gg.solarmc.loader;
    requires gg.solarmc.loader.impl;
    requires gg.solarmc.streamer;
    requires static org.checkerframework.checker.qual;
    exports gg.solarmc.loader.clans;
    provides gg.solarmc.loader.data.DataKeySpi with gg.solarmc.loader.clans.ClansKeySpi;
}