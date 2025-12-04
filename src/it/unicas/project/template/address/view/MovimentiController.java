package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import it.unicas.project.template.address.util.BudgetNotificationHelper;
import it.unicas.project.template.address.util.BudgetNotificationPreferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private MovimentiGateway movimentiGateway = new StaticMovimentiGateway();
    private Supplier<DAOMySQLSettings> settingsSupplier = DAOMySQLSettings::getCurrentDAOMySQLSettings;
    private Function<String, Connection> connectionFactory = url -> {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    // Wrapper per ComboBox Categorie
    private static class CategoryItem {
        int id;
        String name;
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

    public void setMovimentiGateway(MovimentiGateway movimentiGateway) {
        this.movimentiGateway = movimentiGateway;
    }

    public void setSettingsSupplier(Supplier<DAOMySQLSettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
    }

    public void setConnectionFactory(Function<String, Connection> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    private void loadCategories() {
        DAOMySQLSettings settings = settingsSupplier.get();
        String url = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();

        // Ordine per ID ASC come richiesto
        String sql = "SELECT category_id, name FROM categories ORDER BY category_id ASC";

        try (Connection conn = connectionFactory.apply(url);
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
            movementData.setAll(movimentiGateway.findByUser(mainApp.getLoggedUser().getUser_id()));
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
            movimentiGateway.insert(m, userId, categoryId);

            loadMovementsForCurrentUser();

            // Controlla se il budget è stato superato per questa specifica categoria
            checkBudgetAfterTransaction(categoryId);

            // Aggiorna i dati del report e della dashboard se sono aperti
            refreshReportData();
            refreshDashboardData();

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
                movimentiGateway.delete(movement.getMovement_id());
            }

            // Ricarica i dati
            loadMovementsForCurrentUser();

            // Aggiorna lo stato dei budget dopo la cancellazione
            checkBudgetAfterDeletion();

            // Aggiorna i dati del report e della dashboard se sono aperti
            refreshReportData();
            refreshDashboardData();

        } catch (Exception e) {
            showError("Errore cancellazione: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditTransaction() {
        Movimenti selectedMovement = transactionTable.getSelectionModel().getSelectedItem();

        if (selectedMovement == null) {
            showError("Seleziona un movimento da modificare.");
            return;
        }

        // Verifica che sia selezionato solo un movimento
        if (transactionTable.getSelectionModel().getSelectedItems().size() > 1) {
            showError("Puoi modificare un solo movimento alla volta.\nSeleziona un unico movimento.");
            return;
        }

        try {
            // Carica il dialog FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("EditMovimentoDialog.fxml"));
            DialogPane dialogPane = loader.load();

            // Ottieni il controller e passa i dati
            EditMovimentoDialogController controller = loader.getController();
            controller.setMovimento(selectedMovement);

            // Crea e mostra il dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Modifica Movimento");

            // Gestisci il risultato
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                // Valida e salva le modifiche
                if (controller.validateAndSave()) {
                    Movimenti updatedMovement = controller.getMovimento();
                    int categoryId = controller.getSelectedCategoryId();

                    // Salva nel database
                    movimentiGateway.update(updatedMovement, categoryId);

                    // Ricarica i dati
                    loadMovementsForCurrentUser();

                    // Controlla il budget dopo la modifica
                    checkBudgetAfterTransaction(categoryId);

                    // Aggiorna i dati del report e della dashboard
                    refreshReportData();
                    refreshDashboardData();

                    // Mostra un messaggio di successo
                    showSuccess("Movimento modificato con successo!");
                }
            }

        } catch (IOException e) {
            showError("Errore nel caricamento del dialog: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Errore nella modifica: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Controlla il budget del mese corrente per una specifica categoria e mostra una notifica se è stato superato.
     * Viene chiamato dopo l'inserimento di un movimento.
     *
     * @param categoryId ID della categoria del movimento appena inserito
     */
    private void checkBudgetAfterTransaction(int categoryId) {
        if (mainApp == null || mainApp.getLoggedUser() == null) return;

        try {
            int userId = mainApp.getLoggedUser().getUser_id();
            LocalDate now = LocalDate.now();
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();

            List<Budget> budgets = budgetDAO.getBudgetsForMonth(userId, currentMonth, currentYear);

            // 🔁 Prima: per TUTTE le categorie che NON sono più superate,
            // tolgo il flag "già notificato", così se in futuro risuperano il budget
            // la notifica potrà riapparire.
            for (Budget budget : budgets) {
                if (budget.getCategoryId() == 6) continue; // Ignora Stipendio

                boolean isExceeded = budget.getSpentAmount() > budget.getBudgetAmount()
                        && budget.getBudgetAmount() > 0;

                if (!isExceeded) {
                    // La categoria non è più superata → riattivo la notifica
                    BudgetNotificationPreferences.getInstance()
                            .unmarkAsNotified(budget.getCategoryId());
                }
            }

            // 🔔 Poi controllo SOLO la categoria del movimento appena inserito
            BudgetNotificationHelper.checkAndNotifyForCategory(budgets, categoryId);

        } catch (SQLException e) {
            // Silenzioso: non blocchiamo l'operazione se il controllo budget fallisce
            System.err.println("Errore nel controllo budget: " + e.getMessage());
        }
    }

    /**
     * Controlla i budget del mese corrente dopo una cancellazione.
     * Non mostra notifiche, ma aggiorna lo stato delle categorie che non sono più superate.
     */
    private void checkBudgetAfterDeletion() {
        if (mainApp == null || mainApp.getLoggedUser() == null) return;

        try {
            int userId = mainApp.getLoggedUser().getUser_id();
            LocalDate now = LocalDate.now();
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();

            List<Budget> budgets = budgetDAO.getBudgetsForMonth(userId, currentMonth, currentYear);

            // Controlla tutte le categorie per vedere se alcune non sono più superate
            for (Budget budget : budgets) {
                if (budget.getCategoryId() == 6) continue; // Ignora Stipendio

                boolean isExceeded = budget.getSpentAmount() > budget.getBudgetAmount()
                        && budget.getBudgetAmount() > 0;

                if (!isExceeded) {
                    // La categoria non è più superata, rimuovi la marcatura
                    BudgetNotificationPreferences.getInstance().unmarkAsNotified(budget.getCategoryId());
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore nel controllo budget dopo cancellazione: " + e.getMessage());
        }
    }

    /**
     * Aggiorna i dati del report (inclusa la previsione) se il controller del report è disponibile.
     * Questo metodo viene chiamato dopo ogni operazione sui movimenti (insert, delete).
     */
    private void refreshReportData() {
        if (mainApp != null && mainApp.getReportController() != null) {
            mainApp.getReportController().refreshReportData();
        }
    }

    /**
     * Aggiorna i dati della dashboard se il controller della dashboard è disponibile.
     * Questo metodo viene chiamato dopo ogni operazione sui movimenti (insert, delete).
     */
    private void refreshDashboardData() {
        if (mainApp != null && mainApp.getDashboardController() != null) {
            mainApp.getDashboardController().refreshDashboardData();
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public interface MovimentiGateway {
        List<Movimenti> findByUser(int userId) throws DAOException, SQLException;

        void insert(Movimenti m, int userId, int categoryId) throws DAOException, SQLException;

        void delete(int movementId) throws DAOException, SQLException;

        void update(Movimenti m, int categoryId) throws DAOException, SQLException;
    }

    private static class StaticMovimentiGateway implements MovimentiGateway {
        @Override
        public List<Movimenti> findByUser(int userId) throws DAOException, SQLException {
            return MovimentiDAOMySQLImpl.findByUser(userId);
        }

        @Override
        public void insert(Movimenti m, int userId, int categoryId) throws DAOException, SQLException {
            MovimentiDAOMySQLImpl.insert(m, userId, categoryId);
        }

        @Override
        public void delete(int movementId) throws DAOException, SQLException {
            MovimentiDAOMySQLImpl.delete(movementId);
        }

        @Override
        public void update(Movimenti m, int categoryId) throws DAOException, SQLException {
            MovimentiDAOMySQLImpl.update(m, categoryId);
        }
    }
}
