import it.unicas.project.template.address.util.BudgetNotificationPreferencesTest;
import it.unicas.project.template.address.util.DateUtilTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Semplice launcher per i test basato sull'annotazione @Test minimale.
 */
public class TestRunner {

    public static void main(String[] args) throws Exception {
        List<Class<?>> testClasses = List.of(
            DateUtilTest.class,
            BudgetNotificationPreferencesTest.class
        );

        int total = 0;
        int failed = 0;

        for (Class<?> testClass : testClasses) {
            Object instance = testClass.getDeclaredConstructor().newInstance();
            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    total++;
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                        System.out.println("[PASS] " + testClass.getSimpleName() + ": " + method.getName());
                    } catch (InvocationTargetException ex) {
                        failed++;
                        Throwable cause = ex.getTargetException();
                        System.out.println("[FAIL] " + testClass.getSimpleName() + ": " + method.getName() + " -> " + cause.getMessage());
                    }
                }
            }
        }

        System.out.println("Eseguiti " + total + " test. Falliti: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }
}
