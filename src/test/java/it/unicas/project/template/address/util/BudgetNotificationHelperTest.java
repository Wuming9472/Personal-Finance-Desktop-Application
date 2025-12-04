package it.unicas.project.template.address.util;

import it.unicas.project.template.address.model.Budget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test JUnit per la classe BudgetNotificationHelper.
 * Verifica la logica di gestione dei budget:
 * - Rilevamento superamento budget (isBudgetExceeded)
 * - Gestione delle notifiche per categoria
 * - Esclusione della categoria Stipendio (ID 6)
 */
@DisplayName("Test Gestione Budget - BudgetNotificationHelper")
public class BudgetNotificationHelperTest {

    private Budget budgetAlimentari;
    private Budget budgetTrasporti;
    private Budget budgetStipendio;
    private Budget budgetSvago;

    @BeforeEach
    void setUp() {
        // Budget Alimentari: limite 400€
        budgetAlimentari = new Budget(1, 1, 1, 12, 2024, 400.0, "Alimentari", 0.0);

        // Budget Trasporti: limite 150€
        budgetTrasporti = new Budget(2, 2, 1, 12, 2024, 150.0, "Trasporti", 0.0);

        // Budget Stipendio (categoria 6): limite 0€ (categoria speciale)
        budgetStipendio = new Budget(3, 6, 1, 12, 2024, 0.0, "Stipendio", 0.0);

        // Budget Svago: limite 200€
        budgetSvago = new Budget(4, 4, 1, 12, 2024, 200.0, "Svago", 0.0);
    }

    // ============================================================
    // TEST PER isBudgetExceeded() - Verifica superamento budget
    // ============================================================

