package test.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import it.unicas.project.template.address.view.DashboardController;
import javafx.application.Platform;
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DashboardControllerTest {

    /**
     * MainApp finto per restituire un utente loggato senza usare Mockito.
     */
    static class TestMainApp extends MainApp {
        private final User loggedUser;

        TestMainApp(User loggedUser) {
            this.loggedUser = loggedUser;
        }

        @Override
        public User getLoggedUser() {
            return loggedUser;
        }
    }

    /**
     * Stub del DAO movimenti per evitare problemi con Mockito su classi concrete.
     */
    static class StubMovimentiDAO extends MovimentiDAOMySQLImpl {
        float entrate;
        float uscite;
        List<Movimenti> movimenti;

        @Override
        public float getSumByMonth(int userId, int month, int year, String type) {
            if ("Entrata".equalsIgnoreCase(type)) {
                return entrate;
            } else if ("Uscita".equalsIgnoreCase(type)) {
                return uscite;
            }
            return 0f;
        }

        @Override
        public List<Movimenti> selectByUserAndMonthYear(int userId, int month, int year) {
            return movimenti;
        }
    }

    /**
     * Stub del DAO budget per evitare il mock della classe concreta.
     */
    static class StubBudgetDAO extends BudgetDAOMySQLImpl {
        List<Budget> budgets;

        @Override
        public List<Budget> getBudgetsForMonth(int userId, int month, int year) {
            return budgets;
        }
    }

    @BeforeAll
    static void initToolkit() {
        // Inizializza il toolkit JavaFX per poter creare controlli nei test
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

    /**
     * Esegue un Runnable sul JavaFX Application Thread e aspetta che finisca.
     */
    private static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout in runOnFxThreadAndWait");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Crea una Connection fittizia usando Java Proxy per la query di forecast.
     * Restituisce sempre:
     * - totaleEntrate = 200
     * - totaleUscite = 50
     * - giorniConMovimenti = 5
     * - dataRecente = oggi
     */
    private Connection createFakeConnectionForForecast() {
        // ResultSet finto
        InvocationHandler rsHandler = new InvocationHandler() {
            boolean first = true;
            final double totaleEntrate = 200d;
            final double totaleUscite = 50d;
            final int giorniConMovimenti = 5;
            final java.sql.Date dataRecente = java.sql.Date.valueOf(LocalDate.now());

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
                    case "getDouble":
                        String colD = (String) args[0];
                        if ("totaleEntrate".equalsIgnoreCase(colD)) return totaleEntrate;
                        if ("totaleUscite".equalsIgnoreCase(colD)) return totaleUscite;
                        return 0d;
                    case "getInt":
                        String colI = (String) args[0];
                        if ("giorniConMovimenti".equalsIgnoreCase(colI)) return giorniConMovimenti;
                        return 0;
                    case "getDate":
                        String colDt = (String) args[0];
                        if ("dataRecente".equalsIgnoreCase(colDt)) return dataRecente;
                        return null;
                    case "close":
                        return null;
                    default:
                        // per qualsiasi altro metodo, valori di default
                        Class<?> rt = method.getReturnType();
                        if (rt.equals(boolean.class)) return false;
                        if (rt.equals(int.class)) return 0;
                        if (rt.equals(double.class)) return 0d;
                        if (rt.equals(float.class)) return 0f;   // 👈 AGGIUNGI QUESTA RIGA
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
                    case "setInt":
                    case "close":
                        return null;
                    default:
                        Class<?> rt = method.getReturnType();
                        if (rt.equals(boolean.class)) return false;
                        if (rt.equals(int.class)) return 0;
                        if (rt.equals(double.class)) return 0d;
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
    void initializeShouldResetLabelsToPlaceholder() throws Exception {
        DashboardController controller = createControllerWithBasicUi();

        runOnFxThreadAndWait(() -> {
            try {
                invokePrivate(controller, "initialize");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals("...", ((Label) getField(controller, "lblSaldo")).getText());
        assertEquals("...", ((Label) getField(controller, "lblEntrate")).getText());
        assertEquals("...", ((Label) getField(controller, "lblUscite")).getText());
    }

    @Test
    void refreshDashboardDataUsesDaoResults() throws Exception {
        DashboardController controller = createControllerWithBasicUi();

        // MainApp finto con utente loggato
        User user = new User(1, "user", "pwd");
        MainApp mainApp = new TestMainApp(user);

        // 1 movimento di esempio
        Movimenti movement = new Movimenti();
        movement.setType("Entrata");
        movement.setAmount(100f);
        movement.setTitle("Stipendio");
        movement.setDate(LocalDate.now());

        // 1 budget di esempio
        Budget budget = new Budget();
        budget.setCategoryName("Casa");
        budget.setBudgetAmount(200f);
        budget.setSpentAmount(50f);

        // Stub dei DAO
        StubMovimentiDAO movimentiDAO = new StubMovimentiDAO();
        movimentiDAO.entrate = 200f;
        movimentiDAO.uscite = 50f;
        movimentiDAO.movimenti = List.of(movement);

        StubBudgetDAO budgetDAO = new StubBudgetDAO();
        budgetDAO.budgets = List.of(budget);

        it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings settings =
                new it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings();

        controller.setMovimentiDAO(movimentiDAO);
        controller.setBudgetDAO(budgetDAO);
        controller.setSettingsSupplier(() -> settings);
        controller.setConnectionFactory(url -> createFakeConnectionForForecast());

        // setMainApp chiama internamente refreshDashboardData: eseguiamo sul thread FX
        runOnFxThreadAndWait(() -> controller.setMainApp(mainApp));

        // Verifiche sulla UI
        assertEquals("€ 200,00", ((Label) getField(controller, "lblEntrate")).getText());
        assertEquals("€ 50,00", ((Label) getField(controller, "lblUscite")).getText());
        assertEquals("€ 150,00", ((Label) getField(controller, "lblSaldo")).getText());
        assertEquals("N/A", ((Label) getField(controller, "lblPrevisione")).getText());
        assertEquals(1, ((VBox) getField(controller, "boxUltimiMovimenti")).getChildren().size());
        assertFalse(((GridPane) getField(controller, "gridBudgetList")).getChildren().isEmpty());
    }

    @Test
    void navigationMethodsAdjustMonthAndYear() throws Exception {
        DashboardController controller = createControllerWithBasicUi();

        LocalDate january = LocalDate.of(2024, 1, 1);
        setField(controller, "selectedMonth", january.getMonthValue());
        setField(controller, "selectedYear", january.getYear());

        // handlePreviousMonth: 01/2024 -> 12/2023
        runOnFxThreadAndWait(() -> {
            try {
                invokePrivate(controller, "handlePreviousMonth");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(12, (Integer) getField(controller, "selectedMonth"));
        assertEquals(2023, (Integer) getField(controller, "selectedYear"));

        // handleNextMonth: 12/2023 -> 01/2024
        runOnFxThreadAndWait(() -> {
            try {
                invokePrivate(controller, "handleNextMonth");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(1, (Integer) getField(controller, "selectedMonth"));
        assertEquals(2024, (Integer) getField(controller, "selectedYear"));
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
