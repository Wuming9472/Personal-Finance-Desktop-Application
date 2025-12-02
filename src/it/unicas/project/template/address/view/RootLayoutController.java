package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RootLayoutController {

    private MainApp mainApp;

    @FXML private Label lblPageTitle;
    @FXML private Label lblInitials;
    @FXML private MenuButton btnUser;

    // Sidebar components
    @FXML private VBox sidebar;
    @FXML private Button btnToggleMenu;
    @FXML private Button btnDashboard;
    @FXML private Button btnMovimenti;
    @FXML private Button btnBudget;
    @FXML private Button btnReport;
    @FXML private Button btnAccount;

    // Menu text labels (to hide when collapsed)
    @FXML private Label lblDashboard;
    @FXML private Label lblMovimenti;
    @FXML private Label lblBudget;
    @FXML private Label lblReport;
    @FXML private Label lblAccount;

    private boolean isMenuExpanded = true;
    private static final double EXPANDED_WIDTH = 220.0;
    private static final double COLLAPSED_WIDTH = 70.0;
    private Button currentActiveButton = null;

    @FXML
    private void initialize() {
        // Set initial active button
        if (btnDashboard != null) {
            setActiveButton(btnDashboard);
        }

        // Setup tooltips for collapsed menu
        setupTooltips();
    }

    /**
     * Setup tooltips for menu buttons (shown when collapsed)
     */
    private void setupTooltips() {
        // Create tooltips with custom style
        Tooltip dashboardTip = createStyledTooltip("Dashboard");
        Tooltip movimentiTip = createStyledTooltip("Movimenti");
        Tooltip budgetTip = createStyledTooltip("Budget");
        Tooltip reportTip = createStyledTooltip("Report");
        Tooltip accountTip = createStyledTooltip("Account");

        // Apply tooltips to buttons
        if (btnDashboard != null) Tooltip.install(btnDashboard, dashboardTip);
        if (btnMovimenti != null) Tooltip.install(btnMovimenti, movimentiTip);
        if (btnBudget != null) Tooltip.install(btnBudget, budgetTip);
        if (btnReport != null) Tooltip.install(btnReport, reportTip);
        if (btnAccount != null) Tooltip.install(btnAccount, accountTip);
    }

    /**
     * Create a styled tooltip
     */
    private Tooltip createStyledTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(100));
        tooltip.getStyleClass().add("menu-tooltip");
        return tooltip;
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

    /**
     * Toggle menu expansion/collapse with smooth animation
     */
    @FXML
    private void handleToggleMenu() {
        double targetWidth = isMenuExpanded ? COLLAPSED_WIDTH : EXPANDED_WIDTH;

        // Create smooth animation for width change
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(sidebar.prefWidthProperty(), targetWidth);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(300), keyValue);
        timeline.getKeyFrames().add(keyFrame);

        // Animate text visibility
        if (isMenuExpanded) {
            // Collapsing: hide text immediately for smooth transition
            fadeOutLabels();
            btnToggleMenu.setText("☰");
        } else {
            // Expanding: show text after animation starts
            timeline.setOnFinished(e -> fadeInLabels());
            btnToggleMenu.setText("✕");
        }

        timeline.play();
        isMenuExpanded = !isMenuExpanded;
    }

    /**
     * Fade out menu text labels
     */
    private void fadeOutLabels() {
        Label[] labels = {lblDashboard, lblMovimenti, lblBudget, lblReport, lblAccount};

        Timeline fadeTimeline = new Timeline();
        for (Label label : labels) {
            if (label != null) {
                KeyValue kv = new KeyValue(label.opacityProperty(), 0);
                KeyFrame kf = new KeyFrame(Duration.millis(150), kv);
                fadeTimeline.getKeyFrames().add(kf);
            }
        }
        fadeTimeline.setOnFinished(e -> {
            for (Label label : labels) {
                if (label != null) label.setVisible(false);
            }
        });
        fadeTimeline.play();
    }

    /**
     * Fade in menu text labels
     */
    private void fadeInLabels() {
        Label[] labels = {lblDashboard, lblMovimenti, lblBudget, lblReport, lblAccount};

        // First make them visible but transparent
        for (Label label : labels) {
            if (label != null) {
                label.setVisible(true);
                label.setOpacity(0);
            }
        }

        // Then fade in
        Timeline fadeTimeline = new Timeline();
        for (Label label : labels) {
            if (label != null) {
                KeyValue kv = new KeyValue(label.opacityProperty(), 1);
                KeyFrame kf = new KeyFrame(Duration.millis(200), kv);
                fadeTimeline.getKeyFrames().add(kf);
            }
        }
        fadeTimeline.play();
    }

    /**
     * Set a button as active and deactivate others
     */
    private void setActiveButton(Button button) {
        // Remove active style from previous button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("menu-button-active");
        }

        // Add active style to new button
        if (button != null && !button.getStyleClass().contains("menu-button-active")) {
            button.getStyleClass().add("menu-button-active");
        }

        currentActiveButton = button;
    }

    @FXML
    private void handleShowDashboard() {
        if (mainApp != null) {
            mainApp.showDashboard();
            setActiveButton(btnDashboard);
        }
    }

    @FXML
    private void handleShowMovements() {
        if (mainApp != null) {
            mainApp.showMovimenti();
            setActiveButton(btnMovimenti);
        }
    }

    @FXML
    private void handleShowBudget() {
        if (mainApp != null) {
            mainApp.showBudget();
            setActiveButton(btnBudget);
        }
    }

    @FXML
    private void handleShowReport() {
        if (mainApp != null) {
            mainApp.showReport();
            setActiveButton(btnReport);
        }
    }

    @FXML
    private void handleShowAccount() {
        if (mainApp != null) {
            mainApp.showAccountPage();
            setActiveButton(btnAccount);
        }
    }

    // --- MENU UTENTE ---
    @FXML
    private void handleSettings() {
        //  porta alla pagina Account
        if (mainApp != null) {
            mainApp.showAccountPage();
            setActiveButton(btnAccount);
        }
    }

    @FXML
    private void handleExit() {
        // Logout: resetta l'utente e torna alla schermata di login
        if (mainApp != null) {
            mainApp.setLoggedUser(null);
            mainApp.showLogin();
        }
    }
}
