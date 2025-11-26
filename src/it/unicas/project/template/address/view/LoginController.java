package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    // Riferimento alla classe principale per cambiare scena
    private MainApp mainApp;

    /**
     * Inizializza il controller.
     * Viene chiamato automaticamente dopo il caricamento dell'FXML.
     */
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
            // Se non c'è già un messaggio d'errore specifico, mostriamo quello generico
            if (errorLabel == null || !errorLabel.isVisible()) {
                showError("Username o password non validi.");
            }
        }
    }

    /**
     * Logica di validazione delle credenziali contro la tabella Users.
     */
    private boolean isValidCredentials(String user, String pass) {
        // Controllo campi vuoti
        if (user == null || user.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            return false;
        }

        try (Connection connection = DAOMySQLSettings.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM Users WHERE username = ? AND password = ? LIMIT 1")) {

            statement.setString(1, user.trim());
            statement.setString(2, pass.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Errore di connessione al database.");
            return false;
        }
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