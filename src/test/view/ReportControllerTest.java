package test.view;

import it.unicas.project.template.address.view.ReportController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReportControllerTest {

    @BeforeAll
    static void initToolkit() {
        // Inizializza JavaFX
        new JFXPanel();
    }

    /**
     * Crea un controller con i campi FXML minimi inizializzati
     * per poter chiamare initialize() e i metodi di forecast senza NPE.
     */
    private ReportController createControllerWithBasicUi() {
        ReportController controller = new ReportController();

        // campi usati in initialize / range selector / forecast
        setField(controller, "pieChart", new PieChart());
        setField(controller, "cmbRange", new ComboBox<String>());

        setField(controller, "lblRisparmioStimato", new Label());
        setField(controller, "lblCategoriaCritica", new Label());

        setField(controller, "lblPeriodoCalcolo", new Label());
        setField(controller, "lblSaldoStimato", new Label());
        setField(controller, "paneStatus", new AnchorPane());
        setField(controller, "lblStatusIcon", new Label());
        setField(controller, "lblStatusTitolo", new Label());
        setField(controller, "lblStatusMessaggio", new Label());
        setField(controller, "lblGiorniRimanenti", new Label());
        setField(controller, "lblMediaSpeseGiornaliera", new Label());
        setField(controller, "lblMediaEntrateGiornaliera", new Label());
        setField(controller, "lblSpeseProiettateTotali", new Label());
        setField(controller, "lblEntrateProiettateTotali", new Label());
        setField(controller, "lblDisclaimer", new Label());

        // lineChartAndamento può restare null: in initialize c'è il check != null
        return controller;
    }

    /* ========================= TEST BASE ========================= */

    @Test
    void initializeShouldSetupRangeSelectorDefaults() throws Exception {
        ReportController controller = createControllerWithBasicUi();

        invokePrivate(controller, "initialize");

        ComboBox<String> cmbRange = getField(controller, "cmbRange");

        assertNotNull(cmbRange.getItems());
        assertEquals(2, cmbRange.getItems().size());
        assertTrue(cmbRange.getItems().contains("Ultimi 6 mesi"));
        assertTrue(cmbRange.getItems().contains("Ultimo anno"));

        // valore di default
        assertEquals("Ultimi 6 mesi", cmbRange.getValue());
    }

    @Test
    void resolveMonthsBackShouldReturnCorrectValuesBasedOnComboSelection() throws Exception {
        ReportController controller = createControllerWithBasicUi();

        invokePrivate(controller, "initialize");
        ComboBox<String> cmbRange = getField(controller, "cmbRange");

        // default: "Ultimi 6 mesi" -> 6
        int monthsDefault = invokeResolveMonthsBack(controller);
        assertEquals(6, monthsDefault);

        // "Ultimo anno" -> 12
        cmbRange.setValue("Ultimo anno");
        int monthsYear = invokeResolveMonthsBack(controller);
        assertEquals(12, monthsYear);

        // valore nullo -> fallback 6
        cmbRange.setValue(null);
        int monthsNull = invokeResolveMonthsBack(controller);
        assertEquals(6, monthsNull);
    }

    /* ==================== TEST PREVISIONE: DATI INSUFFICIENTI ==================== */

    @Test
    void forecastUiShowsInsufficientDataMessage() throws Exception {
        ReportController controller = createControllerWithBasicUi();

        // Questo è il ramo richiamato da loadForecastData quando giorniConMovimenti < 7
        invokePrivate(controller, "displayInsufficientDataMessage");

        waitForFxEvents();

        Label lblSaldoStimato      = getField(controller, "lblSaldoStimato");
        Label lblPeriodoCalcolo    = getField(controller, "lblPeriodoCalcolo");
        Label lblStatusTitolo      = getField(controller, "lblStatusTitolo");
        Label lblStatusMessaggio   = getField(controller, "lblStatusMessaggio");
        Label lblGiorniRimanenti   = getField(controller, "lblGiorniRimanenti");
        Label lblMediaSpese        = getField(controller, "lblMediaSpeseGiornaliera");
        Label lblMediaEntrate      = getField(controller, "lblMediaEntrateGiornaliera");
        Label lblSpeseProiettate   = getField(controller, "lblSpeseProiettateTotali");
        Label lblEntrateProiettate = getField(controller, "lblEntrateProiettateTotali");

        assertEquals("N/A", lblSaldoStimato.getText());
        assertTrue(lblPeriodoCalcolo.getText().contains("Dati insufficienti")
                || lblPeriodoCalcolo.getText().isEmpty());

        assertEquals("Dati Insufficienti", lblStatusTitolo.getText());
        assertTrue(lblStatusMessaggio.getText().contains("7 giorni"));

        assertEquals("--", lblGiorniRimanenti.getText());
        assertEquals("--", lblMediaSpese.getText());
        assertEquals("--", lblMediaEntrate.getText());
        assertEquals("--", lblSpeseProiettate.getText());
        assertEquals("--", lblEntrateProiettate.getText());
    }

    /* ==================== TEST PREVISIONE: SITUAZIONE STABILE (> 200) ==================== */

    @Test
    void forecastUiShowsPositiveStatusWhenSaldoStimatoIsHigh() throws Exception {
        ReportController controller = createControllerWithBasicUi();

        int currentDay = 10;
        int remainingDays = 20;
        double mediaSpeseGiornaliera = 20.0;
        double speseProiettate = 600.0;
        double saldoStimato = 300.0;              // > 200 → Situazione Stabile
        double totaleEntrate = 800.0;
        double totaleUscite = 500.0;
        double mediaEntrateGiornaliera = 30.0;
        double entrateProiettate = 900.0;

        invokeUpdateForecastUI(
                controller,
                currentDay,
                remainingDays,
                mediaSpeseGiornaliera,
                speseProiettate,
                saldoStimato,
                totaleEntrate,
                totaleUscite,
                mediaEntrateGiornaliera,
                entrateProiettate
        );

        waitForFxEvents();

        Label lblSaldoStimato      = getField(controller, "lblSaldoStimato");
        Label lblGiorniRimanenti   = getField(controller, "lblGiorniRimanenti");
        Label lblMediaSpese        = getField(controller, "lblMediaSpeseGiornaliera");
        Label lblMediaEntrate      = getField(controller, "lblMediaEntrateGiornaliera");
        Label lblSpeseProiettate   = getField(controller, "lblSpeseProiettateTotali");
        Label lblEntrateProiettate = getField(controller, "lblEntrateProiettateTotali");
        Label lblStatusIcon        = getField(controller, "lblStatusIcon");
        Label lblStatusTitolo      = getField(controller, "lblStatusTitolo");
        Label lblStatusMessaggio   = getField(controller, "lblStatusMessaggio");

        // Locale IT → virgola, quindi controllo "€" e "300"
        assertTrue(lblSaldoStimato.getText().contains("€"));
        assertTrue(lblSaldoStimato.getText().contains("300"));

        assertEquals(remainingDays + " gg", lblGiorniRimanenti.getText());
        assertTrue(lblMediaSpese.getText().contains("Uscite: €"));
        assertTrue(lblMediaEntrate.getText().contains("Entrate: €"));
        assertTrue(lblSpeseProiettate.getText().contains("Uscite: €"));
        assertTrue(lblEntrateProiettate.getText().contains("Entrate: €"));

        assertEquals("📈", lblStatusIcon.getText());
        assertEquals("Situazione Stabile", lblStatusTitolo.getText());
        assertTrue(lblStatusMessaggio.getText().contains("Previsione positiva")
                || lblStatusMessaggio.getText().contains("attivo"));
    }

    /* ==================== TEST PREVISIONE: ATTENZIONE (-100 .. 200) ==================== */

    @Test
    void forecastUiShowsWarningStatusWhenSaldoStimatoIsMedium() throws Exception {
        ReportController controller = createControllerWithBasicUi();

        int currentDay = 10;
        int remainingDays = 15;
        double mediaSpeseGiornaliera = 25.0;
        double speseProiettate = 500.0;
        double saldoStimato = 50.0;              // tra -100 e 200 → Attenzione
        double totaleEntrate = 600.0;
        double totaleUscite = 450.0;
        double mediaEntrateGiornaliera = 28.0;
        double entrateProiettate = 650.0;

        invokeUpdateForecastUI(
                controller,
                currentDay,
                remainingDays,
                mediaSpeseGiornaliera,
                speseProiettate,
                saldoStimato,
                totaleEntrate,
                totaleUscite,
                mediaEntrateGiornaliera,
                entrateProiettate
        );

        waitForFxEvents();

        Label lblSaldoStimato    = getField(controller, "lblSaldoStimato");
        Label lblStatusIcon      = getField(controller, "lblStatusIcon");
        Label lblStatusTitolo    = getField(controller, "lblStatusTitolo");
        Label lblStatusMessaggio = getField(controller, "lblStatusMessaggio");

        assertTrue(lblSaldoStimato.getText().contains("€"));
        assertTrue(lblSaldoStimato.getText().contains("50"));

        assertEquals("⚠️", lblStatusIcon.getText());
        assertEquals("Attenzione", lblStatusTitolo.getText());
        assertTrue(lblStatusMessaggio.getText().contains("vicina al limite")
                || lblStatusMessaggio.getText().contains("Monitora le spese"));
    }

    /* ==================== TEST PREVISIONE: SITUAZIONE CRITICA (< -100) ==================== */

    @Test
    void forecastUiShowsCriticalStatusWhenSaldoStimatoIsLow() throws Exception {
        ReportController controller = createControllerWithBasicUi();

        int currentDay = 10;
        int remainingDays = 10;
        double mediaSpeseGiornaliera = 40.0;
        double speseProiettate = 800.0;
        double saldoStimato = -150.0;            // < -100 → Situazione Critica
        double totaleEntrate = 400.0;
        double totaleUscite = 550.0;
        double mediaEntrateGiornaliera = 20.0;
        double entrateProiettate = 650.0;

        invokeUpdateForecastUI(
                controller,
                currentDay,
                remainingDays,
                mediaSpeseGiornaliera,
                speseProiettate,
                saldoStimato,
                totaleEntrate,
                totaleUscite,
                mediaEntrateGiornaliera,
                entrateProiettate
        );

        waitForFxEvents();

        Label lblSaldoStimato    = getField(controller, "lblSaldoStimato");
        Label lblStatusIcon      = getField(controller, "lblStatusIcon");
        Label lblStatusTitolo    = getField(controller, "lblStatusTitolo");
        Label lblStatusMessaggio = getField(controller, "lblStatusMessaggio");

        assertTrue(lblSaldoStimato.getText().contains("€"));
        assertTrue(lblSaldoStimato.getText().contains("150"));

        assertEquals("📉", lblStatusIcon.getText());
        assertEquals("Situazione Critica", lblStatusTitolo.getText());
        assertTrue(lblStatusMessaggio.getText().contains("negativa")
                || lblStatusMessaggio.getText().contains("deficit"));
    }

    /* ===================== helper FX ===================== */

    private void waitForFxEvents() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout in waitForFxEvents");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /* ===================== helper reflection ===================== */

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = ReportController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String fieldName) {
        try {
            var field = ReportController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivate(Object target, String methodName) throws Exception {
        var method = ReportController.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }

    private int invokeResolveMonthsBack(ReportController controller) throws Exception {
        var method = ReportController.class.getDeclaredMethod("resolveMonthsBack");
        method.setAccessible(true);
        Object result = method.invoke(controller);
        return (Integer) result;
    }

    private void invokeUpdateForecastUI(
            ReportController controller,
            int currentDay,
            int remainingDays,
            double mediaSpeseGiornaliera,
            double speseProiettate,
            double saldoStimato,
            double totaleEntrate,
            double totaleUscite,
            double mediaEntrateGiornaliera,
            double entrateProiettate
    ) throws Exception {
        var method = ReportController.class.getDeclaredMethod(
                "updateForecastUI",
                int.class, int.class, double.class, double.class, double.class,
                double.class, double.class, double.class, double.class
        );
        method.setAccessible(true);
        method.invoke(controller,
                currentDay,
                remainingDays,
                mediaSpeseGiornaliera,
                speseProiettate,
                saldoStimato,
                totaleEntrate,
                totaleUscite,
                mediaEntrateGiornaliera,
                entrateProiettate
        );
    }
}
