package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MovimentiController {

    @FXML
    private TableView<?> transactionTable; // Sostituisci <?> con <TransactionModel> quando lo avrai
    @FXML
    private TableColumn<?, ?> dateColumn;
    @FXML
    private TableColumn<?, ?> descriptionColumn;
    @FXML
    private TableColumn<?, ?> categoryColumn;
    @FXML
    private TableColumn<?, ?> amountColumn;

    private MainApp mainApp;

    @FXML
    private void initialize() {
        // Inizializza la tabella qui (es. setCellValueFactory)
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleNewTransaction() {
        System.out.println("Nuova transazione cliccata");
        // mainApp.showNewTransactionDialog();
    }

    @FXML
    private void handleEditTransaction() {
        System.out.println("Modifica transazione cliccata");
    }

    @FXML
    private void handleDeleteTransaction() {
        System.out.println("Elimina transazione cliccata");
    }
}