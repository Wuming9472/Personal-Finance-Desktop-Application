package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;

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

    // Wrapper per ComboBox Categorie (uguale al controller principale)
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
    }

    @FXML
    private void initialize() {
        // Setup Tipo
        typeField.getItems().addAll("Entrata", "Uscita");

        // Setup Metodi di Pagamento
        methodField.getItems().addAll("Contanti", "Bancomat", "Carta di credito", "Bonifico", "Addebito SDD");
        methodField.setEditable(true);

        // Limite Caratteri Descrizione (Max 100)
        descArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                descArea.setText(oldVal); // Blocca scrittura
            } else {
                charCountLabel.setText(newVal.length() + "/100");
            }
        });

        // Carica le categorie dal DB
        loadCategories();
    }

    /**
     * Carica le categorie dal database
     */
    private void loadCategories() {
        DAOMySQLSettings settings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String url = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();

        String sql = "SELECT category_id, name FROM categories ORDER BY category_id ASC";

        try (Connection conn = DriverManager.getConnection(url);
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
     * Precompila i campi con i dati del movimento da modificare
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
     * Valida e salva le modifiche
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Valida l'input e aggiorna l'oggetto movimento
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
     * Ottiene la categoria selezionata
     */
    public int getSelectedCategoryId() {
        return categoryField.getValue() != null ? categoryField.getValue().id : -1;
    }

    /**
     * Ottiene il movimento modificato
     */
    public Movimenti getMovimento() {
        return movimento;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Campi non validi");
        alert.setHeaderText("Correggi i seguenti errori:");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
