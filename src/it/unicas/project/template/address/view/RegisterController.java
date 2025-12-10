package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.UserDAO;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private ComboBox<String> question1Box;
    @FXML private ComboBox<String> question2Box;
    @FXML private ComboBox<String> question3Box;

    @FXML private PasswordField answer1Field;
    @FXML private PasswordField answer2Field;
    @FXML private PasswordField answer3Field;

    @FXML private Label errorLabel;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }

        // Limite username: max 16 caratteri
        usernameField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 16) {
                // Tronca in caso di copia-incolla
                String truncated = newText.substring(0, 16);
                change.setText(truncated.substring(change.getRangeStart(), Math.min(truncated.length(), change.getRangeStart() + change.getText().length())));
                change.setRange(change.getRangeStart(), change.getRangeEnd());
                int newLength = change.getControlText().length() - (change.getRangeEnd() - change.getRangeStart()) + change.getText().length();
                if (newLength > 16) {
                    change.setText(change.getText().substring(0, change.getText().length() - (newLength - 16)));
                }
            }
            return change;
        }));

        // Limite password: max 32 caratteri
        passwordField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 32) {
                int excess = newText.length() - 32;
                if (change.getText().length() > excess) {
                    change.setText(change.getText().substring(0, change.getText().length() - excess));
                } else {
                    return null;
                }
            }
            return change;
        }));

        confirmPasswordField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 32) {
                int excess = newText.length() - 32;
                if (change.getText().length() > excess) {
                    change.setText(change.getText().substring(0, change.getText().length() - excess));
                } else {
                    return null;
                }
            }
            return change;
        }));

        // Limite risposte domande di sicurezza: max 16 caratteri
        answer1Field.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 16) {
                int excess = newText.length() - 16;
                if (change.getText().length() > excess) {
                    change.setText(change.getText().substring(0, change.getText().length() - excess));
                } else {
                    return null;
                }
            }
            return change;
        }));

        answer2Field.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 16) {
                int excess = newText.length() - 16;
                if (change.getText().length() > excess) {
                    change.setText(change.getText().substring(0, change.getText().length() - excess));
                } else {
                    return null;
                }
            }
            return change;
        }));

        answer3Field.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 16) {
                int excess = newText.length() - 16;
                if (change.getText().length() > excess) {
                    change.setText(change.getText().substring(0, change.getText().length() - excess));
                } else {
                    return null;
                }
            }
            return change;
        }));

        // Lista di domande disponibili
        ObservableList<String> questions = FXCollections.observableArrayList(
                "Nome del tuo primo animale?",
                "Città in cui sei nato/a?",
                "Colore preferito?",
                "Nome del tuo migliore amico d'infanzia?",
                "Titolo del tuo film preferito?"
        );

        question1Box.setItems(questions);
        question2Box.setItems(questions);
        question3Box.setItems(questions);
    }

    @FXML
    private void handleRegister() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }

        String user = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String pass = passwordField.getText() != null ? passwordField.getText() : "";
        String confirm = confirmPasswordField.getText() != null ? confirmPasswordField.getText() : "";

        String q1 = question1Box.getValue();
        String q2 = question2Box.getValue();
        String q3 = question3Box.getValue();

        String a1 = answer1Field.getText() != null ? answer1Field.getText().trim() : "";
        String a2 = answer2Field.getText() != null ? answer2Field.getText().trim() : "";
        String a3 = answer3Field.getText() != null ? answer3Field.getText().trim() : "";

        // 1. Validazione campi base
        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            showError("Tutti i campi sono obbligatori.");
            return;
        }

        // 2. Password
        if (!pass.equals(confirm)) {
            showError("Le password non coincidono.");
            return;
        }

        if (pass.length() < 4) {
            showError("La password deve essere di almeno 4 caratteri.");
            return;
        }

        // 3. Domande di sicurezza: tutte selezionate e risposte non vuote
        if (q1 == null || q2 == null || q3 == null) {
            showError("Seleziona tutte e 3 le domande di sicurezza.");
            return;
        }

        if (a1.isEmpty() || a2.isEmpty() || a3.isEmpty()) {
            showError("Compila tutte le risposte alle domande di sicurezza.");
            return;
        }

        // (Opzionale) evitare che scelga la stessa domanda 3 volte
        if (q1.equals(q2) || q1.equals(q3) || q2.equals(q3)) {
            showError("Le 3 domande devono essere diverse tra loro.");
            return;
        }

        // 4. Inserimento nel DB (password in chiaro per ora)
        UserDAO userDAO = new UserDAOMySQLImpl();
        try {
            boolean success = userDAO.register(user, pass);
            if (success) {

                // Prendo l'id dell'utente appena registrato
                int userId = getUserIdByUsername(user);

                // Salvo le 3 domande + risposte scelte dall'utente
                saveSecurityQuestions(userId, q1, a1, q2, a2, q3, a3);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registrazione Completata");
                alert.setHeaderText(null);
                alert.setContentText("Account creato con successo! Ora puoi accedere.");
                alert.showAndWait();

                if (mainApp != null) mainApp.showLogin();

            } else {
                showError("Impossibile creare l'account.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("Username già esistente")) {
                showError("Username già in uso. Scegline un altro.");
            } else {
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
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }

    // =========================
    //   DOMANDE DI SICUREZZA
    // =========================

    private int getUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        throw new SQLException("Utente non trovato dopo la registrazione.");
    }

    private void saveSecurityQuestions(int userId,
                                       String q1, String a1,
                                       String q2, String a2,
                                       String q3, String a3) throws SQLException {

        String sql = "INSERT INTO security_questions (user_id, question, answer) VALUES (?, ?, ?)";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            insertQuestion(ps, userId, q1, a1);
            insertQuestion(ps, userId, q2, a2);
            insertQuestion(ps, userId, q3, a3);
        }
    }

    private void insertQuestion(PreparedStatement ps, int userId, String question, String answer) throws SQLException {
        ps.setInt(1, userId);
        ps.setString(2, question);
        ps.setString(3, answer);
        ps.executeUpdate();
    }
}
