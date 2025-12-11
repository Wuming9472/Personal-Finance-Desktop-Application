package it.unicas.project.template.address;

import java.io.IOException;

import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.view.*;
import it.unicas.project.template.address.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Classe principale dell'applicazione JavaFX.
 * <p>
 * Gestisce:
 * <ul>
 *     <li>l'inizializzazione dello {@link Stage} principale;</li>
 *     <li>la navigazione tra le varie viste (Login, Register, Dashboard, Movimenti, Budget, Report, Account);</li>
 *     <li>la creazione del layout principale ({@link RootLayoutController});</li>
 *     <li>il riferimento all'utente attualmente autenticato ({@link #loggedUser});</li>
 *     <li>l'apertura di finestre di dialogo (impostazioni database, statistiche, ecc.).</li>
 * </ul>
 * L'applicazione parte mostrando la schermata di Login; dopo un login riuscito viene
 * inizializzato il layout principale e vengono caricate le pagine centrali.
 */
public class MainApp extends Application {

    /**
     * Finestra principale dell'applicazione.
     */
    private Stage primaryStage;

    /**
     * Layout principale della finestra (contiene barra laterale/superiore e area centrale).
     */
    private BorderPane rootLayout;

    /**
     * Controller associato al layout principale (menu laterale e barra superiore).
     */
    private RootLayoutController rootController;

    /**
     * Utente attualmente autenticato nell'applicazione.
     */
    private User loggedUser;

    /**
     * Riferimento al controller della pagina Budget, se già caricata.
     */
    private BudgetController budgetController;

    /**
     * Riferimento al controller della pagina Report, se già caricata.
     */
    private ReportController reportController;

    /**
     * Riferimento al controller della pagina Dashboard, se già caricata.
     */
    private DashboardController dashboardController;

    /**
     * Punto di ingresso dell'applicazione JavaFX.
     * <p>
     * Imposta il titolo della finestra principale e mostra inizialmente
     * solo la schermata di Login. Il layout principale verrà caricato
     * successivamente dal {@link LoginController} dopo un login riuscito.
     *
     * @param primaryStage stage principale fornito dal runtime JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BalanceSuite");

        // All'avvio mostriamo solo il Login, senza caricare ancora il RootLayout.
        showLogin();
    }

    /**
     * Mostra la schermata di registrazione utente.
     * <p>
     * Carica il file FXML {@code view/Register.fxml}, imposta la scena
     * sullo stage principale e collega il {@link RegisterController}
     * alla {@code MainApp} per permettere la navigazione.
     */
    public void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Register.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Scene scene = new Scene(page);
            primaryStage.setScene(scene);

            RegisterController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la schermata di Login.
     * <p>
     * Carica il file FXML {@code view/Login.fxml}, sostituisce la scena
     * corrente con la vista di login e collega il {@link LoginController}
     * alla {@code MainApp} per permettere, dopo l'autenticazione,
     * l'inizializzazione del layout principale tramite {@link #initRootLayout()}.
     */
    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Login.fxml"));
            AnchorPane loginView = (AnchorPane) loader.load();

            // Creiamo la scena direttamente con la vista del Login
            Scene scene = new Scene(loginView);
            primaryStage.setScene(scene);

            // Collega il controller
            LoginController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la pagina Report (grafici) nell'area centrale del layout principale.
     * <p>
     * Carica il file FXML {@code view/Report.fxml}, inserisce la vista
     * nel centro del {@link BorderPane} principale e collega il
     * {@link ReportController} alla {@code MainApp}. Imposta inoltre
     * il titolo della pagina nella barra superiore tramite il
     * {@link RootLayoutController}, se presente.
     */
    public void showReport() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Report.fxml"));

            // Il root di Report.fxml è un AnchorPane
            AnchorPane view = (AnchorPane) loader.load();

            rootLayout.setCenter(view);

            ReportController controller = loader.getController();
            controller.setMainApp(this);

            // Salva il riferimento al controller del report
            this.reportController = controller;

