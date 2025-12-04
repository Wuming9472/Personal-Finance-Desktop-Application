package it.unicas.project.template.address.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe per il calcolo dei riepiloghi finanziari.
 * Contiene la logica di business per calcolare:
 * - Saldo mensile (entrate - uscite)
 * - Totali per categoria
 * - Percentuali di distribuzione
 * - Aggregazioni per periodo
 *
 * Questa classe e' stata creata per separare la logica dai controller JavaFX
 * e permettere test unitari appropriati.
 */
public class SummaryCalculator {

    /**
     * Rappresenta un movimento finanziario semplificato per i calcoli.
     */
    public static class Movement {
        private final String type;        // "Entrata" o "Uscita"
        private final double amount;
        private final String category;
        private final int dayOfMonth;

        public Movement(String type, double amount, String category, int dayOfMonth) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.dayOfMonth = dayOfMonth;
        }

        public String getType() { return type; }
        public double getAmount() { return amount; }
        public String getCategory() { return category; }
        public int getDayOfMonth() { return dayOfMonth; }

        public boolean isIncome() {
            return type != null && (type.equalsIgnoreCase("Entrata") || type.equalsIgnoreCase("Income"));
        }

        public boolean isExpense() {
            return type != null && (type.equalsIgnoreCase("Uscita") || type.equalsIgnoreCase("Expense"));
        }
    }

    /**
     * Risultato del calcolo del riepilogo mensile.
     */
    public static class MonthlySummary {
        private final double totalIncome;
        private final double totalExpenses;
        private final double balance;
        private final Map<String, Double> expensesByCategory;
        private final Map<String, Double> incomeByCategory;
        private final int transactionCount;

        public MonthlySummary(double totalIncome, double totalExpenses,
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

        public double getTotalIncome() { return totalIncome; }
        public double getTotalExpenses() { return totalExpenses; }
        public double getBalance() { return balance; }
        public Map<String, Double> getExpensesByCategory() { return expensesByCategory; }
        public Map<String, Double> getIncomeByCategory() { return incomeByCategory; }
        public int getTransactionCount() { return transactionCount; }

        public boolean isPositive() { return balance >= 0; }
        public boolean isNegative() { return balance < 0; }
    }

    /**
     * Risultato dell'aggregazione per periodo (es. 3 giorni).
     */
    public static class PeriodAggregate {
        private final int periodIndex;
        private final int startDay;
        private final int endDay;
        private final double income;
        private final double expenses;
        private final double balance;

        public PeriodAggregate(int periodIndex, int startDay, int endDay, double income, double expenses) {
            this.periodIndex = periodIndex;
            this.startDay = startDay;
            this.endDay = endDay;
            this.income = income;
            this.expenses = expenses;
            this.balance = income - expenses;
        }

        public int getPeriodIndex() { return periodIndex; }
        public int getStartDay() { return startDay; }
        public int getEndDay() { return endDay; }
        public double getIncome() { return income; }
        public double getExpenses() { return expenses; }
        public double getBalance() { return balance; }
        public String getLabel() { return startDay + "-" + endDay; }
    }

    /**
     * Calcola il riepilogo mensile da una lista di movimenti.
     *
     * @param movements Lista dei movimenti del mese
     * @return MonthlySummary con tutti i totali calcolati
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

        return new MonthlySummary(totalIncome, totalExpenses,
            expensesByCategory, incomeByCategory, movements.size());
    }

    /**
     * Calcola il saldo mensile (entrate - uscite).
     *
     * @param totalIncome Totale entrate
     * @param totalExpenses Totale uscite
     * @return Saldo (positivo o negativo)
     */
    public double calculateBalance(double totalIncome, double totalExpenses) {
        return totalIncome - totalExpenses;
    }

    /**
     * Calcola la percentuale di una categoria rispetto al totale.
     *
     * @param categoryAmount Importo della categoria
     * @param total Totale di riferimento
     * @return Percentuale (0-100), 0 se totale e' 0
     */
    public double calculateCategoryPercentage(double categoryAmount, double total) {
        if (total <= 0) {
            return 0.0;
        }
        return (categoryAmount / total) * 100.0;
    }

    /**
     * Aggrega i movimenti in periodi di N giorni.
     *
     * @param movements Lista dei movimenti
     * @param periodDays Numero di giorni per periodo (es. 3)
     * @param daysInMonth Numero di giorni nel mese
     * @return Array di aggregati per periodo
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
     * Calcola i totali giornalieri per un mese.
     *
     * @param movements Lista dei movimenti
     * @param daysInMonth Numero di giorni nel mese
     * @return Array di saldi giornalieri (indice 0 = giorno 1)
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
     * Calcola il saldo cumulativo progressivo.
     *
     * @param dailyBalances Array di saldi giornalieri
     * @return Array di saldi cumulativi
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
     * Trova la categoria con la spesa maggiore.
     *
     * @param expensesByCategory Mappa delle spese per categoria
     * @return Nome della categoria con spesa maggiore, o null se vuota
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
     * Calcola il risparmio (o deficit) mensile come percentuale delle entrate.
     *
     * @param totalIncome Totale entrate
     * @param totalExpenses Totale uscite
     * @return Percentuale di risparmio (positiva) o deficit (negativa)
     */
    public double calculateSavingsRate(double totalIncome, double totalExpenses) {
        if (totalIncome <= 0) {
            return 0.0;
        }
        double savings = totalIncome - totalExpenses;
        return (savings / totalIncome) * 100.0;
    }
}
