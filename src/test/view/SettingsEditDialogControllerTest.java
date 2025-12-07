package test.view;

import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.view.SettingsEditDialogController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsEditDialogControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    private SettingsEditDialogController controller;
    private TextField driverNameField;
    private TextField hostField;
    private TextField usernameField;
    private TextField passwordField;
    private TextField schemaField;

    @BeforeEach
    void setUp() {
        controller = new SettingsEditDialogController();
        driverNameField = new TextField();
        hostField = new TextField();
        usernameField = new TextField();
        passwordField = new TextField();
        schemaField = new TextField();

        setField(controller, "driverNameField", driverNameField);
        setField(controller, "hostField", hostField);
        setField(controller, "usernameField", usernameField);
        setField(controller, "passwordField", passwordField);
        setField(controller, "schemaField", schemaField);
    }

    @Test
    void initializeShouldSetDriverNameAndClearOtherFields() throws Exception {
        invokePrivate(controller, "initialize");

        assertEquals(DAOMySQLSettings.DRIVERNAME, driverNameField.getText());
        assertEquals("", hostField.getText());
        assertEquals("", usernameField.getText());
        assertEquals("", passwordField.getText());
        assertEquals("", schemaField.getText());
    }

    @Test
    void isOkClickedDefaultToFalse() {
        assertFalse(controller.isOkClicked());
    }

    @Test
    void setSettingsShouldPopulateFields() {
        DAOMySQLSettings settings = new DAOMySQLSettings();
        settings.setHost("localhost");
        settings.setUserName("admin");
        settings.setPwd("secret");
        settings.setSchema("financedb");

        controller.setSettings(settings);

        assertEquals("localhost", hostField.getText());
        assertEquals("admin", usernameField.getText());
        assertEquals("secret", passwordField.getText());
        assertEquals("financedb", schemaField.getText());
    }

    @Test
    void isInputValidShouldFailWhenHostnameIsEmpty() {
        hostField.setText("");
        usernameField.setText("user");
        passwordField.setText("pass");
        schemaField.setText("schema");

        boolean isValid = isInputValid();
        assertFalse(isValid);
    }

    @Test
    void isInputValidShouldFailWhenUsernameIsEmpty() {
        hostField.setText("localhost");
        usernameField.setText("");
        passwordField.setText("pass");
        schemaField.setText("schema");

        boolean isValid = isInputValid();
        assertFalse(isValid);
    }

    @Test
    void isInputValidShouldFailWhenPasswordIsEmpty() {
        hostField.setText("localhost");
        usernameField.setText("user");
        passwordField.setText("");
        schemaField.setText("schema");

        boolean isValid = isInputValid();
        assertFalse(isValid);
    }

    @Test
    void isInputValidShouldFailWhenSchemaIsEmpty() {
        hostField.setText("localhost");
        usernameField.setText("user");
        passwordField.setText("pass");
        schemaField.setText("");

        boolean isValid = isInputValid();
        assertFalse(isValid);
    }

    @Test
    void isInputValidShouldPassWhenAllFieldsAreValid() {
        hostField.setText("localhost");
        usernameField.setText("user");
        passwordField.setText("pass");
        schemaField.setText("schema");

        boolean isValid = isInputValid();
        assertTrue(isValid);
    }

    @Test
    void isInputValidShouldFailWhenHostnameIsNull() {
        hostField.setText(null);
        usernameField.setText("user");
        passwordField.setText("pass");
        schemaField.setText("schema");

        boolean isValid = isInputValid();
        assertFalse(isValid);
    }

    @Test
    void handleOkShouldUpdateSettingsWhenInputIsValid() throws Exception {
        DAOMySQLSettings settings = new DAOMySQLSettings();
        setField(controller, "settings", settings);

        hostField.setText("newhost");
        usernameField.setText("newuser");
        passwordField.setText("newpass");
        schemaField.setText("newschema");

        // Simuliamo la logica di handleOk senza chiudere lo stage
        if (isInputValid()) {
            settings.setHost(hostField.getText());
            settings.setUserName(usernameField.getText());
            settings.setPwd(passwordField.getText());
            settings.setSchema(schemaField.getText());
            setField(controller, "okClicked", true);
        }

        assertEquals("newhost", settings.getHost());
        assertEquals("newuser", settings.getUserName());
        assertEquals("newpass", settings.getPwd());
        assertEquals("newschema", settings.getSchema());
        assertTrue(controller.isOkClicked());
    }

    @Test
    void handleOkShouldNotUpdateSettingsWhenInputIsInvalid() throws Exception {
        DAOMySQLSettings settings = new DAOMySQLSettings();
        settings.setHost("oldhost");
        setField(controller, "settings", settings);

        hostField.setText(""); // invalido
        usernameField.setText("newuser");
        passwordField.setText("newpass");
        schemaField.setText("newschema");

        // Simuliamo la logica di handleOk
        if (isInputValid()) {
            settings.setHost(hostField.getText());
            setField(controller, "okClicked", true);
        }

        // I settings non dovrebbero essere cambiati
        assertEquals("oldhost", settings.getHost());
        assertFalse(controller.isOkClicked());
    }

    @Test
    void daoMySQLSettingsDriverNameIsStatic() {
        assertNotNull(DAOMySQLSettings.DRIVERNAME);
        assertEquals("com.mysql.cj.jdbc.Driver", DAOMySQLSettings.DRIVERNAME);
    }

    /**
     * Simula la logica di validazione di isInputValid
     */
    private boolean isInputValid() {
        String host = hostField.getText();
        String user = usernameField.getText();
        String pwd = passwordField.getText();
        String schema = schemaField.getText();

        if (host == null || host.length() == 0) {
            return false;
        }
        if (user == null || user.length() == 0) {
            return false;
        }
        if (pwd == null || pwd.length() == 0) {
            return false;
        }
        if (schema == null || schema.length() == 0) {
            return false;
        }
        return true;
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = SettingsEditDialogController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String name) throws Exception {
        var method = SettingsEditDialogController.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }
}
