package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // box per reset password
    @FXML private VBox resetBox;
    @FXML private Label question1Label;
    @FXML private Label question2Label;
    @FXML private Label question3Label;
    @FXML private PasswordField answer1Field;
    @FXML private PasswordField answer2Field;
    @FXML private PasswordField answer3Field;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmNewPasswordField;

    private MainApp mainApp;
    private User loggedUser;

    private Supplier<Connection> connectionSupplier = () -> {
        try {
            return DAOMySQLSettings.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };


    // dati per il reset
    private int resetUserId = -1;
    private String expectedA1;
    private String expectedA2;
    private String expectedA3;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setConnectionSupplier(Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    @FXML
    private void initialize() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
        if (resetBox != null) {
            resetBox.setVisible(false);
            resetBox.setManaged(false);
        }
    }

    // ================== LOGIN NORMALE ==================

    @FXML
    private void handleLogin() {
        resetStyles();

        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (isValidCredentials(user, pass)) {
            System.out.println("Login effettuato con successo: " + user);

            if (mainApp != null) {
                mainApp.setLoggedUser(loggedUser);
                mainApp.initRootLayout();
                mainApp.showDashboard();
            }
        } else {
            if (errorLabel == null || !errorLabel.isVisible()) {
                showError("Username o password non validi.");
            }
        }
    }

    @FXML
    private void handleRegister() {
        if (mainApp != null) {
            mainApp.showRegister();
        }
    }

    private boolean isValidCredentials(String user, String pass) {
        if (user == null || user.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT user_id, username, password FROM Users WHERE username = ? AND password = ? LIMIT 1";

        try (Connection connection = connectionSupplier.get();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.trim());
            statement.setString(2, pass.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("user_id");
                    String usernameDb = resultSet.getString("username");
                    String passwordDb = resultSet.getString("password");

                    loggedUser = new User(id, usernameDb, passwordDb);
                    return true;
                } else {
                    loggedUser = null;
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Errore di connessione al database.");
            return false;
        }
    }

    // ================== PASSWORD DIMENTICATA ==================

    @FXML
    private void handleForgotPassword() {
        hideError();

        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        if (username.isEmpty()) {
            showError("Inserisci prima lo username.");
            return;
        }

        try (Connection conn = connectionSupplier.get()) {

            // 1) prendo user_id
            String sqlUser = "SELECT user_id FROM Users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resetUserId = rs.getInt("user_id");
                    } else {
                        showError("Utente non trovato.");
                        return;
                    }
                }
            }

            // 2) prendo le 3 domande di sicurezza
            String sqlQ = "SELECT question, answer FROM security_questions WHERE user_id = ? LIMIT 3";
            try (PreparedStatement ps = conn.prepareStatement(sqlQ)) {
                ps.setInt(1, resetUserId);
                try (ResultSet rs = ps.executeQuery()) {

                    int count = 0;
                    while (rs.next()) {
                        String q = rs.getString("question");
                        String a = rs.getString("answer");

                        if (count == 0) {
                            question1Label.setText(q);
                            expectedA1 = a;
                        } else if (count == 1) {
                            question2Label.setText(q);
                            expectedA2 = a;
                        } else if (count == 2) {
                            question3Label.setText(q);
                            expectedA3 = a;
                        }
                        count++;
                    }

                    if (count < 3) {
                        showError("Domande di sicurezza non trovate o incomplete per questo utente.");
                        return;
                    }
                }
            }

            // pulisco i campi di risposta e nuova password
            answer1Field.clear();
            answer2Field.clear();
            answer3Field.clear();
            newPasswordField.clear();
            confirmNewPasswordField.clear();

            // mostro il box di reset
            resetBox.setVisible(true);
            resetBox.setManaged(true);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Errore database: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        hideError();

        if (resetUserId <= 0) {
            showError("Prima clicca su 'Password dimenticata?' e carica le domande.");
            return;
        }

        String a1 = answer1Field.getText() != null ? answer1Field.getText().trim() : "";
        String a2 = answer2Field.getText() != null ? answer2Field.getText().trim() : "";
        String a3 = answer3Field.getText() != null ? answer3Field.getText().trim() : "";

        // confronto (se vuoi ignorare maiuscole/minuscole usa equalsIgnoreCase)
        if (!a1.equals(expectedA1) || !a2.equals(expectedA2) || !a3.equals(expectedA3)) {
            showError("Una o più risposte non sono corrette.");
            return;
        }

        String newPass = newPasswordField.getText() != null ? newPasswordField.getText() : "";
        String confirm = confirmNewPasswordField.getText() != null ? confirmNewPasswordField.getText() : "";

        if (newPass.isEmpty() || confirm.isEmpty()) {
            showError("Inserisci e conferma la nuova password.");
            return;
        }

        if (!newPass.equals(confirm)) {
            showError("Le nuove password non coincidono.");
            return;
        }

        if (newPass.length() < 4) {
            showError("La nuova password deve essere di almeno 4 caratteri.");
            return;
        }

        String sqlUpdate = "UPDATE Users SET password = ? WHERE user_id = ?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {

            ps.setString(1, newPass);   // password in chiaro, come in registrazione
            ps.setInt(2, resetUserId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Password aggiornata");
                alert.setHeaderText(null);
                alert.setContentText("Password reimpostata con successo. Ora puoi effettuare il login.");
                alert.showAndWait();

                // nascondo il box di reset
                resetBox.setVisible(false);
                resetBox.setManaged(false);
                resetUserId = -1;

            } else {
                showError("Impossibile aggiornare la password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Errore database: " + e.getMessage());
        }
    }

    // ================== UTIL ==================

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
        usernameField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-color: transparent;");
        passwordField.setStyle("-fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-color: transparent;");
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
        usernameField.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: transparent;");
        passwordField.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-color: transparent;");
    }

    private void resetStyles() {
        hideError();
    }
}
