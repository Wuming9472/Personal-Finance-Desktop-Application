package it.unicas.project.template.address.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BudgetTest {

    @Test
    @DisplayName("should compute remaining amount and progress ratio")
    void remainingAndProgress() {
        Budget budget = new Budget(1, 2, 3, 4, 2024, 500.0, "Trasporti", 250.0);

        assertEquals(250.0, budget.getRemaining());
        assertEquals(0.5, budget.getProgress());
    }

    @Test
    @DisplayName("should handle zero budget and overspending")
    void zeroBudgetAndOverspend() {
        Budget zeroBudget = new Budget(1, 1, 1, 1, 2024, 0.0, "Test", 100.0);
        assertEquals(0.0, zeroBudget.getProgress());
        assertEquals(-100.0, zeroBudget.getRemaining());

        Budget overspent = new Budget(2, 1, 1, 1, 2024, 200.0, "Test", 260.0);
        assertTrue(overspent.getProgress() > 1.0);
        assertEquals(-60.0, overspent.getRemaining());
    }
}
