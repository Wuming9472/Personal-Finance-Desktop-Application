package it.unicas.project.template.address.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verifica dei metodi di calcolo dei riepiloghi finanziari.
 */
public class SummaryCalculatorTest {

    @Test
    public void calculatesBalanceSignCorrectly() {
        SummaryCalculator calculator = new SummaryCalculator();

        Assertions.assertEquals(500.0, calculator.calculateBalance(1500.0, 1000.0), 0.001);
        Assertions.assertEquals(-250.0, calculator.calculateBalance(750.0, 1000.0), 0.001);
    }

    @Test
    public void categoryPercentageHandlesEdgeCases() {
        SummaryCalculator calculator = new SummaryCalculator();

        Assertions.assertEquals(25.0, calculator.calculateCategoryPercentage(250.0, 1000.0), 0.001);
        Assertions.assertEquals(0.0, calculator.calculateCategoryPercentage(100.0, 0.0), 0.001);
        Assertions.assertEquals(0.0, calculator.calculateCategoryPercentage(50.0, -100.0), 0.001);
    }

    @Test
    public void monthlySummaryAggregatesIncomeExpensesAndCategories() {
        SummaryCalculator calculator = new SummaryCalculator();
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 1000.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Entrata", 200.0, "Bonus", 10),
            new SummaryCalculator.Movement("Uscita", 150.0, "Alimentari", 5),
            new SummaryCalculator.Movement("Uscita", 50.0, "Trasporti", 12)
        );

        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(movements);
        Assertions.assertEquals(1200.0, summary.getTotalIncome(), 0.001);
        Assertions.assertEquals(200.0, summary.getTotalExpenses(), 0.001);
        Assertions.assertEquals(1000.0, summary.getBalance(), 0.001);
        Assertions.assertEquals(4, summary.getTransactionCount());
        Assertions.assertEquals(150.0, summary.getExpensesByCategory().get("Alimentari"), 0.001);
        Assertions.assertEquals(50.0, summary.getExpensesByCategory().get("Trasporti"), 0.001);
        Assertions.assertEquals(2, summary.getIncomeByCategory().size());
    }

    @Test
    public void monthlySummaryHandlesNullOrEmptyMovements() {
        SummaryCalculator calculator = new SummaryCalculator();

        SummaryCalculator.MonthlySummary emptySummary = calculator.calculateMonthlySummary(Collections.emptyList());
        Assertions.assertEquals(0.0, emptySummary.getTotalIncome(), 0.001);
        Assertions.assertEquals(0.0, emptySummary.getTotalExpenses(), 0.001);
        Assertions.assertEquals(0, emptySummary.getTransactionCount());

        SummaryCalculator.MonthlySummary nullSummary = calculator.calculateMonthlySummary(null);
        Assertions.assertEquals(0.0, nullSummary.getTotalIncome(), 0.001);
        Assertions.assertEquals(0.0, nullSummary.getTotalExpenses(), 0.001);
    }

    @Test
    public void aggregateByPeriodBuildsCorrectBuckets() {
        SummaryCalculator calculator = new SummaryCalculator();
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 300.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 50.0, "Alimentari", 2),
            new SummaryCalculator.Movement("Entrata", 200.0, "Bonus", 4),
            new SummaryCalculator.Movement("Uscita", 100.0, "Trasporti", 7)
        );

        SummaryCalculator.PeriodAggregate[] aggregates = calculator.aggregateByPeriod(movements, 3, 30);
        Assertions.assertEquals(10, aggregates.length, 0.001);
        Assertions.assertEquals("1-3", aggregates[0].getLabel());
        Assertions.assertEquals(300.0, aggregates[0].getIncome(), 0.001);
        Assertions.assertEquals(50.0, aggregates[0].getExpenses(), 0.001);
        Assertions.assertEquals(200.0, aggregates[1].getIncome(), 0.001);
        Assertions.assertEquals(100.0, aggregates[2].getExpenses(), 0.001);
    }

    @Test
    public void calculateDailyAndCumulativeBalancesTrackProgression() {
        SummaryCalculator calculator = new SummaryCalculator();
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 500.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 200.0, "Spesa", 3)
        );

        double[] daily = calculator.calculateDailyBalances(movements, 5);
        Assertions.assertEquals(5, daily.length);
        Assertions.assertEquals(500.0, daily[0], 0.001);
        Assertions.assertEquals(-200.0, daily[2], 0.001);

        double[] cumulative = calculator.calculateCumulativeBalance(daily);
        Assertions.assertEquals(300.0, cumulative[2], 0.001);
        Assertions.assertEquals(300.0, cumulative[4], 0.001);
    }

    @Test
    public void findTopExpenseCategoryReturnsLargestValue() {
        SummaryCalculator calculator = new SummaryCalculator();
        Map<String, Double> expenses = new HashMap<>();
        expenses.put("Alimentari", 400.0);
        expenses.put("Trasporti", 150.0);
        expenses.put("Svago", 200.0);

        Assertions.assertEquals("Alimentari", calculator.findTopExpenseCategory(expenses));
        Assertions.assertEquals(null, calculator.findTopExpenseCategory(Collections.emptyMap()));
    }

    @Test
    public void calculateSavingsRateReflectsSurplusAndDeficit() {
        SummaryCalculator calculator = new SummaryCalculator();

        Assertions.assertEquals(20.0, calculator.calculateSavingsRate(2000.0, 1600.0), 0.001);
        Assertions.assertEquals(-50.0, calculator.calculateSavingsRate(1000.0, 1500.0), 0.001);
        Assertions.assertEquals(0.0, calculator.calculateSavingsRate(0.0, 500.0), 0.001);
    }
}