            if (rootController != null) {
                rootController.setPageTitle("Report");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inizializza il layout principale dell'applicazione
     * (barra laterale, barra superiore, area centrale).
     * <p>
     * Questo metodo viene chiamato tipicamente dal {@link LoginController}
     * dopo un login avvenuto con successo. Carica il file FXML
     * {@code view/RootLayout.fxml}, sostituisce la scena di login con quella
     * principale e collega il {@link RootLayoutController} alla {@code MainApp}
     * per gestire la navigazione tra le diverse pagine.
     */
    public void initRootLayout() {
        try {
            // Carica il root layout dal file FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Sostituisce la scena del Login con quella dell'app principale
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Recupera il controller del layout principale
            rootController = loader.getController();
            rootController.setMainApp(this);

            primaryStage.show();

            // Eventualmente si potrebbe caricare subito la Dashboard al centro
            // showDashboard();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la Dashboard (home) al centro del layout principale.
     * <p>
     * Carica il file FXML {@code view/Dashboard.fxml}, imposta la vista
     * al centro del {@link #rootLayout} e collega il {@link DashboardController}
     * alla {@code MainApp}. Aggiorna anche il titolo della pagina tramite
     * il {@link RootLayoutController}, se disponibile.
     */
    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Dashboard.fxml"));
            AnchorPane view = (AnchorPane) loader.load();

            rootLayout.setCenter(view);

            DashboardController controller = loader.getController();
            controller.setMainApp(this);

            // Salva il riferimento al controller della dashboard
            this.dashboardController = controller;

            if (rootController != null) {
                rootController.setPageTitle("Dashboard");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la pagina dei movimenti.
     * <p>
     * Carica il file FXML {@code view/Movimenti.fxml}, inserisce la vista
     * nell'area centrale del layout principale e collega il
     * {@link MovimentiController} alla {@code MainApp}. Aggiorna anche
     * il titolo della pagina tramite il {@link RootLayoutController}.
     */
    public void showMovimenti() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Movimenti.fxml"));
            AnchorPane view = (AnchorPane) loader.load();

            rootLayout.setCenter(view);

            MovimentiController controller = loader.getController();
            controller.setMainApp(this);

            if (rootController != null) {
                rootController.setPageTitle("Movimenti");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la pagina di pianificazione del budget.
     * <p>
     * Carica il file FXML {@code view/Budget.fxml}, inserisce la vista
     * nell'area centrale del layout principale e collega il
     * {@link BudgetController} alla {@code MainApp}. Memorizza inoltre
     * il riferimento al controller per un eventuale utilizzo successivo
     * e aggiorna il titolo della pagina.
     */
    public void showBudget() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Budget.fxml"));
            AnchorPane view = (AnchorPane) loader.load();

            rootLayout.setCenter(view);

            BudgetController controller = loader.getController();
            controller.setMainApp(this);

            // Salva il riferimento al controller del budget
            this.budgetController = controller;

            if (rootController != null) {
                rootController.setPageTitle("Pianificazione Budget");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la pagina delle impostazioni account utente.
     * <p>
     * Carica il file FXML {@code view/Account.fxml}, posiziona la vista
     * al centro del layout principale e collega l'{@link AccountController}
     * alla {@code MainApp}. Aggiorna anche il titolo della pagina.
     */
    public void showAccountPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Account.fxml"));
            AnchorPane view = loader.load();

            rootLayout.setCenter(view);

            AccountController controller = loader.getController();
            controller.setMainApp(this);

            if (rootController != null) {
                rootController.setPageTitle("Account");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apre una finestra di dialogo con le statistiche sui compleanni.
     * <p>
     * Carica il file FXML {@code view/BirthdayStatistics.fxml} e lo mostra
     * in uno {@link Stage} modale rispetto alla finestra principale.
     * Questa funzionalità è indicata come legacy o extra.
     */
    public void showBirthdayStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BirthdayStatistics.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Statistiche");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apre la finestra di dialogo per la modifica delle impostazioni
     * di connessione al database.
     * <p>
     * Carica il file FXML {@code view/SettingsEditDialog.fxml}, inizializza
     * lo {@link SettingsEditDialogController} con l'istanza di
     * {@link DAOMySQLSettings} corrente e mostra lo {@link Stage} in modalità
     * modale. Al termine restituisce il risultato dell'azione utente
     * (pulsante OK premuto oppure no).
     *
     * @param daoMySQLSettings oggetto che rappresenta le impostazioni correnti
     *                         di connessione al database
     * @return {@code true} se l'utente ha confermato le modifiche (OK),
     *         {@code false} in caso di annullamento o errore
     */
    public boolean showSettingsEditDialog(DAOMySQLSettings daoMySQLSettings) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/SettingsEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Impostazioni Database");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            SettingsEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Passa le impostazioni correnti al controller
            controller.setSettings(daoMySQLSettings);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restituisce lo {@link Stage} principale dell'applicazione.
     *
     * @return stage principale
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Metodo main standard per avviare l'applicazione JavaFX.
     *
     * @param args argomenti da riga di comando
     */
    public static void main(String[] args) {
        launch(args);
    }

    // GETTER E SETTER UTENTE LOGGATO

    /**
     * Imposta l'utente attualmente autenticato nell'applicazione.
     *
     * @param user utente loggato da memorizzare
     */
    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    /**
     * Restituisce l'utente attualmente autenticato.
     *
     * @return utente loggato, oppure {@code null} se nessun utente è autenticato
     */
    public User getLoggedUser() {
        return loggedUser;
    }

    /**
     * Restituisce il controller associato alla pagina Budget, se già caricato.
     *
     * @return istanza di {@link BudgetController}, oppure {@code null}
     *         se la pagina non è ancora stata mostrata
     */
    public BudgetController getBudgetController() {
        return budgetController;
    }

    /**
     * Restituisce il controller associato alla pagina Report, se già caricato.
     *
     * @return istanza di {@link ReportController}, oppure {@code null}
     *         se la pagina non è ancora stata mostrata
     */
    public ReportController getReportController() {
        return reportController;
    }

    /**
     * Restituisce il controller associato alla Dashboard, se già caricato.
     *
     * @return istanza di {@link DashboardController}, oppure {@code null}
     *         se la dashboard non è ancora stata mostrata
     */
    public DashboardController getDashboardController() {
        return dashboardController;
    }

}
