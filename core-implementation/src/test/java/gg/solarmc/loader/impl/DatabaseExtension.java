/*
 * DataLoader
 * Copyright © 2021 SolarMC Developers
 *
 * DataLoader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DataLoader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DataLoader. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */

package gg.solarmc.loader.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseExtension implements ParameterResolver {

    private static final AtomicInteger dbNameCounter = new AtomicInteger();
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DatabaseExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        return type.equals(SolarDataConfig.DatabaseCredentials.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return computeCredentials(extensionContext);
    }

    private SolarDataConfig.DatabaseCredentials computeCredentials(ExtensionContext extensionContext) {
        var rootStore = extensionContext.getRoot().getStore(NAMESPACE);
        ClosableMariaDb database = rootStore.getOrComputeIfAbsent(
                Resource.DB,
                (k) -> ClosableMariaDb.createDb(),
                ClosableMariaDb.class);

        int databasePort = database.db().getConfiguration().getPort();
        var contextStore = extensionContext.getStore(NAMESPACE);
        return contextStore.getOrComputeIfAbsent(
                Resource.CREDENTIALS,
                (k) -> createCredentials(databasePort),
                SolarDataConfig.DatabaseCredentials.class);
    }

    private enum Resource {
        DB,
        CREDENTIALS
    }

    private SolarDataConfig.DatabaseCredentials createCredentials(int port) {
        String databaseName = "dataloader_test_" + dbNameCounter.getAndIncrement();
        String username = "root";
        String password = "";
        createDatabase(port, databaseName);

        SolarDataConfig.DatabaseCredentials credentials = mock(SolarDataConfig.DatabaseCredentials.class);
        when(credentials.connectionProperties()).thenReturn(Map.of());
        when(credentials.connectionTimeoutSeconds()).thenReturn(30);
        when(credentials.maxLifetimeMinutes()).thenReturn(30);
        when(credentials.database()).thenReturn(databaseName);
        when(credentials.username()).thenReturn(username);
        when(credentials.password()).thenReturn(password);
        when(credentials.host()).thenReturn("localhost");
        when(credentials.port()).thenReturn(port);
        when(credentials.poolSize()).thenReturn(2);
        return credentials;
    }

    private static void createDatabase(int port, String database) {
        try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:" + port + '/', "root", "");
             PreparedStatement createDatabase = conn.prepareStatement("CREATE DATABASE " + database
                     + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
             PreparedStatement createTablesOne = conn.prepareStatement("""
                     CREATE TABLE `%db%`.`libertybans_names` (
                     `uuid` BINARY(16) NOT NULL,
                     `name` VARCHAR(16) NOT NULL,
                     `updated` BIGINT NOT NULL,
                     PRIMARY KEY (`uuid`, `name`))""".replace("%db%", database));
             PreparedStatement createTablesTwo = conn.prepareStatement("""
                     CREATE TABLE `%db%`.`libertybans_addresses` (
                     `uuid` BINARY(16) NOT NULL,
                     `address` VARBINARY(16) NOT NULL,
                     `updated` BIGINT NOT NULL,
                     PRIMARY KEY (`uuid`, `address`))""".replace("%db%", database))) {

            createDatabase.execute();
            createTablesOne.execute();
            createTablesTwo.execute();
        } catch (SQLException ex) {
            throw Assertions.<RuntimeException>fail(ex);
        }
    }
}
