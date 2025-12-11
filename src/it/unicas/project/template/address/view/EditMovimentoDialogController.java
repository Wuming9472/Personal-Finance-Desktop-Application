package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;

/**
 * Controller JavaFX per la finestra di dialogo di modifica di un movimento.
 * <p>
 * Gestisce il popolamento dei campi, la validazione dell'input
 * e l'aggiornamento dell'oggetto {@link Movimenti} associato.
 */
public class EditMovimentoDialogController {

    @FXML private TextField amountField;
    @FXML private ComboBox<String> typeField;
    @FXML private DatePicker dateField;
    @FXML private ComboBox<CategoryItem> categoryField;
    @FXML private ComboBox<String> methodField;
    @FXML private TextArea descArea;
    @FXML private Label charCountLabel;

    private Movimenti movimento;
    private boolean okClicked = false;

    /**
     * Wrapper per rappresentare una categoria all'interno della ComboBox.
     * <p>
     * Espone un identificativo numerico e un nome leggibile, e
     * ridefinisce {@link #toString()} per mostrare correttamente
     * il nome nella UI.
     */
    public static class CategoryItem {
        int id;
        String name;
        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
        public int getId() {return id;}
    }

    /**
     * Inizializza i campi della dialog:
     * <ul>
     *     <li>popola le ComboBox (tipo, metodo di pagamento);</li>
     *     <li>applica i formattatori per limitare la lunghezza dell'importo,
     *         del metodo di pagamento e della descrizione;</li>
     *     <li>aggiorna il contatore di caratteri;</li>
     *     <li>carica le categorie dal database.</li>
     * </ul>
     * Viene chiamato automaticamente da JavaFX dopo il caricamento dell'FXML.
     */
    @FXML
    private void initialize() {
        // Setup Tipo
        typeField.getItems().addAll("Entrata", "Uscita");

        // Setup Metodi di Pagamento
        methodField.getItems().addAll("Contanti", "Bancomat", "Carta di credito", "Bonifico", "Addebito SDD");
        methodField.setEditable(true);

        // Limite importo (Max 10 cifre)
        amountField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            // Rimuovi caratteri non numerici per contare le cifre (escluso virgola/punto decimale)
            String digitsOnly = newText.replaceAll("[^0-9]", "");
            if (digitsOnly.length() > 10) {
                int excess = digitsOnly.length() - 10;
                if (change.getText().length() > excess) {
                    change.setText(change.getText().substring(0, change.getText().length() - excess));
                } else {
                    return null;
                }
            }
            return change;
        }));

        // Limite metodo di pagamento (Max 32 caratteri) - per input manuale
        methodField.getEditor().setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 32) {
                int excess = newText.length() - 32;
                if (change.getText().length() > excess) {
                    change.setText(change.getText().substring(0, change.getText().length() - excess));
                } else {
                    return null;
                }
            }
            return change;
        }));

        // Limite Caratteri Descrizione/Titolo (Max 40)
        descArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 40) {
                descArea.setText(oldVal); // Blocca scrittura
            } else {
                charCountLabel.setText(newVal.length() + "/40");
            }
        });

        // Carica le categorie dal DB
        loadCategories();
    }

    /**
     * Carica l'elenco delle categorie dal database e lo imposta
     * nella ComboBox delle categorie.
     * <p>
     * In caso di errore durante la lettura dal database mostra
     * un messaggio di errore all'utente.
     */
    private void loadCategories() {
        String sql = "SELECT category_id, name FROM categories ORDER BY category_id ASC";

        try (Connection conn = DAOMySQLSettings.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ObservableList<CategoryItem> categories = FXCollections.observableArrayList();
            while (rs.next()) {
                categories.add(new CategoryItem(rs.getInt("category_id"), rs.getString("name")));
            }
            categoryField.setItems(categories);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Errore nel caricamento delle categorie: " + e.getMessage());
        }
    }

    /**
     * Precompila i campi della dialog con i dati del movimento
     * da modificare.
     * <p>
     * Imposta importo, tipo, data, descrizione, metodo di pagamento
     * e seleziona la categoria corretta nella ComboBox.
     *
     * @param movimento movimento da visualizzare e modificare.
     */
    public void setMovimento(Movimenti movimento) {
        this.movimento = movimento;

        amountField.setText(String.valueOf(movimento.getAmount()));
        typeField.setValue(movimento.getType());
        dateField.setValue(movimento.getDate());
        descArea.setText(movimento.getTitle() != null ? movimento.getTitle() : "");
        methodField.setValue(movimento.getPayment_method());

        // Seleziona la categoria corretta nel ComboBox
        int categoryId = movimento.getCategoryId();
        for (CategoryItem item : categoryField.getItems()) {
            if (item.id == categoryId) {
                categoryField.setValue(item);
                break;
            }
        }
    }

    /**
     * Indica se l'utente ha confermato le modifiche con esito positivo.
     *
     * @return {@code true} se la validazione è andata a buon fine e
     *         l'utente ha confermato, {@code false} altrimenti.
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Valida l'input dell'utente e, se non ci sono errori, aggiorna
     * l'oggetto {@link Movimenti} con i nuovi valori inseriti.
     * <p>
     * Controlla:
     * <ul>
     *     <li>importo non vuoto, numerico e maggiore di zero;</li>
     *     <li>tipo selezionato;</li>
     *     <li>data selezionata;</li>
     *     <li>categoria selezionata.</li>
     * </ul>
     * In caso di errori mostra una finestra di alert.
     *
     * @return {@code true} se i dati sono validi e il movimento è stato aggiornato,
     *         {@code false} in caso di errori di validazione.
     */
    public boolean validateAndSave() {
        String errorMessage = "";

        // Validazione Importo
        if (amountField.getText() == null || amountField.getText().isEmpty()) {
            errorMessage += "Inserisci un importo valido!\n";
        } else {
            try {
                float amount = Float.parseFloat(amountField.getText().replace(",", "."));
                if (amount <= 0) {
                    errorMessage += "L'importo deve essere maggiore di 0!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Formato importo non valido!\n";
            }
        }

        // Validazione Tipo
        if (typeField.getValue() == null) {
            errorMessage += "Seleziona un tipo di movimento!\n";
        }

        // Validazione Data
        if (dateField.getValue() == null) {
            errorMessage += "Seleziona una data!\n";
        }

        // Validazione Categoria
        if (categoryField.getValue() == null) {
            errorMessage += "Seleziona una categoria!\n";
        }

        if (errorMessage.length() == 0) {
            // Aggiorna l'oggetto movimento con i nuovi valori
            movimento.setAmount(Float.parseFloat(amountField.getText().replace(",", ".")));
            movimento.setType(typeField.getValue());
            movimento.setDate(dateField.getValue());
            movimento.setTitle(descArea.getText() != null ? descArea.getText().trim() : "");
            movimento.setPayment_method(methodField.getValue() != null ? methodField.getValue() : "");
            movimento.setCategoryId(categoryField.getValue().id);
            movimento.setCategoryName(categoryField.getValue().name);

            okClicked = true;
            return true;
        } else {
            // Mostra gli errori
            showError(errorMessage);
            return false;
        }
    }

    /**
     * Restituisce l'identificativo numerico della categoria
     * attualmente selezionata nella ComboBox.
     *
     * @return id della categoria selezionata oppure -1 se nessuna categoria è selezionata.
     */
    public int getSelectedCategoryId() {
        return categoryField.getValue() != null ? categoryField.getValue().id : -1;
    }

    /**
     * Restituisce il movimento associato alla dialog, eventualmente
     * aggiornato con i nuovi valori inseriti dall'utente.
     *
     * @return oggetto {@link Movimenti} correntemente modificato.
     */
    public Movimenti getMovimento() {
        return movimento;
    }

    /**
     * Mostra una finestra di dialogo di errore con il messaggio
     * di validazione passato come parametro.
     *
     * @param message testo da visualizzare nel contenuto dell'alert.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Campi non validi");
        alert.setHeaderText("Correggi i seguenti errori:");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
