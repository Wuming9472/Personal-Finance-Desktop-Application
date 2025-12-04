package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.embed.swing.JFXPanel;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @BeforeAll
    static void initToolkit() {
        // Initializes the JavaFX toolkit for tests that instantiate controls
        new JFXPanel();
    }

    private DashboardController createControllerWithBasicUi() {
        DashboardController controller = new DashboardController();
        setField(controller, "lblSaldo", new Label());
        setField(controller, "lblEntrate", new Label());
        setField(controller, "lblUscite", new Label());
        setField(controller, "lblPrevisione", new Label());
        setField(controller, "lblMeseCorrente", new Label());
        setField(controller, "barChartAndamento", new BarChart<>(new CategoryAxis(), new NumberAxis()));
        setField(controller, "boxUltimiMovimenti", new VBox());
        setField(controller, "gridBudgetList", new GridPane());
        setField(controller, "cardPrevisione", new AnchorPane());
        return controller;
    }

    @Test
    void initializeShouldResetLabelsToPlaceholder() throws Exception {
        DashboardController controller = createControllerWithBasicUi();
        invokePrivate(controller, "initialize");

        assertEquals("...", ((Label) getField(controller, "lblSaldo")).getText());
        assertEquals("...", ((Label) getField(controller, "lblEntrate")).getText());
        assertEquals("...", ((Label) getField(controller, "lblUscite")).getText());
    }

    @Test
    void refreshDashboardDataUsesDaoResults() throws Exception {
        DashboardController controller = createControllerWithBasicUi();

        MainApp mainApp = mock(MainApp.class);
        when(mainApp.getLoggedUser()).thenReturn(new User(1, "user", "pwd"));
        setField(controller, "mainApp", mainApp);

        Movimenti movement = new Movimenti();
        movement.setType("Entrata");
        movement.setAmount(100f);
        movement.setTitle("Stipendio");
        movement.setDate(LocalDate.now());

        Budget budget = new Budget();
        budget.setCategoryName("Casa");
        budget.setBudgetAmount(200f);
        budget.setSpentAmount(50f);

        try (MockedConstruction<MovimentiDAOMySQLImpl> movimentiMock = mockConstruction(MovimentiDAOMySQLImpl.class, (mock, context) -> {
                    when(mock.getSumByMonth(anyInt(), anyInt(), anyInt(), eq("Entrata"))).thenReturn(200f);
                    when(mock.getSumByMonth(anyInt(), anyInt(), anyInt(), eq("Uscita"))).thenReturn(50f);
                    when(mock.selectByUserAndMonthYear(anyInt(), anyInt(), anyInt())).thenReturn(List.of(movement));
                });
             MockedConstruction<BudgetDAOMySQLImpl> budgetMock = mockConstruction(BudgetDAOMySQLImpl.class, (mock, context) ->
                     when(mock.getBudgetsForMonth(anyInt(), anyInt(), anyInt())).thenReturn(List.of(budget)));
             MockedStatic<DAOMySQLSettings> settingsMock = mockStatic(DAOMySQLSettings.class);
             MockedStatic<java.sql.DriverManager> driverManagerMock = mockStatic(java.sql.DriverManager.class)) {

            DAOMySQLSettings settings = new DAOMySQLSettings();
            settingsMock.when(DAOMySQLSettings::getCurrentDAOMySQLSettings).thenReturn(settings);

            Connection connection = mock(Connection.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getDouble("totaleEntrate")).thenReturn(200d);
            when(resultSet.getDouble("totaleUscite")).thenReturn(50d);
            when(resultSet.getInt("giorniConMovimenti")).thenReturn(5);
            when(resultSet.getDate("dataRecente")).thenReturn(java.sql.Date.valueOf(LocalDate.now()));

            when(statement.executeQuery()).thenReturn(resultSet);
            when(connection.prepareStatement(any())).thenReturn(statement);
            driverManagerMock.when(() -> java.sql.DriverManager.getConnection(any(String.class))).thenReturn(connection);

            controller.setMainApp(mainApp);

            assertEquals("€ 200.00", ((Label) getField(controller, "lblEntrate")).getText());
            assertEquals("€ 50.00", ((Label) getField(controller, "lblUscite")).getText());
            assertEquals("€ 150.00", ((Label) getField(controller, "lblSaldo")).getText());
            assertEquals("€ 150.00", ((Label) getField(controller, "lblPrevisione")).getText());
            assertEquals(1, ((VBox) getField(controller, "boxUltimiMovimenti")).getChildren().size());
            assertTrue(((GridPane) getField(controller, "gridBudgetList")).getChildren().size() > 0);
        }
    }

    @Test
    void navigationMethodsAdjustMonthAndYear() throws Exception {
        DashboardController controller = createControllerWithBasicUi();
        DashboardController spyController = Mockito.spy(controller);

        doNothing().when(spyController).refreshDashboardData();

        LocalDate january = LocalDate.of(2024, 1, 1);
        setField(spyController, "selectedMonth", january.getMonthValue());
        setField(spyController, "selectedYear", january.getYear());

        invokePrivate(spyController, "handlePreviousMonth");
        assertEquals(12, getField(spyController, "selectedMonth"));
        assertEquals(2023, getField(spyController, "selectedYear"));

        invokePrivate(spyController, "handleNextMonth");
        assertEquals(1, getField(spyController, "selectedMonth"));
        assertEquals(2024, getField(spyController, "selectedYear"));

        verify(spyController, times(2)).refreshDashboardData();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = DashboardController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String fieldName) {
        try {
            var field = DashboardController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String methodName) throws Exception {
        var method = DashboardController.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }
}
