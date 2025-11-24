package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a sidebar, a top bar and space where other JavaFX
 * elements can be placed.
 */
public class RootLayoutController {

    // Riferimento alla MainApp
    private MainApp mainApp;

    // Riferimento alla Label del titolo nella TopBar (collegata via fx:id="lblPageTitle")
    @FXML
    private Label lblPageTitle;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Imposta un titolo di default all'avvio
        if (lblPageTitle != null) {
            lblPageTitle.setText("Dashboard");
        }
    }

    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Metodo pubblico per cambiare il titolo della pagina dalla MainApp
     * o internamente quando si cliccano i bottoni.
     * @param title Il nuovo titolo da mostrare in alto
     */
    public void setPageTitle(String title) {
        if (lblPageTitle != null) {
            lblPageTitle.setText(title);
        }
    }

    // ==========================================
    // GESTIONE SIDEBAR (Bottoni a Sinistra)
    // ==========================================

    @FXML
    private void handleShowDashboard() {
        setPageTitle("Dashboard");
        // Qui chiami il metodo della MainApp per mostrare la Dashboard
        // Esempio: mainApp.showPersonOverview(); oppure mainApp.showDashboard();
        if (mainApp != null) mainApp.showBirthdayStatistics(); // Esempio temporaneo
    }

    @FXML
    private void handleShowMovements() {
        setPageTitle("Movimenti");
        // TODO: Creare metodo mainApp.showMovements();
        System.out.println("Navigazione: Movimenti");
    }

    @FXML
    private void handleShowBudget() {
        setPageTitle("Budget");
        // TODO: Creare metodo mainApp.showBudget();
        System.out.println("Navigazione: Budget");
    }

    // ==========================================
    // GESTIONE MENU UTENTE & IMPOSTAZIONI
    // ==========================================

    /**
     * Gestisce il click su "Impostazioni" o "Profilo"
     */
    @FXML
    private void handleSettings() {
        DAOMySQLSettings daoMySQLSettings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        if (mainApp.showSettingsEditDialog(daoMySQLSettings)){
            DAOMySQLSettings.setCurrentDAOMySQLSettings(daoMySQLSettings);
        }
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("BalanceSuite");
        alert.setHeaderText("About");
        alert.setContentText("Author: Mario Molinara\nApp Version: 1.0");
        alert.showAndWait();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }
}