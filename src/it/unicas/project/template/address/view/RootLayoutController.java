package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

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

    @FXML
    private void initialize() {
        // Imposta Dashboard come selezionato di default
        if (btnDashboard != null) {
            setSelectedButton(btnDashboard);
        }
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
        sidebar.setPrefWidth(70.0);

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
        sidebar.setPrefWidth(220.0);

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
        }
    }

    private void showButtonText(Button button, String text) {
        if (button != null) {
            button.setText(text);
            button.setGraphicTextGap(15.0);
        }
    }

    private void centerButtons() {
        setButtonAlignment(btnDashboard, javafx.geometry.Pos.CENTER);
        setButtonAlignment(btnMovimenti, javafx.geometry.Pos.CENTER);
        setButtonAlignment(btnBudget, javafx.geometry.Pos.CENTER);
        setButtonAlignment(btnReport, javafx.geometry.Pos.CENTER);
        setButtonAlignment(btnAccount, javafx.geometry.Pos.CENTER);
    }

    private void leftAlignButtons() {
        setButtonAlignment(btnDashboard, javafx.geometry.Pos.CENTER_LEFT);
        setButtonAlignment(btnMovimenti, javafx.geometry.Pos.CENTER_LEFT);
        setButtonAlignment(btnBudget, javafx.geometry.Pos.CENTER_LEFT);
        setButtonAlignment(btnReport, javafx.geometry.Pos.CENTER_LEFT);
        setButtonAlignment(btnAccount, javafx.geometry.Pos.CENTER_LEFT);
    }

    private void setButtonAlignment(Button button, javafx.geometry.Pos alignment) {
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
                setIconColor(btn, "#64748b"); // Colore grigio
            }
        }

        // Imposta il pulsante selezionato
        if (button != null) {
            if (!button.getStyleClass().contains("selected")) {
                button.getStyleClass().add("selected");
            }
            setIconColor(button, "#3b82f6"); // Colore blu
        }

        currentSelectedButton = button;
    }

    /**
     * Cambia il colore di tutte le shape nell'icona del pulsante
     */
    private void setIconColor(Button button, String color) {
        if (button == null || button.getGraphic() == null) return;

        Color fillColor = Color.web(color);
        setColorRecursive(button.getGraphic(), fillColor);
    }

    /**
     * Applica il colore ricorsivamente a tutte le shape
     */
    private void setColorRecursive(Node node, Color color) {
        if (node instanceof Rectangle) {
            Rectangle rect = (Rectangle) node;
            // Cambia fill solo se non è trasparente
            if (rect.getFill() != null && !rect.getFill().equals(Color.TRANSPARENT)) {
                rect.setFill(color);
            }
            // Cambia stroke solo se presente
            if (rect.getStroke() != null && !rect.getStroke().equals(Color.TRANSPARENT)) {
                rect.setStroke(color);
            }
        } else if (node instanceof Circle) {
            Circle circle = (Circle) node;
            if (circle.getFill() != null && !circle.getFill().equals(Color.TRANSPARENT)) {
                circle.setFill(color);
            }
            if (circle.getStroke() != null && !circle.getStroke().equals(Color.TRANSPARENT)) {
                circle.setStroke(color);
            }
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                setColorRecursive(child, color);
            }
        }
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