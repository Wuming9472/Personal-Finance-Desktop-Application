package it.unicas.project.template.address;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

import it.unicas.project.template.address.model.Amici;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.view.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    /**
     * Constructor
     */
    public MainApp() {
    }

    /**
     * The data as an observable list of Colleghis.
     */
    private ObservableList<Amici> colleghiData = FXCollections.observableArrayList();

    /**
     * Returns the data as an observable list of Colleghis.
     * @return
     */
    public ObservableList<Amici> getColleghiData() {
        return colleghiData;
    }

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.primaryStage.setTitle("Amici app");

    // Set the application icon.
    primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));

    initRootLayout();
    showColleghiOverview();

    primaryStage.show();


  }

    /**
     * Initializes the root layout and tries to load the last opened
     * Amici file.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class
                    .getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            primaryStage.setOnCloseRequest(windowEvent ->
            {
                windowEvent.consume();
                handleExit();
            });


            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the application.
     */
    public void handleExit() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Are you sure?");
        alert.setHeaderText("Exit");
        alert.setContentText("Exit from application.");

        ButtonType buttonTypeOne = new ButtonType("Yes");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            System.exit(0);
        }

    }



    /**
     * Shows the Amici overview inside the root layout.
     */
    public void showColleghiOverview() {
        try {
            // Load Amici overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ColleghiOverview.fxml"));

            // Set Amici overview into the center of root layout.
            rootLayout.setCenter(loader.load());

            // Give the controller access to the main app.
            ColleghiOverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean showSettingsEditDialog(DAOMySQLSettings daoMySQLSettings){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/SettingsEditDialog.fxml"));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("DAO settings");
            dialogStage.initModality((Modality.WINDOW_MODAL));
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);


            // Set the colleghi into the controller.
            SettingsEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setSettings(daoMySQLSettings);

            // Set the dialog icon.
            dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();


            return controller.isOkClicked();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Opens a dialog to edit details for the specified colleghi. If the user
     * clicks OK, the changes are saved into the provided colleghi object and true
     * is returned.
     *
     * @param colleghi the colleghi object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showColleghiEditDialog(Amici colleghi, boolean verifyLen) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ColleghiEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Amici");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the colleghi into the controller.
            ColleghiEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage, verifyLen);
            controller.setColleghi(colleghi);

            // Set the dialog icon.
            dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Opens a dialog to show birthday statistics.
     */
    public void showBirthdayStatistics() {
        try {
            // Load the fxml file and create a new stage for the popup.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/BirthdayStatistics.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Birthday Statistics");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);

            dialogStage.setScene(new Scene(loader.load()));

            // Set the Colleghis into the controller.
            BirthdayStatisticsController controller = loader.getController();
            controller.setColleghiData(colleghiData);

            // Set the dialog icon.
            dialogStage.getIcons().add(new Image("file:resources/images/calendar.png"));

            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Amici file preference, i.e. the file that was last opened.
     * The preference is read from the OS specific registry. If no such
     * preference can be found, null is returned.
     *
     * @return
     */
    public File getColleghiFilePath() {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath", null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file the file or null to remove the path
     */
    public void setColleghiFilePath(File file) {
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if (file != null) {
            prefs.put("filePath", file.getPath());

            // Update the stage title.
            primaryStage.setTitle("AddressApp - " + file.getName());
        } else {
            prefs.remove("filePath");

            // Update the stage title.
            primaryStage.setTitle("AddressApp");
        }
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        MainApp.launch(args);
        //System.out.println("Finito");
    }
}


class MyEventHandler implements EventHandler<WindowEvent> {
    @Override
    public void handle(WindowEvent windowEvent) {
        windowEvent.consume();
        //handleExit();
    }
}