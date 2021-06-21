package gg.solarmc.loader.impl.test.extension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

record SqlModes(String sqlMode, String oldMode) {

    static SqlModes readSqlModes() {
        URL sqlModesResource = SqlModes.class.getResource("/sql-modes.txt");
        Objects.requireNonNull(sqlModesResource, "Resource missing");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream input = sqlModesResource.openStream()) {
            input.transferTo(output);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        String[] modes = output.toString(StandardCharsets.UTF_8).split("\n", -1);
        return new SqlModes(modes[0], modes[1]);
    }
}
