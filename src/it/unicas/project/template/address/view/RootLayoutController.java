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

/**
 * Controller principale per il layout root dell'applicazione.
 * <p>
 * Gestisce la struttura generale dell'interfaccia utente, inclusi:
 * <ul>
 *   <li>La sidebar di navigazione con menu collassabile</li>
 *   <li>La barra superiore con informazioni utente</li>
 *   <li>La navigazione tra le diverse sezioni dell'app</li>
 *   <li>Le animazioni di transizione del menu</li>
 * </ul>
 * </p>
 * <p>
 * La sidebar supporta due modalità:
 * <ul>
 *   <li><b>Espansa</b>: mostra icone e testo dei menu (260px)</li>
 *   <li><b>Collassata</b>: mostra solo le icone con tooltip (82px)</li>
 * </ul>
 * </p>
 *
 * @author Personal Finance Team
 * @version 1.0
 * @see MainApp
 */
public class RootLayoutController {

    /** Riferimento all'applicazione principale. */
    private MainApp mainApp;

    /** Label per il titolo della pagina corrente. */
    @FXML
    private Label lblPageTitle;

    /** Label per le iniziali dell'utente nell'avatar. */
    @FXML
    private Label lblInitials;

    /** MenuButton con il nome utente e opzioni dropdown. */
    @FXML
    private MenuButton btnUser;

    /** Container principale della sidebar. */
    @FXML
    private VBox sidebar;

    /** Pulsante per espandere/collassare il menu. */
    @FXML
    private Button btnToggleMenu;

    /** Pulsante di navigazione alla Dashboard. */
    @FXML
    private Button btnDashboard;

    /** Pulsante di navigazione ai Movimenti. */
    @FXML
    private Button btnMovimenti;

    /** Pulsante di navigazione al Budget. */
    @FXML
    private Button btnBudget;

    /** Pulsante di navigazione ai Report. */
    @FXML
    private Button btnReport;

    /** Pulsante di navigazione all'Account. */
    @FXML
    private Button btnAccount;

    /** Container per l'icona del toggle menu. */
    @FXML
    private StackPane toggleIconContainer;

    /** Freccia SVG per l'animazione del toggle. */
    @FXML
    private SVGPath toggleArrow;

    /** Label testo per il menu Dashboard. */
    @FXML
    private Label lblDashboard;

    /** Label testo per il menu Movimenti. */
    @FXML
    private Label lblMovimenti;

    /** Label testo per il menu Budget. */
    @FXML
    private Label lblBudget;

    /** Label testo per il menu Report. */
    @FXML
    private Label lblReport;

    /** Label testo per il menu Account. */
    @FXML
    private Label lblAccount;

    /** Flag che indica se il menu è espanso. */
    private boolean isMenuExpanded = true;

    /** Larghezza della sidebar quando espansa (in pixel). */
    private static final double EXPANDED_WIDTH = 260.0;

    /** Larghezza della sidebar quando collassata (in pixel). */
    private static final double COLLAPSED_WIDTH = 82.0;

    /** Riferimento al pulsante del menu attualmente attivo. */
    private Button currentActiveButton = null;

    /**
     * Inizializza il controller.
     * <p>
     * Questo metodo viene chiamato automaticamente dopo il caricamento
     * del file FXML. Configura i tooltip per i pulsanti del menu e
     * imposta la Dashboard come sezione attiva di default.
     * </p>
     */
    @FXML
    private void initialize() {
        setupTooltips();

        if (btnDashboard != null) {
            setActiveButton(btnDashboard);
        }
    }

