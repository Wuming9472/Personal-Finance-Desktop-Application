package it.unicas.project.template.address.util;

import java.io.*;
import java.time.YearMonth;
import java.util.*;

/**
 * Gestisce le preferenze per le notifiche di budget superato.
 * Tiene traccia di:
 * - Categorie già notificate come superate nel mese corrente
 * - Categorie per cui l'utente ha scelto "Non mostrare più" per il mese corrente
 */
public class BudgetNotificationPreferences {

    private static final String DEFAULT_PREFERENCES_FILE = "budget_notifications.json";
    private static String preferencesFile = DEFAULT_PREFERENCES_FILE;
    private static BudgetNotificationPreferences instance;

    // Mappa: mese-anno -> set di categoryId già notificate come superate
    // Esempio: "2025-11" -> {1, 3, 5}
    private Map<String, Set<Integer>> notifiedExceededCategories;

    // Mappa: mese-anno -> mappa categoria -> budgetAmount al momento del "Non mostrare più"
    private Map<String, Map<Integer, Double>> dismissedNotifications;

    private BudgetNotificationPreferences() {
        this.notifiedExceededCategories = new HashMap<>();
        this.dismissedNotifications = new HashMap<>();
        load();
    }

    public static synchronized BudgetNotificationPreferences getInstance() {
        if (instance == null) {
            instance = new BudgetNotificationPreferences();
        }
        return instance;
    }

    /**
     * Permette di reimpostare il file di preferenze e l'istanza singleton.
     * Usato dai test per lavorare su file temporanei senza toccare le preferenze reali.
     */
    public static synchronized void resetForTesting(String customFilePath) {
        preferencesFile = customFilePath != null ? customFilePath : DEFAULT_PREFERENCES_FILE;
        instance = null;
    }

    /**
     * Verifica se una categoria è già stata notificata come superata nel mese corrente
     */
    public boolean wasAlreadyNotifiedThisMonth(int categoryId) {
        String currentMonth = getCurrentMonthKey();
        Set<Integer> notifiedThisMonth = notifiedExceededCategories.get(currentMonth);
        return notifiedThisMonth != null && notifiedThisMonth.contains(categoryId);
    }

    /**
     * Segna una categoria come notificata per il mese corrente
     */
    public void markAsNotified(int categoryId) {
        String currentMonth = getCurrentMonthKey();
        notifiedExceededCategories.computeIfAbsent(currentMonth, k -> new HashSet<>()).add(categoryId);
        save();
    }

    /**
     * Rimuove la marcatura di notifica per una categoria nel mese corrente
     * (utile quando il budget torna sotto il limite)
     */
    public void unmarkAsNotified(int categoryId) {
        String currentMonth = getCurrentMonthKey();
        Set<Integer> notifiedThisMonth = notifiedExceededCategories.get(currentMonth);
        if (notifiedThisMonth != null) {
            notifiedThisMonth.remove(categoryId);
            if (notifiedThisMonth.isEmpty()) {
                notifiedExceededCategories.remove(currentMonth);
            }
            save();
        }
    }

    /**
     * Verifica se per il mese corrente l'utente ha scelto "Non mostrare più" per una categoria
     * con il limite indicato. Se il limite è stato aumentato rispetto a quello salvato, la
     * dismissione viene rimossa per permettere di notificare nuovamente.
     */
    public boolean isNotificationDismissedForCurrentMonth(int categoryId, double currentBudgetAmount) {
        String currentMonth = getCurrentMonthKey();
        Map<Integer, Double> dismissedThisMonth = dismissedNotifications.get(currentMonth);
        if (dismissedThisMonth == null) {
            return false;
        }

        Double dismissedAmount = dismissedThisMonth.get(categoryId);
        if (dismissedAmount == null) {
            return false;
        }

        if (currentBudgetAmount > dismissedAmount) {
            // Il limite è stato alzato: annulla la dismissione
            dismissedThisMonth.remove(categoryId);
            if (dismissedThisMonth.isEmpty()) {
                dismissedNotifications.remove(currentMonth);
            }
            save();
            return false;
        }

        return true;
    }

    /**
     * Registra la scelta "Non mostrare più" per il mese corrente includendo il limite attuale.
     */
    public void dismissNotificationForCurrentMonth(int categoryId, double budgetAmount) {
        String currentMonth = getCurrentMonthKey();
        dismissedNotifications.computeIfAbsent(currentMonth, k -> new HashMap<>())
            .put(categoryId, budgetAmount);
        save();
    }

