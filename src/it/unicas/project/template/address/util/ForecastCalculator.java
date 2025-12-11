package it.unicas.project.template.address.util;

/**
 * Classe per il calcolo delle previsioni finanziarie mensili.
 * <p>
 * Contiene la logica di business per calcolare, a partire dai dati reali del mese:
 * <ul>
 *     <li>media giornaliera di spese ed entrate;</li>
 *     <li>proiezione delle spese (e opzionalmente delle entrate) a fine mese;</li>
 *     <li>saldo stimato a fine mese;</li>
 *     <li>stato della situazione (stabile, attenzione, critica).</li>
 * </ul>
 * La logica è stata estratta dai controller per migliorare la testabilità e
 * mantenere la separazione tra logica applicativa e livello di presentazione.
 */
public class ForecastCalculator {

    /**
     * Risultato del calcolo di previsione mensile.
     * <p>
     * Incapsula tutti i dati derivati dalla chiamata a
     * {@link #calculateForecast(double, double, int, int, int)}, inclusi:
     * <ul>
     *     <li>validità della previsione;</li>
     *     <li>eventuale messaggio di errore;</li>
     *     <li>giorno corrente e giorni rimanenti nel mese;</li>
     *     <li>medie giornaliere di entrate e uscite;</li>
     *     <li>totali proiettati di entrate e uscite a fine mese;</li>
     *     <li>saldo stimato a fine mese;</li>
     *     <li>stato della previsione ({@link ForecastStatus}).</li>
     * </ul>
     */
    public static class ForecastResult {

        /** Indica se il risultato è valido (previsione calcolata) o contiene un errore. */
        private final boolean valid;

        /** Messaggio di errore, valorizzato solo se {@link #valid} è {@code false}. */
        private final String errorMessage;

        /** Giorno corrente del mese utilizzato per il calcolo (1–31). */
        private final int currentDay;

        /** Numero di giorni rimanenti nel mese a partire dal giorno corrente. */
        private final int remainingDays;

        /** Media giornaliera delle uscite calcolata sui giorni trascorsi. */
        private final double dailyExpenseAverage;


        /** Totale delle uscite proiettato a fine mese. */
        private final double projectedTotalExpenses;


        /** Saldo stimato a fine mese (entrate - uscite). */
        private final double estimatedBalance;

        /** Stato della previsione sulla base del saldo stimato. */
        private final ForecastStatus status;

        /// Costruttore privato usato dai factory method statici
        /// [#insufficient(String)] e
        /// [#valid(int, int, double, double, double, double, double, ForecastStatus)].
        ///
        /// @param valid                  `true` se la previsione è valida,
        ///                               `false` in caso di errore o dati insufficienti
        /// @param errorMessage           messaggio di errore (o `null` se non applicabile)
        /// @param currentDay             giorno corrente del mese
        /// @param remainingDays          giorni rimanenti nel mese
        /// @param dailyExpenseAverage    media giornaliera delle uscite
        /// @param projectedTotalExpenses totale uscite proiettato a fine mese
        /// @param estimatedBalance       saldo stimato a fine mese
        /// @param status                 stato della previsione
        private ForecastResult(boolean valid, String errorMessage, int currentDay, int remainingDays,
                               double dailyExpenseAverage, double projectedTotalExpenses,
                               double estimatedBalance, ForecastStatus status) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.currentDay = currentDay;
            this.remainingDays = remainingDays;
            this.dailyExpenseAverage = dailyExpenseAverage;
            this.projectedTotalExpenses = projectedTotalExpenses;
            this.estimatedBalance = estimatedBalance;
            this.status = status;
        }

        /**
         * Indica se la previsione è valida.
         *
         * @return {@code true} se il risultato contiene una previsione,
         *         {@code false} se rappresenta un errore o dati insufficienti
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Restituisce il messaggio di errore associato al risultato.
         *
         * @return messaggio di errore se {@link #isValid()} è {@code false},
         *         altrimenti {@code null}
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Restituisce il giorno corrente del mese utilizzato per il calcolo.
         *
         * @return giorno corrente del mese (1–31), oppure 0 se dati insufficienti
         */
        public int getCurrentDay() {
            return currentDay;
        }

