package it.unicas.project.template.address.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test JUnit per la classe Budget.
 * Verifica i metodi di calcolo per la gestione dei budget:
 * - getProgress(): calcolo percentuale di utilizzo del budget
 * - getRemaining(): calcolo del budget rimanente
 */
@DisplayName("Test Gestione Budget - Modello Budget")
public class BudgetTest {

    private Budget budget;

    @BeforeEach
    void setUp() {
        // Budget di esempio: limite 400€, speso 0€
        budget = new Budget(1, 1, 1, 12, 2024, 400.0, "Alimentari", 0.0);
    }

    // ============================================================
    // TEST PER getProgress() - Calcolo percentuale di utilizzo
    // ============================================================

    @Test
    @DisplayName("getProgress() restituisce 0 quando nulla e' stato speso")
    void progressIsZeroWhenNothingSpent() {
        budget.setSpentAmount(0.0);
        budget.setBudgetAmount(400.0);

        assertEquals(0.0, budget.getProgress(), 0.001,
            "Il progress deve essere 0 quando non e' stato speso nulla");
    }

    @Test
    @DisplayName("getProgress() restituisce 0.5 (50%) quando speso meta' del budget")
    void progressIsFiftyPercentWhenHalfSpent() {
        budget.setBudgetAmount(400.0);
        budget.setSpentAmount(200.0);

        assertEquals(0.5, budget.getProgress(), 0.001,
            "Il progress deve essere 0.5 quando si e' spesa meta' del budget");
    }

    @Test
    @DisplayName("getProgress() restituisce 1.0 (100%) quando speso tutto il budget")
    void progressIsOneHundredPercentWhenFullySpent() {
        budget.setBudgetAmount(400.0);
        budget.setSpentAmount(400.0);

        assertEquals(1.0, budget.getProgress(), 0.001,
            "Il progress deve essere 1.0 quando si e' speso tutto il budget");
    }

    @Test
    @DisplayName("getProgress() restituisce valore > 1 quando budget superato")
    void progressExceedsOneWhenOverBudget() {
        budget.setBudgetAmount(400.0);
        budget.setSpentAmount(500.0);

        assertEquals(1.25, budget.getProgress(), 0.001,
            "Il progress deve essere 1.25 (125%) quando si e' speso 500 su 400");
    }

    @Test
    @DisplayName("getProgress() restituisce 0 quando budget e' 0 (divisione per zero)")
    void progressIsZeroWhenBudgetIsZero() {
        budget.setBudgetAmount(0.0);
        budget.setSpentAmount(100.0);

        assertEquals(0.0, budget.getProgress(), 0.001,
            "Il progress deve essere 0 per evitare divisione per zero");
    }

    @ParameterizedTest
    @DisplayName("getProgress() calcola correttamente diverse percentuali")
    @CsvSource({
        "100.0, 25.0, 0.25",    // 25%
        "100.0, 75.0, 0.75",    // 75%
        "200.0, 50.0, 0.25",    // 25%
        "150.0, 150.0, 1.0",    // 100%
        "300.0, 450.0, 1.5"     // 150% (superato)
    })
    void progressCalculatesCorrectPercentages(double budgetAmount, double spentAmount, double expectedProgress) {
        budget.setBudgetAmount(budgetAmount);
        budget.setSpentAmount(spentAmount);

        assertEquals(expectedProgress, budget.getProgress(), 0.001,
            String.format("Con budget %.2f e speso %.2f, progress deve essere %.2f",
                budgetAmount, spentAmount, expectedProgress));
    }

    // ============================================================
    // TEST PER getRemaining() - Calcolo budget rimanente
    // ============================================================

    @Test
    @DisplayName("getRemaining() restituisce intero budget quando nulla e' stato speso")
    void remainingEqualsFullBudgetWhenNothingSpent() {
        budget.setBudgetAmount(400.0);
        budget.setSpentAmount(0.0);

        assertEquals(400.0, budget.getRemaining(), 0.001,
            "Il remaining deve essere uguale al budget quando non si e' speso nulla");
    }

    @Test
    @DisplayName("getRemaining() restituisce la differenza corretta")
    void remainingCalculatesCorrectDifference() {
        budget.setBudgetAmount(400.0);
        budget.setSpentAmount(150.0);

        assertEquals(250.0, budget.getRemaining(), 0.001,
            "400 - 150 = 250 di budget rimanente");
    }

    @Test
    @DisplayName("getRemaining() restituisce 0 quando speso uguale al budget")
    void remainingIsZeroWhenFullySpent() {
        budget.setBudgetAmount(400.0);
        budget.setSpentAmount(400.0);

        assertEquals(0.0, budget.getRemaining(), 0.001,
            "Il remaining deve essere 0 quando si e' speso tutto");
    }

    @Test
    @DisplayName("getRemaining() restituisce valore negativo quando budget superato")
    void remainingIsNegativeWhenOverBudget() {
        budget.setBudgetAmount(400.0);
        budget.setSpentAmount(500.0);

        assertEquals(-100.0, budget.getRemaining(), 0.001,
            "Il remaining deve essere -100 quando si e' speso 500 su 400");
    }

