open module gg.solarmc.loader.impl.test {
    requires gg.solarmc.loader.impl;

    exports gg.solarmc.loader.impl.test;
    exports gg.solarmc.loader.impl.test.extension;

    requires transitive org.junit.jupiter.api;
    requires org.mockito;
    requires org.mockito.junit.jupiter;
    requires net.bytebuddy; // required by mockito
    requires exec;
    requires mariaDB4j.core;
}