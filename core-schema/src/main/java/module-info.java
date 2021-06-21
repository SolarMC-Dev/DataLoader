module gg.solarmc.loader.schema {

	requires transitive org.jooq;
	requires java.xml;

	exports gg.solarmc.loader.schema;
	exports gg.solarmc.loader.schema.tables;
	exports gg.solarmc.loader.schema.tables.records;
    exports gg.solarmc.loader.schema.routines;
}