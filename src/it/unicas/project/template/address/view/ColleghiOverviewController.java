
package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Amici;
import it.unicas.project.template.address.model.dao.mysql.ColleghiDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.DAOException;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.util.DateUtil;
import javafx.util.Callback;

import java.util.List;

public class ColleghiOverviewController {
    @FXML
    private TableView<Amici> colleghiTableView;
    @FXML
    private TableColumn<Amici, String> nomeColumn;
    @FXML
    private TableColumn<Amici, String> cognomeColumn;

    @FXML
    private Label nomeLabel;
    @FXML
    private Label cognomeLabel;
    @FXML
    private Label telefonoLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label compleannoLabel;

    // Reference to the main application.
    private MainApp mainApp;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public ColleghiOverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the Amici table with the two columns.
        nomeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Amici, String>, ObservableValue<String>>() {
          public ObservableValue<String> call(TableColumn.CellDataFeatures<Amici, String> p) {
            // p.getValue() returns the Person instance for a particular TableView row
            return p.getValue().nomeProperty();
          }
        });

        //nomeColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        cognomeColumn.setCellValueFactory(cellData -> cellData.getValue().cognomeProperty());

        // Clear Amici details.
        showColleghiDetails(null);

        // Listen for selection changes and show the Amici details when changed.
        colleghiTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showColleghiDetails(newValue));
        colleghiTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> System.out.println("Click on the table"));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the table
        colleghiTableView.setItems(mainApp.getColleghiData());
    }

    /**
     * Fills all text fields to show details about the colleghi.
     * If the specified colleghi is null, all text fields are cleared.
     *
     * @param colleghi the colleghi or null
     */
    private void showColleghiDetails(Amici colleghi) {
        if (colleghi != null) {
            // Fill the labels with info from the colleghi object.
            nomeLabel.setText(colleghi.getNome());
            cognomeLabel.setText(colleghi.getCognome());
            telefonoLabel.setText(colleghi.getTelefono());
            emailLabel.setText(colleghi.getEmail());
            compleannoLabel.setText(colleghi.getCompleanno());
        } else {
            // Amici is null, remove all the text.
            nomeLabel.setText("");
            cognomeLabel.setText("");
            telefonoLabel.setText("");
            emailLabel.setText("");
            compleannoLabel.setText("");
        }
    }

    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeleteColleghi() {

      int selectedIndex = colleghiTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {

            Amici colleghi = colleghiTableView.getItems().get(selectedIndex);
            try {
                ColleghiDAOMySQLImpl.getInstance().delete(colleghi);
                mainApp.getColleghiData().remove(selectedIndex);
                //colleghiTableView.getItems().remove(selectedIndex);
            } catch (DAOException e) {
              Alert alert = new Alert(AlertType.ERROR);
              alert.initOwner(mainApp.getPrimaryStage());
              alert.setTitle("Error during DB interaction");
              alert.setHeaderText("Error during insert ...");
              alert.setContentText(e.getMessage());

              alert.showAndWait();
            }
        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Amici Selected");
            alert.setContentText("Please select a Amici in the table.");

            alert.showAndWait();
        }
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new Amici.
     */
    @FXML
    private void handleNewColleghi() {
        Amici tempColleghi = new Amici();
        boolean okClicked = mainApp.showColleghiEditDialog(tempColleghi, true);

        if (okClicked) {
            try {
                ColleghiDAOMySQLImpl.getInstance().insert(tempColleghi);
                mainApp.getColleghiData().add(tempColleghi);
                //colleghiTableView.getItems().add(tempColleghi);
            } catch (DAOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Error during DB interaction");
                alert.setHeaderText("Error during insert ...");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Called when the user clicks the search button. Opens a dialog to edit
     * details for a new Amici.
     */
    @FXML
    private void handleSearchColleghi() {
        Amici tempColleghi = new Amici("","","","","", null);
        boolean okClicked = mainApp.showColleghiEditDialog(tempColleghi,false);
        if (okClicked) {
            //mainApp.getColleghiData().add(tempColleghi);
            try {
                List<Amici> list = ColleghiDAOMySQLImpl.getInstance().select(tempColleghi);
                mainApp.getColleghiData().clear();
                mainApp.getColleghiData().addAll(list);
            } catch (DAOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.initOwner(mainApp.getPrimaryStage());
                alert.setTitle("Error during DB interaction");
                alert.setHeaderText("Error during search ...");
                alert.setContentText(e.getMessage());

                alert.showAndWait();
            }
        }
    }



    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected Amici.
     */
    @FXML
    private void handleEditColleghi() {
        Amici selectedColleghi = colleghiTableView.getSelectionModel().getSelectedItem();
        if (selectedColleghi != null) {
            boolean okClicked = mainApp.showColleghiEditDialog(selectedColleghi,true);
            if (okClicked) {
                try {
                    ColleghiDAOMySQLImpl.getInstance().update(selectedColleghi);
                    showColleghiDetails(selectedColleghi);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            // Nothing selected.
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Amici Selected");
            alert.setContentText("Please select a Amici into the table.");

            alert.showAndWait();
        }
    }
}
