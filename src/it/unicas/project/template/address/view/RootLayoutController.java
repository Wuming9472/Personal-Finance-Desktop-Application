package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;

public class RootLayoutController {

    private MainApp mainApp;

    @FXML private Label lblPageTitle;
    @FXML private Label lblInitials;
    @FXML private MenuButton btnUser;

    @FXML
    private void initialize() {
        // Init
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Appena colleghiamo la MainApp, recuperiamo l'utente loggato e aggiorniamo la grafica
        if (mainApp != null && mainApp.getLoggedUser() != null) {
            updateUserInfo(mainApp.getLoggedUser().getUsername());
        }
    }

    /**
     * Aggiorna nome e iniziali nella barra in alto.
     */
    public void updateUserInfo(String username) {
        if (username == null || username.isEmpty()) {
            btnUser.setText("Ospite");
            lblInitials.setText("?");
            return;
        }

        // 1. Imposta il nome completo nel menu
        btnUser.setText(username);

        // 2. Calcola le iniziali
        String initials = "";
        String[] parts = username.trim().split("\\s+"); // Divide per spazi

        if (parts.length == 1) {
            // Caso: Solo una parola (es. "mario" -> "M")
            if (parts[0].length() > 0) {
                initials = parts[0].substring(0, 1).toUpperCase();
            }
        } else if (parts.length >= 2) {
            // Caso: Due o più parole (es. "Mario Rossi" -> "MR")
            String first = parts[0].substring(0, 1);
            String second = parts[1].substring(0, 1);
            initials = (first + second).toUpperCase();
        }

        lblInitials.setText(initials);
    }

    public void setPageTitle(String title) {
        if (lblPageTitle != null) lblPageTitle.setText(title);
    }


    @FXML private void handleShowDashboard() { if (mainApp != null) mainApp.showDashboard(); }
    @FXML private void handleShowMovements() { if (mainApp != null) mainApp.showMovimenti(); }
    @FXML private void handleShowBudget() { if (mainApp != null) mainApp.showBudget(); }

    // --- MENU UTENTE ---
    @FXML
    private void handleSettings() {
        //  porta alla pagina Account
        if (mainApp != null) {
            mainApp.showAccountPage();
        }
    }


    @FXML
    private void handleShowAccount() {
        if (mainApp != null) {
            mainApp.showAccountPage();
        }
    }




    @FXML
    private void handleExit() {
        System.exit(0);
    }
}