    @ParameterizedTest
    @DisplayName("getRemaining() calcola correttamente diversi scenari")
    @CsvSource({
        "500.0, 100.0, 400.0",     // Normale
        "200.0, 200.0, 0.0",       // Esatto
        "150.0, 200.0, -50.0",     // Superato
        "1000.0, 250.0, 750.0",    // Grande budget
        "0.0, 0.0, 0.0"            // Zero
    })
    void remainingCalculatesCorrectValues(double budgetAmount, double spentAmount, double expectedRemaining) {
        budget.setBudgetAmount(budgetAmount);
        budget.setSpentAmount(spentAmount);

        assertEquals(expectedRemaining, budget.getRemaining(), 0.001,
            String.format("Con budget %.2f e speso %.2f, remaining deve essere %.2f",
                budgetAmount, spentAmount, expectedRemaining));
    }

    // ============================================================
    // TEST PER COSTRUTTORI E GETTER/SETTER
    // ============================================================

    @Test
    @DisplayName("Costruttore vuoto inizializza tutti i campi a valori di default")
    void emptyConstructorInitializesDefaults() {
        Budget emptyBudget = new Budget();

        assertEquals(0, emptyBudget.getBudgetId());
        assertEquals(0, emptyBudget.getCategoryId());
        assertEquals(0, emptyBudget.getUserId());
        assertEquals(0, emptyBudget.getMonth());
        assertEquals(0, emptyBudget.getYear());
        assertEquals(0.0, emptyBudget.getBudgetAmount(), 0.001);
        assertEquals("", emptyBudget.getCategoryName());
        assertEquals(0.0, emptyBudget.getSpentAmount(), 0.001);
    }

    @Test
    @DisplayName("Costruttore completo imposta tutti i valori correttamente")
    void fullConstructorSetsAllValues() {
        Budget fullBudget = new Budget(10, 3, 5, 6, 2025, 250.0, "Bollette", 75.50);

        assertEquals(10, fullBudget.getBudgetId());
        assertEquals(3, fullBudget.getCategoryId());
        assertEquals(5, fullBudget.getUserId());
        assertEquals(6, fullBudget.getMonth());
        assertEquals(2025, fullBudget.getYear());
        assertEquals(250.0, fullBudget.getBudgetAmount(), 0.001);
        assertEquals("Bollette", fullBudget.getCategoryName());
        assertEquals(75.50, fullBudget.getSpentAmount(), 0.001);
    }

    @Test
    @DisplayName("I setter aggiornano correttamente i valori")
    void settersUpdateValuesCorrectly() {
        Budget b = new Budget();

        b.setBudgetId(99);
        b.setCategoryId(7);
        b.setUserId(42);
        b.setMonth(11);
        b.setYear(2024);
        b.setBudgetAmount(500.0);
        b.setCategoryName("Investimenti");
        b.setSpentAmount(123.45);

        assertEquals(99, b.getBudgetId());
        assertEquals(7, b.getCategoryId());
        assertEquals(42, b.getUserId());
        assertEquals(11, b.getMonth());
        assertEquals(2024, b.getYear());
        assertEquals(500.0, b.getBudgetAmount(), 0.001);
        assertEquals("Investimenti", b.getCategoryName());
        assertEquals(123.45, b.getSpentAmount(), 0.001);
    }

    // ============================================================
    // TEST DI INTEGRAZIONE - Scenari realistici
    // ============================================================

    @Test
    @DisplayName("Scenario: Budget Alimentari con spese progressive")
    void scenarioFoodBudgetWithProgressiveSpending() {
        Budget alimentari = new Budget(1, 1, 1, 12, 2024, 400.0, "Alimentari", 0.0);

        // Inizio mese: nulla speso
        assertEquals(0.0, alimentari.getProgress(), 0.001);
        assertEquals(400.0, alimentari.getRemaining(), 0.001);

        // Meta' mese: speso 180€
        alimentari.setSpentAmount(180.0);
        assertEquals(0.45, alimentari.getProgress(), 0.001);
        assertEquals(220.0, alimentari.getRemaining(), 0.001);

        // Tre quarti: speso 320€
        alimentari.setSpentAmount(320.0);
        assertEquals(0.80, alimentari.getProgress(), 0.001);
        assertEquals(80.0, alimentari.getRemaining(), 0.001);

        // Fine mese: superato, speso 450€
        alimentari.setSpentAmount(450.0);
        assertEquals(1.125, alimentari.getProgress(), 0.001);
        assertEquals(-50.0, alimentari.getRemaining(), 0.001);
    }

    @Test
    @DisplayName("Scenario: Multipli budget con diverse situazioni")
    void scenarioMultipleBudgetsWithDifferentStatuses() {
        Budget trasporti = new Budget(2, 2, 1, 12, 2024, 150.0, "Trasporti", 50.0);
        Budget svago = new Budget(3, 4, 1, 12, 2024, 200.0, "Svago", 200.0);
        Budget salute = new Budget(4, 5, 1, 12, 2024, 100.0, "Salute", 150.0);

        // Trasporti: 33% usato, 100€ rimanenti
        assertTrue(trasporti.getProgress() < 0.5, "Trasporti sotto il 50%");
        assertEquals(100.0, trasporti.getRemaining(), 0.001);

        // Svago: 100% usato esattamente
        assertEquals(1.0, svago.getProgress(), 0.001);
        assertEquals(0.0, svago.getRemaining(), 0.001);

        // Salute: 150% superato
        assertTrue(salute.getProgress() > 1.0, "Salute ha superato il budget");
        assertEquals(-50.0, salute.getRemaining(), 0.001);
    }
}
