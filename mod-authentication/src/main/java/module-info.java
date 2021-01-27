module gg.solarmc.loader.authentication {
	exports gg.solarmc.loader.authentication;

	requires transitive gg.solarmc.loader;
	requires gg.solarmc.loader.impl;
	requires de.mkammerer.argon2;

	provides gg.solarmc.loader.data.DataKeySpi with gg.solarmc.loader.authentication.AuthKeySpi;
}