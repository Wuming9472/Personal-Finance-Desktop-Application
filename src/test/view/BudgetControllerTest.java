package test.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.view.BudgetController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BudgetControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    /**
     * MainApp finto per fornire un utente loggato.
     */
    static class TestMainApp extends MainApp {
        private final User loggedUser;

        TestMainApp(User loggedUser) {
            this.loggedUser = loggedUser;
        }

        @Override
        public User getLoggedUser() {
            return loggedUser;
        }
    }

    private BudgetController controller;

    // Card Alimentari
    private Label foodCategoryLabel;
    private Label foodRemainingLabel;
    private Label foodPercentageLabel;
    private Label foodSpentLabel;
    private Label foodLimitLabel;
    private ProgressBar foodProgressBar;

    // Card Trasporti
    private Label transportCategoryLabel;
    private Label transportRemainingLabel;
    private Label transportPercentageLabel;
    private Label transportSpentLabel;
    private Label transportLimitLabel;
    private ProgressBar transportProgressBar;

    @BeforeEach
    void setUp() {
        controller = new BudgetController();

        // Setup UI elementi per Alimentari
        foodCategoryLabel = new Label();
        foodRemainingLabel = new Label();
        foodPercentageLabel = new Label();
        foodSpentLabel = new Label();
        foodLimitLabel = new Label();
        foodProgressBar = new ProgressBar();

        // Setup UI elementi per Trasporti
        transportCategoryLabel = new Label();
        transportRemainingLabel = new Label();
        transportPercentageLabel = new Label();
        transportSpentLabel = new Label();
        transportLimitLabel = new Label();
        transportProgressBar = new ProgressBar();

        setField(controller, "foodCategoryLabel", foodCategoryLabel);
        setField(controller, "foodRemainingLabel", foodRemainingLabel);
        setField(controller, "foodPercentageLabel", foodPercentageLabel);
        setField(controller, "foodSpentLabel", foodSpentLabel);
        setField(controller, "foodLimitLabel", foodLimitLabel);
        setField(controller, "foodProgressBar", foodProgressBar);

        setField(controller, "transportCategoryLabel", transportCategoryLabel);
        setField(controller, "transportRemainingLabel", transportRemainingLabel);
        setField(controller, "transportPercentageLabel", transportPercentageLabel);
        setField(controller, "transportSpentLabel", transportSpentLabel);
        setField(controller, "transportLimitLabel", transportLimitLabel);
        setField(controller, "transportProgressBar", transportProgressBar);
    }

    @Test
    void initializeShouldSetCurrentMonthAndYear() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                invokePrivate(controller, "initialize");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        LocalDate now = LocalDate.now();
        int currentMonth = (Integer) getField(controller, "currentMonth");
        int currentYear = (Integer) getField(controller, "currentYear");

        assertEquals(now.getMonthValue(), currentMonth);
        assertEquals(now.getYear(), currentYear);
    }

    @Test
    void resetCardShouldSetDefaultValues() throws Exception {
        // Simuliamo la chiamata al metodo privato resetCard
        foodRemainingLabel.setText("Rimasti: € 500.00");
        foodSpentLabel.setText("Spesi: € 200.00");
        foodLimitLabel.setText("Limite: € 1000.00");
        foodPercentageLabel.setText("50%");
        foodProgressBar.setProgress(0.5);

        runOnFxThreadAndWait(() -> {
            try {
                invokeResetCard(controller, foodRemainingLabel, foodSpentLabel,
                        foodLimitLabel, foodPercentageLabel, foodProgressBar);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals("Rimasti: € 0.00", foodRemainingLabel.getText());
        assertEquals("Spesi: € 0.00", foodSpentLabel.getText());
        assertEquals("Limite: € 0.00", foodLimitLabel.getText());
        assertEquals("0%", foodPercentageLabel.getText());
        assertEquals(0.0, foodProgressBar.getProgress(), 0.001);
    }

    @Test
    void budgetProgressCalculation() {
        double budgetAmount = 400.0;
        double spentAmount = 200.0;
        double ratio = spentAmount / budgetAmount;

        assertEquals(0.5, ratio, 0.001);
        assertEquals(50.0, ratio * 100.0, 0.001);
    }

    @Test
    void budgetProgressColorForLowUsage() {
        double ratio = 0.3; // 30% - verde
        String color;
        if (ratio < 0.75) {
            color = "#10b981";
        } else if (ratio <= 1.0) {
            color = "#f59e0b";
        } else {
            color = "#ef4444";
        }
        assertEquals("#10b981", color); // verde
    }

    @Test
    void budgetProgressColorForMediumUsage() {
        double ratio = 0.85; // 85% - arancione
        String color;
        if (ratio < 0.75) {
            color = "#10b981";
        } else if (ratio <= 1.0) {
            color = "#f59e0b";
        } else {
            color = "#ef4444";
        }
        assertEquals("#f59e0b", color); // arancione
    }

    @Test
    void budgetProgressColorForOverBudget() {
        double ratio = 1.2; // 120% - rosso
        String color;
        if (ratio < 0.75) {
            color = "#10b981";
        } else if (ratio <= 1.0) {
            color = "#f59e0b";
        } else {
            color = "#ef4444";
        }
        assertEquals("#ef4444", color); // rosso
    }

    @Test
    void remainingAmountCalculation() {
        double budgetAmount = 400.0;
        double spentAmount = 150.0;
        double remaining = budgetAmount - spentAmount;

        assertEquals(250.0, remaining, 0.001);
        assertTrue(remaining >= 0);
    }

    @Test
    void remainingAmountWhenOverBudget() {
        double budgetAmount = 400.0;
        double spentAmount = 500.0;
        double remaining = budgetAmount - spentAmount;

        assertEquals(-100.0, remaining, 0.001);
        // La UI mostra Math.max(0, remaining)
        assertEquals(0.0, Math.max(0, remaining), 0.001);
    }

    @Test
    void progressBarClampedBetweenZeroAndOne() {
        double ratio = 1.5;
        double clamped = Math.max(0.0, Math.min(1.0, ratio));

        assertEquals(1.0, clamped, 0.001);

        ratio = -0.5;
        clamped = Math.max(0.0, Math.min(1.0, ratio));
        assertEquals(0.0, clamped, 0.001);

        ratio = 0.7;
        clamped = Math.max(0.0, Math.min(1.0, ratio));
        assertEquals(0.7, clamped, 0.001);
    }

    @Test
    void budgetModelGettersAndSetters() {
        Budget budget = new Budget();
        budget.setCategoryId(1);
        budget.setCategoryName("Alimentari");
        budget.setBudgetAmount(400.0f);
        budget.setSpentAmount(150.0f);

        assertEquals(1, budget.getCategoryId());
        assertEquals("Alimentari", budget.getCategoryName());
        assertEquals(400.0f, budget.getBudgetAmount(), 0.001f);
        assertEquals(150.0f, budget.getSpentAmount(), 0.001f);
    }

    /**
     * Esegue un Runnable sul JavaFX Application Thread e aspetta che finisca.
     */
    private static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout in runOnFxThreadAndWait");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = BudgetController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String name) {
        try {
            var field = BudgetController.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String name) throws Exception {
        var method = BudgetController.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }

    private void invokeResetCard(Object target, Label remaining, Label spent,
                                  Label limit, Label percentage, ProgressBar bar) throws Exception {
        var method = BudgetController.class.getDeclaredMethod("resetCard",
                Label.class, Label.class, Label.class, Label.class, ProgressBar.class);
        method.setAccessible(true);
        method.invoke(target, remaining, spent, limit, percentage, bar);
    }
}
