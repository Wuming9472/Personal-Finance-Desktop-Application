package org.junit.jupiter.api;

import java.util.Objects;

/**
 * Utility di asserzione minimale per non dipendere da JUnit esterno.
 */
public final class Assertions {

    private Assertions() { }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, "Expected condition to be true");
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, "Expected condition to be false");
    }

    public static <T> void assertEquals(T expected, T actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    public static <T> void assertEquals(T expected, T actual) {
        assertEquals(expected, actual, "Values are not equal");
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    public static void assertEquals(double expected, double actual, double delta) {
        assertEquals(expected, actual, delta, "Values are not equal within delta " + delta);
    }

    public static void assertNull(Object value, String message) {
        if (value != null) {
            throw new AssertionError(message + " (was: " + value + ")");
        }
    }

    public static void assertNull(Object value) {
        assertNull(value, "Expected value to be null");
    }

    public static void assertNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    public static void assertNotNull(Object value) {
        assertNotNull(value, "Expected value to be not null");
    }
}
