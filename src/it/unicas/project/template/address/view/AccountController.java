package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class AccountController {

    @FXML private Label lblInitials;
    @FXML private Label lblUsername;

    @FXML private PasswordField txtOldPwd;
    @FXML private PasswordField txtNewPwd;
    @FXML private PasswordField txtRepeatPwd;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Popola solo username e initials
        var user = mainApp.getLoggedUser();
        if (user != null) {
            String username = user.getUsername();

            lblUsername.setText(username != null ? username : "Username");

            if (username != null && !username.isEmpty()) {
                lblInitials.setText(username.substring(0, 1).toUpperCase());
            } else {
                lblInitials.setText("?");
            }
        }
    }

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
