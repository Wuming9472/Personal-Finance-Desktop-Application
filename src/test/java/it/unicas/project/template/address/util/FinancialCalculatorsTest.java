package it.unicas.project.template.address.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinancialCalculatorsTest {

    private final ForecastCalculator forecastCalculator = new ForecastCalculator();
    private final SummaryCalculator summaryCalculator = new SummaryCalculator();

    @Nested
    @DisplayName("ForecastCalculator")
    class ForecastCalculatorTests {

        @Test
        @DisplayName("should reject insufficient movement days")
        void insufficientData() {
            ForecastCalculator.ForecastResult result = forecastCalculator.calculateForecast(
                1000, 500, 2, 5, 30);

            assertFalse(result.isValid());
            assertEquals(ForecastCalculator.ForecastStatus.INSUFFICIENT_DATA, result.getStatus());
            assertTrue(result.getErrorMessage().contains("insufficienti"));
        }

        @Test
        @DisplayName("should calculate projections and classify status")
        void validProjectionStableStatus() {
            ForecastCalculator.ForecastResult result = forecastCalculator.calculateForecast(
                1500, 400, 5, 10, 30);

            assertTrue(result.isValid());
            assertEquals(20, result.getRemainingDays());
            assertEquals(150.0, result.getDailyIncomeAverage());
            assertEquals(40.0, result.getDailyExpenseAverage());
            assertEquals(1500 + 150.0 * 20, result.getProjectedTotalIncome());
            assertEquals(400 + 40.0 * 20, result.getProjectedTotalExpenses());
            assertEquals(
                result.getProjectedTotalIncome() - result.getProjectedTotalExpenses(),
                result.getEstimatedBalance());
            assertEquals(ForecastCalculator.ForecastStatus.STABLE, result.getStatus());
        }

        @Test
        @DisplayName("should classify warning and critical thresholds")
        void warningAndCriticalClassification() {
            assertEquals(ForecastCalculator.ForecastStatus.WARNING, forecastCalculator.determineStatus(0));
            assertEquals(ForecastCalculator.ForecastStatus.CRITICAL, forecastCalculator.determineStatus(-150));
        }

        @Test
        @DisplayName("should guard against invalid current day")
        void invalidCurrentDay() {
            ForecastCalculator.ForecastResult result = forecastCalculator.calculateForecast(
                500, 200, 5, 0, 30);

            assertFalse(result.isValid());
            assertEquals(ForecastCalculator.ForecastStatus.INSUFFICIENT_DATA, result.getStatus());
        }

        @Test
        @DisplayName("should decline when incomes/expenses are negative")
        void negativeInputs() {
            ForecastCalculator.ForecastResult result = forecastCalculator.calculateForecast(
                -100, -50, 5, 10, 30);

            assertFalse(result.isValid());
            assertEquals(ForecastCalculator.ForecastStatus.INSUFFICIENT_DATA, result.getStatus());
            assertTrue(result.getErrorMessage().toLowerCase().contains("valori"));
        }
    }

    @Nested
    @DisplayName("SummaryCalculator")
    class SummaryCalculatorTests {

        private List<SummaryCalculator.Movement> demoMovements() {
            return Arrays.asList(
                new SummaryCalculator.Movement("Entrata", 1000, "Stipendio", 1),
                new SummaryCalculator.Movement("Uscita", 200, "Spesa", 2),
                new SummaryCalculator.Movement("Income", 500, "Bonus", 5),
                new SummaryCalculator.Movement("Expense", 300, "Trasporti", 6)
            );
        }

        @Test
        @DisplayName("should compute monthly totals and categories")
        void monthlySummary() {
            SummaryCalculator.MonthlySummary summary = summaryCalculator.calculateMonthlySummary(demoMovements());

            assertEquals(1500.0, summary.getTotalIncome());
            assertEquals(500.0, summary.getTotalExpenses());
            assertEquals(1000.0, summary.getBalance());
            assertEquals(4, summary.getTransactionCount());
            assertEquals(200.0, summary.getExpensesByCategory().get("Spesa"));
            assertEquals(300.0, summary.getExpensesByCategory().get("Trasporti"));
            assertEquals(1000.0, summary.getIncomeByCategory().get("Stipendio"));
            assertEquals(500.0, summary.getIncomeByCategory().get("Bonus"));
            assertTrue(summary.isPositive());
            assertFalse(summary.isNegative());
        }

        @Test
        @DisplayName("should aggregate by custom period")
        void aggregateByPeriod() {
            SummaryCalculator.PeriodAggregate[] aggregates = summaryCalculator.aggregateByPeriod(demoMovements(), 3, 30);

            assertEquals(10, aggregates.length);
            assertEquals(1000.0, aggregates[0].getIncome());
            assertEquals(200.0, aggregates[0].getExpenses());
            assertEquals("1-3", aggregates[0].getLabel());
            assertEquals(500.0, aggregates[1].getIncome());
            assertEquals(300.0, aggregates[1].getExpenses());
        }

        @Test
        @DisplayName("should calculate daily and cumulative balances")
        void dailyAndCumulativeBalances() {
            double[] daily = summaryCalculator.calculateDailyBalances(demoMovements(), 7);
            assertEquals(7, daily.length);
            assertEquals(1000.0, daily[0]);
            assertEquals(-200.0, daily[1]);
            assertEquals(0.0, daily[2]);
            assertEquals(0.0, daily[3]);
            assertEquals(500.0, daily[4]);
            assertEquals(-300.0, daily[5]);

            double[] cumulative = summaryCalculator.calculateCumulativeBalance(daily);
            assertArrayEquals(new double[] {1000, 800, 800, 800, 1300, 1000, 1000}, cumulative);
        }

        @Test
        @DisplayName("should identify top expense category")
        void topExpenseCategory() {
            Map<String, Double> expenses = summaryCalculator.calculateMonthlySummary(demoMovements()).getExpensesByCategory();
            assertEquals("Trasporti", summaryCalculator.findTopExpenseCategory(expenses));
            assertNull(summaryCalculator.findTopExpenseCategory(Collections.emptyMap()));
        }

        @Test
        @DisplayName("should compute savings rate and handle zero income")
        void savingsRate() {
            SummaryCalculator.MonthlySummary summary = summaryCalculator.calculateMonthlySummary(demoMovements());
            assertEquals(66.66666666666666, summaryCalculator.calculateSavingsRate(
                summary.getTotalIncome(), summary.getTotalExpenses()));

            assertEquals(0.0, summaryCalculator.calculateSavingsRate(0, 100));
        }

        @Test
        @DisplayName("should handle empty inputs gracefully")
        void emptyInputs() {
            SummaryCalculator.MonthlySummary summary = summaryCalculator.calculateMonthlySummary(Collections.emptyList());
            assertEquals(0.0, summary.getTotalIncome());
            assertEquals(0.0, summary.getTotalExpenses());
            assertEquals(0.0, summary.getBalance());
            assertEquals(0, summary.getTransactionCount());

            SummaryCalculator.PeriodAggregate[] aggregates = summaryCalculator.aggregateByPeriod(Collections.emptyList(), 5, 10);
            assertEquals(2, aggregates.length);
            assertEquals(0.0, aggregates[0].getIncome());
            assertEquals(0.0, aggregates[0].getExpenses());
        }
    }
}
