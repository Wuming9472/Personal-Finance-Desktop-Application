package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Amici;
import it.unicas.project.template.address.util.DateUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Dialog to edit details of a colleghi.
 *
 * @author Mario Molinara
 */
public class ColleghiEditDialogController {

    @FXML
    private TextField nomeField;
    @FXML
    private TextField cognomeField;
    @FXML
    private TextField telefonoField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField compleannoField;


    private Stage dialogStage;
    private Amici colleghi;
    private boolean okClicked = false;
    private boolean verifyLen = true;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage, boolean verifyLen) {
        this.dialogStage = dialogStage;
        this.verifyLen = verifyLen;

        // Set the dialog icon.
        //this.dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));
    }

    /**
     * Sets the colleghi to be edited in the dialog.
     *
     * @param colleghi
     */
    public void setColleghi(Amici colleghi) {
        this.colleghi = colleghi;

        nomeField.setText(colleghi.getNome());
        cognomeField.setText(colleghi.getCognome());
        telefonoField.setText(colleghi.getTelefono());
        emailField.setText(colleghi.getEmail());
        compleannoField.setText(colleghi.getCompleanno());
        compleannoField.setPromptText("dd-mm-yyyy");
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid(verifyLen)) {
            colleghi.setNome(nomeField.getText());
            colleghi.setCognome(cognomeField.getText());
            colleghi.setTelefono(telefonoField.getText());
            colleghi.setEmail(emailField.getText());
            if (compleannoField.getText() != null){
                colleghi.setCompleanno(compleannoField.getText());
            }
            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid(boolean verifyLen) {
        String errorMessage = "";

        if (nomeField.getText() == null || (verifyLen && nomeField.getText().length() == 0)) {
            errorMessage += "No valid first name!\n";
        }
        if (cognomeField.getText() == null || (verifyLen && cognomeField.getText().length() == 0)) {
            errorMessage += "No valid last name!\n";
        }
        if (telefonoField.getText() == null || (verifyLen && telefonoField.getText().length() == 0)) {
            errorMessage += "No valid telephone number!\n";
        }

        if (emailField.getText() == null || (verifyLen && emailField.getText().length() == 0)) {
            errorMessage += "No valid email!\n";
        }
        if (compleannoField.getText() == null && verifyLen){
            errorMessage += "No valid birthday!\n";
        }

        if (compleannoField.getText() != null && verifyLen){
            if (compleannoField.getText().length() == 0){
                errorMessage += "No valid birthday!\n";
            }
            if (!DateUtil.validDate(compleannoField.getText())) {
                errorMessage += "No valid birthday. Use the format dd-mm-yyyy!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}
