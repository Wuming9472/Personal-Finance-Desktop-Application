package it.unicas.project.template.address.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import it.unicas.project.template.address.MainApp;

public class DashboardController {

    // Riferimenti agli oggetti nel FXML (le Card)
    @FXML
    private Label lblSaldo;
    @FXML
    private Label lblEntrate;
    @FXML
    private Label lblUscite;
    @FXML
    private Label lblPrevisione;

    // Riferimento alla MainApp
    private MainApp mainApp;

    /**
     * Initializes the controller class. 
     * Questo metodo viene chiamato automaticamente dopo il caricamento del file fxml.
     */
    @FXML
    private void initialize() {
        // Qui inizializziamo i dati. 
        // Se i label sono nulli (perché magari hai cambiato ID nel fxml), evitiamo il crash.
        if (lblSaldo != null) lblSaldo.setText("€ 2121.31");
        if (lblEntrate != null) lblEntrate.setText("€ 2500.00");
        if (lblUscite != null) lblUscite.setText("€ 378.69");
        if (lblPrevisione != null) lblPrevisione.setText("€ 473.36");

        // NOTA: Ho rimosso tutto il codice che faceva "nomeColumn.setCellValueFactory..."
        // perché quella tabella NON ESISTE PIÙ in questa schermata.
    }

    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}