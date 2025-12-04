package test.view;

import it.unicas.project.template.address.view.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControllerInstantiationTest {

    @Test
    void instantiateDashboardControllers() {
        assertNotNull(new DashboardController());
        assertNotNull(new BudgetController());
        assertNotNull(new MovimentiController());
    }

    @Test
    void instantiateAuthControllers() {
        assertNotNull(new LoginController());
        assertNotNull(new RegisterController());
    }
}
