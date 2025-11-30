package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import it.unicas.project.template.address.util.BudgetNotificationHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.paint.Color;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovimentiController {

    // TABELLA
    @FXML private TableView<Movimenti> transactionTable;
    @FXML private TableColumn<Movimenti, LocalDate> dateColumn;
    @FXML private TableColumn<Movimenti, String> typeColumn;
    @FXML private TableColumn<Movimenti, String> categoryColumn; // Ora si popolerà!
    @FXML private TableColumn<Movimenti, String> titleColumn;
    @FXML private TableColumn<Movimenti, String> paymentMethodColumn;
    @FXML private TableColumn<Movimenti, Number> amountColumn;

    // INPUT SINISTRA
    @FXML private TextField amountField;
    @FXML private ComboBox<String> typeField;
    @FXML private DatePicker dateField;
    @FXML private ComboBox<CategoryItem> categoryField; // Dal DB
    @FXML private ComboBox<String> methodField; // Fisso
    @FXML private TextArea descArea;
    @FXML private Label charCountLabel;

    private MainApp mainApp;
    private final ObservableList<Movimenti> movementData = FXCollections.observableArrayList();
    private final BudgetDAOMySQLImpl budgetDAO = new BudgetDAOMySQLImpl();

    // Wrapper per ComboBox Categorie
    private static class CategoryItem {
        int id; String name;
        public CategoryItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    @FXML
    private void initialize() {
        // 1. SETUP COLONNE
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        paymentMethodColumn.setCellValueFactory(cellData -> cellData.getValue().payment_methodProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryNameProperty());

        // Colori Importo
        amountColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item.floatValue()));
                    Movimenti row = getTableView().getItems().get(getIndex());
                    if (row.getType().equalsIgnoreCase("Uscita")) {
                        setTextFill(Color.web("#ef4444"));
                        setText("- " + getText());
                    } else {
                        setTextFill(Color.web("#10b981"));
                        setText("+ " + getText());
                    }
                }
            }
        });

        // 2. SETUP INPUT FIELDS
        typeField.getItems().addAll("Entrata", "Uscita");
        typeField.setValue("Uscita");
        dateField.setValue(LocalDate.now());

        // Metodi di Pagamento (Ordine Richiesto)
        methodField.getItems().addAll("Contanti", "Bancomat", "Carta di credito", "Bonifico", "Addebito SDD");

        // 3. LIMITE CARATTERI DESCRIZIONE (Max 100)
        descArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 100) {
                descArea.setText(oldVal); // Blocca scrittura
            } else {
                charCountLabel.setText(newVal.length() + "/100");
            }
        });

        // 4. ABILITA SELEZIONE MULTIPLA
        transactionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        transactionTable.setItems(movementData);
        loadCategories();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        loadMovementsForCurrentUser();
    }

    private void loadCategories() {
        DAOMySQLSettings settings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String url = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();

        // Ordine per ID ASC come richiesto
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
        }
    }

    private void loadMovementsForCurrentUser() {
        if (mainApp == null || mainApp.getLoggedUser() == null) return;
        try {
            movementData.setAll(MovimentiDAOMySQLImpl.findByUser(mainApp.getLoggedUser().getUser_id()));
            transactionTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewTransaction() {
        if (mainApp == null || mainApp.getLoggedUser() == null) {
            showError("Devi effettuare il login.");
            return;
        }

        // Validazione Minima (Importo e Categoria)
        if (amountField.getText().isEmpty()) {
            showError("Inserisci un importo.");
            return;
        }
        if (categoryField.getValue() == null) {
            showError("Seleziona una categoria.");
            return;
        }
        if (Float.parseFloat(amountField.getText()) <= 0) {
            showError("Inserisci un importo maggiore di 0.");
            return;
        }

        try {
            int userId = mainApp.getLoggedUser().getUser_id();
            int categoryId = categoryField.getValue().id;

            String type = typeField.getValue();
            LocalDate date = dateField.getValue();
            float amount = Float.parseFloat(amountField.getText().replace(",", "."));

            // Facoltativi
            String desc = (descArea.getText() == null) ? "" : descArea.getText().trim();
            String method = (methodField.getValue() == null) ? "" : methodField.getValue();

            Movimenti m = new Movimenti(null, type, date, amount, desc, method);

            // Insert DB
            MovimentiDAOMySQLImpl.insert(m, userId, categoryId);

            loadMovementsForCurrentUser();

            // Controlla se il budget è stato superato dopo l'inserimento
            checkBudgetAfterTransaction();

            // Reset Campi (lascio la data e il tipo perché comodi)
            amountField.clear();
            descArea.clear();
            methodField.getSelectionModel().clearSelection();
            categoryField.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            showError("L'importo non è valido.");
        } catch (Exception e) {
            showError("Errore salvataggio: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTransaction() {
        ObservableList<Movimenti> selectedItems = transactionTable.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            showError("Seleziona almeno una riga da eliminare.");
            return;
        }

        // Chiedi conferma se ci sono più elementi selezionati
        if (selectedItems.size() > 1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText("Sei sicuro di voler eliminare " + selectedItems.size() + " transazioni?");
            if (alert.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }

        try {
            // Creo una lista temporanea per evitare ConcurrentModificationException
            List<Movimenti> toDelete = new ArrayList<>(selectedItems);

            for (Movimenti movement : toDelete) {
                MovimentiDAOMySQLImpl.delete(movement.getMovement_id());
            }

            // Ricarica i dati
            loadMovementsForCurrentUser();

            // Controlla se il budget è stato superato dopo la cancellazione
            checkBudgetAfterTransaction();

        } catch (Exception e) {
            showError("Errore cancellazione: " + e.getMessage());
        }
    }

    /**
     * Controlla i budget del mese corrente e mostra una notifica se qualche budget è stato superato.
     * Viene chiamato dopo l'inserimento o la cancellazione di un movimento.
     */
    private void checkBudgetAfterTransaction() {
        if (mainApp == null || mainApp.getLoggedUser() == null) return;

        try {
            int userId = mainApp.getLoggedUser().getUser_id();
            LocalDate now = LocalDate.now();
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();

            List<Budget> budgets = budgetDAO.getBudgetsForMonth(userId, currentMonth, currentYear);
            BudgetNotificationHelper.checkAndNotifyBudgetExceeded(budgets);

        } catch (SQLException e) {
            // Silenzioso: non blocchiamo l'operazione se il controllo budget fallisce
            System.err.println("Errore nel controllo budget: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}