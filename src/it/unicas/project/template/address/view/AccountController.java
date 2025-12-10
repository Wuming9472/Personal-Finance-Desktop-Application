package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountController {

    @FXML private Label lblInitials;
    @FXML private Label lblUsername;

    @FXML private PasswordField txtOldPwd;
    @FXML private PasswordField txtNewPwd;
    @FXML private PasswordField txtRepeatPwd;

    // Domande di sicurezza
    @FXML private ComboBox<String> question1Box;
    @FXML private ComboBox<String> question2Box;
    @FXML private ComboBox<String> question3Box;

    @FXML private PasswordField answer1Field;
    @FXML private PasswordField answer2Field;
    @FXML private PasswordField answer3Field;

    private MainApp mainApp;

    @FXML
    private void initialize() {
        // Popola la lista di domande nei ComboBox
        ObservableList<String> questions = FXCollections.observableArrayList(
                "Nome del tuo primo animale?",
                "Città in cui sei nato/a?",
                "Colore preferito?",
                "Nome del tuo migliore amico d'infanzia?",
                "Titolo del tuo film preferito?"
        );

        if (question1Box != null) {
            question1Box.setItems(questions);
            question2Box.setItems(questions);
            question3Box.setItems(questions);
        }

        // Limite password: max 32 caratteri
        if (txtOldPwd != null) {
            txtOldPwd.setTextFormatter(new TextFormatter<String>(change -> {
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
        }

        if (txtNewPwd != null) {
            txtNewPwd.setTextFormatter(new TextFormatter<String>(change -> {
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
        }

        if (txtRepeatPwd != null) {
            txtRepeatPwd.setTextFormatter(new TextFormatter<String>(change -> {
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
        }

        // Limite risposte domande di sicurezza: max 16 caratteri
        if (answer1Field != null) {
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
        }

        if (answer2Field != null) {
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
        }

        if (answer3Field != null) {
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
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        var user = mainApp.getLoggedUser();
        if (user != null) {
            String username = user.getUsername();

            lblUsername.setText(username != null ? username : "Username");

            if (username != null && !username.isEmpty()) {
                lblInitials.setText(username.substring(0, 1).toUpperCase());
            } else {
                lblInitials.setText("?");
            }

            // carica domande già salvate
            try {
                loadSecurityQuestions(user.getUser_id());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Impossibile caricare le domande di sicurezza.");
            }
        }
    }

    // ================== CAMBIO PASSWORD ==================

    @FXML
    private void handleSaveChanges() {
        try {
            int userId = mainApp.getLoggedUser().getUser_id();
            String oldP = txtOldPwd.getText();
            String newP = txtNewPwd.getText();
            String repP = txtRepeatPwd.getText();

            if (oldP.isEmpty() || newP.isEmpty() || repP.isEmpty()) {
                showError("Compila tutti i campi.");
                return;
            }

            if (newP.length() < 4) {
                showError("La nuova password deve contenere almeno 4 caratteri.");
                return;
            }

            if (!newP.equals(repP)) {
                showError("Le nuove password non coincidono.");
                return;
            }

            UserDAOMySQLImpl dao = new UserDAOMySQLImpl();
            boolean ok = dao.updatePassword(userId, oldP, newP);

            if (ok) {
                showInfo("Password aggiornata correttamente!");
                txtOldPwd.clear();
                txtNewPwd.clear();
                txtRepeatPwd.clear();
            } else {
                showError("La password attuale è errata o l'utente non esiste.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore interno: " + e.getMessage());
        }
    }

    // ================== ELIMINA ACCOUNT (con conferma) ==================

    @FXML
    private void handleDeleteAccount() {
        try {
            int userId = mainApp.getLoggedUser().getUser_id();

            // popup di conferma
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Conferma eliminazione");
            confirm.setHeaderText("Eliminare definitivamente l'account?");
            confirm.setContentText("Tutti i tuoi dati (inclusi movimenti e domande di sicurezza) verranno rimossi. "
                    + "L'operazione non è reversibile.");

            ButtonType btnDelete = new ButtonType("Elimina", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnCancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnDelete, btnCancel);

            var result = confirm.showAndWait();
            if (result.isEmpty() || result.get() == btnCancel) {
                return; // utente ha annullato
            }

            // se arriva qui ha cliccato "Elimina"
            UserDAOMySQLImpl dao = new UserDAOMySQLImpl();
            boolean ok = dao.deleteUser(userId);

            if (ok) {
                showInfo("Account eliminato.");
                mainApp.showLogin();
            } else {
                showError("Impossibile eliminare account.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore interno.");
        }
    }

    // ================== DOMANDE DI SICUREZZA ==================

    private void loadSecurityQuestions(int userId) throws SQLException {
        String sql = "SELECT question, answer FROM security_questions WHERE user_id = ? LIMIT 3";

        try (Connection conn = DAOMySQLSettings.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    String q = rs.getString("question");
                    // String a = rs.getString("answer"); // per sicurezza NON precompiliamo le risposte

                    if (count == 0 && question1Box != null) {
                        question1Box.setValue(q);
                    } else if (count == 1 && question2Box != null) {
                        question2Box.setValue(q);
                    } else if (count == 2 && question3Box != null) {
                        question3Box.setValue(q);
                    }
                    count++;
                }
            }
        }
    }

    @FXML
    private void handleSaveSecurityQuestions() {
        try {
            int userId = mainApp.getLoggedUser().getUser_id();

            String q1 = question1Box.getValue();
            String q2 = question2Box.getValue();
            String q3 = question3Box.getValue();

            String a1 = answer1Field.getText() != null ? answer1Field.getText().trim() : "";
            String a2 = answer2Field.getText() != null ? answer2Field.getText().trim() : "";
            String a3 = answer3Field.getText() != null ? answer3Field.getText().trim() : "";

            if (q1 == null || q2 == null || q3 == null) {
                showError("Seleziona tutte e 3 le domande di sicurezza.");
                return;
            }

            if (a1.isEmpty() || a2.isEmpty() || a3.isEmpty()) {
                showError("Inserisci tutte e 3 le risposte.");
                return;
            }

            if (q1.equals(q2) || q1.equals(q3) || q2.equals(q3)) {
                showError("Le 3 domande devono essere diverse tra loro.");
                return;
            }

            String deleteSql = "DELETE FROM security_questions WHERE user_id = ?";
            String insertSql = "INSERT INTO security_questions (user_id, question, answer) VALUES (?, ?, ?)";

            try (Connection conn = DAOMySQLSettings.getConnection()) {

                // cancello le vecchie
                try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                    del.setInt(1, userId);
                    del.executeUpdate();
                }

                // inserisco le nuove
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    insertQuestion(ins, userId, q1, a1);
                    insertQuestion(ins, userId, q2, a2);
                    insertQuestion(ins, userId, q3, a3);
                }
            }

            showInfo("Domande di sicurezza aggiornate correttamente.");
            answer1Field.clear();
            answer2Field.clear();
            answer3Field.clear();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore nel salvataggio delle domande di sicurezza.");
        }
    }

    private void insertQuestion(PreparedStatement ps, int userId, String question, String answer) throws SQLException {
        ps.setInt(1, userId);
        ps.setString(2, question);
        ps.setString(3, answer);
        ps.executeUpdate();
    }

    // ================== UTIL ==================

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}
