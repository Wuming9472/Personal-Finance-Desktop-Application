package it.unicas.project.template.address.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test JUnit per la classe SummaryCalculator.
 * Verifica la logica di calcolo dei riepiloghi finanziari:
 * - Saldo mensile (entrate - uscite)
 * - Totali e percentuali per categoria
 * - Aggregazioni per periodo
 * - Trend e saldi cumulativi
 */
@DisplayName("Test Riepiloghi - SummaryCalculator")
public class SummaryCalculatorTest {

    private SummaryCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SummaryCalculator();
    }

    // ============================================================
    // TEST PER calculateBalance()
    // ============================================================

    @Test
    @DisplayName("calculateBalance() calcola saldo positivo correttamente")
    void balanceIsPositiveWhenIncomeExceedsExpenses() {
        double balance = calculator.calculateBalance(1500.0, 1000.0);
        assertEquals(500.0, balance, 0.001, "1500 - 1000 = 500");
    }

    @Test
    @DisplayName("calculateBalance() calcola saldo negativo correttamente")
    void balanceIsNegativeWhenExpensesExceedIncome() {
        double balance = calculator.calculateBalance(800.0, 1200.0);
        assertEquals(-400.0, balance, 0.001, "800 - 1200 = -400");
    }

    @Test
    @DisplayName("calculateBalance() calcola saldo zero correttamente")
    void balanceIsZeroWhenEqual() {
        double balance = calculator.calculateBalance(1000.0, 1000.0);
        assertEquals(0.0, balance, 0.001);
    }

    @ParameterizedTest
    @DisplayName("calculateBalance() scenari multipli")
    @CsvSource({
        "2000.0, 500.0, 1500.0",
        "100.0, 100.0, 0.0",
        "0.0, 500.0, -500.0",
        "3000.0, 0.0, 3000.0"
    })
    void balanceVariousScenarios(double income, double expenses, double expected) {
        assertEquals(expected, calculator.calculateBalance(income, expenses), 0.001);
    }

    // ============================================================
    // TEST PER calculateCategoryPercentage()
    // ============================================================

    @Test
    @DisplayName("calculateCategoryPercentage() calcola percentuale corretta")
    void categoryPercentageCalculatesCorrectly() {
        // 250€ su 1000€ = 25%
        double percentage = calculator.calculateCategoryPercentage(250.0, 1000.0);
        assertEquals(25.0, percentage, 0.001);
    }

    @Test
    @DisplayName("calculateCategoryPercentage() restituisce 0 per totale zero")
    void categoryPercentageReturnsZeroForZeroTotal() {
        assertEquals(0.0, calculator.calculateCategoryPercentage(100.0, 0.0), 0.001);
    }

    @Test
    @DisplayName("calculateCategoryPercentage() restituisce 0 per totale negativo")
    void categoryPercentageReturnsZeroForNegativeTotal() {
        assertEquals(0.0, calculator.calculateCategoryPercentage(100.0, -50.0), 0.001);
    }

    @ParameterizedTest
    @DisplayName("calculateCategoryPercentage() scenari multipli")
    @CsvSource({
        "500.0, 1000.0, 50.0",    // 50%
        "100.0, 400.0, 25.0",     // 25%
        "333.0, 1000.0, 33.3",    // ~33%
        "1000.0, 1000.0, 100.0",  // 100%
        "0.0, 1000.0, 0.0"        // 0%
    })
    void categoryPercentageVariousScenarios(double amount, double total, double expected) {
        assertEquals(expected, calculator.calculateCategoryPercentage(amount, total), 0.1);
    }

    // ============================================================
    // TEST PER calculateMonthlySummary()
    // ============================================================

    @Test
    @DisplayName("calculateMonthlySummary() gestisce lista vuota")
    void monthlySummaryHandlesEmptyList() {
        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(new ArrayList<>());

        assertEquals(0.0, summary.getTotalIncome(), 0.001);
        assertEquals(0.0, summary.getTotalExpenses(), 0.001);
        assertEquals(0.0, summary.getBalance(), 0.001);
        assertEquals(0, summary.getTransactionCount());
        assertTrue(summary.getExpensesByCategory().isEmpty());
    }

    @Test
    @DisplayName("calculateMonthlySummary() gestisce lista null")
    void monthlySummaryHandlesNullList() {
        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(null);

        assertEquals(0.0, summary.getTotalIncome(), 0.001);
        assertEquals(0.0, summary.getTotalExpenses(), 0.001);
    }

    @Test
    @DisplayName("calculateMonthlySummary() calcola correttamente entrate e uscite")
    void monthlySummaryCalculatesCorrectTotals() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 1500.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 400.0, "Alimentari", 5),
            new SummaryCalculator.Movement("Uscita", 150.0, "Trasporti", 10),
            new SummaryCalculator.Movement("Entrata", 200.0, "Bonus", 15),
            new SummaryCalculator.Movement("Uscita", 100.0, "Svago", 20)
        );

        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(movements);

        assertEquals(1700.0, summary.getTotalIncome(), 0.001);  // 1500 + 200
        assertEquals(650.0, summary.getTotalExpenses(), 0.001); // 400 + 150 + 100
        assertEquals(1050.0, summary.getBalance(), 0.001);      // 1700 - 650
        assertEquals(5, summary.getTransactionCount());
        assertTrue(summary.isPositive());
    }

    @Test
    @DisplayName("calculateMonthlySummary() aggrega correttamente per categoria")
    void monthlySummaryAggregatesByCategory() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Uscita", 100.0, "Alimentari", 1),
            new SummaryCalculator.Movement("Uscita", 150.0, "Alimentari", 5),
            new SummaryCalculator.Movement("Uscita", 50.0, "Trasporti", 10),
            new SummaryCalculator.Movement("Uscita", 80.0, "Svago", 15)
        );

        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(movements);

        Map<String, Double> expensesByCategory = summary.getExpensesByCategory();
        assertEquals(250.0, expensesByCategory.get("Alimentari"), 0.001); // 100 + 150
        assertEquals(50.0, expensesByCategory.get("Trasporti"), 0.001);
        assertEquals(80.0, expensesByCategory.get("Svago"), 0.001);
    }

    @Test
    @DisplayName("calculateMonthlySummary() riconosce saldo negativo")
    void monthlySummaryRecognizesNegativeBalance() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 500.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 800.0, "Bollette", 10)
        );

        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(movements);

        assertTrue(summary.isNegative());
        assertFalse(summary.isPositive());
        assertEquals(-300.0, summary.getBalance(), 0.001);
    }

    // ============================================================
    // TEST PER aggregateByPeriod()
    // ============================================================

    @Test
    @DisplayName("aggregateByPeriod() crea periodi corretti per mese di 30 giorni")
    void aggregateByPeriodCreatesCorrectPeriods() {
        List<SummaryCalculator.Movement> movements = new ArrayList<>();
        SummaryCalculator.PeriodAggregate[] periods = calculator.aggregateByPeriod(movements, 3, 30);

        assertEquals(10, periods.length, "30 giorni / 3 = 10 periodi");
        assertEquals("1-3", periods[0].getLabel());
        assertEquals("4-6", periods[1].getLabel());
        assertEquals("28-30", periods[9].getLabel());
    }

    @Test
    @DisplayName("aggregateByPeriod() aggrega movimenti nei periodi corretti")
    void aggregateByPeriodAggregatesMovementsCorrectly() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 100.0, "Stipendio", 1),  // Periodo 0
            new SummaryCalculator.Movement("Uscita", 50.0, "Alimentari", 2),   // Periodo 0
            new SummaryCalculator.Movement("Entrata", 200.0, "Bonus", 5),       // Periodo 1
            new SummaryCalculator.Movement("Uscita", 75.0, "Trasporti", 7)     // Periodo 2
        );

        SummaryCalculator.PeriodAggregate[] periods = calculator.aggregateByPeriod(movements, 3, 30);

        // Periodo 0 (giorni 1-3): entrata 100, uscita 50
        assertEquals(100.0, periods[0].getIncome(), 0.001);
        assertEquals(50.0, periods[0].getExpenses(), 0.001);
        assertEquals(50.0, periods[0].getBalance(), 0.001);

        // Periodo 1 (giorni 4-6): entrata 200
        assertEquals(200.0, periods[1].getIncome(), 0.001);
        assertEquals(0.0, periods[1].getExpenses(), 0.001);

        // Periodo 2 (giorni 7-9): uscita 75
        assertEquals(0.0, periods[2].getIncome(), 0.001);
        assertEquals(75.0, periods[2].getExpenses(), 0.001);
    }

    @Test
    @DisplayName("aggregateByPeriod() gestisce parametri non validi")
    void aggregateByPeriodHandlesInvalidParams() {
        List<SummaryCalculator.Movement> movements = new ArrayList<>();

        assertEquals(0, calculator.aggregateByPeriod(movements, 0, 30).length);
        assertEquals(0, calculator.aggregateByPeriod(movements, 3, 0).length);
        assertEquals(0, calculator.aggregateByPeriod(movements, -1, 30).length);
    }

    @Test
    @DisplayName("aggregateByPeriod() gestisce mese di 31 giorni")
    void aggregateByPeriodHandles31DayMonth() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Uscita", 100.0, "Altro", 31)
        );

        SummaryCalculator.PeriodAggregate[] periods = calculator.aggregateByPeriod(movements, 3, 31);

        assertEquals(11, periods.length); // ceil(31/3) = 11
        assertEquals("31-31", periods[10].getLabel());
        assertEquals(100.0, periods[10].getExpenses(), 0.001);
    }

    // ============================================================
    // TEST PER calculateDailyBalances()
    // ============================================================

    @Test
    @DisplayName("calculateDailyBalances() calcola saldi giornalieri corretti")
    void dailyBalancesCalculateCorrectly() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 1000.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 50.0, "Alimentari", 1),
            new SummaryCalculator.Movement("Uscita", 100.0, "Trasporti", 5)
        );

        double[] balances = calculator.calculateDailyBalances(movements, 10);

        assertEquals(10, balances.length);
        assertEquals(950.0, balances[0], 0.001);  // Giorno 1: 1000 - 50
        assertEquals(0.0, balances[1], 0.001);    // Giorno 2: nessun movimento
        assertEquals(-100.0, balances[4], 0.001); // Giorno 5: -100
    }

    @Test
    @DisplayName("calculateDailyBalances() gestisce giorni senza movimenti")
    void dailyBalancesHandlesEmptyDays() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 500.0, "Bonus", 15)
        );

        double[] balances = calculator.calculateDailyBalances(movements, 30);

        assertEquals(30, balances.length);
        assertEquals(0.0, balances[0], 0.001);   // Giorno 1
        assertEquals(500.0, balances[14], 0.001); // Giorno 15
        assertEquals(0.0, balances[29], 0.001);  // Giorno 30
    }

    // ============================================================
    // TEST PER calculateCumulativeBalance()
    // ============================================================

    @Test
    @DisplayName("calculateCumulativeBalance() calcola saldo cumulativo corretto")
    void cumulativeBalanceCalculatesCorrectly() {
        double[] dailyBalances = {100.0, -50.0, 200.0, -30.0, 0.0};

        double[] cumulative = calculator.calculateCumulativeBalance(dailyBalances);

        assertEquals(5, cumulative.length);
        assertEquals(100.0, cumulative[0], 0.001);  // 100
        assertEquals(50.0, cumulative[1], 0.001);   // 100 - 50
        assertEquals(250.0, cumulative[2], 0.001);  // 50 + 200
        assertEquals(220.0, cumulative[3], 0.001);  // 250 - 30
        assertEquals(220.0, cumulative[4], 0.001);  // 220 + 0
    }

    @Test
    @DisplayName("calculateCumulativeBalance() gestisce array vuoto")
    void cumulativeBalanceHandlesEmptyArray() {
        double[] cumulative = calculator.calculateCumulativeBalance(new double[0]);
        assertEquals(0, cumulative.length);
    }

    @Test
    @DisplayName("calculateCumulativeBalance() gestisce null")
    void cumulativeBalanceHandlesNull() {
        double[] cumulative = calculator.calculateCumulativeBalance(null);
        assertEquals(0, cumulative.length);
    }

    // ============================================================
    // TEST PER findTopExpenseCategory()
    // ============================================================

    @Test
    @DisplayName("findTopExpenseCategory() trova la categoria con spesa maggiore")
    void topExpenseCategoryFindsCorrectCategory() {
        Map<String, Double> expenses = new HashMap<>();
        expenses.put("Alimentari", 400.0);
        expenses.put("Trasporti", 150.0);
        expenses.put("Svago", 200.0);
        expenses.put("Bollette", 350.0);

        String top = calculator.findTopExpenseCategory(expenses);
        assertEquals("Alimentari", top);
    }

    @Test
    @DisplayName("findTopExpenseCategory() gestisce mappa vuota")
    void topExpenseCategoryHandlesEmptyMap() {
        assertNull(calculator.findTopExpenseCategory(new HashMap<>()));
    }

    @Test
    @DisplayName("findTopExpenseCategory() gestisce null")
    void topExpenseCategoryHandlesNull() {
        assertNull(calculator.findTopExpenseCategory(null));
    }

    @Test
    @DisplayName("findTopExpenseCategory() con una sola categoria")
    void topExpenseCategoryWithSingleCategory() {
        Map<String, Double> expenses = new HashMap<>();
        expenses.put("Unica", 100.0);

        assertEquals("Unica", calculator.findTopExpenseCategory(expenses));
    }

    // ============================================================
    // TEST PER calculateSavingsRate()
    // ============================================================

    @Test
    @DisplayName("calculateSavingsRate() calcola tasso di risparmio positivo")
    void savingsRateCalculatesPositiveSavings() {
        // Entrate 2000, Uscite 1600, Risparmio 400 = 20%
        double rate = calculator.calculateSavingsRate(2000.0, 1600.0);
        assertEquals(20.0, rate, 0.001);
    }

    @Test
    @DisplayName("calculateSavingsRate() calcola tasso di deficit negativo")
    void savingsRateCalculatesNegativeDeficit() {
        // Entrate 1000, Uscite 1500, Deficit -500 = -50%
        double rate = calculator.calculateSavingsRate(1000.0, 1500.0);
        assertEquals(-50.0, rate, 0.001);
    }

    @Test
    @DisplayName("calculateSavingsRate() gestisce entrate zero")
    void savingsRateHandlesZeroIncome() {
        assertEquals(0.0, calculator.calculateSavingsRate(0.0, 500.0), 0.001);
    }

    @ParameterizedTest
    @DisplayName("calculateSavingsRate() scenari multipli")
    @CsvSource({
        "1000.0, 700.0, 30.0",    // 30% risparmio
        "1000.0, 1000.0, 0.0",    // 0% (pareggio)
        "1000.0, 1200.0, -20.0",  // -20% (deficit)
        "500.0, 250.0, 50.0"      // 50% risparmio
    })
    void savingsRateVariousScenarios(double income, double expenses, double expected) {
        assertEquals(expected, calculator.calculateSavingsRate(income, expenses), 0.001);
    }

    // ============================================================
    // TEST DI INTEGRAZIONE - Scenari realistici
    // ============================================================

    @Test
    @DisplayName("Scenario: Mese tipico con stipendio e spese varie")
    void scenarioTypicalMonth() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            // Entrate
            new SummaryCalculator.Movement("Entrata", 1800.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Entrata", 100.0, "Rimborso", 15),

            // Uscite
            new SummaryCalculator.Movement("Uscita", 450.0, "Alimentari", 3),
            new SummaryCalculator.Movement("Uscita", 120.0, "Trasporti", 5),
            new SummaryCalculator.Movement("Uscita", 350.0, "Bollette", 10),
            new SummaryCalculator.Movement("Uscita", 100.0, "Alimentari", 18),
            new SummaryCalculator.Movement("Uscita", 80.0, "Svago", 20),
            new SummaryCalculator.Movement("Uscita", 200.0, "Salute", 25)
        );

        SummaryCalculator.MonthlySummary summary = calculator.calculateMonthlySummary(movements);

        // Verifica totali
        assertEquals(1900.0, summary.getTotalIncome(), 0.001);
        assertEquals(1300.0, summary.getTotalExpenses(), 0.001);
        assertEquals(600.0, summary.getBalance(), 0.001);
        assertTrue(summary.isPositive());

        // Verifica aggregazione categorie
        Map<String, Double> expenses = summary.getExpensesByCategory();
        assertEquals(550.0, expenses.get("Alimentari"), 0.001); // 450 + 100
        assertEquals(120.0, expenses.get("Trasporti"), 0.001);
        assertEquals(350.0, expenses.get("Bollette"), 0.001);

        // Verifica categoria principale
        String topCategory = calculator.findTopExpenseCategory(expenses);
        assertEquals("Alimentari", topCategory);

        // Verifica tasso risparmio
        double savingsRate = calculator.calculateSavingsRate(
            summary.getTotalIncome(), summary.getTotalExpenses());
        assertEquals(31.58, savingsRate, 0.1); // ~31.6% risparmiato
    }

    @Test
    @DisplayName("Scenario: Andamento mensile con aggregazione per 3 giorni")
    void scenarioMonthlyTrendWithPeriods() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 1500.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 100.0, "Spesa", 2),
            new SummaryCalculator.Movement("Uscita", 50.0, "Spesa", 4),
            new SummaryCalculator.Movement("Uscita", 200.0, "Bolletta", 10),
            new SummaryCalculator.Movement("Uscita", 150.0, "Spesa", 15),
            new SummaryCalculator.Movement("Entrata", 200.0, "Bonus", 20)
        );

        SummaryCalculator.PeriodAggregate[] periods = calculator.aggregateByPeriod(movements, 3, 30);

        // Verifica struttura
        assertEquals(10, periods.length);

        // Periodo 0 (1-3): Entrata 1500, Uscita 100
        assertEquals(1500.0, periods[0].getIncome(), 0.001);
        assertEquals(100.0, periods[0].getExpenses(), 0.001);

        // Periodo 1 (4-6): Uscita 50
        assertEquals(0.0, periods[1].getIncome(), 0.001);
        assertEquals(50.0, periods[1].getExpenses(), 0.001);

        // Periodo 3 (10-12): Uscita 200
        assertEquals(0.0, periods[3].getIncome(), 0.001);
        assertEquals(200.0, periods[3].getExpenses(), 0.001);
    }

    @Test
    @DisplayName("Scenario: Saldo cumulativo progressivo")
    void scenarioCumulativeBalance() {
        List<SummaryCalculator.Movement> movements = Arrays.asList(
            new SummaryCalculator.Movement("Entrata", 1000.0, "Stipendio", 1),
            new SummaryCalculator.Movement("Uscita", 200.0, "Spesa", 5),
            new SummaryCalculator.Movement("Uscita", 300.0, "Bolletta", 10),
            new SummaryCalculator.Movement("Entrata", 100.0, "Rimborso", 15)
        );

        double[] daily = calculator.calculateDailyBalances(movements, 20);
        double[] cumulative = calculator.calculateCumulativeBalance(daily);

        // Giorno 1: +1000
        assertEquals(1000.0, cumulative[0], 0.001);

        // Giorni 2-4: invariato
        assertEquals(1000.0, cumulative[3], 0.001);

        // Giorno 5: 1000 - 200 = 800
        assertEquals(800.0, cumulative[4], 0.001);

        // Giorno 10: 800 - 300 = 500
        assertEquals(500.0, cumulative[9], 0.001);

        // Giorno 15: 500 + 100 = 600
        assertEquals(600.0, cumulative[14], 0.001);

        // Fine mese: invariato
        assertEquals(600.0, cumulative[19], 0.001);
    }

    // ============================================================
    // TEST PER Movement inner class
    // ============================================================

    @Test
    @DisplayName("Movement.isIncome() riconosce entrate")
    void movementRecognizesIncome() {
        assertTrue(new SummaryCalculator.Movement("Entrata", 100, "Cat", 1).isIncome());
        assertTrue(new SummaryCalculator.Movement("ENTRATA", 100, "Cat", 1).isIncome());
        assertTrue(new SummaryCalculator.Movement("Income", 100, "Cat", 1).isIncome());
        assertTrue(new SummaryCalculator.Movement("income", 100, "Cat", 1).isIncome());
    }

    @Test
    @DisplayName("Movement.isExpense() riconosce uscite")
    void movementRecognizesExpense() {
        assertTrue(new SummaryCalculator.Movement("Uscita", 100, "Cat", 1).isExpense());
        assertTrue(new SummaryCalculator.Movement("USCITA", 100, "Cat", 1).isExpense());
        assertTrue(new SummaryCalculator.Movement("Expense", 100, "Cat", 1).isExpense());
        assertTrue(new SummaryCalculator.Movement("expense", 100, "Cat", 1).isExpense());
    }

    @Test
    @DisplayName("Movement gestisce tipo null")
    void movementHandlesNullType() {
        SummaryCalculator.Movement m = new SummaryCalculator.Movement(null, 100, "Cat", 1);
        assertFalse(m.isIncome());
        assertFalse(m.isExpense());
    }
}
