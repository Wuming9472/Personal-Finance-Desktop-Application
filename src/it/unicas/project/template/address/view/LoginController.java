package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    static private TextField usernameField;
    @FXML
    static private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    // Riferimento alla classe principale per cambiare scena
    private MainApp mainApp;

    /**
     * Inizializza il controller.
     * Viene chiamato automaticamente dopo il caricamento dell'FXML.
     */

    public static String getUsernameField() {
        return usernameField.getText();
    }

    public String getPasswordField() {
        return passwordField.getText();
    }

    @FXML
    private void initialize() {
        // Nascondiamo il messaggio di errore all'avvio
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }

    /**
     * Imposta il riferimento alla MainApp.
     * Necessario per poter chiamare i metodi di navigazione dopo il login.
     * * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Gestisce il click sul bottone "ACCEDI".
     */
    @FXML
    private void handleLogin() {
        // Resetta gli stili di errore (bordi rossi)
        resetStyles();

        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (isValidCredentials(user, pass)) {
            // Login riuscito
            System.out.println("Login effettuato con successo: " + user);

            if (mainApp != null) {
                // 1. Carica il layout principale (Barra laterale + Topbar)
                mainApp.initRootLayout();
                // 2. Carica la dashboard al centro
                mainApp.showDashboard();
            }
        } else {
            // Login fallito
            showError("Username o password non validi.");
        }
    }

    /**
     * Logica di validazione delle credenziali.
     * Per ora è hardcoded, in futuro qui farai la query al DB.
     */
    private boolean isValidCredentials(String user, String pass) {
        // Controllo campi vuoti
        if (user == null || user.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            return false;
        }

        // TODO: Sostituire con controllo reale sul Database (DAO)
        // Per ora accetta tutto purché non vuoto, oppure metti "admin" "admin"
        return true;
    }

    /**
     * Mostra l'errore a video e colora i bordi di rosso.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
        // Colora i bordi di rosso per feedback visivo
        usernameField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-color: transparent;");
        passwordField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-color: transparent;");
    }

    /**
     * Ripristina lo stile normale dei campi.
     */
    private void resetStyles() {
        if (errorLabel != null) errorLabel.setVisible(false);

        // Stile default (grigio chiaro) o trasparente se gestito dal CSS
        usernameField.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: transparent;");
        passwordField.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: transparent;");
    }
}