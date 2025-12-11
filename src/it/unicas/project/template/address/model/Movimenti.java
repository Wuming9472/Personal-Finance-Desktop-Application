package it.unicas.project.template.address.model;

import java.time.LocalDate;
import javafx.beans.property.*;

/**
 * Rappresenta un singolo movimento contabile (entrata o uscita)
 * registrato dall'utente.
 * <p>
 * Contiene informazioni su:
 * <ul>
 *     <li>tipo di movimento (entrata/uscita);</li>
 *     <li>data;</li>
 *     <li>importo;</li>
 *     <li>titolo/descrizione;</li>
 *     <li>metodo di pagamento;</li>
 *     <li>categoria (id e nome) per la visualizzazione nella UI.</li>
 * </ul>
 * <br>
 * La classe utilizza le JavaFX Property per supportare il binding
 * diretto con i controlli dell'interfaccia grafica (TableView, ecc.).
 */
public class Movimenti {

    // Campi Property (Snake_case come da tua definizione)
    private final IntegerProperty movement_id;
    private final StringProperty type;
    private final ObjectProperty<LocalDate> date;
    private final FloatProperty amount;
    private final StringProperty title;
    private final StringProperty payment_method;

    // NUOVI CAMPI (Per visualizzare la Categoria nella tabella)
    // Li inizializziamo fuori dal costruttore per non romperlo
    private final StringProperty categoryName = new SimpleStringProperty("");
    private final IntegerProperty categoryId = new SimpleIntegerProperty(0);



    /**
     * Costruttore di default.
     * <p>
     * Inizializza il movimento con valori di base (id non valido,
     * importo a zero, stringhe vuote), da popolare successivamente
     * tramite i setter.
     */
    public Movimenti() {
        this(null, null, null, null, null, null);
    }

    /**
     * Costruttore completo.
     *
     * @param id              identificativo univoco del movimento (può essere {@code null} per movimenti nuovi).
     * @param type            tipo di movimento ("Entrata" o "Uscita").
     * @param date            data del movimento.
     * @param amount          importo del movimento.
     * @param title           titolo/descrizione breve del movimento.
     * @param payment_method  metodo di pagamento utilizzato (es. contanti, carta, bonifico).
     */
    public Movimenti(Integer id, String type, LocalDate date, Float amount, String title, String payment_method) {
        this.movement_id = new SimpleIntegerProperty(id != null ? id : -1); // Un piccolo fix per evitare null pointer sul Integer
        this.type = new SimpleStringProperty(type);
        this.date = new SimpleObjectProperty<>(date);
        this.amount = new SimpleFloatProperty(amount != null ? amount : 0.0f);
        this.title = new SimpleStringProperty(title != null ? title : "");
        this.payment_method = new SimpleStringProperty(payment_method != null ? payment_method : "");
    }

    /**
     * Imposta il titolo/descrizione del movimento e restituisce
     * la stessa istanza per permettere una scrittura fluente.
     *
     * @param val titolo/descrizione da impostare.
     * @return questa stessa istanza di {@code Movimenti}.
     */
    public Movimenti title(String val) {
        this.title.set(val);
        return this;
    }

    /**
     * Imposta il metodo di pagamento del movimento e restituisce
     * la stessa istanza per permettere una scrittura fluente.
     *
     * @param val metodo di pagamento da impostare.
     * @return questa stessa istanza di {@code Movimenti}.
     */
    public Movimenti paymentMethod(String val) {
        this.payment_method.set(val);
        return this;
    }

    // ========================================================================
    // GETTERS & SETTERS & PROPERTIES (Aggiornati per Movimenti)
    // ========================================================================

    // --- ID ---
    public Integer getMovement_id() {
        return movement_id.get();
    }

    public void setMovement_id(int movement_id) {
        this.movement_id.set(movement_id);
    }

    public IntegerProperty movement_idProperty() {
        return movement_id;
    }

    // --- TYPE ---
    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    // --- DATE ---
    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    // --- AMOUNT ---
    public float getAmount() {
        return amount.get();
    }

    public void setAmount(float amount) {
        this.amount.set(amount);
    }

    public FloatProperty amountProperty() {
        return amount;
    }

    // --- TITLE ---
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    // --- PAYMENT METHOD ---
    public String getPayment_method() {
        return payment_method.get();
    }

    public void setPayment_method(String payment_method) {
        this.payment_method.set(payment_method);
    }

    public StringProperty payment_methodProperty() {
        return payment_method;
    }

    // --- NUOVI GETTERS/SETTERS PER CATEGORIA ---
    public String getCategoryName() {
        return categoryName.get();
    }

    public void setCategoryName(String name) {
        this.categoryName.set(name);
    }

    public StringProperty categoryNameProperty() {
        return categoryName;
    }

    public int getCategoryId() {
        return categoryId.get();
    }

    public void setCategoryId(int id) {
        this.categoryId.set(id);
    }

    public IntegerProperty categoryIdProperty() {
        return categoryId;
    }
}