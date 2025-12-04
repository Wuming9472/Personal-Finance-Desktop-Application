package it.unicas.project.template.address.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verifica dei metodi di calcolo delle previsioni finanziarie.
 */
public class ForecastCalculatorTest {

    @Test
    public void calculatesAveragesAndProjections() {
        ForecastCalculator calculator = new ForecastCalculator();

        Assertions.assertEquals(30.0, calculator.calculateDailyAverage(300.0, 10), 0.001);
        Assertions.assertEquals(0.0, calculator.calculateDailyAverage(100.0, 0), 0.001);
        Assertions.assertEquals(900.0, calculator.calculateProjectedTotal(300.0, 30.0, 20), 0.001);
        Assertions.assertEquals(300.0, calculator.calculateProjectedTotal(300.0, 30.0, -5), 0.001);
    }

    @Test
    public void determineStatusUsesConfiguredThresholds() {
        ForecastCalculator calculator = new ForecastCalculator();

        Assertions.assertEquals(ForecastCalculator.ForecastStatus.STABLE, calculator.determineStatus(250.0));
        Assertions.assertEquals(ForecastCalculator.ForecastStatus.WARNING, calculator.determineStatus(0.0));
        Assertions.assertEquals(ForecastCalculator.ForecastStatus.CRITICAL, calculator.determineStatus(-150.0));
    }

    @Test
    public void calculateForecastHandlesInvalidInputs() {
        ForecastCalculator calculator = new ForecastCalculator();

        ForecastCalculator.ForecastResult insufficient = calculator.calculateForecast(1000.0, 500.0, 2, 10, 30);
        Assertions.assertFalse(insufficient.isValid());
        Assertions.assertEquals(ForecastCalculator.ForecastStatus.INSUFFICIENT_DATA, insufficient.getStatus());

        ForecastCalculator.ForecastResult invalidDay = calculator.calculateForecast(1000.0, 500.0, 5, 0, 30);
        Assertions.assertFalse(invalidDay.isValid());
    }

    @Test
    public void calculateForecastProducesConsistentSummary() {
        ForecastCalculator calculator = new ForecastCalculator();

        ForecastCalculator.ForecastResult result = calculator.calculateForecast(1000.0, 500.0, 10, 10, 30);
        Assertions.assertTrue(result.isValid());
        Assertions.assertEquals(20, result.getRemainingDays());
        Assertions.assertEquals(100.0, result.getDailyIncomeAverage(), 0.001);
        Assertions.assertEquals(50.0, result.getDailyExpenseAverage(), 0.001);
        Assertions.assertEquals(3000.0, result.getProjectedTotalIncome(), 0.001);
        Assertions.assertEquals(1500.0, result.getProjectedTotalExpenses(), 0.001);
        Assertions.assertEquals(1500.0, result.getEstimatedBalance(), 0.001);
        Assertions.assertEquals(ForecastCalculator.ForecastStatus.STABLE, result.getStatus());
    }

    @Test
    public void hasEnoughDataRespectsMinimumThreshold() {
        ForecastCalculator calculator = new ForecastCalculator();

        Assertions.assertTrue(calculator.hasEnoughData(3));
        Assertions.assertFalse(calculator.hasEnoughData(2));
    }
}