    @Test
    @DisplayName("isBudgetExceeded() restituisce false quando speso < limite")
    void budgetNotExceededWhenUnderLimit() {
        budgetAlimentari.setSpentAmount(200.0);
        budgetAlimentari.setBudgetAmount(400.0);

        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            "Budget non superato quando speso (200) < limite (400)");
    }

    @Test
    @DisplayName("isBudgetExceeded() restituisce false quando speso = limite")
    void budgetNotExceededWhenEqualToLimit() {
        budgetAlimentari.setSpentAmount(400.0);
        budgetAlimentari.setBudgetAmount(400.0);

        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            "Budget non superato quando speso (400) = limite (400)");
    }

    @Test
    @DisplayName("isBudgetExceeded() restituisce true quando speso > limite")
    void budgetExceededWhenOverLimit() {
        budgetAlimentari.setSpentAmount(450.0);
        budgetAlimentari.setBudgetAmount(400.0);

        assertTrue(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            "Budget superato quando speso (450) > limite (400)");
    }

    @Test
    @DisplayName("isBudgetExceeded() restituisce false per budget null")
    void budgetNotExceededWhenNull() {
        assertFalse(BudgetNotificationHelper.isBudgetExceeded(null),
            "Deve restituire false per budget null");
    }

    @Test
    @DisplayName("isBudgetExceeded() restituisce false per categoria Stipendio (ID 6)")
    void budgetNotExceededForSalaryCategory() {
        budgetStipendio.setSpentAmount(5000.0);
        budgetStipendio.setBudgetAmount(100.0);

        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetStipendio),
            "Categoria Stipendio (ID 6) deve essere sempre ignorata");
    }

    @Test
    @DisplayName("isBudgetExceeded() restituisce false quando limite e' 0")
    void budgetNotExceededWhenLimitIsZero() {
        budgetAlimentari.setSpentAmount(100.0);
        budgetAlimentari.setBudgetAmount(0.0);

        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            "Budget con limite 0 non puo' essere superato");
    }

    @ParameterizedTest
    @DisplayName("isBudgetExceeded() scenari multipli")
    @CsvSource({
        "100.0, 200.0, false",    // Sotto limite
        "200.0, 200.0, false",    // Esattamente al limite
        "200.01, 200.0, true",    // Appena superato
        "500.0, 200.0, true",     // Molto superato
        "0.0, 100.0, false",      // Nulla speso
        "100.0, 0.0, false"       // Limite zero
    })
    void budgetExceededVariousScenarios(double spent, double limit, boolean expected) {
        budgetAlimentari.setSpentAmount(spent);
        budgetAlimentari.setBudgetAmount(limit);

        assertEquals(expected, BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            String.format("Con speso=%.2f e limite=%.2f, exceeded deve essere %s", spent, limit, expected));
    }

    // ============================================================
    // TEST PER checkAndNotifyForCategory() - Logica notifiche
    // ============================================================

    @Test
    @DisplayName("checkAndNotifyForCategory() restituisce false per lista null")
    void notifyReturnsFalseForNullList() {
        assertFalse(BudgetNotificationHelper.checkAndNotifyForCategory(null, 1));
    }

    @Test
    @DisplayName("checkAndNotifyForCategory() restituisce false per lista vuota")
    void notifyReturnsFalseForEmptyList() {
        assertFalse(BudgetNotificationHelper.checkAndNotifyForCategory(new ArrayList<>(), 1));
    }

    @Test
    @DisplayName("checkAndNotifyForCategory() restituisce false per categoria Stipendio")
    void notifyReturnsFalseForSalaryCategory() {
        budgetStipendio.setSpentAmount(5000.0);
        budgetStipendio.setBudgetAmount(100.0);

        List<Budget> budgets = Arrays.asList(budgetStipendio);

        assertFalse(BudgetNotificationHelper.checkAndNotifyForCategory(budgets, 6),
            "La categoria Stipendio (6) deve essere sempre ignorata");
    }

    @Test
    @DisplayName("checkAndNotifyForCategory() restituisce false quando categoria non trovata")
    void notifyReturnsFalseWhenCategoryNotFound() {
        List<Budget> budgets = Arrays.asList(budgetAlimentari, budgetTrasporti);

        assertFalse(BudgetNotificationHelper.checkAndNotifyForCategory(budgets, 99),
            "Deve restituire false se la categoria non esiste nella lista");
    }

    @Test
    @DisplayName("checkAndNotifyForCategory() restituisce false quando budget non superato")
    void notifyReturnsFalseWhenBudgetNotExceeded() {
        budgetAlimentari.setSpentAmount(200.0);
        budgetAlimentari.setBudgetAmount(400.0);

        List<Budget> budgets = Arrays.asList(budgetAlimentari);

        assertFalse(BudgetNotificationHelper.checkAndNotifyForCategory(budgets, 1),
            "Nessuna notifica se il budget non e' superato");
    }

    // ============================================================
    // TEST INTEGRAZIONE - Scenari realistici
    // ============================================================

    @Test
    @DisplayName("Scenario: Multipli budget con situazioni diverse")
    void scenarioMultipleBudgetsWithDifferentStatuses() {
        // Alimentari: 80% usato (non superato)
        budgetAlimentari.setSpentAmount(320.0);
        budgetAlimentari.setBudgetAmount(400.0);

        // Trasporti: 100% usato esattamente (non superato)
        budgetTrasporti.setSpentAmount(150.0);
        budgetTrasporti.setBudgetAmount(150.0);

        // Svago: 120% usato (SUPERATO!)
        budgetSvago.setSpentAmount(240.0);
        budgetSvago.setBudgetAmount(200.0);

        // Verifica stati
        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari));
        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetTrasporti));
        assertTrue(BudgetNotificationHelper.isBudgetExceeded(budgetSvago));
    }

    @Test
    @DisplayName("Scenario: Budget con valori limite")
    void scenarioBoundaryValues() {
        // Esattamente al limite
        budgetAlimentari.setSpentAmount(400.0);
        budgetAlimentari.setBudgetAmount(400.0);
        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            "Al 100% esatto non e' superato");

        // Un centesimo sopra
        budgetAlimentari.setSpentAmount(400.01);
        assertTrue(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            "A 400.01 su 400 e' superato");

        // Un centesimo sotto
        budgetAlimentari.setSpentAmount(399.99);
        assertFalse(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
            "A 399.99 su 400 non e' superato");
    }

    @Test
    @DisplayName("Scenario: Budget con percentuali progressive")
    void scenarioProgressiveSpending() {
        double[] percentages = {0.0, 0.25, 0.50, 0.75, 0.99, 1.0, 1.01, 1.5, 2.0};
        boolean[] expectedExceeded = {false, false, false, false, false, false, true, true, true};

        for (int i = 0; i < percentages.length; i++) {
            budgetAlimentari.setSpentAmount(400.0 * percentages[i]);
            budgetAlimentari.setBudgetAmount(400.0);

            assertEquals(expectedExceeded[i],
                BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari),
                String.format("Al %.0f%% il budget %s essere superato",
                    percentages[i] * 100,
                    expectedExceeded[i] ? "DEVE" : "NON deve"));
        }
    }

    @Test
    @DisplayName("Scenario: Tutte le categorie standard")
    void scenarioAllStandardCategories() {
        List<Budget> allBudgets = Arrays.asList(
            new Budget(1, 1, 1, 12, 2024, 400.0, "Alimentari", 450.0),   // Superato
            new Budget(2, 2, 1, 12, 2024, 150.0, "Trasporti", 100.0),     // OK
            new Budget(3, 3, 1, 12, 2024, 300.0, "Bollette", 300.0),      // Al limite
            new Budget(4, 4, 1, 12, 2024, 200.0, "Svago", 250.0),         // Superato
            new Budget(5, 5, 1, 12, 2024, 100.0, "Salute", 50.0),         // OK
            new Budget(6, 6, 1, 12, 2024, 0.0, "Stipendio", 3000.0),      // Ignorato
            new Budget(7, 7, 1, 12, 2024, 200.0, "Investimenti", 200.0),  // Al limite
            new Budget(8, 8, 1, 12, 2024, 100.0, "Altro", 120.0)          // Superato
        );

        // Conta budget superati (escluso Stipendio)
        long exceededCount = allBudgets.stream()
            .filter(BudgetNotificationHelper::isBudgetExceeded)
            .count();

        assertEquals(3, exceededCount,
            "Devono esserci 3 budget superati (Alimentari, Svago, Altro)");
    }

    @Test
    @DisplayName("Verifica che Alimentari, Svago e Altro siano superati")
    void verifySpecificCategoriesExceeded() {
        Budget alimentari = new Budget(1, 1, 1, 12, 2024, 400.0, "Alimentari", 450.0);
        Budget svago = new Budget(4, 4, 1, 12, 2024, 200.0, "Svago", 250.0);
        Budget altro = new Budget(8, 8, 1, 12, 2024, 100.0, "Altro", 120.0);
        Budget trasporti = new Budget(2, 2, 1, 12, 2024, 150.0, "Trasporti", 100.0);

        assertTrue(BudgetNotificationHelper.isBudgetExceeded(alimentari), "Alimentari superato");
        assertTrue(BudgetNotificationHelper.isBudgetExceeded(svago), "Svago superato");
        assertTrue(BudgetNotificationHelper.isBudgetExceeded(altro), "Altro superato");
        assertFalse(BudgetNotificationHelper.isBudgetExceeded(trasporti), "Trasporti OK");
    }

    // ============================================================
    // TEST PER calcoli correlati (usando Budget.getProgress())
    // ============================================================

    @Test
    @DisplayName("Integrazione: isBudgetExceeded coerente con Budget.getProgress()")
    void integrationWithBudgetProgress() {
        double[] spentAmounts = {0.0, 200.0, 400.0, 500.0, 600.0};

        for (double spent : spentAmounts) {
            budgetAlimentari.setSpentAmount(spent);
            budgetAlimentari.setBudgetAmount(400.0);

            boolean exceeded = BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari);
            double progress = budgetAlimentari.getProgress();

            // isBudgetExceeded e' true se e solo se progress > 1.0
            assertEquals(progress > 1.0, exceeded,
                String.format("Con speso=%.2f, progress=%.2f, exceeded deve essere %s",
                    spent, progress, progress > 1.0));
        }
    }

    @Test
    @DisplayName("Integrazione: Verifica coerenza con Budget.getRemaining()")
    void integrationWithBudgetRemaining() {
        budgetAlimentari.setSpentAmount(500.0);
        budgetAlimentari.setBudgetAmount(400.0);

        assertTrue(BudgetNotificationHelper.isBudgetExceeded(budgetAlimentari));
        assertEquals(-100.0, budgetAlimentari.getRemaining(), 0.001);
        assertTrue(budgetAlimentari.getRemaining() < 0,
            "Remaining negativo deve corrispondere a budget superato");
    }
}
