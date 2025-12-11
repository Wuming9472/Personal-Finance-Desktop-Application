package it.unicas.project.template.address.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility per il calcolo dei riepiloghi finanziari a partire da una lista di movimenti.
 * <p>
 * Questa classe contiene la logica di business per calcolare:
 * <ul>
 *     <li>Riepilogo mensile (totale entrate, totale uscite, saldo)</li>
 *     <li>Totali per categoria (entrate e uscite)</li>
 *     <li>Percentuali di distribuzione rispetto a un totale</li>
 *     <li>Aggregazioni per periodo (es. ogni N giorni)</li>
 *     <li>Saldi giornalieri e saldi cumulativi</li>
 *     <li>Categoria di spesa principale</li>
 *     <li>Tasso di risparmio mensile</li>
 * </ul>
 * <p>
 * La logica è stata separata dai controller JavaFX per favorire la testabilità
 * tramite test unitari e per mantenere i controller più snelli.
 */
public class SummaryCalculator {

    /**
     * Rappresenta un movimento finanziario semplificato utilizzato per i calcoli.
     * <p>
     * Ogni movimento è caratterizzato da:
     * <ul>
     *     <li>un tipo ({@code "Entrata"} o {@code "Uscita"}, oppure {@code "Income"} / {@code "Expense"})</li>
     *     <li>un importo</li>
     *     <li>una categoria (es. "Affitto", "Stipendio", "Cibo")</li>
     *     <li>un giorno del mese (1–31)</li>
     * </ul>
     * I metodi {@link #isIncome()} e {@link #isExpense()} interpretano il tipo
     * in modo case-insensitive e supportano sia le stringhe italiane che inglesi.
     */
    public static class Movement {

        /** Tipo del movimento: "Entrata"/"Income" oppure "Uscita"/"Expense". */
        private final String type;

        /** Importo del movimento. Deve essere non negativo per avere senso contabile. */
        private final double amount;

        /** Categoria del movimento (es. "Cibo", "Affitto", "Stipendio"). */
        private final String category;

        /** Giorno del mese (1–31) a cui il movimento si riferisce. */
        private final int dayOfMonth;

        /**
         * Crea un nuovo movimento finanziario.
         *
         * @param type       tipo del movimento ({@code "Entrata"} / {@code "Uscita"}
         *                   oppure {@code "Income"} / {@code "Expense"}), case-insensitive
         * @param amount     importo del movimento
         * @param category   categoria associata al movimento
         * @param dayOfMonth giorno del mese a cui il movimento si riferisce (1–31)
         */
        public Movement(String type, double amount, String category, int dayOfMonth) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.dayOfMonth = dayOfMonth;
        }

        /**
         * Restituisce il tipo di movimento.
         *
         * @return tipo del movimento (es. {@code "Entrata"}, {@code "Uscita"})
         */
        public String getType() {
            return type;
        }

        /**
         * Restituisce l'importo del movimento.
         *
         * @return importo associato al movimento
         */
        public double getAmount() {
            return amount;
        }

        /**
         * Restituisce la categoria del movimento.
         *
         * @return nome della categoria
         */
        public String getCategory() {
            return category;
        }

        /**
         * Restituisce il giorno del mese a cui il movimento si riferisce.
         *
         * @return giorno del mese (1–31)
         */
        public int getDayOfMonth() {
            return dayOfMonth;
        }

        /**
         * Indica se il movimento è un'entrata.
         * <p>
         * Viene considerato entrata se il tipo è uguale (case-insensitive) a
         * {@code "Entrata"} oppure {@code "Income"}.
         *
         * @return {@code true} se il movimento è un'entrata, {@code false} altrimenti
         */
        public boolean isIncome() {
            return type != null && (type.equalsIgnoreCase("Entrata") || type.equalsIgnoreCase("Income"));
        }

