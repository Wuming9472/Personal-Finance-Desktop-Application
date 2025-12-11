package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Controller per il dialog di modifica delle impostazioni di connessione al database.
 * <p>
 * Permette all'utente di configurare i parametri di connessione MySQL:
 * <ul>
 *   <li>Host del server</li>
 *   <li>Username</li>
 *   <li>Password</li>
 *   <li>Nome dello schema/database</li>
 * </ul>
 
 * <p>
 * Il dialog valida l'input dell'utente prima di salvare le modifiche
 * e mostra messaggi di errore appropriati in caso di campi non validi.
 
 *
 * @author Mario Molinara
 * @version 1.0
 * @see DAOMySQLSettings
 */
public class SettingsEditDialogController {

    /** Campo di testo per il nome del driver JDBC (sola lettura). */
    @FXML
    private TextField driverNameField;

    /** Campo di testo per l'hostname del server MySQL. */
    @FXML
    private TextField hostField;

    /** Campo di testo per il nome utente del database. */
    @FXML
    private TextField usernameField;

    /** Campo di testo per la password del database. */
    @FXML
    private TextField passwordField;

    /** Campo di testo per il nome dello schema/database. */
    @FXML
    private TextField schemaField;

    /** Riferimento allo Stage del dialog. */
    private Stage dialogStage;

    /** Oggetto settings da modificare. */
    private DAOMySQLSettings settings;

    /** Flag che indica se l'utente ha confermato le modifiche. */
    private boolean okClicked = false;

    /**
     * Inizializza il controller.
     * <p>
     * Questo metodo viene chiamato automaticamente dopo il caricamento
     * del file FXML. Imposta il campo del driver name con il valore
     * di default e svuota gli altri campi.
     
     */
    @FXML
    private void initialize() {
        driverNameField.setText(DAOMySQLSettings.DRIVERNAME);
        hostField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        schemaField.setText("");
    }

    /**
     * Imposta lo Stage del dialog.
     * <p>
     * Configura anche l'icona del dialog utilizzando l'immagine
     * presente nella cartella resources.
     
     *
     * @param dialogStage lo Stage da associare a questo dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;

        // Set the dialog icon.
        this.dialogStage.getIcons().add(new Image("file:resources/images/edit.png"));
    }

    /**
     * Imposta l'oggetto settings da modificare nel dialog.
     * <p>
     * Popola i campi di testo con i valori correnti delle impostazioni.
     
     *
     * @param settings l'oggetto {@link DAOMySQLSettings} da modificare
     */
    public void setSettings(DAOMySQLSettings settings) {
        this.settings = settings;
        hostField.setText(settings.getHost());
        usernameField.setText(settings.getUserName());
        passwordField.setText(settings.getPwd());
        schemaField.setText(settings.getSchema());
    }

    /**
     * Verifica se l'utente ha confermato le modifiche.
     *
     * @return {@code true} se l'utente ha cliccato OK, {@code false} altrimenti
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Gestisce il click sul pulsante OK.
     * <p>
     * Valida l'input dell'utente e, se valido, aggiorna l'oggetto
     * settings con i nuovi valori e chiude il dialog.
     
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            settings.setHost(hostField.getText());
            settings.setUserName(usernameField.getText());
            settings.setPwd(passwordField.getText());
            settings.setSchema(schemaField.getText());

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Gestisce il click sul pulsante Cancel.
     * <p>
     * Chiude il dialog senza salvare le modifiche.
     
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Valida l'input dell'utente nei campi di testo.
     * <p>
     * Verifica che tutti i campi obbligatori siano compilati:
     * <ul>
     *   <li>Hostname</li>
     *   <li>Username</li>
     *   <li>Password</li>
     *   <li>Schema</li>
     * </ul>
     * In caso di errori, mostra un alert con l'elenco dei problemi.
     
     *
     * @return {@code true} se l'input è valido, {@code false} altrimenti
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (hostField.getText() == null || hostField.getText().length() == 0) {
            errorMessage += "No valid hostname!\n";
        }
        if (usernameField.getText() == null || usernameField.getText().length() == 0) {
            errorMessage += "No valid username!\n";
        }

        if (passwordField.getText() == null || passwordField.getText().length() == 0) {
            errorMessage += "No valid password!\n";
        }
        if (schemaField.getText() == null || schemaField.getText().length() == 0) {
            errorMessage += "No valid schema!\n";
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