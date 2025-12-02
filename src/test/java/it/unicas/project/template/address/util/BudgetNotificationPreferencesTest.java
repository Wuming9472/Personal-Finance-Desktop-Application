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
    public void canEnableAndDisableNotificationsPerCategory() throws IOException {
        BudgetNotificationPreferences prefs = newPreferencesWithTempFile();

        Assertions.assertFalse(prefs.isNotificationDisabled(3));
        prefs.disableNotificationForCategory(3);
        Assertions.assertTrue(prefs.isNotificationDisabled(3));

        prefs.enableNotificationForCategory(3);
        Assertions.assertFalse(prefs.isNotificationDisabled(3));
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
        Files.write(tempFile, (
                "notified." + staleMonth + "=2,4\n" +
                "disabled=9\n").getBytes());

        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        Map<String, Set<Integer>> snapshot = prefs.getNotifiedExceededCategoriesSnapshot();
        Assertions.assertTrue(snapshot.isEmpty(), "I mesi più vecchi di 3 mesi devono essere rimossi");
        Assertions.assertTrue(prefs.getDisabledCategoriesSnapshot().contains(9), "Le preferenze valide devono restare");
    }

    @Test
    public void persistsChangesToDisk() throws IOException {
        Path tempFile = Files.createTempFile("prefs", ".txt");
        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        prefs.disableNotificationForCategory(12);
        prefs.markAsNotified(5);

        // Forza il reload dal file
        BudgetNotificationPreferences.resetForTesting(tempFile.toString());
        BudgetNotificationPreferences reloaded = BudgetNotificationPreferences.getInstance();

        Assertions.assertTrue(reloaded.isNotificationDisabled(12));
        Assertions.assertTrue(reloaded.wasAlreadyNotifiedThisMonth(5));
    }
}
