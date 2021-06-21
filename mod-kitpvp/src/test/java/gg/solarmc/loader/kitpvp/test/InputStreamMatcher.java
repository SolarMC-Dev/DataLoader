package gg.solarmc.loader.kitpvp.test;

import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

record InputStreamMatcher(byte[] data) implements ArgumentMatcher<InputStream> {

    @Override
    public boolean matches(InputStream inputStream) {
        if (inputStream == null) {
            return false;
        }
        try {
            return Arrays.equals(data, inputStream.readAllBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        return "InputStreamMatcher " + Arrays.toString(data);
    }
}