        /**
         * Restituisce il numero di giorni rimanenti nel mese.
         *
         * @return giorni rimanenti (può essere 0, ad esempio a fine mese)
         */
        public int getRemainingDays() {
            return remainingDays;
        }

        /**
         * Restituisce la media giornaliera delle uscite.
         *
         * @return media giornaliera delle spese
         */
        public double getDailyExpenseAverage() {
            return dailyExpenseAverage;
        }


        /**
         * Restituisce il totale delle uscite proiettato a fine mese.
         *
         * @return totale uscite proiettato
         */
        public double getProjectedTotalExpenses() {
            return projectedTotalExpenses;
        }


        /**
         * Restituisce il saldo stimato a fine mese.
         *
         * @return saldo stimato (entrate - uscite), positivo o negativo
         */
        public double getEstimatedBalance() {
            return estimatedBalance;
        }

        /**
         * Restituisce lo stato della previsione.
         *
         * @return stato della previsione basato sul saldo stimato
         */
        public ForecastStatus getStatus() {
            return status;
        }

        /**
         * Crea un risultato che rappresenta una previsione non valida
         * a causa di dati insufficienti o parametri non corretti.
         * <p>
         * Tutti i valori numerici saranno impostati a 0 e lo status sarà
         * {@link ForecastStatus#INSUFFICIENT_DATA}.
         *
         * @param message messaggio descrittivo dell'errore o della mancanza di dati
         * @return istanza di {@link ForecastResult} non valida
         */
        public static ForecastResult insufficient(String message) {
            return new ForecastResult(false, message, 0, 0, 0, 0, 0, ForecastStatus.INSUFFICIENT_DATA);
        }