    /**
     * Configura i tooltip per i pulsanti del menu.
     * <p>
     * I tooltip vengono mostrati quando il menu è collassato,
     * permettendo all'utente di identificare le voci di menu
     * anche senza il testo visibile.
     * </p>
     */
    private void setupTooltips() {
        Tooltip dashboardTip = createStyledTooltip("Dashboard");
        Tooltip movimentiTip = createStyledTooltip("Movimenti");
        Tooltip budgetTip = createStyledTooltip("Budget");
        Tooltip reportTip = createStyledTooltip("Report");
        Tooltip accountTip = createStyledTooltip("Account");

        if (btnDashboard != null) Tooltip.install(btnDashboard, dashboardTip);
        if (btnMovimenti != null) Tooltip.install(btnMovimenti, movimentiTip);
        if (btnBudget != null) Tooltip.install(btnBudget, budgetTip);
        if (btnReport != null) Tooltip.install(btnReport, reportTip);
        if (btnAccount != null) Tooltip.install(btnAccount, accountTip);
    }

    /**
     * Crea un tooltip con stile personalizzato.
     *
     * @param text il testo da mostrare nel tooltip
     * @return un nuovo {@link Tooltip} configurato
     */
    private Tooltip createStyledTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(100));
        tooltip.getStyleClass().add("menu-tooltip");
        return tooltip;
    }

    /**
     * Imposta il riferimento all'applicazione principale.
     * <p>
     * Aggiorna automaticamente le informazioni dell'utente loggato
     * nella barra superiore.
     * </p>
     *
     * @param mainApp l'istanza di {@link MainApp}
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        if (mainApp != null && mainApp.getLoggedUser() != null) {
            updateUserInfo(mainApp.getLoggedUser().getUsername());
        }
    }

    /**
     * Aggiorna le informazioni utente nella barra superiore.
     * <p>
     * Imposta il nome completo nel menu e calcola le iniziali
     * da mostrare nell'avatar. Le iniziali vengono estratte
     * dalla prima lettera di ogni parola del nome (max 2 lettere).
     * </p>
     *
     * @param username il nome utente da visualizzare
     */
    public void updateUserInfo(String username) {
        if (username == null || username.isEmpty()) {
            btnUser.setText("Ospite");
            lblInitials.setText("?");
            return;
        }

        btnUser.setText(username);

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

    /**
     * Imposta il titolo della pagina corrente.
     *
     * @param title il titolo da visualizzare
     */
    public void setPageTitle(String title) {
        if (lblPageTitle != null) lblPageTitle.setText(title);
    }

    /**
     * Gestisce l'espansione/collasso del menu laterale.
     * <p>
     * Esegue un'animazione fluida che:
     * <ul>
     *   <li>Ridimensiona la larghezza della sidebar</li>
     *   <li>Ruota la freccia del toggle</li>
     *   <li>Dissolve/mostra le etichette di testo</li>
     * </ul>
     * La durata dell'animazione è di 350ms.
     * </p>
     */
    @FXML
    private void handleToggleMenu() {
        double targetWidth = isMenuExpanded ? COLLAPSED_WIDTH : EXPANDED_WIDTH;
        double arrowRotation = isMenuExpanded ? 180.0 : 0.0;

        Timeline widthTimeline = new Timeline();
        KeyValue widthKV = new KeyValue(sidebar.prefWidthProperty(), targetWidth);
        KeyFrame widthKF = new KeyFrame(Duration.millis(350), widthKV);
        widthTimeline.getKeyFrames().add(widthKF);

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(350), toggleArrow);
        rotateTransition.setToAngle(arrowRotation);

        if (isMenuExpanded) {
            fadeOutLabels();
            if (!sidebar.getStyleClass().contains("collapsed")) {
                widthTimeline.setOnFinished(e -> sidebar.getStyleClass().add("collapsed"));
            }
        } else {
            sidebar.getStyleClass().remove("collapsed");
            widthTimeline.setOnFinished(e -> fadeInLabels());
        }

        ParallelTransition parallelTransition = new ParallelTransition(widthTimeline, rotateTransition);
        parallelTransition.play();

        isMenuExpanded = !isMenuExpanded;
    }

    /**
     * Esegue l'animazione di dissolvenza in uscita per le etichette del menu.
     * <p>
     * Le etichette vengono nascoste con un fade out di 200ms e poi
     * rimosse dal layout (setManaged false).
     * </p>
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
     * Esegue l'animazione di dissolvenza in entrata per le etichette del menu.
     * <p>
     * Le etichette vengono mostrate con un fade in di 250ms,
     * con un leggero ritardo per un effetto più fluido.
     * </p>
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
                fade.setDelay(Duration.millis(100));
                fade.play();
            }
        }
    }

    /**
     * Imposta un pulsante come attivo e disattiva gli altri.
     * <p>
     * Gestisce lo stile visivo dei pulsanti del menu, applicando
     * la classe CSS "menu-button-active" al pulsante selezionato
     * e aggiornando il colore del testo.
     * </p>
     *
     * @param button il pulsante da impostare come attivo
     */
    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("menu-button-active");
            resetButtonTextColor(currentActiveButton);
        }

        if (button != null && !button.getStyleClass().contains("menu-button-active")) {
            button.getStyleClass().add("menu-button-active");
            setButtonTextColor(button, "#FFFFFF");
        }

        currentActiveButton = button;
    }

    /**
     * Imposta il colore del testo per l'etichetta di un pulsante menu.
     *
     * @param button il pulsante di cui modificare l'etichetta
     * @param color  il colore in formato CSS (es. "#FFFFFF")
     */
    private void setButtonTextColor(Button button, String color) {
        Label label = getButtonLabel(button);
        if (label != null) {
            label.setStyle("-fx-text-fill: " + color + ";");
        }
    }

    /**
     * Ripristina il colore di default del testo per l'etichetta di un pulsante.
     *
     * @param button il pulsante di cui ripristinare l'etichetta
     */
    private void resetButtonTextColor(Button button) {
        Label label = getButtonLabel(button);
        if (label != null) {
            label.setStyle("");
        }
    }

    /**
     * Restituisce l'etichetta associata a un pulsante del menu.
     *
     * @param button il pulsante di cui ottenere l'etichetta
     * @return la {@link Label} associata, o {@code null} se non trovata
     */
    private Label getButtonLabel(Button button) {
        if (button == btnDashboard) return lblDashboard;
        if (button == btnMovimenti) return lblMovimenti;
        if (button == btnBudget) return lblBudget;
        if (button == btnReport) return lblReport;
        if (button == btnAccount) return lblAccount;
        return null;
    }

    /**
     * Gestisce la navigazione alla sezione Dashboard.
     */
    @FXML
    private void handleShowDashboard() {
        if (mainApp != null) {
            mainApp.showDashboard();
            setActiveButton(btnDashboard);
        }
    }

    /**
     * Gestisce la navigazione alla sezione Movimenti.
     */
    @FXML
    private void handleShowMovements() {
        if (mainApp != null) {
            mainApp.showMovimenti();
            setActiveButton(btnMovimenti);
        }
    }

    /**
     * Gestisce la navigazione alla sezione Budget.
     */
    @FXML
    private void handleShowBudget() {
        if (mainApp != null) {
            mainApp.showBudget();
            setActiveButton(btnBudget);
        }
    }

    /**
     * Gestisce la navigazione alla sezione Report.
     */
    @FXML
    private void handleShowReport() {
        if (mainApp != null) {
            mainApp.showReport();
            setActiveButton(btnReport);
        }
    }

    /**
     * Gestisce la navigazione alla sezione Account.
     */
    @FXML
    private void handleShowAccount() {
        if (mainApp != null) {
            mainApp.showAccountPage();
            setActiveButton(btnAccount);
        }
    }

    /**
     * Gestisce il click su "Impostazioni" nel menu utente.
     * <p>
     * Attualmente reindirizza alla pagina Account.
     * </p>
     */
    @FXML
    private void handleSettings() {
        if (mainApp != null) {
            mainApp.showAccountPage();
            setActiveButton(btnAccount);
        }
    }

    /**
     * Gestisce il logout dell'utente.
     * <p>
     * Resetta l'utente loggato e torna alla schermata di login.
     * </p>
     */
    @FXML
    private void handleExit() {
        if (mainApp != null) {
            mainApp.setLoggedUser(null);
            mainApp.showLogin();
        }
    }
}