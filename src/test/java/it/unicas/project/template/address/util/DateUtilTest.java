package it.unicas.project.template.address.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class DateUtilTest {

    @Test
    public void formatsDateUsingExpectedPattern() {
        LocalDate date = LocalDate.of(2024, 12, 25);
        String formatted = DateUtil.format(date);
        Assertions.assertEquals("25-12-2024", formatted, "Il formato dd-MM-yyyy deve essere rispettato");
    }

    @Test
    public void formatReturnsNullWhenDateIsNull() {
        Assertions.assertNull(DateUtil.format(null), "Una data nulla deve restituire null");
    }

    @Test
    public void parsesValidDateString() {
        LocalDate parsed = DateUtil.parse("01-03-2025");
        Assertions.assertNotNull(parsed, "Una stringa valida deve essere convertita");
        Assertions.assertEquals(LocalDate.of(2025, 3, 1), parsed, "La data risultante deve essere corretta");
    }

    @Test
    public void parseReturnsNullForInvalidDate() {
        Assertions.assertNull(DateUtil.parse("invalid"), "Una stringa non valida deve restituire null");
    }

    @Test
    public void validDateDelegatesToParse() {
        Assertions.assertTrue(DateUtil.validDate("10-10-2030"));
        Assertions.assertFalse(DateUtil.validDate("32-99-2030"));
    }
}
