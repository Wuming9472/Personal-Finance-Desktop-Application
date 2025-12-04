package it.unicas.project.template.address.util;

/**
 * Classe per il calcolo delle previsioni finanziarie mensili.
 * Contiene la logica di business per calcolare:
 * - Media giornaliera di spese ed entrate
 * - Proiezione a fine mese
 * - Saldo stimato
 *
 * Questa classe e' stata estratta dai controller per migliorare la testabilita'.
 */
public class ForecastCalculator {

    /**
     * Risultato del calcolo di previsione.
     * Contiene tutti i dati calcolati per la previsione mensile.
     */
    public static class ForecastResult {
        private final boolean valid;
        private final String errorMessage;
        private final int currentDay;
        private final int remainingDays;
        private final double dailyExpenseAverage;
        private final double dailyIncomeAverage;
        private final double projectedTotalExpenses;
        private final double projectedTotalIncome;
        private final double estimatedBalance;
        private final ForecastStatus status;

        private ForecastResult(boolean valid, String errorMessage, int currentDay, int remainingDays,
                              double dailyExpenseAverage, double dailyIncomeAverage,
                              double projectedTotalExpenses, double projectedTotalIncome,
                              double estimatedBalance, ForecastStatus status) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.currentDay = currentDay;
            this.remainingDays = remainingDays;
            this.dailyExpenseAverage = dailyExpenseAverage;
            this.dailyIncomeAverage = dailyIncomeAverage;
            this.projectedTotalExpenses = projectedTotalExpenses;
            this.projectedTotalIncome = projectedTotalIncome;
            this.estimatedBalance = estimatedBalance;
            this.status = status;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public int getCurrentDay() { return currentDay; }
        public int getRemainingDays() { return remainingDays; }
        public double getDailyExpenseAverage() { return dailyExpenseAverage; }
        public double getDailyIncomeAverage() { return dailyIncomeAverage; }
        public double getProjectedTotalExpenses() { return projectedTotalExpenses; }
        public double getProjectedTotalIncome() { return projectedTotalIncome; }
        public double getEstimatedBalance() { return estimatedBalance; }
        public ForecastStatus getStatus() { return status; }

        /**
         * Crea un risultato di errore (dati insufficienti).
         */
        public static ForecastResult insufficient(String message) {
            return new ForecastResult(false, message, 0, 0, 0, 0, 0, 0, 0, ForecastStatus.INSUFFICIENT_DATA);
        }

        /**
         * Crea un risultato valido con tutti i calcoli.
         */
        public static ForecastResult valid(int currentDay, int remainingDays,
                                           double dailyExpenseAverage, double dailyIncomeAverage,
                                           double projectedTotalExpenses, double projectedTotalIncome,
                                           double estimatedBalance, ForecastStatus status) {
            return new ForecastResult(true, null, currentDay, remainingDays,
                dailyExpenseAverage, dailyIncomeAverage,
                projectedTotalExpenses, projectedTotalIncome, estimatedBalance, status);
        }
    }

    /**
     * Stato della previsione basato sul saldo stimato.
     */
    public enum ForecastStatus {
        INSUFFICIENT_DATA,  // Dati insufficienti per calcolare
        STABLE,            // Saldo > 200€ - Situazione stabile
        WARNING,           // Saldo tra -100€ e 200€ - Attenzione
        CRITICAL           // Saldo < -100€ - Situazione critica
    }

    // Soglie per la classificazione dello status
    public static final double STABLE_THRESHOLD = 200.0;
    public static final double CRITICAL_THRESHOLD = -100.0;
    public static final int MINIMUM_DAYS_FOR_FORECAST = 3;

    /**
     * Calcola la previsione mensile basandosi sui dati reali.
     *
     * @param totalIncome Totale entrate nel periodo corrente
     * @param totalExpenses Totale uscite nel periodo corrente
     * @param daysWithMovements Numero di giorni con movimenti registrati
     * @param currentDay Giorno corrente del mese (1-31)
     * @param daysInMonth Numero totale di giorni nel mese
     * @return ForecastResult con tutti i calcoli o errore se dati insufficienti
     */
    public ForecastResult calculateForecast(double totalIncome, double totalExpenses,
                                            int daysWithMovements, int currentDay, int daysInMonth) {

        // Validazione: servono almeno 3 giorni di dati
        if (daysWithMovements < MINIMUM_DAYS_FOR_FORECAST) {
            return ForecastResult.insufficient(
                "Dati insufficienti: necessari almeno " + MINIMUM_DAYS_FOR_FORECAST +
                " giorni con movimenti (trovati: " + daysWithMovements + ")");
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

        // Calcolo medie giornaliere (basate sul giorno corrente, non sui giorni con movimenti)
        double dailyExpenseAverage = calculateDailyAverage(totalExpenses, currentDay);
        double dailyIncomeAverage = calculateDailyAverage(totalIncome, currentDay);

        // Calcolo proiezioni totali
        double projectedExpenses = calculateProjectedTotal(totalExpenses, dailyExpenseAverage, remainingDays);
        double projectedIncome = calculateProjectedTotal(totalIncome, dailyIncomeAverage, remainingDays);

        // Calcolo saldo stimato
        double estimatedBalance = calculateEstimatedBalance(projectedIncome, projectedExpenses);

        // Determina lo status
        ForecastStatus status = determineStatus(estimatedBalance);

        return ForecastResult.valid(currentDay, remainingDays,
            dailyExpenseAverage, dailyIncomeAverage,
            projectedExpenses, projectedIncome, estimatedBalance, status);
    }

    /**
     * Calcola la media giornaliera.
     *
     * @param total Totale (entrate o uscite)
     * @param days Numero di giorni
     * @return Media giornaliera (0 se days <= 0)
     */
    public double calculateDailyAverage(double total, int days) {
        if (days <= 0) {
            return 0.0;
        }
        return total / days;
    }

    /**
     * Calcola il totale proiettato a fine mese.
     *
     * @param currentTotal Totale attuale
     * @param dailyAverage Media giornaliera
     * @param remainingDays Giorni rimanenti nel mese
     * @return Totale proiettato
     */
    public double calculateProjectedTotal(double currentTotal, double dailyAverage, int remainingDays) {
        if (remainingDays < 0) {
            return currentTotal;
        }
        return currentTotal + (dailyAverage * remainingDays);
    }

    /**
     * Calcola il saldo stimato a fine mese.
     *
     * @param projectedIncome Entrate proiettate
     * @param projectedExpenses Uscite proiettate
     * @return Saldo stimato (entrate - uscite)
     */
    public double calculateEstimatedBalance(double projectedIncome, double projectedExpenses) {
        return projectedIncome - projectedExpenses;
    }

    /**
     * Determina lo status della previsione in base al saldo.
     *
     * @param estimatedBalance Saldo stimato
     * @return ForecastStatus corrispondente
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
     * Verifica se i dati sono sufficienti per una previsione.
     *
     * @param daysWithMovements Numero di giorni con movimenti
     * @return true se ci sono abbastanza dati
     */
    public boolean hasEnoughData(int daysWithMovements) {
        return daysWithMovements >= MINIMUM_DAYS_FOR_FORECAST;
    }
}
