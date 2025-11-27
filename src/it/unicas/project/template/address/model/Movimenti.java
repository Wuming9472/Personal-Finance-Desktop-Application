package it.unicas.project.template.address.model;

import java.time.LocalDate;
import javafx.beans.property.*;

/**
 * Model class for Movimenti.
 * Updated to replace the old Amici class.
 */
public class Movimenti {

    // Campi Property (Snake_case come da tua definizione)
    private final IntegerProperty movement_id;
    private final StringProperty type;
    private final ObjectProperty<LocalDate> date;
    private final FloatProperty amount;
    private final StringProperty description;
    private final StringProperty payment_method;

    // NUOVI CAMPI (Per visualizzare la Categoria nella tabella)
    // Li inizializziamo fuori dal costruttore per non romperlo
    private final StringProperty categoryName = new SimpleStringProperty("");
    private final IntegerProperty categoryId = new SimpleIntegerProperty(0);

    // ========================================================================
    // COSTRUTTORI (INTATTI come richiesto)
    // ========================================================================

    /**
     * Default constructor.
     */
    public Movimenti() {
        this(null, null, null, null, null, null);
    }

    public Movimenti(Integer id, String type, LocalDate date, Float amount, String description, String payment_method) {
        this.movement_id = new SimpleIntegerProperty(id != null ? id : -1); // Un piccolo fix per evitare null pointer sul Integer
        this.type = new SimpleStringProperty(type);
        this.date = new SimpleObjectProperty<>(date);
        this.amount = new SimpleFloatProperty(amount != null ? amount : 0.0f);
        this.description = new SimpleStringProperty(description != null ? description : "");
        this.payment_method = new SimpleStringProperty(payment_method != null ? payment_method : "");
    }

    // ========================================================================
    // METODI FLUENT (INTATTI come richiesto)
    // ========================================================================

    // Metodo per aggiungere la descrizione (opzionale)
    public Movimenti description(String val) {
        this.description.set(val);
        return this;
    }

    // Metodo per aggiungere il metodo di pagamento (opzionale)
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

    // --- DESCRIPTION ---
    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
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