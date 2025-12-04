package test.java.it.unicas.project.template.address.model;

import it.unicas.project.template.address.model.Budget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Verifica dei metodi di gestione del budget.
 */
public class BudgetTest {

    @Test
    public void progressUsesBudgetAndSpentAmountsSafely() {
        Budget budget = new Budget(1, 1, 1, 12, 2024, 400.0, "Alimentari", 0.0);

        budget.setSpentAmount(0.0);
        Assertions.assertEquals(0.0, budget.getProgress(), 0.001);

        budget.setSpentAmount(200.0);
        Assertions.assertEquals(0.5, budget.getProgress(), 0.001);

        budget.setSpentAmount(500.0);
        Assertions.assertEquals(1.25, budget.getProgress(), 0.001);

        budget.setBudgetAmount(0.0);
        Assertions.assertEquals(0.0, budget.getProgress(), 0.001);
    }

    @Test
    public void remainingReflectsOverspendAndSurplus() {
        Budget budget = new Budget(1, 1, 1, 12, 2024, 300.0, "Trasporti", 0.0);

        budget.setSpentAmount(100.0);
        Assertions.assertEquals(200.0, budget.getRemaining(), 0.001);

        budget.setSpentAmount(300.0);
        Assertions.assertEquals(0.0, budget.getRemaining(), 0.001);

        budget.setSpentAmount(450.0);
        Assertions.assertEquals(-150.0, budget.getRemaining(), 0.001);
    }

    @Test
    public void defaultConstructorInitializesSafeDefaults() {
        Budget budget = new Budget();

        Assertions.assertEquals(0, budget.getBudgetId());
        Assertions.assertEquals(0, budget.getCategoryId());
        Assertions.assertEquals(0, budget.getUserId());
        Assertions.assertEquals(0, budget.getMonth());
        Assertions.assertEquals(0, budget.getYear());
        Assertions.assertEquals(0.0, budget.getBudgetAmount(), 0.001);
        Assertions.assertEquals("", budget.getCategoryName());
        Assertions.assertEquals(0.0, budget.getSpentAmount(), 0.001);
    }

    @Test
    public void settersUpdateAllFields() {
        Budget budget = new Budget();

        budget.setBudgetId(10);
        budget.setCategoryId(5);
        budget.setUserId(2);
        budget.setMonth(11);
        budget.setYear(2025);
        budget.setBudgetAmount(250.0);
        budget.setCategoryName("Svago");
        budget.setSpentAmount(75.0);

        Assertions.assertEquals(10, budget.getBudgetId());
        Assertions.assertEquals(5, budget.getCategoryId());
        Assertions.assertEquals(2, budget.getUserId());
        Assertions.assertEquals(11, budget.getMonth());
        Assertions.assertEquals(2025, budget.getYear());
        Assertions.assertEquals(250.0, budget.getBudgetAmount(), 0.001);
        Assertions.assertEquals("Svago", budget.getCategoryName());
        Assertions.assertEquals(75.0, budget.getSpentAmount(), 0.001);
    }
}
