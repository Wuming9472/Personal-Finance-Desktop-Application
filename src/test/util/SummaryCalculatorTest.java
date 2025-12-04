package test.util;

import it.unicas.project.template.address.util.SummaryCalculator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SummaryCalculatorTest {

    private final SummaryCalculator calculator = new SummaryCalculator();

    @Test
    void calculateMonthlySummaryHandlesIncomeAndExpenses() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 1000, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 200, "Viaggio", 2),
            new SummaryCalculator.Movement("Uscita", 100, "Spesa", 3),
            new SummaryCalculator.Movement("Income", 300, "Bonus", 4)
        );

        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(movements);

        assertEquals(1300, summary.getTotalIncome());
        assertEquals(300, summary.getTotalExpenses());
        assertEquals(1000, summary.getBalance());
        assertEquals(2, summary.getExpensesByCategory().size());
        assertEquals(2, summary.getIncomeByCategory().size());
        assertEquals(4, summary.getTransactionCount());
        assertTrue(summary.isPositive());
        assertFalse(summary.isNegative());
    }

    @Test
    void calculateMonthlySummaryWithNoData() {
        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(Collections.emptyList());

        assertEquals(0, summary.getTotalIncome());
        assertEquals(0, summary.getTotalExpenses());
        assertEquals(0, summary.getBalance());
        assertEquals(0, summary.getTransactionCount());
        assertTrue(summary.getExpensesByCategory().isEmpty());
        assertTrue(summary.getIncomeByCategory().isEmpty());
    }

    @Test
    void aggregateByPeriodSplitsIntoBuckets() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 100, "A", 1),
            new SummaryCalculator.Movement("Uscita", 50, "B", 3),
            new SummaryCalculator.Movement("Entrata", 200, "A", 5)
        );

        SummaryCalculator.PeriodAggregate[] aggregates = calculator.aggregateByPeriod(movements, 2, 6);

        assertEquals(3, aggregates.length);
        assertEquals(100, aggregates[0].getIncome());
        assertEquals(50, aggregates[1].getExpenses());
        assertEquals(200, aggregates[2].getIncome());
    }

    @Test
    void calculateCategoryPercentageHandlesZeroTotals() {
        assertEquals(0.0, calculator.calculateCategoryPercentage(100, 0));
        assertEquals(50.0, calculator.calculateCategoryPercentage(50, 100));
    }
}
