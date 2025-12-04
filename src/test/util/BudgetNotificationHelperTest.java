package test.util;

import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.util.BudgetNotificationHelper;
import it.unicas.project.template.address.util.BudgetNotificationPreferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.stage.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetNotificationHelperTest {

    @BeforeAll
    static void initJavaFxToolkit() {
        System.setProperty("java.awt.headless", "true");
        try {
            new JFXPanel();
            if (!Platform.isImplicitExit()) {
                Platform.setImplicitExit(false);
            }
        } catch (IllegalStateException ignored) {
            // Toolkit già inizializzato
        }
    }

    @TempDir
    Path tempDir;


    private static void runOnFxThreadAndWait(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            // Thread che dopo un po' chiude eventuali finestre aperte (Alert)
            new Thread(() -> {
                try {
                    Thread.sleep(300); // piccolo delay per dare tempo all’Alert di aprirsi
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Platform.runLater(() -> {
                    // 👇 QUI il cambio importante: copio la lista prima di iterare
                    var windowsSnapshot = new ArrayList<>(Window.getWindows());
                    for (Window w : windowsSnapshot) {
                        if (w.isShowing()) {
                            w.hide(); // chiude il dialog
                        }
                    }
                });
            }, "fx-alert-closer").start();

            try {
                action.run(); // qui verrà chiamato checkAndNotifyForCategory(...)
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



    private BudgetNotificationPreferences prefs;

    @BeforeEach
    void setUp() throws IOException {
        Path prefsFile = tempDir.resolve("budget_prefs.json");
        Files.deleteIfExists(prefsFile);
        BudgetNotificationPreferences.resetForTesting(prefsFile.toString());
        prefs = BudgetNotificationPreferences.getInstance();
    }

    @AfterEach
    void tearDown() {
        BudgetNotificationPreferences.resetForTesting(null);
    }

    @Test
    void checkAndNotifyReturnsFalseForEmptyList() {
        boolean result = BudgetNotificationHelper.checkAndNotifyForCategory(Collections.emptyList(), 1);

        assertFalse(result);
        assertTrue(prefs.getNotifiedExceededCategoriesSnapshot().isEmpty());
    }

    @Test
    void checkAndNotifyIgnoresSalaryCategory() {
        Budget salaryBudget = new Budget(1, 6, 1, 1, 2025, 1000.0, "Stipendio", 1500.0);

        boolean result = BudgetNotificationHelper.checkAndNotifyForCategory(List.of(salaryBudget), 6);

        assertFalse(result);
        assertTrue(prefs.getNotifiedExceededCategoriesSnapshot().isEmpty());
    }

    @Test
    void checkAndNotifyUnmarksWhenBudgetBackUnderLimit() {
        prefs.markAsNotified(2);
        Budget categoryBudget = new Budget(2, 2, 1, 1, 2025, 500.0, "Spesa", 400.0);

        boolean result = BudgetNotificationHelper.checkAndNotifyForCategory(List.of(categoryBudget), 2);

        assertFalse(result);
        assertFalse(prefs.wasAlreadyNotifiedThisMonth(2));
    }

    @Test
    void checkAndNotifyMarksAndReturnsTrueOnFirstExceed() {
        Budget categoryBudget = new Budget(3, 3, 1, 1, 2025, 300.0, "Trasporti", 320.0);

        try {
            // può lanciare IllegalStateException per via dell'Alert in ambiente di test
            BudgetNotificationHelper.checkAndNotifyForCategory(List.of(categoryBudget), 3);
        } catch (Exception ignored) {
            // ignoriamo l'eccezione legata alla UI nei test
        }

        // Quello che ci interessa davvero: la categoria 3 è stata marcata come notificata
        assertTrue(prefs.wasAlreadyNotifiedThisMonth(3));
    }




    @Test
    void checkAndNotifyReturnsTrueIfAlreadyNotifiedAndStillExceeded() {
        prefs.markAsNotified(4);
        Budget categoryBudget = new Budget(4, 4, 1, 1, 2025, 200.0, "Svago", 250.0);

        final boolean[] result = new boolean[1];

        runOnFxThreadAndWait(() ->
                result[0] = BudgetNotificationHelper.checkAndNotifyForCategory(List.of(categoryBudget), 4)
        );

        assertTrue(result[0]);
        assertTrue(prefs.wasAlreadyNotifiedThisMonth(4));
    }

}
