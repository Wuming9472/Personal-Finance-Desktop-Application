package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Tooltip;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RootLayoutController {

    private MainApp mainApp;

    // UI Components
    @FXML private Label lblPageTitle;
    @FXML private Label lblInitials;
    @FXML private MenuButton btnUser;

    // Sidebar components
    @FXML private VBox sidebar;
    @FXML private Button btnToggleSidebar;
    @FXML private Button btnDashboard;
    @FXML private Button btnMovimenti;
    @FXML private Button btnBudget;
    @FXML private Button btnReport;
    @FXML private Button btnAccount;

    // State
    private boolean sidebarExpanded = true;
    private Button currentSelectedButton = null;

    private static final double EXPANDED_WIDTH = 240.0;
    private static final double COLLAPSED_WIDTH = 86.0;
    private static final PseudoClass COLLAPSED = PseudoClass.getPseudoClass("collapsed");

    @FXML
    private void initialize() {
        // Imposta Dashboard come selezionato di default
        if (btnDashboard != null) {
            setSelectedButton(btnDashboard);
        }

        configureButton(btnDashboard, "Dashboard");
        configureButton(btnMovimenti, "Movimenti");
        configureButton(btnBudget, "Budget");
        configureButton(btnReport, "Report");
        configureButton(btnAccount, "Account");
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
        String[] parts = username.trim().split("\\s+");

        if (parts.length == 1) {
            if (parts[0].length() > 0) {
                initials = parts[0].substring(0, 1).toUpperCase();
            }
        } else if (parts.length >= 2) {
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
     * Toggle sidebar - apre/chiude il menu laterale
     */
    @FXML
    private void handleToggleSidebar() {
        if (sidebarExpanded) {
            collapseSidebar();
        } else {
            expandSidebar();
        }
    }

    /**
     * Collassa la sidebar mostrando solo le icone
     */
    private void collapseSidebar() {
        animateSidebarWidth(COLLAPSED_WIDTH);
        sidebar.pseudoClassStateChanged(COLLAPSED, true);

        // Nascondi il testo dei pulsanti
        hideButtonText(btnDashboard);
        hideButtonText(btnMovimenti);
        hideButtonText(btnBudget);
        hideButtonText(btnReport);
        hideButtonText(btnAccount);

        // Centra le icone
        centerButtons();

        sidebarExpanded = false;
    }

    /**
     * Espande la sidebar mostrando icone + testo
     */
    private void expandSidebar() {
        animateSidebarWidth(EXPANDED_WIDTH);
        sidebar.pseudoClassStateChanged(COLLAPSED, false);

        // Mostra il testo dei pulsanti
        showButtonText(btnDashboard, "Dashboard");
        showButtonText(btnMovimenti, "Movimenti");
        showButtonText(btnBudget, "Budget");
        showButtonText(btnReport, "Report");
        showButtonText(btnAccount, "Account");

        // Allinea a sinistra
        leftAlignButtons();

        sidebarExpanded = true;
    }

    private void hideButtonText(Button button) {
        if (button != null) {
            button.setText("");
            button.setGraphicTextGap(0.0);
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

    private void showButtonText(Button button, String text) {
        if (button != null) {
            button.setText(text);
            button.setGraphicTextGap(15.0);
            button.setContentDisplay(ContentDisplay.LEFT);
        }
    }

    private void centerButtons() {
        setButtonAlignment(btnDashboard, Pos.CENTER);
        setButtonAlignment(btnMovimenti, Pos.CENTER);
        setButtonAlignment(btnBudget, Pos.CENTER);
        setButtonAlignment(btnReport, Pos.CENTER);
        setButtonAlignment(btnAccount, Pos.CENTER);
    }

    private void leftAlignButtons() {
        setButtonAlignment(btnDashboard, Pos.CENTER_LEFT);
        setButtonAlignment(btnMovimenti, Pos.CENTER_LEFT);
        setButtonAlignment(btnBudget, Pos.CENTER_LEFT);
        setButtonAlignment(btnReport, Pos.CENTER_LEFT);
        setButtonAlignment(btnAccount, Pos.CENTER_LEFT);
    }

    private void setButtonAlignment(Button button, Pos alignment) {
        if (button != null) {
            button.setAlignment(alignment);
        }
    }

    /**
     * Imposta un pulsante come selezionato e rimuove la selezione dagli altri
     */
    private void setSelectedButton(Button button) {
        // Lista di tutti i pulsanti del menu
        Button[] menuButtons = {btnDashboard, btnMovimenti, btnBudget, btnReport, btnAccount};

        // Resetta tutti i pulsanti
        for (Button btn : menuButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("selected");
            }
        }

        // Imposta il pulsante selezionato
        if (button != null) {
            if (!button.getStyleClass().contains("selected")) {
                button.getStyleClass().add("selected");
            }
        }

        currentSelectedButton = button;
    }

    private void animateSidebarWidth(double targetWidth) {
        if (sidebar == null) {
            return;
        }

        double startWidth = sidebar.getWidth() > 0 ? sidebar.getWidth() : sidebar.getPrefWidth();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sidebar.prefWidthProperty(), startWidth)),
                new KeyFrame(Duration.millis(240), new KeyValue(sidebar.prefWidthProperty(), targetWidth))
        );

        timeline.setOnFinished(event -> {
            sidebar.setPrefWidth(targetWidth);
            sidebar.setMinWidth(targetWidth);
            sidebar.setMaxWidth(targetWidth);
        });

        timeline.play();
    }

    private void configureButton(Button button, String label) {
        if (button == null) {
            return;
        }

        button.setText(label);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(14.0);
        button.setTooltip(new Tooltip(label));
    }

    // --- NAVIGATION HANDLERS ---

    @FXML
    private void handleShowDashboard() {
        setSelectedButton(btnDashboard);
        if (mainApp != null) mainApp.showDashboard();
    }

    @FXML
    private void handleShowMovements() {
        setSelectedButton(btnMovimenti);
        if (mainApp != null) mainApp.showMovimenti();
    }

    @FXML
    private void handleShowBudget() {
        setSelectedButton(btnBudget);
        if (mainApp != null) mainApp.showBudget();
    }

    @FXML
    private void handleShowReport() {
        setSelectedButton(btnReport);
        if (mainApp != null) {
            mainApp.showReport();
        }
    }

    @FXML
    private void handleShowAccount() {
        setSelectedButton(btnAccount);
        if (mainApp != null) {
            mainApp.showAccountPage();
        }
    }

    // --- MENU UTENTE ---

    @FXML
    private void handleSettings() {
        setSelectedButton(btnAccount);
        if (mainApp != null) {
            mainApp.showAccountPage();
        }
    }

    @FXML
    private void handleExit() {
        if (mainApp != null) {
            mainApp.setLoggedUser(null);
            mainApp.showLogin();
        }
    }
}
