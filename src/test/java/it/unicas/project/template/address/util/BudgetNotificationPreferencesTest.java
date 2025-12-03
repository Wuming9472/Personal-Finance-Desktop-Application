package it.unicas.project.template.address.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

public class BudgetNotificationPreferencesTest {

    private BudgetNotificationPreferences newPreferencesWithTempFile() throws IOException {
        Path tempFile = Files.createTempFile("prefs", ".txt");
        Files.deleteIfExists(tempFile);
        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        return BudgetNotificationPreferences.getInstance();
    }

    @Test
    public void dismissalsAreScopedToMonthAndBudgetAmount() throws IOException {
        BudgetNotificationPreferences prefs = newPreferencesWithTempFile();
        YearMonth current = YearMonth.now();

        prefs.dismissNotificationForCurrentMonth(3, 200.0);
        Assertions.assertTrue(prefs.isNotificationDismissedForCurrentMonth(3, 200.0));

        // Limite aumentato: la dismissione viene rimossa
        Assertions.assertFalse(prefs.isNotificationDismissedForCurrentMonth(3, 250.0));
        Assertions.assertFalse(prefs.getDismissedNotificationsSnapshot().containsKey(current.toString()));

        // Dati di un mese precedente non interferiscono con il mese corrente
        Path tempFile = Files.createTempFile("prefs-stored", ".txt");
        String lastMonth = YearMonth.now().minusMonths(1).toString();
        Files.write(tempFile, ("dismissed." + lastMonth + "=3:300.0\n").getBytes());

        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        BudgetNotificationPreferences reloaded = BudgetNotificationPreferences.getInstance();
        Assertions.assertFalse(reloaded.isNotificationDismissedForCurrentMonth(3, 200.0));
    }

    @Test
    public void tracksNotificationsForCurrentMonth() throws IOException {
        BudgetNotificationPreferences prefs = newPreferencesWithTempFile();

        Assertions.assertFalse(prefs.wasAlreadyNotifiedThisMonth(7));
        prefs.markAsNotified(7);
        Assertions.assertTrue(prefs.wasAlreadyNotifiedThisMonth(7));

        prefs.unmarkAsNotified(7);
        Assertions.assertFalse(prefs.wasAlreadyNotifiedThisMonth(7));
    }

    @Test
    public void cleansOldMonthsWhenLoadingPreferences() throws IOException {
        Path tempFile = Files.createTempFile("prefs", ".txt");
        String staleMonth = YearMonth.now().minusMonths(4).toString();
        String recentMonth = YearMonth.now().minusMonths(1).toString();
        Files.write(tempFile, (
                "notified." + staleMonth + "=2,4\n" +
                "dismissed." + staleMonth + "=1:100.0\n" +
                "dismissed." + recentMonth + "=2:200.0\n"
        ).getBytes());

        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        Map<String, Set<Integer>> notifiedSnapshot = prefs.getNotifiedExceededCategoriesSnapshot();
        Map<String, Map<Integer, Double>> dismissedSnapshot = prefs.getDismissedNotificationsSnapshot();

        Assertions.assertTrue(notifiedSnapshot.isEmpty(), "I mesi più vecchi di 3 mesi devono essere rimossi");
        Assertions.assertTrue(dismissedSnapshot.containsKey(recentMonth), "I mesi recenti devono essere mantenuti");
    }

    @Test
    public void persistsChangesToDisk() throws IOException {
        Path tempFile = Files.createTempFile("prefs", ".txt");
        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        prefs.dismissNotificationForCurrentMonth(12, 150.0);
        prefs.markAsNotified(5);

        // Forza il reload dal file
        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        BudgetNotificationPreferences reloaded = BudgetNotificationPreferences.getInstance();

        Assertions.assertTrue(reloaded.isNotificationDismissedForCurrentMonth(12, 150.0));
        Assertions.assertTrue(reloaded.wasAlreadyNotifiedThisMonth(5));
    }
}
