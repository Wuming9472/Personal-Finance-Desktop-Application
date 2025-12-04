package it.unicas.project.template.address.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Copia archiviata del vecchio test di DateUtil.
 * Non fa parte della suite di build principale.
 */
class DateUtilTest {

    @Test
    void formatHandlesNullDates() {
        assertNull(DateUtil.format(null), "La formattazione di una data null deve restituire null");
    }

    @Test
    void formatProducesExpectedPattern() {
        LocalDate sample = LocalDate.of(2024, 5, 10);
        assertEquals("10-05-2024", DateUtil.format(sample));
    }

    @Test
    void parseReturnsDateWhenPatternMatches() {
        LocalDate parsed = DateUtil.parse("01-12-2025");
        assertNotNull(parsed);
        assertEquals(LocalDate.of(2025, 12, 1), parsed);
    }

    @Test
    void parseReturnsNullOnInvalidValue() {
        assertNull(DateUtil.parse("invalid-date"));
        assertNull(DateUtil.parse("31-02-2024"));
    }

    @Test
    void validDateDelegatesToParse() {
        assertTrue(DateUtil.validDate("15-03-2023"));
        assertFalse(DateUtil.validDate("99-99-9999"));
    }
}
