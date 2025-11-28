package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;

public class AccountController {

    @FXML private Label lblUserId;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Mostro l'ID utente
        int id = mainApp.getLoggedUser().getUser_id();
        lblUserId.setText("Utente #" + id);
    }

    // 🔵 Questo è il metodo che mancava!
    @FXML
    private void handleSaveChanges() {
        try {
            int userId = mainApp.getLoggedUser().getUser_id();
            String oldP = oldPasswordField.getText();
            String newP = newPasswordField.getText();

            if (oldP.isEmpty() || newP.isEmpty()) {
                showError("Compila tutti i campi.");
                return;
            }

            UserDAOMySQLImpl dao = new UserDAOMySQLImpl();

            boolean ok = dao.updatePassword(userId, oldP, newP);

            if (ok) {
                showInfo("Password aggiornata correttamente!");
                oldPasswordField.clear();
                newPasswordField.clear();
            } else {
                showError("La password attuale è errata.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore interno: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAccount() {
        try {
            int userId = mainApp.getLoggedUser().getUser_id();

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
