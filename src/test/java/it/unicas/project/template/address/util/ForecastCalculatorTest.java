package it.unicas.project.template.address.util;

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
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(900, 300, 5, 10, 30);

        assertTrue(result.isValid());
        assertEquals(20, result.getRemainingDays());
        assertTrue(result.getProjectedTotalIncome() > result.getProjectedTotalExpenses());
        assertEquals(ForecastCalculator.ForecastStatus.STABLE, result.getStatus());
    }

    @Test
    void warnsWhenBalanceCritical() {
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(200, 600, 5, 10, 30);

        assertEquals(ForecastCalculator.ForecastStatus.CRITICAL, result.getStatus());
    }
}
