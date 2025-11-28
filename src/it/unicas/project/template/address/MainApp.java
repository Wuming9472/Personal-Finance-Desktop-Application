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


public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    // Riferimento al controller della barra laterale/superiore
    private RootLayoutController rootController;

    private User loggedUser;

    private BudgetController budgetController;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BalanceSuite");

        // STEP 1: All'avvio mostriamo SOLO il Login.
        // Non carichiamo ancora il RootLayout.
        showLogin();

    }


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
     * Mostra la schermata di Login a schermo intero (o dimensione prefissata).
     * Questa sostituisce qualsiasi altra scena precedente.
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
            controller.setMainApp(this); // Passiamo this per permettere al Login di chiamare initRootLayout() dopo

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inizializza il layout principale (Menu laterale + Barra superiore).
     * Questo metodo viene chiamato DAL LOGIN CONTROLLER dopo che l'accesso è riuscito.
     */
    public void initRootLayout() {
        try {
            // Carica il root layout dal file fxml
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Sostituisce la scena del Login con quella dell'App Principale
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Imposta dimensioni minime o massimizza se vuoi
            // primaryStage.setMaximized(true);

            // Dà al controller accesso alla main app (per la navigazione)
            rootController = loader.getController();
            rootController.setMainApp(this);

            primaryStage.show();

            // Una volta caricato lo scheletro (Root), carichiamo subito la Dashboard al centro
            //showDashboard();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la Dashboard (Home) al centro del RootLayout.
     */
    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Dashboard.fxml")); // O DashboardOverview.fxml
            AnchorPane view = (AnchorPane) loader.load();

            rootLayout.setCenter(view);

            // ECCO IL PUNTO FONDAMENTALE:
            DashboardController controller = loader.getController();
            controller.setMainApp(this); // <--- SENZA QUESTO RIMANE A "..."

            if (rootController != null) {
                rootController.setPageTitle("Dashboard");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mostra la pagina dei Movimenti.
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
     * Mostra la pagina del Budget.
     */
    public void showBudget() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Budget.fxml"));
            AnchorPane view = (AnchorPane) loader.load();

            rootLayout.setCenter(view);

            BudgetController controller = loader.getController();
            controller.setMainApp(this);

            // *** Salvo il riferimento al controller del budget
            this.budgetController = controller;

            if (rootController != null) {
                rootController.setPageTitle("Pianificazione Budget");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apre il dialog per le statistiche compleanni (funzionalità legacy/extra).
     */
    public void showBirthdayStatistics() {
        try {
            // Nota: Qui puntavi a Budget.fxml per errore, rimetto BirthdayStatistics se esiste,
            // altrimenti commenta questo blocco.
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
     * Apre il dialog per le impostazioni del database.
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

            // IMPORTANTE: Assicurati che il metodo nel controller si chiami così
            // Se nel controller è "setSettings", cambia questa riga in controller.setSettings(...)
            controller.setSettings(daoMySQLSettings);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ritorna lo stage principale.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    // GETTER E SETTER UTENTE LOGGATO

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public User getLoggedUser() {
        return loggedUser;
    }



}