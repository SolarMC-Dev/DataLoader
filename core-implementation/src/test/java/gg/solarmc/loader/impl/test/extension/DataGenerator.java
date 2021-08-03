package gg.solarmc.loader.impl.test.extension;

import gg.solarmc.loader.impl.UserDetails;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Helps generate random data
 *
 */
public final class DataGenerator {

    private DataGenerator() {}

    private static String randomUsername() {
        var tlr = ThreadLocalRandom.current();
        char[] nameChars = new char[tlr.nextInt(1, 16)];
        for (int n = 0; n < nameChars.length; n++) {
            nameChars[n] = (char) (tlr.nextInt(26) + 'a');
        }
        return String.valueOf(nameChars);
    }

    /**
     * Generates an IPv4 or IPv6 address
     *
     * @return a random address
     */
    public static InetAddress randomAddress() {
        var tlr = ThreadLocalRandom.current();
        byte[] addressBytes = new byte[(tlr.nextBoolean() ? 4 : 16)];
        tlr.nextBytes(addressBytes);
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates a new user with random details
     *
     * @return a new random user
     */
    public static UserDetails newRandomUser() {
        return new UserDetails(UUID.randomUUID(), randomUsername(), randomAddress());
    }

    /**
     * Generates random bytes
     *
     * @param minLength the minimum length of the array
     * @param maxLength the maximum length of the array
     * @return the bytes
     */
    public static byte[] randomBytes(int minLength, int maxLength) {
        if (minLength < 0) {
            throw new IllegalArgumentException("Min length cannot be less than 0");
        }
        var tlr = ThreadLocalRandom.current();
        byte[] bytes = new byte[tlr.nextInt(minLength, maxLength)];
        tlr.nextBytes(bytes);
        return bytes;
    }

    /**
     * Gets a random positive integer
     *
     * @return the random integer
     */
    public static int randomPositiveInteger() {
        // Use less than MAX_VALUE to allow for some further addition
        return ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE / 4) + 1;
    }

    /**
     * Gets a random negative integer
     *
     * @return the random integer
     */
    public static int randomNegativeInteger() {
        return ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, 0);
    }

    /**
     * Gets a random integer between the specified values
     *
     * @param minimum the minimum value, inclusive
     * @param maximum the maximum value, inclusive
     * @return the random integer
     */
    public static int randomIntegerBetween(int minimum, int maximum) {
        if (maximum < minimum) {
            throw new IllegalArgumentException("Maximum must be equal to or greater than minimum");
        }
        return 1 + ThreadLocalRandom.current().nextInt(minimum - 1, maximum);
    }

}
