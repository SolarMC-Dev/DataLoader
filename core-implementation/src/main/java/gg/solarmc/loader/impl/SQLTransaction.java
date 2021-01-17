package gg.solarmc.loader.impl;

import gg.solarmc.loader.Transaction;

import java.sql.Connection;
//TODO: Implement this - A248
public class SQLTransaction implements Transaction {

    public Connection getConnection() {
        return null;
    }

    @Override
    public void markReadOnly() {
        return;
    }
}
