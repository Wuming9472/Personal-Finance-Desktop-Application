package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MovimentiControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
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

        try (MockedStatic<DAOMySQLSettings> settingsMock = mockStatic(DAOMySQLSettings.class);
             MockedStatic<java.sql.DriverManager> driverMock = mockStatic(java.sql.DriverManager.class)) {

            DAOMySQLSettings settings = new DAOMySQLSettings();
            settingsMock.when(DAOMySQLSettings::getCurrentDAOMySQLSettings).thenReturn(settings);

            Connection connection = mock(Connection.class);
            Statement statement = mock(Statement.class);
            ResultSet resultSet = mock(ResultSet.class);
            driverMock.when(() -> java.sql.DriverManager.getConnection(anyString())).thenReturn(connection);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            invokePrivate(controller, "initialize");
        }

        ComboBox<String> typeField = getField(controller, "typeField");
        Label charCountLabel = getField(controller, "charCountLabel");

        assertEquals("Uscita", typeField.getValue());
        assertEquals("0/100", charCountLabel.getText());
    }

    @Test
    void setMainAppShouldLoadMovementsForLoggedUser() throws Exception {
        MovimentiController controller = buildControllerWithUi();
        invokePrivate(controller, "initialize");

        MainApp mainApp = mock(MainApp.class);
        when(mainApp.getLoggedUser()).thenReturn(new User(7, "demo", "pwd"));

        Movimenti movimento = new Movimenti();
        movimento.setAmount(10f);
        movimento.setType("Uscita");
        movimento.setDate(LocalDate.now());
        movimento.setPayment_method("Carta");
        movimento.setTitle("Spesa");

        try (MockedStatic<MovimentiDAOMySQLImpl> daoMock = mockStatic(MovimentiDAOMySQLImpl.class)) {
            daoMock.when(() -> MovimentiDAOMySQLImpl.findByUser(7)).thenReturn(List.of(movimento));
            controller.setMainApp(mainApp);
        }

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
