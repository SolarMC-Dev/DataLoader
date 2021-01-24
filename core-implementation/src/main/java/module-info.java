module gg.solarmc.loader.impl {
	requires transitive java.sql;
	requires transitive gg.solarmc.loader.schema;
	requires transitive gg.solarmc.loader;

	uses gg.solarmc.loader.data.DataKeySpi;

	requires org.slf4j;
	requires org.flywaydb.core;
	requires com.zaxxer.hikari;
	requires space.arim.dazzleconf.ext.snakeyaml;
}