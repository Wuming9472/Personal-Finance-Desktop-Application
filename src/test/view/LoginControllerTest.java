package test.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.view.LoginController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    @Test
    void initializeShouldHideErrorsAndResetBox() throws Exception {
        LoginController controller = new LoginController();
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        Label errorLabel = new Label();
        VBox resetBox = new VBox();

        setField(controller, "usernameField", username);
        setField(controller, "passwordField", password);
        setField(controller, "errorLabel", errorLabel);
        setField(controller, "resetBox", resetBox);

        invokePrivate(controller, "initialize");

        assertFalse(errorLabel.isVisible());
        assertFalse(resetBox.isVisible());
        assertFalse(resetBox.isManaged());
    }

    @Test
    void handleLoginShouldNavigateWhenCredentialsAreValid() throws Exception {
        LoginController controller = new LoginController();
        MainApp mainApp = mock(MainApp.class);
        setField(controller, "mainApp", mainApp);

        TextField username = new TextField("john");
        PasswordField password = new PasswordField();
        password.setText("secret");
        Label errorLabel = new Label();
        setField(controller, "usernameField", username);
        setField(controller, "passwordField", password);
        setField(controller, "errorLabel", errorLabel);

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        controller.setConnectionSupplier(() -> connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("user_id")).thenReturn(99);
        when(resultSet.getString("username")).thenReturn("john");
        when(resultSet.getString("password")).thenReturn("secret");

        invokePrivate(controller, "handleLogin");

        verify(mainApp).setLoggedUser(any(User.class));
        verify(mainApp).initRootLayout();
        verify(mainApp).showDashboard();
        assertTrue(errorLabel.getText().isEmpty());
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = LoginController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String name) throws Exception {
        var method = LoginController.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }
}
