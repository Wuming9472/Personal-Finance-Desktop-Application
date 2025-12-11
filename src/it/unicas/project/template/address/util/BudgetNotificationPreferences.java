package it.unicas.project.template.address.util;

import java.io.*;
import java.time.YearMonth;
import java.util.*;

/**
 * Gestisce le preferenze per le notifiche di budget superato.
 * <p>
 * Questa classe si occupa di:
 * <ul>
 *     <li>tenere traccia delle categorie per cui il superamento del budget
 *         è già stato notificato nel mese corrente;</li>
 *     <li>ricordare le categorie per cui l'utente ha selezionato
 *         la scelta "Non mostrare più" nel mese corrente, insieme
 *         al limite di budget impostato in quel momento;</li>
 *     <li>persistenza di queste informazioni su file;</li>
 *     <li>pulizia periodica dei dati relativi a mesi troppo vecchi.</li>
 * </ul>
 * <p>
 * L'implementazione utilizza un pattern <em>singleton</em>:
 * l'accesso all'istanza avviene tramite {@link #getInstance()}.
 * Il file di preferenze può essere personalizzato (ad esempio nei test)
 * usando {@link #resetForTesting(String)}.
 */
public class BudgetNotificationPreferences {

    /**
     * Percorso predefinito del file di preferenze utilizzato per salvare e
     * caricare le informazioni sulle notifiche di budget.
     */
    private static final String DEFAULT_PREFERENCES_FILE = "budget_notifications.json";

    /**
     * Percorso del file attualmente in uso.
     * <p>
     * Può essere modificato tramite {@link #resetForTesting(String)}
     * (utile per i test automatizzati).
     */
    private static String preferencesFile = DEFAULT_PREFERENCES_FILE;

    /**
     * Istanza singleton della classe.
     */
    private static BudgetNotificationPreferences instance;

    /**
     * Mappa: chiave mese-anno ({@code "YYYY-MM"}) →
     * set di {@code categoryId} già notificati come superati.
     * <p>
     * Esempio:
     * <pre>
     * "2025-11" -> {1, 3, 5}
     * </pre>
     */
    private Map<String, Set<Integer>> notifiedExceededCategories;

    /**
     * Mappa: chiave mese-anno ({@code "YYYY-MM"}) →
     * mappa {@code categoryId} → {@code budgetAmount} salvato al momento
     * della scelta "Non mostrare più".
     * <p>
     * Questo consente di ripristinare le notifiche se l'utente aumenta
     * il limite di budget rispetto a quello registrato.
     */
    private Map<String, Map<Integer, Double>> dismissedNotifications;

    /**
     * Costruttore privato: inizializza le strutture dati e carica le
     * preferenze dal file associato a {@link #preferencesFile}.
     * <p>
     * L'istanza viene creata tramite {@link #getInstance()}.
     */
    private BudgetNotificationPreferences() {
        this.notifiedExceededCategories = new HashMap<>();
        this.dismissedNotifications = new HashMap<>();
        load();
    }

    /**
     * Restituisce l'unica istanza di {@link BudgetNotificationPreferences}.
     * <p>
     * Se l'istanza non esiste ancora, viene creata e inizializzata
     * caricando il file di preferenze corrente.
     *
     * @return istanza singleton di {@link BudgetNotificationPreferences}
     */
    public static synchronized BudgetNotificationPreferences getInstance() {
        if (instance == null) {
            instance = new BudgetNotificationPreferences();
        }
        return instance;
    }

    /**
     * Reimposta il file di preferenze e la relativa istanza singleton.
     * <p>
     * Questo metodo è pensato principalmente per i test, per poter
     * lavorare su file temporanei senza modificare le preferenze reali
     * dell'applicazione.
     *
     * @param customFilePath percorso del file di preferenze da utilizzare;
     *                       se {@code null}, viene ripristinato il file
     *                       predefinito {@link #DEFAULT_PREFERENCES_FILE}
     */
    public static synchronized void resetForTesting(String customFilePath) {
        preferencesFile = customFilePath != null ? customFilePath : DEFAULT_PREFERENCES_FILE;
        instance = null;
    }

    /**
     * Verifica se una categoria è già stata notificata come avente
     * budget superato nel mese corrente.
     *
     * @param categoryId identificativo della categoria
     * @return {@code true} se la categoria risulta già notificata
     *         per il mese corrente, {@code false} altrimenti
     */
    public boolean wasAlreadyNotifiedThisMonth(int categoryId) {
        String currentMonth = getCurrentMonthKey();
        Set<Integer> notifiedThisMonth = notifiedExceededCategories.get(currentMonth);
        return notifiedThisMonth != null && notifiedThisMonth.contains(categoryId);
    }

    /**
     * Segna una categoria come notificata per il mese corrente,
     * in modo che successive verifiche possano sapere che l'avviso
     * è già stato mostrato.
     *
     * @param categoryId identificativo della categoria da marcare come notificata
     */
    public void markAsNotified(int categoryId) {
        String currentMonth = getCurrentMonthKey();
        notifiedExceededCategories.computeIfAbsent(currentMonth, k -> new HashSet<>()).add(categoryId);
        save();
    }