        /**
         * Indica se il movimento è un'uscita.
         * <p>
         * Viene considerato uscita se il tipo è uguale (case-insensitive) a
         * {@code "Uscita"} oppure {@code "Expense"}.
         *
         * @return {@code true} se il movimento è un'uscita, {@code false} altrimenti
         */
        public boolean isExpense() {
            return type != null && (type.equalsIgnoreCase("Uscita") || type.equalsIgnoreCase("Expense"));
        }
    }

    /**
     * Rappresenta il risultato del calcolo del riepilogo mensile.
     * <p>
     * Contiene:
     * <ul>
     *     <li>totale entrate</li>
     *     <li>totale uscite</li>
     *     <li>saldo (entrate - uscite)</li>
     *     <li>totali per categoria per le uscite</li>
     *     <li>totali per categoria per le entrate</li>
     *     <li>numero di transazioni considerate</li>
     * </ul>
     * Il saldo viene calcolato automaticamente come differenza tra entrate e uscite.
     */
    public static class MonthlySummary {

        /** Totale delle entrate del periodo. */
        private final double totalIncome;

        /** Totale delle uscite del periodo. */
        private final double totalExpenses;

        /** Saldo complessivo (entrate - uscite). Può essere positivo o negativo. */
        private final double balance;

        /** Mappa delle uscite totali per categoria. */
        private final Map<String, Double> expensesByCategory;

        /** Mappa delle entrate totali per categoria. */
        private final Map<String, Double> incomeByCategory;

        /** Numero complessivo di transazioni (entrate + uscite) considerate nel riepilogo. */
        private final int transactionCount;

        /**
         * Crea un nuovo riepilogo mensile.
         * <p>
         * Il campo {@code balance} viene calcolato come
         * {@code totalIncome - totalExpenses}.
         *
         * @param totalIncome        totale delle entrate
         * @param totalExpenses      totale delle uscite
         * @param expensesByCategory mappa delle uscite aggregate per categoria
         * @param incomeByCategory   mappa delle entrate aggregate per categoria
         * @param transactionCount   numero totale di movimenti considerati
         */
        public MonthlySummary(double totalIncome,
                              double totalExpenses,
                              Map<String, Double> expensesByCategory,
                              Map<String, Double> incomeByCategory,
                              int transactionCount) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.balance = totalIncome - totalExpenses;
            this.expensesByCategory = expensesByCategory;
            this.incomeByCategory = incomeByCategory;
            this.transactionCount = transactionCount;
        }

        /**
         * Restituisce il totale delle entrate.
         *
         * @return totale entrate
         */
        public double getTotalIncome() {
            return totalIncome;
        }

        /**
         * Restituisce il totale delle uscite.
         *
         * @return totale uscite
         */
        public double getTotalExpenses() {
            return totalExpenses;
        }

        /**
         * Restituisce il saldo complessivo (entrate - uscite).
         *
         * @return saldo del periodo, positivo o negativo
         */
        public double getBalance() {
            return balance;
        }

        /**
         * Restituisce la mappa delle uscite per categoria.
         *
         * @return mappa categoria → totale uscite
         */
        public Map<String, Double> getExpensesByCategory() {
            return expensesByCategory;
        }

        /**
         * Restituisce la mappa delle entrate per categoria.
         *
         * @return mappa categoria → totale entrate
         */
        public Map<String, Double> getIncomeByCategory() {
            return incomeByCategory;
        }

        /**
         * Restituisce il numero totale di transazioni considerate.
         *
         * @return numero di movimenti (entrate + uscite)
         */
        public int getTransactionCount() {
            return transactionCount;
        }

        /**
         * Indica se il saldo del riepilogo è positivo o nullo.
         *
         * @return {@code true} se il saldo è maggiore o uguale a 0, {@code false} altrimenti
         */
        public boolean isPositive() {
            return balance >= 0;
        }

        /**
         * Indica se il saldo del riepilogo è negativo.
         *
         * @return {@code true} se il saldo è minore di 0, {@code false} altrimenti
         */
        public boolean isNegative() {
            return balance < 0;
        }
    }

    /**
     * Rappresenta il risultato dell'aggregazione dei movimenti
     * all'interno di un determinato periodo di giorni.
     * <p>
     * Ogni oggetto descrive:
     * <ul>
     *     <li>l'indice del periodo (a partire da 0)</li>
     *     <li>il giorno iniziale e finale del periodo</li>
     *     <li>le entrate totali nel periodo</li>
     *     <li>le uscite totali nel periodo</li>
     *     <li>il saldo del periodo (entrate - uscite)</li>
     * </ul>
     */
    public static class PeriodAggregate {

        /** Indice del periodo (0-based). */
        private final int periodIndex;

        /** Giorno iniziale del periodo (incluso). */
        private final int startDay;

        /** Giorno finale del periodo (incluso). */
        private final int endDay;

        /** Totale entrate del periodo. */
        private final double income;

        /** Totale uscite del periodo. */
        private final double expenses;

        /** Saldo del periodo (entrate - uscite). */
        private final double balance;

        /**
         * Crea un nuovo aggregato di periodo.
         * <p>
         * Il saldo viene calcolato come {@code income - expenses}.
         *
         * @param periodIndex indice logico del periodo (0-based)
         * @param startDay    giorno iniziale del periodo (incluso)
         * @param endDay      giorno finale del periodo (incluso)
         * @param income      totale entrate nel periodo
         * @param expenses    totale uscite nel periodo
         */
        public PeriodAggregate(int periodIndex,
                               int startDay,
                               int endDay,
                               double income,
                               double expenses) {
            this.periodIndex = periodIndex;
            this.startDay = startDay;
            this.endDay = endDay;
            this.income = income;
            this.expenses = expenses;
            this.balance = income - expenses;
        }

        /**
         * Restituisce l'indice del periodo.
         *
         * @return indice del periodo (0-based)
         */
        public int getPeriodIndex() {
            return periodIndex;
        }

        /**
         * Restituisce il giorno iniziale del periodo.
         *
         * @return giorno iniziale (incluso)
         */
        public int getStartDay() {
            return startDay;
        }

        /**
         * Restituisce il giorno finale del periodo.
         *
         * @return giorno finale (incluso)
         */
        public int getEndDay() {
            return endDay;
        }

        /**
         * Restituisce il totale delle entrate nel periodo.
         *
         * @return entrate del periodo
         */
        public double getIncome() {
            return income;
        }

        /**
         * Restituisce il totale delle uscite nel periodo.
         *
         * @return uscite del periodo
         */
        public double getExpenses() {
            return expenses;
        }

        /**
         * Restituisce il saldo del periodo (entrate - uscite).
         *
         * @return saldo del periodo
         */
        public double getBalance() {
            return balance;
        }

        /**
         * Restituisce un'etichetta testuale del periodo nel formato {@code "startDay-endDay"}.
         * <p>
         * Esempio: per startDay = 1 ed endDay = 3, restituisce {@code "1-3"}.
         *
         * @return etichetta testuale del periodo
         */
        public String getLabel() {
            return startDay + "-" + endDay;
        }
    }

    /**
     * Calcola il riepilogo mensile a partire da una lista di movimenti.
     * <p>
     * La lista viene analizzata per:
     * <ul>
     *     <li>somma delle entrate e delle uscite</li>
     *     <li>aggregazione per categoria (entrate e uscite separatamente)</li>
     *     <li>conteggio del numero di movimenti</li>
     * </ul>
     * Se la lista è {@code null} o vuota, viene restituito un riepilogo
     * con tutti i valori a 0 e mappe vuote.
     *
     * @param movements lista dei movimenti del mese (può essere {@code null} o vuota)
     * @return oggetto {@link MonthlySummary} con tutti i totali calcolati
     */
    public MonthlySummary calculateMonthlySummary(List<Movement> movements) {
        double totalIncome = 0.0;
        double totalExpenses = 0.0;
        Map<String, Double> expensesByCategory = new HashMap<>();
        Map<String, Double> incomeByCategory = new HashMap<>();

        if (movements == null || movements.isEmpty()) {
            return new MonthlySummary(0, 0, expensesByCategory, incomeByCategory, 0);
        }

        for (Movement m : movements) {
            if (m.isIncome()) {
                totalIncome += m.getAmount();
                incomeByCategory.merge(m.getCategory(), m.getAmount(), Double::sum);
            } else if (m.isExpense()) {
                totalExpenses += m.getAmount();
                expensesByCategory.merge(m.getCategory(), m.getAmount(), Double::sum);
            }
        }

        return new MonthlySummary(
                totalIncome,
                totalExpenses,
                expensesByCategory,
                incomeByCategory,
                movements.size()
        );
    }

    /**
     * Calcola il saldo mensile come differenza tra entrate e uscite.
     *
     * @param totalIncome   totale delle entrate
     * @param totalExpenses totale delle uscite
     * @return saldo calcolato come {@code totalIncome - totalExpenses}
     */
    public double calculateBalance(double totalIncome, double totalExpenses) {
        return totalIncome - totalExpenses;
    }

    /**
     * Calcola la percentuale di una categoria rispetto a un totale.
     * <p>
     * Se il totale è minore o uguale a 0, la percentuale restituita è 0
     * per evitare divisioni non significative.
     *
     * @param categoryAmount importo della categoria
     * @param total          totale di riferimento
     * @return percentuale (0–100) dell'importo di categoria rispetto al totale,
     *         o 0 se {@code total <= 0}
     */
    public double calculateCategoryPercentage(double categoryAmount, double total) {
        if (total <= 0) {
            return 0.0;
        }
        return (categoryAmount / total) * 100.0;
    }

    /**
     * Aggrega i movimenti in periodi consecutivi di N giorni.
     * <p>
     * Esempio: con {@code periodDays = 3} e {@code daysInMonth = 31}, verranno creati
     * {@code ceil(31 / 3) = 11} periodi:
     * <pre>
     * Periodo 0: giorni  1–3
     * Periodo 1: giorni  4–6
     * ...
     * Periodo 9: giorni 28–30
     * Periodo 10: giorni 31–31
     * </pre>
     * Per ogni periodo vengono calcolate le entrate, le uscite e il saldo.
     * I movimenti che cadono al di fuori dell'intervallo [1, daysInMonth] vengono ignorati.
     * <p>
     * Se {@code periodDays <= 0} o {@code daysInMonth <= 0}, viene restituito un array vuoto.
     *
     * @param movements   lista dei movimenti da aggregare (può essere {@code null})
     * @param periodDays  numero di giorni inclusi in ciascun periodo (es. 3)
     * @param daysInMonth numero totale di giorni nel mese
     * @return array di {@link PeriodAggregate}, uno per ciascun periodo
     */
    public PeriodAggregate[] aggregateByPeriod(List<Movement> movements, int periodDays, int daysInMonth) {
        if (periodDays <= 0 || daysInMonth <= 0) {
            return new PeriodAggregate[0];
        }

        int numPeriods = (int) Math.ceil((double) daysInMonth / periodDays);
        double[] periodIncome = new double[numPeriods];
        double[] periodExpenses = new double[numPeriods];

        if (movements != null) {
            for (Movement m : movements) {
                int periodIndex = (m.getDayOfMonth() - 1) / periodDays;
                if (periodIndex >= 0 && periodIndex < numPeriods) {
                    if (m.isIncome()) {
                        periodIncome[periodIndex] += m.getAmount();
                    } else if (m.isExpense()) {
                        periodExpenses[periodIndex] += m.getAmount();
                    }
                }
            }
        }

        PeriodAggregate[] result = new PeriodAggregate[numPeriods];
        for (int i = 0; i < numPeriods; i++) {
            int startDay = i * periodDays + 1;
            int endDay = Math.min((i + 1) * periodDays, daysInMonth);
            result[i] = new PeriodAggregate(i, startDay, endDay, periodIncome[i], periodExpenses[i]);
        }

        return result;
    }

    /**
     * Calcola i saldi giornalieri per un mese.
     * <p>
     * Per ogni giorno del mese viene calcolato il saldo locale come:
     * <ul>
     *     <li>somma delle entrate del giorno</li>
     *     <li>meno somma delle uscite del giorno</li>
     * </ul>
     * I giorni senza movimenti avranno valore 0.
     * <p>
     * Se {@code daysInMonth <= 0}, viene restituito un array vuoto.
     * I movimenti con giorno al di fuori dell'intervallo [1, daysInMonth] vengono ignorati.
     *
     * @param movements   lista dei movimenti (può essere {@code null})
     * @param daysInMonth numero di giorni nel mese
     * @return array di saldi giornalieri: l'indice 0 corrisponde al giorno 1
     */
    public double[] calculateDailyBalances(List<Movement> movements, int daysInMonth) {
        if (daysInMonth <= 0) {
            return new double[0];
        }

        double[] dailyBalances = new double[daysInMonth];

        if (movements != null) {
            for (Movement m : movements) {
                int dayIndex = m.getDayOfMonth() - 1;
                if (dayIndex >= 0 && dayIndex < daysInMonth) {
                    if (m.isIncome()) {
                        dailyBalances[dayIndex] += m.getAmount();
                    } else if (m.isExpense()) {
                        dailyBalances[dayIndex] -= m.getAmount();
                    }
                }
            }
        }

        return dailyBalances;
    }

    /**
     * Calcola il saldo cumulativo progressivo a partire dai saldi giornalieri.
     * <p>
     * Per ogni posizione dell'array viene calcolata la somma cumulativa di tutti
     * i saldi precedenti, inclusa la posizione corrente:
     * <pre>
     * cumulative[i] = dailyBalances[0] + ... + dailyBalances[i]
     * </pre>
     * Se l'array in ingresso è {@code null} o vuoto, viene restituito un array vuoto.
     *
     * @param dailyBalances array di saldi giornalieri (può essere {@code null})
     * @return array di saldi cumulativi di stessa lunghezza di {@code dailyBalances}
     */
    public double[] calculateCumulativeBalance(double[] dailyBalances) {
        if (dailyBalances == null || dailyBalances.length == 0) {
            return new double[0];
        }

        double[] cumulative = new double[dailyBalances.length];
        double runningTotal = 0;

        for (int i = 0; i < dailyBalances.length; i++) {
            runningTotal += dailyBalances[i];
            cumulative[i] = runningTotal;
        }

        return cumulative;
    }

    /**
     * Trova la categoria con la spesa complessiva maggiore.
     * <p>
     * Se la mappa è {@code null} o vuota, viene restituito {@code null}.
     * In caso di parità di importo massimo tra più categorie, viene restituita
     * la prima categoria incontrata durante l'iterazione.
     *
     * @param expensesByCategory mappa delle spese per categoria
     *                           (categoria → totale speso)
     * @return nome della categoria con spesa maggiore,
     *         oppure {@code null} se la mappa è {@code null} o vuota
     */
    public String findTopExpenseCategory(Map<String, Double> expensesByCategory) {
        if (expensesByCategory == null || expensesByCategory.isEmpty()) {
            return null;
        }

        String topCategory = null;
        double maxAmount = 0;

        for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        return topCategory;
    }

    /**
     * Calcola il tasso di risparmio (o deficit) mensile come percentuale delle entrate.
     * <p>
     * Il tasso viene calcolato come:
     * <pre>
     * savings = totalIncome - totalExpenses
     * savingsRate = (savings / totalIncome) * 100
     * </pre>
     * Se {@code totalIncome <= 0}, il tasso restituito è 0 per evitare
     * divisioni non significative.
     * <p>
     * Un valore positivo indica una quota di reddito risparmiata,
     * mentre un valore negativo indica un deficit.
     *
     * @param totalIncome   totale delle entrate
     * @param totalExpenses totale delle uscite
     * @return percentuale di risparmio (positiva) o deficit (negativa)
     *         rispetto alle entrate, oppure 0 se {@code totalIncome <= 0}
     */
    public double calculateSavingsRate(double totalIncome, double totalExpenses) {
        if (totalIncome <= 0) {
            return 0.0;
        }
        double savings = totalIncome - totalExpenses;
        return (savings / totalIncome) * 100.0;
    }
}
