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

import gg.solarmc.loader.Transaction;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of transaction. Produced in {@link TransactionSource}
 */
public class SQLTransaction implements Transaction, AutoCloseable {

    private final Connection connection;

    SQLTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void markReadOnly() {
        try {
            connection.setReadOnly(true);
        } catch (SQLException ex) {
            throw handler().handle(ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(Class<T> propertyClass) {
        if (propertyClass.equals(Connection.class)) {
            return (T)connection;
        }
        if (propertyClass.equals(DSLContext.class)) {
            return (T) DSL.using(connection, SQLDialect.MARIADB);
        }
        if (propertyClass.equals(SQLExceptionHandler.class)) {
            return (T) handler();
        }
        throw new IllegalArgumentException(
                "Transaction implementation SQLTransaction does not provide property of " + propertyClass.getName());
    }

    private SQLExceptionHandler handler() {
        return new SQLExceptionHandler();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
