package it.unicas.project.template.address.model;

import javafx.beans.property.*;

/**
 * Rappresenta il budget mensile di una specifica categoria per un utente.
 * <p>
 * I campi principali (budgetId, categoryId, userId, month, year, budgetAmount)
 * corrispondono alle colonne della tabella <code>budgets</code> nel database.
 * I campi aggiuntivi (<code>categoryName</code>, <code>spentAmount</code>) non
 * sono persistiti direttamente nella tabella, ma vengono calcolati/arricchiti
 * a runtime e utilizzati per la visualizzazione nell'interfaccia grafica.
 * <br>
 * La classe utilizza le JavaFX Property per permettere il binding con i controlli
 * dell'interfaccia utente.
 */
public class Budget {

    // Campi che corrispondono alla tabella 'budgets' del DB
    private final IntegerProperty budgetId;
    private final IntegerProperty categoryId;
    private final IntegerProperty userId;
    private final IntegerProperty month;
    private final IntegerProperty year;
    private final DoubleProperty budgetAmount; // Il limite fissato (es. 400€)

    // Campi EXTRA (non sono nella tabella budgets, ma ci servono per la grafica)
    private final StringProperty categoryName; // Es. "Alimentari"
    private final DoubleProperty spentAmount;  // Es. 120.50€ (somma delle spese reali)

    /**
     * Costruttore di default.
     * <p>
     * Inizializza il budget con valori di default (zero o stringa vuota).
     * Utile per framework come JavaFX e per eventuali casi in cui
     * l'oggetto venga popolato successivamente tramite setter.
     */
    public Budget() {
        this(0, 0, 0, 0, 0, 0.0, "", 0.0);
    }

    /**
     * Costruttore completo.
     *
     * @param budgetId     identificativo univoco del budget.
     * @param categoryId   identificativo della categoria di spesa.
     * @param userId       identificativo dell'utente proprietario del budget.
     * @param month        mese di riferimento (1-12).
     * @param year         anno di riferimento.
     * @param budgetAmount importo massimo previsto per la categoria.
     * @param categoryName nome descrittivo della categoria (es. "Alimentari").
     * @param spentAmount  importo già speso per la categoria nel periodo.
     */
    public Budget(int budgetId, int categoryId, int userId, int month, int year,
                  double budgetAmount, String categoryName, double spentAmount) {
        this.budgetId = new SimpleIntegerProperty(budgetId);
        this.categoryId = new SimpleIntegerProperty(categoryId);
        this.userId = new SimpleIntegerProperty(userId);
        this.month = new SimpleIntegerProperty(month);
        this.year = new SimpleIntegerProperty(year);
        this.budgetAmount = new SimpleDoubleProperty(budgetAmount);
        this.categoryName = new SimpleStringProperty(categoryName);
        this.spentAmount = new SimpleDoubleProperty(spentAmount);
    }

    // --- GETTER E SETTER (Stile JavaFX Property per binding futuri) ---

    public int getBudgetId() { return budgetId.get(); }
    public void setBudgetId(int budgetId) { this.budgetId.set(budgetId); }
    public IntegerProperty budgetIdProperty() { return budgetId; }

    public int getCategoryId() { return categoryId.get(); }
    public void setCategoryId(int categoryId) { this.categoryId.set(categoryId); }
    public IntegerProperty categoryIdProperty() { return categoryId; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }

    public int getMonth() { return month.get(); }
    public void setMonth(int month) { this.month.set(month); }

    public int getYear() { return year.get(); }
    public void setYear(int year) { this.year.set(year); }

    public double getBudgetAmount() { return budgetAmount.get(); }
    public void setBudgetAmount(double budgetAmount) { this.budgetAmount.set(budgetAmount); }
    public DoubleProperty budgetAmountProperty() { return budgetAmount; }

    public String getCategoryName() { return categoryName.get(); }
    public void setCategoryName(String categoryName) { this.categoryName.set(categoryName); }
    public StringProperty categoryNameProperty() { return categoryName; }

    public double getSpentAmount() { return spentAmount.get(); }
    public void setSpentAmount(double spentAmount) { this.spentAmount.set(spentAmount); }
    public DoubleProperty spentAmountProperty() { return spentAmount; }

    // --- METODI DI UTILITÀ PER LA GRAFICA ---

    /**
     * Calcola la percentuale di spesa (da 0.0 a 1.0).
     * Se il budget è 0, ritorna 0 per evitare divisioni per zero.
     */
    public double getProgress() {
        if (getBudgetAmount() == 0) return 0.0;
        return getSpentAmount() / getBudgetAmount();
    }

    /**
     * Calcola quanto rimane del budget.
     * Può essere negativo se hai sforato.
     */
    public double getRemaining() {
        return getBudgetAmount() - getSpentAmount();
    }
}