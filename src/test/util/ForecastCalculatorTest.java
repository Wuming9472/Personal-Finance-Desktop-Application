package test.util;

import it.unicas.project.template.address.util.ForecastCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForecastCalculatorTest {

    private final ForecastCalculator calculator = new ForecastCalculator();

    @Test
    void insufficientDataWhenNotEnoughDays() {
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(100, 50, 2, 5, 30);
        assertFalse(result.isValid());
        assertEquals(ForecastCalculator.ForecastStatus.INSUFFICIENT_DATA, result.getStatus());
    }

    @Test
    void calculatesProjectedTotalsAndStatus() {
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(900, 300, 7, 10, 30);

        assertTrue(result.isValid());
        assertEquals(20, result.getRemainingDays());
        assertEquals(ForecastCalculator.ForecastStatus.WARNING, result.getStatus());
    }

    @Test
    void warnsWhenBalanceCritical() {
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(200, 600, 7, 10, 30);

        assertEquals(ForecastCalculator.ForecastStatus.CRITICAL, result.getStatus());
    }
}
