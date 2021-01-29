module gg.solarmc.loader.kitpvp {
	exports gg.solarmc.loader.kitpvp;

	requires transitive gg.solarmc.loader;
	requires gg.solarmc.loader.impl;
	requires com.github.benmanes.caffeine;

	provides gg.solarmc.loader.data.DataKeySpi with gg.solarmc.loader.kitpvp.KitPvpKeySpi;
}