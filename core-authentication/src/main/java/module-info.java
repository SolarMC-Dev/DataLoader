module gg.solarmc.loader.authentication {
	exports gg.solarmc.loader.authentication;
	exports gg.solarmc.loader.authentication.internal to gg.solarmc.loader.authentication.test;

	requires transitive gg.solarmc.loader;
	requires gg.solarmc.loader.impl;
	requires de.mkammerer.argon2.nolibs;
	requires org.slf4j;
	requires space.arim.omnibus;
}