        /**
         * Crea un risultato valido contenente tutti i calcoli relativi alla previsione.
         * <p>
         * Questo factory method viene tipicamente utilizzato dal metodo
         * {@link ForecastCalculator#calculateForecast(double, double, int, int, int)}.
         *
         * @param currentDay             giorno corrente del mese utilizzato per il calcolo
         * @param remainingDays          giorni rimanenti nel mese
         * @param dailyExpenseAverage    media giornaliera delle uscite
         * @param projectedTotalExpenses totale uscite proiettato a fine mese
         * @param estimatedBalance       saldo stimato a fine mese
         * @param status                 stato della previsione
         * @return istanza di {@link ForecastResult} valida
         */
        public static ForecastResult valid(int currentDay, int remainingDays,
                                           double dailyExpenseAverage, double projectedTotalExpenses,
                                           double estimatedBalance, ForecastStatus status) {
            return new ForecastResult(true, null, currentDay, remainingDays,
                    dailyExpenseAverage, projectedTotalExpenses, estimatedBalance, status);
        }
    }

    /**
     * Stato della previsione basato sul saldo stimato a fine mese.
     * <p>
     * Lo stato viene determinato dal metodo
     * {@link #determineStatus(double)} utilizzando le soglie
     * {@link #STABLE_THRESHOLD} e {@link #CRITICAL_THRESHOLD}.
     */
    public enum ForecastStatus {

        /**
         * Dati insufficienti per calcolare una previsione attendibile.
         * Di solito generato quando i giorni con movimenti sono inferiori a
         * {@link ForecastCalculator#MINIMUM_DAYS_FOR_FORECAST}.
         */
        INSUFFICIENT_DATA,

        /**
         * Situazione stabile: il saldo stimato è sufficientemente positivo
         * (maggiore di {@link ForecastCalculator#STABLE_THRESHOLD}).
         */
        STABLE,

        /**
         * Situazione di attenzione: il saldo stimato si trova tra
         * {@link ForecastCalculator#CRITICAL_THRESHOLD} (incluso)
         * e {@link ForecastCalculator#STABLE_THRESHOLD} (incluso).
         */
        WARNING,

        /**
         * Situazione critica: il saldo stimato è inferiore a
         * {@link ForecastCalculator#CRITICAL_THRESHOLD}.
         */
        CRITICAL
    }

    /**
     * Soglia superiore del saldo stimato oltre la quale la situazione
     * viene considerata stabile ({@link ForecastStatus#STABLE}).
     */
    public static final double STABLE_THRESHOLD = 200.0;

    /**
     * Soglia inferiore del saldo stimato al di sotto della quale la situazione
     * viene considerata critica ({@link ForecastStatus#CRITICAL}).
     */
    public static final double CRITICAL_THRESHOLD = -100.0;

    /**
     * Numero minimo di giorni con movimenti richiesti per considerare
     * i dati sufficienti a calcolare una previsione attendibile.
     */
    public static final int MINIMUM_DAYS_FOR_FORECAST = 7;

    /**
     * Calcola la previsione mensile basandosi sui dati reali disponibili fino al giorno corrente.
     * <p>
     * Il metodo esegue le seguenti operazioni:
     * <ol>
     *     <li>verifica che ci siano almeno {@link #MINIMUM_DAYS_FOR_FORECAST} giorni con movimenti;</li>
     *     <li>valida i parametri relativi al giorno corrente e al numero di giorni nel mese;</li>
     *     <li>calcola i giorni rimanenti nel mese;</li>
     *     <li>calcola le medie giornaliere di entrate e uscite;</li>
     *     <li>calcola il totale delle uscite proiettato a fine mese;</li>
     *     <li>calcola il saldo stimato a fine mese;</li>
     *     <li>determina lo stato della previsione ({@link ForecastStatus}).</li>
     * </ol>
     * Se una delle condizioni di validazione fallisce, viene restituito un
     * risultato non valido tramite {@link ForecastResult#insufficient(String)}.
     *
     * @param totalIncome       totale delle entrate registrate fino al giorno corrente
     * @param totalExpenses     totale delle uscite registrate fino al giorno corrente
     * @param daysWithMovements numero di giorni del mese in cui sono stati registrati movimenti
     * @param currentDay        giorno corrente del mese (1–31)
     * @param daysInMonth       numero totale di giorni nel mese (tipicamente 28–31)
     * @return un oggetto {@link ForecastResult} contenente tutti i calcoli
     *         oppure un risultato non valido con messaggio di errore
     */
    public ForecastResult calculateForecast(double totalIncome, double totalExpenses,
                                            int daysWithMovements, int currentDay, int daysInMonth) {

        // Validazione: servono almeno N giorni di dati (ora 7)
        if (daysWithMovements < MINIMUM_DAYS_FOR_FORECAST) {
            return ForecastResult.insufficient(
                    "Dati insufficienti: necessari almeno " + MINIMUM_DAYS_FOR_FORECAST +
                            " giorni con movimenti (trovati: " + daysWithMovements + ")"
            );
        }

        // Validazione parametri
        if (currentDay <= 0 || currentDay > daysInMonth) {
            return ForecastResult.insufficient("Giorno corrente non valido: " + currentDay);
        }

        if (daysInMonth <= 0 || daysInMonth > 31) {
            return ForecastResult.insufficient("Giorni nel mese non validi: " + daysInMonth);
        }

        // Calcolo dei giorni rimanenti
        int remainingDays = daysInMonth - currentDay;

        // Medie giornaliere (basate sul giorno corrente)
        double dailyExpenseAverage = calculateDailyAverage(totalExpenses, currentDay);

        // Proiezione delle uscite a fine mese
        double projectedExpenses = calculateProjectedTotal(totalExpenses, dailyExpenseAverage, remainingDays);


        // saldo stimato = entrate REALI - uscite PROIETTATE
        double estimatedBalance = calculateEstimatedBalance(totalIncome, projectedExpenses);

        // Determina lo status con le soglie configurate
        ForecastStatus status = determineStatus(estimatedBalance);

        return ForecastResult.valid(
                currentDay,
                remainingDays,
                dailyExpenseAverage,
                projectedExpenses,
                estimatedBalance,
                status
        );
    }

    /**
     * Calcola la media giornaliera di un totale (entrate o uscite).
     * <p>
     * Se il numero di giorni è minore o uguale a 0, viene restituito 0
     * per evitare divisioni non significative.
     *
     * @param total totale (entrate o uscite) da distribuire sui giorni
     * @param days  numero di giorni considerati
     * @return media giornaliera, oppure 0 se {@code days <= 0}
     */
    public double calculateDailyAverage(double total, int days) {
        if (days <= 0) {
            return 0.0;
        }
        return total / days;
    }

    /**
     * Calcola il totale proiettato a fine mese a partire
     * dal totale corrente, da una media giornaliera e dai giorni rimanenti.
     * <p>
     * La formula utilizzata è:
     * <pre>
     * projected = currentTotal + dailyAverage * remainingDays
     * </pre>
     * Se {@code remainingDays < 0}, viene restituito semplicemente
     * {@code currentTotal}.
     *
     * @param currentTotal  totale attuale (entrate o uscite)
     * @param dailyAverage  media giornaliera corrispondente al totale
     * @param remainingDays giorni rimanenti nel mese
     * @return totale proiettato a fine mese
     */
    public double calculateProjectedTotal(double currentTotal, double dailyAverage, int remainingDays) {
        if (remainingDays < 0) {
            return currentTotal;
        }
        return currentTotal + (dailyAverage * remainingDays);
    }

    /**
     * Calcola il saldo stimato a fine mese a partire dalle entrate
     * e dalle uscite proiettate.
     * <p>
     * La formula utilizzata è:
     * <pre>
     * estimatedBalance = totalIncome - projectedExpenses
     * </pre>
     *
     * @param totalIncome   entrate totali fino al giorno corrente
     * @param projectedExpenses uscite proiettate a fine mese
     * @return saldo stimato (entrate - uscite)
     */
    public double calculateEstimatedBalance(double totalIncome, double projectedExpenses) {
        return totalIncome - projectedExpenses;
    }

    /**
     * Determina lo stato della previsione in base al saldo stimato.
     * <p>
     * Le regole di classificazione sono:
     * <ul>
     *     <li>se {@code estimatedBalance > STABLE_THRESHOLD} →
     *         {@link ForecastStatus#STABLE}</li>
     *     <li>se {@code CRITICAL_THRESHOLD <= estimatedBalance <= STABLE_THRESHOLD} →
     *         {@link ForecastStatus#WARNING}</li>
     *     <li>se {@code estimatedBalance < CRITICAL_THRESHOLD} →
     *         {@link ForecastStatus#CRITICAL}</li>
     * </ul>
     *
     * @param estimatedBalance saldo stimato a fine mese
     * @return stato corrispondente al saldo stimato
     */
    public ForecastStatus determineStatus(double estimatedBalance) {
        if (estimatedBalance > STABLE_THRESHOLD) {
            return ForecastStatus.STABLE;
        } else if (estimatedBalance >= CRITICAL_THRESHOLD) {
            return ForecastStatus.WARNING;
        } else {
            return ForecastStatus.CRITICAL;
        }
    }

    /**
     * Verifica se il numero di giorni con movimenti è sufficiente
     * per effettuare una previsione.
     * <p>
     * Confronta il parametro con
     * {@link ForecastCalculator#MINIMUM_DAYS_FOR_FORECAST}.
     *
     * @param daysWithMovements numero di giorni del mese in cui sono stati registrati movimenti
     * @return {@code true} se {@code daysWithMovements} è maggiore o uguale a
     *         {@link #MINIMUM_DAYS_FOR_FORECAST}, {@code false} altrimenti
     */
    public boolean hasEnoughData(int daysWithMovements) {
        return daysWithMovements >= MINIMUM_DAYS_FOR_FORECAST;
    }
}
