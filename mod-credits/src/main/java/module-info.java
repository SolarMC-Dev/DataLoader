module gg.solarmc.loader.credits {
	exports gg.solarmc.loader.credits;

	requires transitive gg.solarmc.loader;
	requires gg.solarmc.loader.impl;
	requires space.arim.dazzleconf.ext.snakeyaml;

	provides gg.solarmc.loader.data.DataKeySpi with gg.solarmc.loader.credits.CreditsKeySpi;
}