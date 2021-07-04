package gg.solarmc.loader.impl.launch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

record SqlModes(String sqlMode) {

    static SqlModes readSqlModes() {
        URL sqlModesResource = SqlModes.class.getResource("/sql-modes.txt");
        Objects.requireNonNull(sqlModesResource, "Resource missing");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream input = sqlModesResource.openStream()) {
            input.transferTo(output);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return new SqlModes(output.toString(StandardCharsets.UTF_8));
    }

    String queryToSetModes() {
        return "SET @@SQL_MODE = CONCAT(@@SQL_MODE, '," + sqlMode() + "')";
    }
}
