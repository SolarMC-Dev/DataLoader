module gg.solarmc.loader.impl {
	requires com.zaxxer.hikari;
	requires transitive gg.solarmc.loader.schema;
	requires transitive gg.solarmc.loader;
	requires transitive java.sql;
	requires org.flywaydb.core;
	requires org.slf4j;
	requires space.arim.dazzleconf.ext.snakeyaml;
	requires transitive space.arim.omnibus;

	exports gg.solarmc.loader.impl;
	opens gg.solarmc.loader.impl to space.arim.dazzleconf;
	uses gg.solarmc.loader.data.DataKeySpi;
}