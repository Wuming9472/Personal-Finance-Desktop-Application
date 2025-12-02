package it.unicas.project.template.address.util;

import java.io.*;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gestisce le preferenze per le notifiche di budget superato.
 * Tiene traccia di:
 * - Categorie con notifiche disabilitate permanentemente
 * - Categorie già notificate come superate nel mese corrente
 */
public class BudgetNotificationPreferences {

    private static final String DEFAULT_PREFERENCES_FILE = "budget_notifications.json";
    private static String preferencesFile = DEFAULT_PREFERENCES_FILE;
    private static BudgetNotificationPreferences instance;

    // Categorie con notifiche disabilitate permanentemente (categoryId)
    private Set<Integer> disabledCategories;

    // Mappa: mese-anno -> set di categoryId già notificati come superati
    // Esempio: "2025-11" -> {1, 3, 5}
    private Map<String, Set<Integer>> notifiedExceededCategories;

    private BudgetNotificationPreferences() {
        this.disabledCategories = new HashSet<>();
        this.notifiedExceededCategories = new HashMap<>();
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
     * Verifica se una categoria ha le notifiche disabilitate permanentemente
     */
    public boolean isNotificationDisabled(int categoryId) {
        return disabledCategories.contains(categoryId);
    }

    /**
     * Disabilita permanentemente le notifiche per una categoria
     */
    public void disableNotificationForCategory(int categoryId) {
        disabledCategories.add(categoryId);
        save();
    }

    /**
     * Riabilita le notifiche per una categoria
     */
    public void enableNotificationForCategory(int categoryId) {
        disabledCategories.remove(categoryId);
        save();
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
     * Pulisce le notifiche di mesi vecchi (mantiene solo gli ultimi 3 mesi)
     */
    public void cleanOldMonths() {
        YearMonth current = YearMonth.now();
        Set<String> toRemove = new HashSet<>();

        for (String monthKey : notifiedExceededCategories.keySet()) {
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
        if (!toRemove.isEmpty()) {
            save();
        }
    }

    // Metodi di supporto per i test automatizzati (stessa package visibility)
    Set<Integer> getDisabledCategoriesSnapshot() {
        return new HashSet<>(disabledCategories);
    }

    Map<String, Set<Integer>> getNotifiedExceededCategoriesSnapshot() {
        Map<String, Set<Integer>> snapshot = new HashMap<>();
        notifiedExceededCategories.forEach((k, v) -> snapshot.put(k, new HashSet<>(v)));
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
     * disabled=1,3,5
     * notified.2025-11=1,3,5
     * notified.2025-12=2,4
     */
    private void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(preferencesFile))) {
            // Salva categorie disabilitate
            if (!disabledCategories.isEmpty()) {
                writer.print("disabled=");
                writer.println(String.join(",",
                    disabledCategories.stream().map(String::valueOf).toArray(String[]::new)));
            }

            // Salva categorie notificate per mese
            for (Map.Entry<String, Set<Integer>> entry : notifiedExceededCategories.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    writer.print("notified." + entry.getKey() + "=");
                    writer.println(String.join(",",
                        entry.getValue().stream().map(String::valueOf).toArray(String[]::new)));
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

                if (key.equals("disabled")) {
                    // Carica categorie disabilitate
                    if (!value.isEmpty()) {
                        String[] ids = value.split(",");
                        for (String id : ids) {
                            try {
                                disabledCategories.add(Integer.parseInt(id.trim()));
                            } catch (NumberFormatException e) {
                                // Ignora valori non validi
                            }
                        }
                    }
                } else if (key.startsWith("notified.")) {
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
                }
            }

            // Pulisci mesi vecchi al caricamento
            cleanOldMonths();

        } catch (IOException e) {
            System.err.println("Errore nel caricamento delle preferenze notifiche: " + e.getMessage());
        }
    }
}
