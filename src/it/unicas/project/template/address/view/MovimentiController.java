package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class MovimentiController {

    @FXML
    private TableView<Movimenti> transactionTable;

    @FXML
    private TableColumn<Movimenti, LocalDate> dateColumn;
    @FXML
    private TableColumn<Movimenti, String> descriptionColumn;
    @FXML
    private TableColumn<Movimenti, String> categoryColumn; // per ora vuota
    @FXML
    private TableColumn<Movimenti, Number> amountColumn;
    @FXML
    private TableColumn<Movimenti, Void> actionsColumn; // c'è nell'FXML

    private MainApp mainApp;

    private final ObservableList<Movimenti> movementData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // collega le colonne alle property della classe Movimenti
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        // categoryColumn e actionsColumn le sistemerai dopo

        transactionTable.setItems(movementData);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        loadMovementsForCurrentUser();   // appena ho la MainApp, carico i movimenti dal DB
    }

    private void loadMovementsForCurrentUser() {
        if (mainApp == null || mainApp.getLoggedUser() == null) {
            return;
        }

        int userId = mainApp.getLoggedUser().getUser_id();  // ⬅ usa la tua classe User

        try {
            movementData.setAll(MovimentiDAOMySQLImpl.findByUser(userId));
            transactionTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore durante il caricamento dei movimenti.");
        }
    }

    @FXML
    private void handleNewTransaction() {
        if (mainApp == null || mainApp.getLoggedUser() == null) {
            showError("Nessun utente loggato.");
            return;
        }

        int userId = mainApp.getLoggedUser().getUser_id();

        // 🔴 TEST: inseriamo un movimento finto
        Movimenti m = new Movimenti(
                null,
                "entrata",                    // type
                LocalDate.now(),              // date
                10.0f,                        // amount
                "Test inserimento",           // description
                "Contanti"                    // payment_method
        );

        int categoryId = 1; // ⚠️ metti qui un category_id che ESISTE nel DB

        try {
            MovimentiDAOMySQLImpl.insert(m, userId, categoryId);
            loadMovementsForCurrentUser();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore durante l'inserimento del movimento.");
        }
    }

    @FXML
    private void handleEditTransaction() {
        System.out.println("Modifica transazione cliccata");
    }

    @FXML
    private void handleDeleteTransaction() {
        System.out.println("Elimina transazione cliccata");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
