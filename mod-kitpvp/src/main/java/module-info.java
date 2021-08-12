module gg.solarmc.loader.kitpvp {
	requires com.github.benmanes.caffeine;
	requires transitive gg.solarmc.loader;
	requires gg.solarmc.loader.impl;
	requires static org.checkerframework.checker.qual;
	exports gg.solarmc.loader.kitpvp;
	uses gg.solarmc.loader.kitpvp.ItemSerializer;
	provides gg.solarmc.loader.data.DataKeySpi with gg.solarmc.loader.kitpvp.KitPvpKeySpi;
}