    /**
     * Pulisce le notifiche di mesi vecchi (mantiene solo gli ultimi 3 mesi)
     */
    public void cleanOldMonths() {
        YearMonth current = YearMonth.now();
        Set<String> toRemove = new HashSet<>();

        for (String monthKey : new HashSet<>(notifiedExceededCategories.keySet())) {
            try {
                YearMonth monthYear = YearMonth.parse(monthKey);
                // Rimuovi se più vecchio di 3 mesi
                if (monthYear.isBefore(current.minusMonths(3))) {
                    toRemove.add(monthKey);
                }
            } catch (Exception e) {
                // Chiave malformata, rimuovi
                toRemove.add(monthKey);
            }
        }

        toRemove.forEach(notifiedExceededCategories::remove);
        toRemove.forEach(dismissedNotifications::remove);
        if (!toRemove.isEmpty()) {
            save();
        }
    }

    // Metodi di supporto per i test automatizzati (stessa package visibility)
    public Map<String, Set<Integer>> getNotifiedExceededCategoriesSnapshot() {
        Map<String, Set<Integer>> snapshot = new HashMap<>();
        notifiedExceededCategories.forEach((k, v) -> snapshot.put(k, new HashSet<>(v)));
        return snapshot;
    }

    public Map<String, Map<Integer, Double>> getDismissedNotificationsSnapshot() {
        Map<String, Map<Integer, Double>> snapshot = new HashMap<>();
        dismissedNotifications.forEach((month, map) -> snapshot.put(month, new HashMap<>(map)));
        return snapshot;
    }

    /**
     * Ottiene la chiave del mese corrente in formato "YYYY-MM"
     */
    private String getCurrentMonthKey() {
        return YearMonth.now().toString();
    }

    /**
     * Salva le preferenze su file di testo
     * Formato:
     * notified.2025-11=1,3,5
     * notified.2025-12=2,4
     * dismissed.2025-12=3:400.0,4:250.0
     */
    private void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(preferencesFile))) {
            // Salva categorie notificate per mese
            for (Map.Entry<String, Set<Integer>> entry : notifiedExceededCategories.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    writer.print("notified." + entry.getKey() + "=");
                    writer.println(String.join(",",
                        entry.getValue().stream().map(String::valueOf).toArray(String[]::new)));
                }
            }

            // Salva categorie dismesse per mese con il relativo limite
            for (Map.Entry<String, Map<Integer, Double>> entry : dismissedNotifications.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    writer.print("dismissed." + entry.getKey() + "=");
                    List<String> values = new ArrayList<>();
                    entry.getValue().forEach((catId, amount) -> values.add(catId + ":" + amount));
                    writer.println(String.join(",", values));
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle preferenze notifiche: " + e.getMessage());
        }
    }

    /**
     * Carica le preferenze dal file di testo
     */
    private void load() {
        File file = new File(preferencesFile);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalsIndex = line.indexOf('=');
                if (equalsIndex == -1) {
                    continue;
                }

                String key = line.substring(0, equalsIndex);
                String value = line.substring(equalsIndex + 1);

                if (key.startsWith("notified.")) {
                    // Carica categorie notificate per mese
                    String monthKey = key.substring("notified.".length());
                    if (!value.isEmpty()) {
                        String[] ids = value.split(",");
                        Set<Integer> categoryIds = new HashSet<>();
                        for (String id : ids) {
                            try {
                                categoryIds.add(Integer.parseInt(id.trim()));
                            } catch (NumberFormatException e) {
                                // Ignora valori non validi
                            }
                        }
                        if (!categoryIds.isEmpty()) {
                            notifiedExceededCategories.put(monthKey, categoryIds);
                        }
                    }
                } else if (key.startsWith("dismissed.")) {
                    String monthKey = key.substring("dismissed.".length());
                    if (!value.isEmpty()) {
                        Map<Integer, Double> dismissedForMonth = new HashMap<>();
                        String[] parts = value.split(",");
                        for (String part : parts) {
                            String[] pair = part.split(":");
                            if (pair.length != 2) {
                                continue;
                            }
                            try {
                                int categoryId = Integer.parseInt(pair[0].trim());
                                double amount = Double.parseDouble(pair[1].trim());
                                dismissedForMonth.put(categoryId, amount);
                            } catch (NumberFormatException e) {
                                // Ignora valori non validi
                            }
                        }
                        if (!dismissedForMonth.isEmpty()) {
                            dismissedNotifications.put(monthKey, dismissedForMonth);
                        }
                    }
                }
            }

            // Pulisci mesi vecchi al caricamento
            cleanOldMonths();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento delle preferenze notifiche: " + e.getMessage());
        }
    }
}
