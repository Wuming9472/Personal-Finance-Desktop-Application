package it.unicas.project.template.address.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    @DisplayName("should format and parse roundtrip with default pattern")
    void formatAndParseRoundtrip() {
        LocalDate date = LocalDate.of(2024, 12, 25);
        String formatted = DateUtil.format(date);

        assertEquals("25-12-2024", formatted);
        assertEquals(date, DateUtil.parse(formatted));
    }

    @Test
    @DisplayName("should handle null and invalid inputs")
    void nullAndInvalidInputs() {
        assertNull(DateUtil.format(null));
        assertNull(DateUtil.parse("not-a-date"));
        assertFalse(DateUtil.validDate("31-02-2024"));
        assertTrue(DateUtil.validDate("01-01-2024"));
    }
}
