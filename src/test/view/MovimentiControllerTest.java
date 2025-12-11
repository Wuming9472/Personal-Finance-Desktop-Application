package test.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.view.MovimentiController;
import it.unicas.project.template.address.view.MovimentiController.MovimentiGateway;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MovimentiControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    /**
     * MainApp finto per evitare Mockito su MainApp (estende Application).
     */
    static class TestMainApp extends MainApp {
        private final User user;

        TestMainApp(User user) {
            this.user = user;
        }

        @Override
        public User getLoggedUser() {
            return user;
        }
    }

    /**
     * Stub di MovimentiGateway (così evitiamo Mockito anche qui, per coerenza).
     */
    static class StubMovimentiGateway implements MovimentiGateway {
        private List<Movimenti> data;

        void setData(List<Movimenti> data) {
            this.data = data;
        }

        @Override
        public List<Movimenti> findByUser(int userId) {
            return data;
        }

        @Override
        public void insert(Movimenti m, int userId, int categoryId) throws DAOException, SQLException {

        }

        @Override
        public void delete(int movementId) throws DAOException, SQLException {

        }

        @Override
        public void update(Movimenti m, int categoryId) throws DAOException, SQLException {

        }
    }

    /**
     * Connection finta per initialize():
     * esegue una query che restituisce un ResultSet vuoto (next() == false).
     */
    private Connection createFakeConnectionForInitialize() {
        // ResultSet vuoto
        InvocationHandler rsHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "next":
                        return false;
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

        // Statement che restituisce sempre quel ResultSet
        InvocationHandler stmtHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "executeQuery":
                        return rs;
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
        Statement stmt = (Statement) Proxy.newProxyInstance(
                Statement.class.getClassLoader(),
                new Class[]{Statement.class},
                stmtHandler
        );

        // Connection che restituisce sempre quello Statement
        InvocationHandler connHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                switch (name) {
                    case "createStatement":
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

    private MovimentiController buildControllerWithUi() {
        MovimentiController controller = new MovimentiController();
        setField(controller, "transactionTable", new TableView<>());
        setField(controller, "dateColumn", new TableColumn<>());
        setField(controller, "typeColumn", new TableColumn<>());
        setField(controller, "categoryColumn", new TableColumn<>());
        setField(controller, "titleColumn", new TableColumn<>());
        setField(controller, "paymentMethodColumn", new TableColumn<>());
        setField(controller, "amountColumn", new TableColumn<>());
        setField(controller, "amountField", new TextField());
        setField(controller, "typeField", new ComboBox<>());
        setField(controller, "dateField", new DatePicker());
        setField(controller, "categoryField", new ComboBox<>());
        setField(controller, "methodField", new ComboBox<>());
        setField(controller, "descArea", new TextArea());
        setField(controller, "charCountLabel", new Label());
        return controller;
    }

    @Test
    void initializeShouldPrepareDefaultInputs() throws Exception {
        MovimentiController controller = buildControllerWithUi();

        DAOMySQLSettings settings = new DAOMySQLSettings();
        Connection connection = createFakeConnectionForInitialize();

        controller.setSettingsSupplier(() -> settings);
        controller.setConnectionFactory(url -> connection);

        invokePrivate(controller, "initialize");

        ComboBox<String> typeField = getField(controller, "typeField");
        Label charCountLabel = getField(controller, "charCountLabel");

        assertEquals("Uscita", typeField.getValue());
        assertEquals("/40", charCountLabel.getText());

        TextArea descArea = getField(controller, "descArea");
        descArea.setText("prova123"); //8 caratteri
        assertEquals("8/40", charCountLabel.getText());
    }

    @Test
    void setMainAppShouldLoadMovementsForLoggedUser() throws Exception {
        MovimentiController controller = buildControllerWithUi();
        invokePrivate(controller, "initialize");

        User user = new User(7, "demo", "pwd");
        MainApp mainApp = new TestMainApp(user);

        Movimenti movimento = new Movimenti();
        movimento.setAmount(10f);
        movimento.setType("Uscita");
        movimento.setDate(LocalDate.now());
        movimento.setPayment_method("Carta");
        movimento.setTitle("Spesa");

        StubMovimentiGateway gateway = new StubMovimentiGateway();
        gateway.setData(List.of(movimento));

        controller.setMovimentiGateway(gateway);
        controller.setMainApp(mainApp);

        TableView<Movimenti> table = getField(controller, "transactionTable");
        assertEquals(1, table.getItems().size());
        assertEquals("Spesa", table.getItems().get(0).getTitle());
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = MovimentiController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String name) {
        try {
            var field = MovimentiController.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String name) throws Exception {
        var method = MovimentiController.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }
}
