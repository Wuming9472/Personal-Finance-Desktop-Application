package test.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.view.LoginController;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    /**
     * MainApp finto, senza Mockito.
     */
    static class TestMainApp extends MainApp {
        User loggedUser;
        boolean initRootLayoutCalled = false;
        boolean showDashboardCalled = false;

        @Override
        public void setLoggedUser(User user) {
            this.loggedUser = user;
        }

        @Override
        public void initRootLayout() {
            this.initRootLayoutCalled = true;
        }

        @Override
        public void showDashboard() {
            this.showDashboardCalled = true;
        }
    }

    /**
     * Connection finta per il login:
     * - la query restituisce sempre UN record:
     *   user_id=99, username=john, password=secret
     */
    private Connection createFakeConnectionForLogin() {
        // ResultSet finto
        InvocationHandler rsHandler = new InvocationHandler() {
            boolean first = true;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "next":
                        if (first) {
                            first = false;
                            return true;
                        }
                        return false;
                    case "getInt":
                        if ("user_id".equalsIgnoreCase((String) args[0])) {
                            return 99;
                        }
                        return 0;
                    case "getString":
                        String col = (String) args[0];
                        if ("username".equalsIgnoreCase(col)) return "john";
                        if ("password".equalsIgnoreCase(col)) return "secret";
                        return null;
                    case "close":
                        return null;
                    default:
                        Class<?> rt = method.getReturnType();
                        if (rt.equals(boolean.class)) return false;
                        if (rt.equals(int.class)) return 0;
                        if (rt.equals(double.class)) return 0d;
                        if (rt.equals(float.class)) return 0f;
                        return null;
                }
            }
        };
        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                rsHandler
        );

        // PreparedStatement finto
        InvocationHandler stmtHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "executeQuery":
                        return rs;
                    case "setString":
                    case "close":
                        return null;
                    default:
                        Class<?> rt = method.getReturnType();
                        if (rt.equals(boolean.class)) return false;
                        if (rt.equals(int.class)) return 0;
                        if (rt.equals(double.class)) return 0d;
                        if (rt.equals(float.class)) return 0f;
                        return null;
                }
            }
        };
        PreparedStatement stmt = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                stmtHandler
        );

        // Connection finta
        InvocationHandler connHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "prepareStatement":
                        return stmt;
                    case "close":
                        return null;
                    default:
                        Class<?> rt = method.getReturnType();
                        if (rt.equals(boolean.class)) return false;
                        if (rt.equals(int.class)) return 0;
                        if (rt.equals(double.class)) return 0d;
                        if (rt.equals(float.class)) return 0f;
                        return null;
                }
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                connHandler
        );
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

        TestMainApp mainApp = new TestMainApp();
        setField(controller, "mainApp", mainApp);

        TextField username = new TextField("john");
        PasswordField password = new PasswordField();
        password.setText("secret");
        Label errorLabel = new Label();

        setField(controller, "usernameField", username);
        setField(controller, "passwordField", password);
        setField(controller, "errorLabel", errorLabel);

        // Connection finta per il login
        Connection connection = createFakeConnectionForLogin();
        controller.setConnectionSupplier(() -> connection);

        invokePrivate(controller, "handleLogin");

        // Verifica che il MainApp finto sia stato usato come ci aspettiamo
        assertNotNull(mainApp.loggedUser);
        assertEquals(99, mainApp.loggedUser.getUser_id());
        assertEquals("john", mainApp.loggedUser.getUsername());
        assertTrue(mainApp.initRootLayoutCalled);
        assertTrue(mainApp.showDashboardCalled);
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
