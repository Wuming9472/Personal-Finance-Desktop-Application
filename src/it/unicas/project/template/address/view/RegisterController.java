package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.UserDAO;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleRegister() {
        errorLabel.setVisible(false);
        String user = usernameField.getText();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        // 1. Validazione Campi
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Tutti i campi sono obbligatori.");
            return;
        }

        // 2. Validazione Password
        if (!pass.equals(confirm)) {
            showError("Le password non coincidono.");
            return;
        }

        if (pass.length() < 4) {
            showError("La password deve essere di almeno 4 caratteri.");
            return;
        }

        // 3. Inserimento nel DB
        UserDAO userDAO = new UserDAOMySQLImpl();
        try {
            boolean success = userDAO.register(user, pass);
            if (success) {
                // Successo!
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Registrazione Completata");
                alert.setHeaderText(null);
                alert.setContentText("Account creato con successo! Ora puoi accedere.");
                alert.showAndWait();

                // Torna al Login
                if (mainApp != null) mainApp.showLogin();
            } else {
                showError("Impossibile creare l'account.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Username già esistente")) {
                showError("Username già in uso. Scegline un altro.");
            } else {
                e.printStackTrace();
                showError("Errore database: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBackToLogin() {
        if (mainApp != null) {
            mainApp.showLogin();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}