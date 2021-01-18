package gg.solarmc.loader.impl;

import gg.solarmc.loader.Transaction;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLTransaction implements Transaction, AutoCloseable {

    private final Connection connection;

    SQLTransaction(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void markReadOnly() {
        try {
            connection.setReadOnly(true);
        } catch (SQLException ex) {
            throw rethrow(ex);
        }
    }

    /*
     * Useful for data module implementors
     */

    public DSLContext jooq() {
        return DSL.using(connection, SQLDialect.MARIADB);
    }

    public RuntimeException rethrow(SQLException cause) {
        return new UncheckedSQLException(cause);
    }

    public RuntimeException rethrow(String message, SQLException cause) {
        return new UncheckedSQLException(message, cause);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
