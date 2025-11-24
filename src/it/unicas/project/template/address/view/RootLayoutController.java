package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;

/**
 * Il controller per il layout principale (Barra Laterale + Barra Superiore).
 * Gestisce la navigazione tra le varie schermate.
 */
public class RootLayoutController {

    // Riferimento alla MainApp per chiamare i metodi di cambio schermata
    private MainApp mainApp;

    // Riferimento alla Label del titolo nella TopBar (collegata via fx:id="lblPageTitle" nel FXML)
    @FXML
    private Label lblPageTitle;

    /**
     * Inizializza il controller. Viene chiamato automaticamente dopo il caricamento del fxml.
     */
    @FXML
    private void initialize() {
        // Imposta un titolo di default se necessario, ma viene gestito meglio dai metodi sotto
    }

    /**
     * Chiamato dalla MainApp per darsi un riferimento a se stessa.
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Aggiorna il titolo visualizzato nella barra in alto.
     * @param title Il nuovo titolo da mostrare
     */
    public void setPageTitle(String title) {
        if (lblPageTitle != null) {
            lblPageTitle.setText(title);
        }
    }

    // ==========================================
    // GESTIONE SIDEBAR (NAVIGAZIONE)
    // ==========================================

    @FXML
    private void handleShowDashboard() {
        if (mainApp != null) {
            // Mostra la Dashboard (Home)
            mainApp.showDashboard();
            // Il titolo viene aggiornato dentro showDashboard() o possiamo forzarlo qui
            setPageTitle("Dashboard");
        }
    }

    @FXML
    private void handleShowMovements() {
        if (mainApp != null) {
            // Mostra la schermata Movimenti
            mainApp.showMovimenti();
            setPageTitle("Movimenti");
        }
    }

    @FXML
    private void handleShowBudget() {
        if (mainApp != null) {
            // Mostra la schermata Budget
            mainApp.showBudget();
            setPageTitle("Pianificazione Budget");
        }
    }

    // ==========================================
    // GESTIONE MENU UTENTE & SISTEMA
    // ==========================================

    /**
     * Gestisce il click su "Impostazioni" (es. Database).
     */
    @FXML
    private void handleSettings() {
        DAOMySQLSettings daoMySQLSettings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        if (mainApp != null && mainApp.showSettingsEditDialog(daoMySQLSettings)){
            DAOMySQLSettings.setCurrentDAOMySQLSettings(daoMySQLSettings);
        }
    }

    /**
     * Apre il dialog delle informazioni (About).
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("BalanceSuite");
        alert.setHeaderText("Informazioni su BalanceSuite");
        alert.setContentText("Applicazione di gestione finanziaria personale.\nVersione: 1.0\nAutore: Mario Molinara");
        alert.showAndWait();
    }

    /**
     * Chiude l'applicazione.
     */
    @FXML
    private void handleExit() {
        System.exit(0);
    }
}