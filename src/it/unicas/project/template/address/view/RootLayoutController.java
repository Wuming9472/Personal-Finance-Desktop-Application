package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.ParallelTransition;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
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
    
    // Toggle icon components
    @FXML private StackPane toggleIconContainer;
    @FXML private SVGPath toggleArrow;

    // Menu text labels (to hide when collapsed)
    @FXML private Label lblDashboard;
    @FXML private Label lblMovimenti;
    @FXML private Label lblBudget;
    @FXML private Label lblReport;
    @FXML private Label lblAccount;

    private boolean isMenuExpanded = true;
    private static final double EXPANDED_WIDTH = 260.0;
    private static final double COLLAPSED_WIDTH = 82.0;
    private Button currentActiveButton = null;

    @FXML
    private void initialize() {
        // Setup tooltips for collapsed menu
        setupTooltips();

        // Set initial active button (must be after tooltips setup)
        if (btnDashboard != null) {
            setActiveButton(btnDashboard);
        }
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
        double arrowRotation = isMenuExpanded ? 180.0 : 0.0;

        // Create smooth animation for width change
        Timeline widthTimeline = new Timeline();
        KeyValue widthKV = new KeyValue(sidebar.prefWidthProperty(), targetWidth);
        KeyFrame widthKF = new KeyFrame(Duration.millis(350), widthKV);
        widthTimeline.getKeyFrames().add(widthKF);

        // Create rotation animation for the arrow
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(350), toggleArrow);
        rotateTransition.setToAngle(arrowRotation);

        // Animate text visibility
        if (isMenuExpanded) {
            // Collapsing: hide text with fade out
            fadeOutLabels();
            // Add collapsed class for CSS styling
            if (!sidebar.getStyleClass().contains("collapsed")) {
                widthTimeline.setOnFinished(e -> sidebar.getStyleClass().add("collapsed"));
            }
        } else {
            // Expanding: remove collapsed class and show text
            sidebar.getStyleClass().remove("collapsed");
            widthTimeline.setOnFinished(e -> fadeInLabels());
        }

        // Play both animations
        ParallelTransition parallelTransition = new ParallelTransition(widthTimeline, rotateTransition);
        parallelTransition.play();
        
        isMenuExpanded = !isMenuExpanded;
    }

    /**
     * Fade out menu text labels
     */
    private void fadeOutLabels() {
        Label[] labels = {lblDashboard, lblMovimenti, lblBudget, lblReport, lblAccount};

        for (Label label : labels) {
            if (label != null) {
                FadeTransition fade = new FadeTransition(Duration.millis(200), label);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(e -> {
                    label.setVisible(false);
                    label.setManaged(false);
                });
                fade.play();
            }
        }
    }

    /**
     * Fade in menu text labels
     */
    private void fadeInLabels() {
        Label[] labels = {lblDashboard, lblMovimenti, lblBudget, lblReport, lblAccount};

        for (Label label : labels) {
            if (label != null) {
                label.setVisible(true);
                label.setManaged(true);
                label.setOpacity(0);
                
                FadeTransition fade = new FadeTransition(Duration.millis(250), label);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.setDelay(Duration.millis(100)); // Slight delay for smoother effect
                fade.play();
            }
        }
    }

    /**
     * Set a button as active and deactivate others
     */
    private void setActiveButton(Button button) {
        // Remove active style from previous button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("menu-button-active");
            // Reset text color to default (dark)
            resetButtonTextColor(currentActiveButton);
        }

        // Add active style to new button
        if (button != null && !button.getStyleClass().contains("menu-button-active")) {
            button.getStyleClass().add("menu-button-active");
            // Set text color to white
            setButtonTextColor(button, "#FFFFFF");
        }

        currentActiveButton = button;
    }

    /**
     * Set text color for a menu button's label
     */
    private void setButtonTextColor(Button button, String color) {
        Label label = getButtonLabel(button);
        if (label != null) {
            label.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    /**
     * Reset text color for a menu button's label
     */
    private void resetButtonTextColor(Button button) {
        Label label = getButtonLabel(button);
        if (label != null) {
            label.setStyle(""); // Remove inline style to use CSS default
        }
    }

    /**
     * Get the label associated with a menu button
     */
    private Label getButtonLabel(Button button) {
        if (button == btnDashboard) return lblDashboard;
        if (button == btnMovimenti) return lblMovimenti;
        if (button == btnBudget) return lblBudget;
        if (button == btnReport) return lblReport;
        if (button == btnAccount) return lblAccount;
        return null;
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
