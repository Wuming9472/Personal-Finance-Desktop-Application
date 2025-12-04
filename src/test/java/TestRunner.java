import it.unicas.project.template.address.model.BudgetTest;
import it.unicas.project.template.address.util.DateUtilTest;
import it.unicas.project.template.address.util.ForecastCalculatorTest;
import it.unicas.project.template.address.util.SummaryCalculatorTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Launcher minimale per eseguire i test senza dipendenze esterne.
 */
public class TestRunner {

    public static void main(String[] args) throws Exception {
        List<Class<?>> testClasses = List.of(
            BudgetTest.class,
            ForecastCalculatorTest.class,
            SummaryCalculatorTest.class,
            DateUtilTest.class
        );

        int total = 0;
        int failed = 0;

        System.out.println("Esecuzione test unitari");
        System.out.println("=======================\n");

        for (Class<?> testClass : testClasses) {
            Object instance = testClass.getDeclaredConstructor().newInstance();
            System.out.println("-- " + testClass.getSimpleName());

            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    total++;
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                        System.out.println("  [PASS] " + method.getName());
                    } catch (InvocationTargetException ex) {
                        failed++;
                        Throwable cause = ex.getTargetException();
                        System.out.println("  [FAIL] " + method.getName());
                        System.out.println("         -> " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                    }
                }
            }

            System.out.println();
        }

        System.out.println("Riepilogo: " + (total - failed) + "/" + total + " test passati");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
