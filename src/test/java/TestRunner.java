import it.unicas.project.template.address.model.BudgetTest;
import it.unicas.project.template.address.util.BudgetNotificationHelperTest;
import it.unicas.project.template.address.util.BudgetNotificationPreferencesTest;
import it.unicas.project.template.address.util.DateUtilTest;
import it.unicas.project.template.address.util.ForecastCalculatorTest;
import it.unicas.project.template.address.util.SummaryCalculatorTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Launcher per tutti i test JUnit del progetto.
 *
 * Test inclusi:
 * - BudgetTest: Test per la gestione dei budget (getProgress, getRemaining)
 * - ForecastCalculatorTest: Test per il calcolo delle previsioni
 * - SummaryCalculatorTest: Test per il calcolo dei riepiloghi
 * - BudgetNotificationHelperTest: Test per la gestione notifiche budget
 * - DateUtilTest: Test per le utility di date
 * - BudgetNotificationPreferencesTest: Test per le preferenze notifiche
 *
 * Requisiti del professore coperti:
 * 1. Calcolo dei riepiloghi -> SummaryCalculatorTest
 * 2. Previsioni -> ForecastCalculatorTest
 * 3. Gestione dei budget -> BudgetTest, BudgetNotificationHelperTest
 */
public class TestRunner {

    public static void main(String[] args) throws Exception {
        List<Class<?>> testClasses = List.of(
            // Test principali richiesti dal professore
            BudgetTest.class,                      // Gestione budget (model)
            ForecastCalculatorTest.class,          // Previsioni
            SummaryCalculatorTest.class,           // Riepiloghi
            BudgetNotificationHelperTest.class,    // Gestione budget (notifiche)
            // Test utility
            DateUtilTest.class,
            BudgetNotificationPreferencesTest.class
        );

        int total = 0;
        int failed = 0;
        int skipped = 0;

        System.out.println("=".repeat(60));
        System.out.println("ESECUZIONE TEST JUNIT - Personal Finance Application");
        System.out.println("=".repeat(60));
        System.out.println();

        for (Class<?> testClass : testClasses) {
            System.out.println("--- " + testClass.getSimpleName() + " ---");
            Object instance = testClass.getDeclaredConstructor().newInstance();

            // Trova metodo @BeforeEach se presente
            Method beforeEach = null;
            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(BeforeEach.class)) {
                    beforeEach = method;
                    beforeEach.setAccessible(true);
                    break;
                }
            }

            for (Method method : testClass.getDeclaredMethods()) {
                // Esegui solo metodi @Test (i @ParameterizedTest richiedono JUnit Platform)
                if (method.isAnnotationPresent(Test.class)) {
                    total++;
                    try {
                        // Esegui @BeforeEach prima di ogni test
                        if (beforeEach != null) {
                            beforeEach.invoke(instance);
                        }

                        method.setAccessible(true);
                        method.invoke(instance);
                        System.out.println("  [PASS] " + method.getName());
                    } catch (InvocationTargetException ex) {
                        failed++;
                        Throwable cause = ex.getTargetException();
                        System.out.println("  [FAIL] " + method.getName());
                        System.out.println("         -> " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                    }
                } else if (method.isAnnotationPresent(ParameterizedTest.class)) {
                    // I test parametrici richiedono JUnit Platform per l'esecuzione completa
                    skipped++;
                    System.out.println("  [SKIP] " + method.getName() + " (ParameterizedTest - usa JUnit Platform)");
                }
            }
            System.out.println();
        }

        System.out.println("=".repeat(60));
        System.out.println("RIEPILOGO TEST");
        System.out.println("=".repeat(60));
        System.out.println("Eseguiti: " + total);
        System.out.println("Passati:  " + (total - failed));
        System.out.println("Falliti:  " + failed);
        System.out.println("Saltati:  " + skipped + " (ParameterizedTest - eseguire con Maven/Gradle)");
        System.out.println();

        if (failed > 0) {
            System.out.println("STATO: FALLITO");
            System.exit(1);
        } else {
            System.out.println("STATO: TUTTI I TEST PASSATI!");
        }
    }
}
