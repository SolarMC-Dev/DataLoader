package gg.solarmc.loader.impl.test.extension;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DataGeneratorTest {

    @RepeatedTest(10)
    public void randomIntegerBetween() {
        int min = 2;
        int max = 5;
        int number = DataGenerator.randomIntegerBetween(min, max);
        assertFalse(number > max, () -> "" + number);
        assertFalse(number < min, () -> "" + number);
    }

    @Test
    public void randomIntegerBetweenOneResult() {
        assertEquals(5, DataGenerator.randomIntegerBetween(5, 5));
    }
}
