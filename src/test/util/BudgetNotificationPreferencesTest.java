package test.util;

import it.unicas.project.template.address.util.BudgetNotificationPreferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class BudgetNotificationPreferencesTest {

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("prefs", ".txt");
        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
        BudgetNotificationPreferences.resetForTesting(null);
    }

    @Test
    void markAndUnmarkNotifications() {
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        assertFalse(prefs.wasAlreadyNotifiedThisMonth(3));
        prefs.markAsNotified(3);
        assertTrue(prefs.wasAlreadyNotifiedThisMonth(3));

        prefs.unmarkAsNotified(3);
        assertFalse(prefs.wasAlreadyNotifiedThisMonth(3));
    }

    @Test
    void dismissNotificationRestoredWhenBudgetIncreases() {
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        prefs.dismissNotificationForCurrentMonth(2, 200.0);
        assertTrue(prefs.isNotificationDismissedForCurrentMonth(2, 200.0));

        // Increasing the budget should clear the dismissal
        assertFalse(prefs.isNotificationDismissedForCurrentMonth(2, 300.0));
    }

    @Test
    void cleanOldMonthsRemovesStaleEntries() {
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        injectOldMonthData(prefs);

        prefs.cleanOldMonths();

        assertFalse(prefs.getNotifiedExceededCategoriesSnapshot().containsKey("2020-01"));
        assertFalse(prefs.getDismissedNotificationsSnapshot().containsKey("2020-01"));
    }

    private void injectOldMonthData(BudgetNotificationPreferences prefs) {
        try {
            Field notifiedField = BudgetNotificationPreferences.class.getDeclaredField("notifiedExceededCategories");
            Field dismissedField = BudgetNotificationPreferences.class.getDeclaredField("dismissedNotifications");
            notifiedField.setAccessible(true);
            dismissedField.setAccessible(true);

            HashMap<String, HashSet<Integer>> notified = new HashMap<>();
            notified.put("2020-01", new HashSet<>(java.util.Set.of(1, 2)));
            notifiedField.set(prefs, notified);

            HashMap<String, java.util.Map<Integer, Double>> dismissed = new HashMap<>();
            dismissed.put("2020-01", new java.util.HashMap<>(java.util.Map.of(1, 100.0)));
            dismissedField.set(prefs, dismissed);
        } catch (ReflectiveOperationException e) {
            fail("Unable to inject test data: " + e.getMessage());
        }
    }
}
