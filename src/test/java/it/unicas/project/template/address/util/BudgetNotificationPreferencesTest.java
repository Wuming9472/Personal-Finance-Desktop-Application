package it.unicas.project.template.address.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BudgetNotificationPreferencesTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetPreferences() {
        BudgetNotificationPreferences.resetForTesting(tempDir.resolve("prefs.txt").toString());
    }

    @AfterEach
    void cleanupSingleton() {
        BudgetNotificationPreferences.resetForTesting(null);
    }

    @Test
    @DisplayName("should mark and unmark notifications with persistence")
    void markAndUnmarkCycle() {
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        assertFalse(prefs.wasAlreadyNotifiedThisMonth(3));
        prefs.markAsNotified(3);
        assertTrue(prefs.wasAlreadyNotifiedThisMonth(3));

        // reload from disk to ensure persistence
        BudgetNotificationPreferences.resetForTesting(tempDir.resolve("prefs.txt").toString());
        prefs = BudgetNotificationPreferences.getInstance();
        assertTrue(prefs.wasAlreadyNotifiedThisMonth(3));

        prefs.unmarkAsNotified(3);
        assertFalse(prefs.wasAlreadyNotifiedThisMonth(3));
    }

    @Test
    @DisplayName("should respect dismiss choice until budget increases")
    void dismissNotificationsUntilBudgetRaised() {
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        prefs.dismissNotificationForCurrentMonth(2, 250.0);
        assertTrue(prefs.isNotificationDismissedForCurrentMonth(2, 200.0));

        // When the budget limit grows, the dismissal should be cleared
        assertFalse(prefs.isNotificationDismissedForCurrentMonth(2, 300.0));
        assertFalse(prefs.isNotificationDismissedForCurrentMonth(2, 250.0));
    }

    @Test
    @DisplayName("should clean old months from both tracked maps")
    void cleanOldMonths() throws Exception {
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();
        String staleMonth = YearMonth.now().minusMonths(5).toString();

        setInternalMaps(prefs, staleMonth);
        assertFalse(prefs.getNotifiedExceededCategoriesSnapshot().isEmpty());
        assertFalse(prefs.getDismissedNotificationsSnapshot().isEmpty());

        prefs.cleanOldMonths();

        assertTrue(prefs.getNotifiedExceededCategoriesSnapshot().isEmpty());
        assertTrue(prefs.getDismissedNotificationsSnapshot().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private void setInternalMaps(BudgetNotificationPreferences prefs, String monthKey) throws Exception {
        Field notifiedField = BudgetNotificationPreferences.class.getDeclaredField("notifiedExceededCategories");
        Field dismissedField = BudgetNotificationPreferences.class.getDeclaredField("dismissedNotifications");
        notifiedField.setAccessible(true);
        dismissedField.setAccessible(true);

        Map<String, Set<Integer>> notified = (Map<String, Set<Integer>>) notifiedField.get(prefs);
        Map<String, Map<Integer, Double>> dismissed = (Map<String, Map<Integer, Double>>) dismissedField.get(prefs);

        notified.put(monthKey, Set.of(1, 2));
        Map<Integer, Double> dismissedMap = new HashMap<>();
        dismissedMap.put(5, 100.0);
        dismissed.put(monthKey, dismissedMap);
    }
}
