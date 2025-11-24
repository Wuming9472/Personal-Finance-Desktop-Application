package it.unicas.project.template.address;

import java.io.IOException;

import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.view.BudgetController;
import it.unicas.project.template.address.view.DashboardController;
import it.unicas.project.template.address.view.MovimentiController;
import it.unicas.project.template.address.view.RootLayoutController;
import it.unicas.project.template.address.view.SettingsEditDialogController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private RootLayoutController rootController; // Riferimento al controller della barra laterale/superiore

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("BalanceSuite");

        // Imposta l'icona dell'applicazione (assicurati di avere il file o rimuovi questa riga)
        // this.primaryStage.getIcons().add(new Image("file:resources/images/logo.png"));

        initRootLayout();

        // All'avvio mostra subito la Dashboard
        showDashboard();
    }

    /**
     * Inizializza il layout principale (Barra laterale + Barra superiore).
     */
    public void initRootLayout() {
        try {
            // Carica il root layout dal file fxml
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Mostra la scena contenente il root layout
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Dà al controller accesso alla main app (per la navigazione)
            rootController = loader.getController();
            rootController.setMainApp(this);

            primaryStage.show();
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
            // Assicurati che il nome del file sia corretto (es. DashboardOverview.fxml)
            loader.setLocation(MainApp.class.getResource("view/Dashboard.fxml"));
            AnchorPane view = (AnchorPane) loader.load();

            // Imposta la vista al centro
            rootLayout.setCenter(view);

            // Collega il controller
            DashboardController controller = loader.getController();
            // controller.setMainApp(this); // Scommenta se aggiungi il metodo setMainApp nel controller

            // Aggiorna il titolo nella barra in alto
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
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Budget.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Birthday Statistics");
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
}