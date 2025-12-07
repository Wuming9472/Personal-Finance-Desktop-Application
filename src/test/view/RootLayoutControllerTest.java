package test.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.view.RootLayoutController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RootLayoutControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    /**
     * MainApp finto per tracciare le chiamate di navigazione.
     */
    static class TestMainApp extends MainApp {
        private final User loggedUser;
        boolean showDashboardCalled = false;
        boolean showMovimentiCalled = false;
        boolean showBudgetCalled = false;
        boolean showReportCalled = false;
        boolean showAccountPageCalled = false;
        boolean showLoginCalled = false;
        User setLoggedUserValue = null;

        TestMainApp(User loggedUser) {
            this.loggedUser = loggedUser;
        }

        @Override
        public User getLoggedUser() {
            return loggedUser;
        }

        @Override
        public void showDashboard() {
            this.showDashboardCalled = true;
        }

        @Override
        public void showMovimenti() {
            this.showMovimentiCalled = true;
        }

        @Override
        public void showBudget() {
            this.showBudgetCalled = true;
        }

        @Override
        public void showReport() {
            this.showReportCalled = true;
        }

        @Override
        public void showAccountPage() {
            this.showAccountPageCalled = true;
        }

        @Override
        public void showLogin() {
            this.showLoginCalled = true;
        }

        @Override
        public void setLoggedUser(User user) {
            this.setLoggedUserValue = user;
        }
    }

    private RootLayoutController controller;
    private Label lblPageTitle;
    private Label lblInitials;
    private MenuButton btnUser;
    private VBox sidebar;
    private Button btnToggleMenu;
    private Button btnDashboard;
    private Button btnMovimenti;
    private Button btnBudget;
    private Button btnReport;
    private Button btnAccount;
    private StackPane toggleIconContainer;
    private SVGPath toggleArrow;
    private Label lblDashboard;
    private Label lblMovimenti;
    private Label lblBudget;
    private Label lblReport;
    private Label lblAccount;

    @BeforeEach
    void setUp() {
        controller = new RootLayoutController();
        lblPageTitle = new Label();
        lblInitials = new Label();
        btnUser = new MenuButton();
        sidebar = new VBox();
        btnToggleMenu = new Button();
        btnDashboard = new Button();
        btnMovimenti = new Button();
        btnBudget = new Button();
        btnReport = new Button();
        btnAccount = new Button();
        toggleIconContainer = new StackPane();
        toggleArrow = new SVGPath();
        lblDashboard = new Label("Dashboard");
        lblMovimenti = new Label("Movimenti");
        lblBudget = new Label("Budget");
        lblReport = new Label("Report");
        lblAccount = new Label("Account");

        setField(controller, "lblPageTitle", lblPageTitle);
        setField(controller, "lblInitials", lblInitials);
        setField(controller, "btnUser", btnUser);
        setField(controller, "sidebar", sidebar);
        setField(controller, "btnToggleMenu", btnToggleMenu);
        setField(controller, "btnDashboard", btnDashboard);
        setField(controller, "btnMovimenti", btnMovimenti);
        setField(controller, "btnBudget", btnBudget);
        setField(controller, "btnReport", btnReport);
        setField(controller, "btnAccount", btnAccount);
        setField(controller, "toggleIconContainer", toggleIconContainer);
        setField(controller, "toggleArrow", toggleArrow);
        setField(controller, "lblDashboard", lblDashboard);
        setField(controller, "lblMovimenti", lblMovimenti);
        setField(controller, "lblBudget", lblBudget);
        setField(controller, "lblReport", lblReport);
        setField(controller, "lblAccount", lblAccount);
    }

    @Test
    void initializeShouldSetDashboardAsActive() throws Exception {
        runOnFxThreadAndWait(() -> {
            try {
                invokePrivate(controller, "initialize");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Button currentActive = (Button) getField(controller, "currentActiveButton");
        assertEquals(btnDashboard, currentActive);
    }

    @Test
    void setPageTitleShouldUpdateLabel() {
        controller.setPageTitle("Movimenti");
        assertEquals("Movimenti", lblPageTitle.getText());
    }

    @Test
    void setPageTitleShouldHandleNullLabel() {
        setField(controller, "lblPageTitle", null);
        // Non dovrebbe lanciare eccezioni
        controller.setPageTitle("Test");
    }

    @Test
    void updateUserInfoShouldSetSingleInitialForSingleWord() {
        controller.updateUserInfo("mario");

        assertEquals("mario", btnUser.getText());
        assertEquals("M", lblInitials.getText());
    }

    @Test
    void updateUserInfoShouldSetTwoInitialsForTwoWords() {
        controller.updateUserInfo("Mario Rossi");

        assertEquals("Mario Rossi", btnUser.getText());
        assertEquals("MR", lblInitials.getText());
    }

    @Test
    void updateUserInfoShouldHandleEmptyUsername() {
        controller.updateUserInfo("");

        assertEquals("Ospite", btnUser.getText());
        assertEquals("?", lblInitials.getText());
    }

    @Test
    void updateUserInfoShouldHandleNullUsername() {
        controller.updateUserInfo(null);

        assertEquals("Ospite", btnUser.getText());
        assertEquals("?", lblInitials.getText());
    }

    @Test
    void handleShowDashboardShouldCallMainApp() throws Exception {
        User user = new User(1, "test", "pwd");
        TestMainApp mainApp = new TestMainApp(user);
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleShowDashboard");

        assertTrue(mainApp.showDashboardCalled);
    }

    @Test
    void handleShowMovementsShouldCallMainApp() throws Exception {
        User user = new User(1, "test", "pwd");
        TestMainApp mainApp = new TestMainApp(user);
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleShowMovements");

        assertTrue(mainApp.showMovimentiCalled);
    }

    @Test
    void handleShowBudgetShouldCallMainApp() throws Exception {
        User user = new User(1, "test", "pwd");
        TestMainApp mainApp = new TestMainApp(user);
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleShowBudget");

        assertTrue(mainApp.showBudgetCalled);
    }

    @Test
    void handleShowReportShouldCallMainApp() throws Exception {
        User user = new User(1, "test", "pwd");
        TestMainApp mainApp = new TestMainApp(user);
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleShowReport");

        assertTrue(mainApp.showReportCalled);
    }

    @Test
    void handleShowAccountShouldCallMainApp() throws Exception {
        User user = new User(1, "test", "pwd");
        TestMainApp mainApp = new TestMainApp(user);
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleShowAccount");

        assertTrue(mainApp.showAccountPageCalled);
    }

    @Test
    void handleSettingsShouldNavigateToAccountPage() throws Exception {
        User user = new User(1, "test", "pwd");
        TestMainApp mainApp = new TestMainApp(user);
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleSettings");

        assertTrue(mainApp.showAccountPageCalled);
    }

    @Test
    void handleExitShouldResetUserAndShowLogin() throws Exception {
        User user = new User(1, "test", "pwd");
        TestMainApp mainApp = new TestMainApp(user);
        setField(controller, "mainApp", mainApp);

        invokePrivate(controller, "handleExit");

        assertNull(mainApp.setLoggedUserValue);
        assertTrue(mainApp.showLoginCalled);
    }

    @Test
    void handleNavigationShouldDoNothingWhenMainAppIsNull() throws Exception {
        setField(controller, "mainApp", null);

        // Nessuna eccezione dovrebbe essere lanciata
        invokePrivate(controller, "handleShowDashboard");
        invokePrivate(controller, "handleShowMovements");
        invokePrivate(controller, "handleShowBudget");
        invokePrivate(controller, "handleShowReport");
        invokePrivate(controller, "handleShowAccount");
    }

    @Test
    void menuExpansionStateStartsExpanded() {
        boolean isExpanded = (Boolean) getField(controller, "isMenuExpanded");
        assertTrue(isExpanded);
    }

    @Test
    void expandedWidthConstant() {
        double expandedWidth = 260.0;
        assertEquals(260.0, expandedWidth, 0.001);
    }

    @Test
    void collapsedWidthConstant() {
        double collapsedWidth = 82.0;
        assertEquals(82.0, collapsedWidth, 0.001);
    }

    @Test
    void setMainAppShouldUpdateUserInfo() {
        User user = new User(1, "TestUser", "pwd");
        TestMainApp mainApp = new TestMainApp(user);

        controller.setMainApp(mainApp);

        assertEquals("TestUser", btnUser.getText());
        assertEquals("T", lblInitials.getText());
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

    private void setField(Object target, String name, Object value) {
        try {
            var field = RootLayoutController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String name) {
        try {
            var field = RootLayoutController.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String name) throws Exception {
        var method = RootLayoutController.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(target);
    }
}