    /**
     * Rimuove la marcatura di notifica per una categoria nel mese corrente.
     * <p>
     * Questo è utile quando il budget torna sotto il limite e si desidera
     * permettere una futura nuova notifica per la stessa categoria.
     *
     * @param categoryId identificativo della categoria da smarcare
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
     * Verifica se, per il mese corrente, l'utente ha scelto "Non mostrare più"
     * per una specifica categoria con il limite indicato.
     * <p>
     * Se il limite di budget attuale ({@code currentBudgetAmount}) è maggiore
     * del limite salvato al momento della dismissione, la dismissione viene
     * annullata per consentire nuove notifiche (si assume che l'utente abbia
     * cambiato intenzione, aumentando il budget).
     *
     * @param categoryId          identificativo della categoria
     * @param currentBudgetAmount limite di budget attuale per la categoria
     * @return {@code true} se, con il limite attuale, la notifica deve
     *         rimanere soppressa; {@code false} se non è stata dismessa
     *         o se la dismissione è stata invalidata (budget aumentato)
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
     * Registra la scelta "Non mostrare più" per la categoria specificata
     * nel mese corrente, memorizzando il limite di budget attuale.
     * <p>
     * In futuro, se il limite verrà aumentato oltre questo valore,
     * la dismissione potrà essere automaticamente rimossa.
     *
     * @param categoryId   identificativo della categoria
     * @param budgetAmount limite di budget corrente da associare alla dismissione
     */
    public void dismissNotificationForCurrentMonth(int categoryId, double budgetAmount) {
        String currentMonth = getCurrentMonthKey();
        dismissedNotifications.computeIfAbsent(currentMonth, k -> new HashMap<>())
                .put(categoryId, budgetAmount);
        save();
    }

    /**
     * Pulisce le notifiche relative a mesi troppo vecchi.
     * <p>
     * Vengono mantenuti solo i dati relativi agli ultimi tre mesi
     * (incluso il mese corrente); i dati più vecchi vengono eliminati
     * sia da {@link #notifiedExceededCategories} che da
     * {@link #dismissedNotifications}.
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
                // Chiave malformata, rimuovi in ogni caso
                toRemove.add(monthKey);
            }
        }

        toRemove.forEach(notifiedExceededCategories::remove);
        toRemove.forEach(dismissedNotifications::remove);
        if (!toRemove.isEmpty()) {
            save();
        }
    }

    /**
     * Restituisce uno snapshot (copia) della mappa delle categorie già
     * notificate per ciascun mese.
     * <p>
     * Ogni {@link Set} contenuto nella mappa risultante è una nuova
     * istanza, in modo da evitare modifiche esterne allo stato interno
     * della classe. Pensato principalmente per uso in test automatizzati.
     *
     * @return mappa (copia) mese-anno → set di categoryId notificati
     */
    public Map<String, Set<Integer>> getNotifiedExceededCategoriesSnapshot() {
        Map<String, Set<Integer>> snapshot = new HashMap<>();
        notifiedExceededCategories.forEach((k, v) -> snapshot.put(k, new HashSet<>(v)));
        return snapshot;
    }

    /**
     * Restituisce uno snapshot (copia) della mappa delle notifiche
     * dismesse per ciascun mese.
     * <p>
     * Ogni mappa interna (categoria → limite) è copiata per evitare
     * che il chiamante possa modificare lo stato interno dell'oggetto.
     * Pensato principalmente per uso in test automatizzati.
     *
     * @return mappa (copia) mese-anno → mappa categoria → limite salvato
     */
    public Map<String, Map<Integer, Double>> getDismissedNotificationsSnapshot() {
        Map<String, Map<Integer, Double>> snapshot = new HashMap<>();
        dismissedNotifications.forEach((month, map) -> snapshot.put(month, new HashMap<>(map)));
        return snapshot;
    }

    /**
     * Ottiene la chiave del mese corrente nel formato {@code "YYYY-MM"},
     * coerente con il formato restituito da {@link YearMonth#toString()}.
     *
     * @return stringa rappresentante il mese corrente, es. {@code "2025-11"}
     */
    private String getCurrentMonthKey() {
        return YearMonth.now().toString();
    }

    /**
     * Salva le preferenze sul file configurato in {@link #preferencesFile}
     * usando un semplice formato testuale riga per riga.
     * <p>
     * Formato di esempio:
     * <pre>
     * notified.2025-11=1,3,5
     * notified.2025-12=2,4
     * dismissed.2025-12=3:400.0,4:250.0
     * </pre>
     * In caso di errore di I/O, il problema viene loggato su {@code System.err}.
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
     * Carica le preferenze dal file configurato in {@link #preferencesFile}.
     * <p>
     * Il metodo legge il file riga per riga e popola le strutture dati
     * {@link #notifiedExceededCategories} e {@link #dismissedNotifications}.
     * <ul>
     *     <li>Le righe che iniziano con {@code "notified."} rappresentano
     *         categorie già notificate in un dato mese;</li>
     *     <li>le righe che iniziano con {@code "dismissed."} rappresentano
     *         categorie dismesse per un dato mese, con il relativo limite.</li>
     * </ul>
     * Le righe non valide o formattate in modo errato vengono ignorate.
     * <p>
     * Al termine del caricamento viene invocato {@link #cleanOldMonths()}
     * per rimuovere eventuali dati troppo vecchi.
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
                    // Carica categorie dismesse per mese con il relativo limite
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
