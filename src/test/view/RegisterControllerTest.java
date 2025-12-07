package test.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.view.RegisterController;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    /**
     * MainApp finto per tracciare le chiamate a showLogin.
     */
    static class TestMainApp extends MainApp {
        boolean showLoginCalled = false;

        @Override
        public void showLogin() {
            this.showLoginCalled = true;
        }
    }

    private RegisterController controller;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<String> question1Box;
    private ComboBox<String> question2Box;
    private ComboBox<String> question3Box;
    private PasswordField answer1Field;
    private PasswordField answer2Field;
    private PasswordField answer3Field;
    private Label errorLabel;

    @BeforeEach
    void setUp() {
        controller = new RegisterController();
        usernameField = new TextField();
        passwordField = new PasswordField();
        confirmPasswordField = new PasswordField();
        question1Box = new ComboBox<>();
        question2Box = new ComboBox<>();
        question3Box = new ComboBox<>();
        answer1Field = new PasswordField();
        answer2Field = new PasswordField();
        answer3Field = new PasswordField();
        errorLabel = new Label();

        setField(controller, "usernameField", usernameField);
        setField(controller, "passwordField", passwordField);
        setField(controller, "confirmPasswordField", confirmPasswordField);
        setField(controller, "question1Box", question1Box);
        setField(controller, "question2Box", question2Box);
        setField(controller, "question3Box", question3Box);
        setField(controller, "answer1Field", answer1Field);
        setField(controller, "answer2Field", answer2Field);
        setField(controller, "answer3Field", answer3Field);
        setField(controller, "errorLabel", errorLabel);
    }

    @Test
    void initializeShouldHideErrorLabelAndPopulateQuestions() throws Exception {
        invokePrivate(controller, "initialize");

        assertFalse(errorLabel.isVisible());
        assertEquals("", errorLabel.getText());
        assertEquals(5, question1Box.getItems().size());
        assertEquals(5, question2Box.getItems().size());
        assertEquals(5, question3Box.getItems().size());
    }

    @Test
    void handleRegisterShouldShowErrorWhenFieldsAreEmpty() throws Exception {
        invokePrivate(controller, "initialize");

        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");

        invokePrivate(controller, "handleRegister");

        assertTrue(errorLabel.isVisible());
        assertEquals("Tutti i campi sono obbligatori.", errorLabel.getText());
    }

    @Test
    void handleRegisterShouldShowErrorWhenPasswordsDoNotMatch() throws Exception {
        invokePrivate(controller, "initialize");

        usernameField.setText("testuser");
        passwordField.setText("password1");
        confirmPasswordField.setText("password2");

        invokePrivate(controller, "handleRegister");

        assertTrue(errorLabel.isVisible());
        assertEquals("Le password non coincidono.", errorLabel.getText());
    }

    @Test
    void handleRegisterShouldShowErrorWhenPasswordTooShort() throws Exception {
        invokePrivate(controller, "initialize");

        usernameField.setText("testuser");
        passwordField.setText("abc");
        confirmPasswordField.setText("abc");

        invokePrivate(controller, "handleRegister");

        assertTrue(errorLabel.isVisible());
        assertEquals("La password deve essere di almeno 4 caratteri.", errorLabel.getText());
    }

    @Test
    void handleRegisterShouldShowErrorWhenSecurityQuestionsNotSelected() throws Exception {
        invokePrivate(controller, "initialize");

        usernameField.setText("testuser");
        passwordField.setText("password");
        confirmPasswordField.setText("password");
        // Domande non selezionate

        invokePrivate(controller, "handleRegister");

        assertTrue(errorLabel.isVisible());
        assertEquals("Seleziona tutte e 3 le domande di sicurezza.", errorLabel.getText());
    }

    @Test
    void handleRegisterShouldShowErrorWhenSecurityAnswersEmpty() throws Exception {
        invokePrivate(controller, "initialize");

        usernameField.setText("testuser");
        passwordField.setText("password");
        confirmPasswordField.setText("password");

        question1Box.setValue("Nome del tuo primo animale?");
        question2Box.setValue("Città in cui sei nato/a?");
        question3Box.setValue("Colore preferito?");
        // Risposte vuote

        invokePrivate(controller, "handleRegister");

        assertTrue(errorLabel.isVisible());
        assertEquals("Compila tutte le risposte alle domande di sicurezza.", errorLabel.getText());
    }

    @Test
    void handleRegisterShouldShowErrorWhenQuestionsAreDuplicate() throws Exception {
        invokePrivate(controller, "initialize");

        usernameField.setText("testuser");
        passwordField.setText("password");
        confirmPasswordField.setText("password");

        question1Box.setValue("Nome del tuo primo animale?");
        question2Box.setValue("Nome del tuo primo animale?"); // Stessa domanda
        question3Box.setValue("Colore preferito?");

        answer1Field.setText("Fido");
        answer2Field.setText("Roma");
        answer3Field.setText("Blu");

        invokePrivate(controller, "handleRegister");

        assertTrue(errorLabel.isVisible());
        assertEquals("Le 3 domande devono essere diverse tra loro.", errorLabel.getText());
    }

    @Test
    void handleBackToLoginShouldCallMainAppShowLogin() throws Exception {
        TestMainApp mainApp = new TestMainApp();
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleBackToLogin");

        assertTrue(mainApp.showLoginCalled);
    }

    @Test
    void handleBackToLoginShouldDoNothingWhenMainAppIsNull() throws Exception {
        setField(controller, "mainApp", null);

        // Non dovrebbe lanciare eccezioni
        invokePrivate(controller, "handleBackToLogin");
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = RegisterController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String name) throws Exception {
        var method = RegisterController.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }
}
