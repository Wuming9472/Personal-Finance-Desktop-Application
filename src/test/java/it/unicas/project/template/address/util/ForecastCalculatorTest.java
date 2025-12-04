package it.unicas.project.template.address.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test JUnit per la classe ForecastCalculator.
 * Verifica la logica di calcolo delle previsioni finanziarie:
 * - Media giornaliera di spese/entrate
 * - Proiezione a fine mese
 * - Saldo stimato
 * - Classificazione dello status (Stable/Warning/Critical)
 */
@DisplayName("Test Previsioni - ForecastCalculator")
public class ForecastCalculatorTest {

    private ForecastCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ForecastCalculator();
    }

    // ============================================================
    // TEST PER calculateDailyAverage()
    // ============================================================

    @Test
    @DisplayName("calculateDailyAverage() calcola correttamente la media")
    void dailyAverageCalculatesCorrectly() {
        // 300€ spesi in 10 giorni = 30€/giorno
        double average = calculator.calculateDailyAverage(300.0, 10);
        assertEquals(30.0, average, 0.001, "300 / 10 = 30");
    }

    @Test
    @DisplayName("calculateDailyAverage() restituisce 0 per giorni <= 0")
    void dailyAverageReturnsZeroForInvalidDays() {
        assertEquals(0.0, calculator.calculateDailyAverage(100.0, 0), 0.001);
        assertEquals(0.0, calculator.calculateDailyAverage(100.0, -5), 0.001);
    }

    @ParameterizedTest
    @DisplayName("calculateDailyAverage() scenari multipli")
    @CsvSource({
        "600.0, 30, 20.0",      // 600/30 = 20
        "150.0, 15, 10.0",      // 150/15 = 10
        "1000.0, 10, 100.0",    // 1000/10 = 100
        "0.0, 10, 0.0",         // 0/10 = 0
        "450.0, 9, 50.0"        // 450/9 = 50
    })
    void dailyAverageVariousScenarios(double total, int days, double expected) {
        assertEquals(expected, calculator.calculateDailyAverage(total, days), 0.001);
    }

    // ============================================================
    // TEST PER calculateProjectedTotal()
    // ============================================================

    @Test
    @DisplayName("calculateProjectedTotal() proietta correttamente a fine mese")
    void projectedTotalCalculatesCorrectly() {
        // Totale attuale 300€, media 30€/giorno, 20 giorni rimanenti
        // Proiezione: 300 + (30 * 20) = 900€
        double projected = calculator.calculateProjectedTotal(300.0, 30.0, 20);
        assertEquals(900.0, projected, 0.001);
    }

    @Test
    @DisplayName("calculateProjectedTotal() gestisce giorni rimanenti = 0")
    void projectedTotalWithZeroRemainingDays() {
        // Ultimo giorno del mese: proiezione = totale attuale
        double projected = calculator.calculateProjectedTotal(500.0, 50.0, 0);
        assertEquals(500.0, projected, 0.001);
    }

    @Test
    @DisplayName("calculateProjectedTotal() gestisce giorni rimanenti negativi")
    void projectedTotalWithNegativeRemainingDays() {
        // Se negativo (errore), ritorna solo il totale attuale
        double projected = calculator.calculateProjectedTotal(500.0, 50.0, -5);
        assertEquals(500.0, projected, 0.001);
    }

    @ParameterizedTest
    @DisplayName("calculateProjectedTotal() scenari multipli")
    @CsvSource({
        "100.0, 10.0, 10, 200.0",     // 100 + (10*10) = 200
        "500.0, 25.0, 15, 875.0",     // 500 + (25*15) = 875
        "200.0, 0.0, 20, 200.0",      // 200 + (0*20) = 200 (nessuna spesa media)
        "0.0, 50.0, 5, 250.0"         // 0 + (50*5) = 250
    })
    void projectedTotalVariousScenarios(double current, double dailyAvg, int remaining, double expected) {
        assertEquals(expected, calculator.calculateProjectedTotal(current, dailyAvg, remaining), 0.001);
    }

    // ============================================================
    // TEST PER calculateEstimatedBalance()
    // ============================================================

    @Test
    @DisplayName("calculateEstimatedBalance() calcola saldo positivo")
    void estimatedBalancePositive() {
        // Entrate 1000€, Uscite 600€ = Saldo +400€
        double balance = calculator.calculateEstimatedBalance(1000.0, 600.0);
        assertEquals(400.0, balance, 0.001);
    }

    @Test
    @DisplayName("calculateEstimatedBalance() calcola saldo negativo")
    void estimatedBalanceNegative() {
        // Entrate 500€, Uscite 800€ = Saldo -300€
        double balance = calculator.calculateEstimatedBalance(500.0, 800.0);
        assertEquals(-300.0, balance, 0.001);
    }

    @Test
    @DisplayName("calculateEstimatedBalance() calcola saldo zero")
    void estimatedBalanceZero() {
        double balance = calculator.calculateEstimatedBalance(500.0, 500.0);
        assertEquals(0.0, balance, 0.001);
    }

    // ============================================================
    // TEST PER determineStatus()
    // ============================================================

    @Test
    @DisplayName("determineStatus() classifica STABLE per saldo > 200")
    void statusIsStableForHighBalance() {
        assertEquals(ForecastCalculator.ForecastStatus.STABLE,
            calculator.determineStatus(500.0), "Saldo 500€ = STABLE");
        assertEquals(ForecastCalculator.ForecastStatus.STABLE,
            calculator.determineStatus(201.0), "Saldo 201€ = STABLE");
    }

    @Test
    @DisplayName("determineStatus() classifica WARNING per saldo tra -100 e 200")
    void statusIsWarningForMediumBalance() {
        assertEquals(ForecastCalculator.ForecastStatus.WARNING,
            calculator.determineStatus(200.0), "Saldo 200€ = WARNING");
        assertEquals(ForecastCalculator.ForecastStatus.WARNING,
            calculator.determineStatus(0.0), "Saldo 0€ = WARNING");
        assertEquals(ForecastCalculator.ForecastStatus.WARNING,
            calculator.determineStatus(-100.0), "Saldo -100€ = WARNING");
    }

    @Test
    @DisplayName("determineStatus() classifica CRITICAL per saldo < -100")
    void statusIsCriticalForLowBalance() {
        assertEquals(ForecastCalculator.ForecastStatus.CRITICAL,
            calculator.determineStatus(-101.0), "Saldo -101€ = CRITICAL");
        assertEquals(ForecastCalculator.ForecastStatus.CRITICAL,
            calculator.determineStatus(-500.0), "Saldo -500€ = CRITICAL");
    }

    @ParameterizedTest
    @DisplayName("determineStatus() soglie esatte")
    @CsvSource({
        "200.01, STABLE",
        "200.0, WARNING",
        "100.0, WARNING",
        "0.0, WARNING",
        "-99.99, WARNING",
        "-100.0, WARNING",
        "-100.01, CRITICAL"
    })
    void statusBoundaryValues(double balance, String expectedStatus) {
        ForecastCalculator.ForecastStatus expected =
            ForecastCalculator.ForecastStatus.valueOf(expectedStatus);
        assertEquals(expected, calculator.determineStatus(balance));
    }

    // ============================================================
    // TEST PER hasEnoughData()
    // ============================================================

    @Test
    @DisplayName("hasEnoughData() restituisce true per >= 3 giorni")
    void hasEnoughDataWithSufficientDays() {
        assertTrue(calculator.hasEnoughData(3), "3 giorni = sufficiente");
        assertTrue(calculator.hasEnoughData(10), "10 giorni = sufficiente");
        assertTrue(calculator.hasEnoughData(30), "30 giorni = sufficiente");
    }

    @Test
    @DisplayName("hasEnoughData() restituisce false per < 3 giorni")
    void hasEnoughDataWithInsufficientDays() {
        assertFalse(calculator.hasEnoughData(2), "2 giorni = insufficiente");
        assertFalse(calculator.hasEnoughData(1), "1 giorno = insufficiente");
        assertFalse(calculator.hasEnoughData(0), "0 giorni = insufficiente");
        assertFalse(calculator.hasEnoughData(-1), "-1 giorni = insufficiente");
    }

    // ============================================================
    // TEST PER calculateForecast() - Metodo principale
    // ============================================================

    @Test
    @DisplayName("calculateForecast() restituisce errore per dati insufficienti")
    void forecastReturnsErrorForInsufficientData() {
        // Solo 2 giorni di dati
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            1000.0, 500.0, 2, 15, 30);

        assertFalse(result.isValid(), "Deve essere non valido con 2 giorni");
        assertEquals(ForecastCalculator.ForecastStatus.INSUFFICIENT_DATA, result.getStatus());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("insufficienti"));
    }

    @Test
    @DisplayName("calculateForecast() restituisce errore per giorno non valido")
    void forecastReturnsErrorForInvalidDay() {
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            1000.0, 500.0, 10, 0, 30);

        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("non valido"));
    }

    @Test
    @DisplayName("calculateForecast() calcola correttamente previsione completa")
    void forecastCalculatesCompleteResult() {
        // Scenario: mese con 30 giorni, siamo al giorno 10
        // Entrate totali: 1000€ (media: 100€/giorno)
        // Uscite totali: 500€ (media: 50€/giorno)
        // Giorni rimanenti: 20
        // Proiezione entrate: 1000 + (100 * 20) = 3000€
        // Proiezione uscite: 500 + (50 * 20) = 1500€
        // Saldo stimato: 3000 - 1500 = 1500€ (STABLE)

        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            1000.0, 500.0, 10, 10, 30);

        assertTrue(result.isValid(), "Deve essere valido");
        assertEquals(10, result.getCurrentDay());
        assertEquals(20, result.getRemainingDays());
        assertEquals(100.0, result.getDailyIncomeAverage(), 0.001);
        assertEquals(50.0, result.getDailyExpenseAverage(), 0.001);
        assertEquals(3000.0, result.getProjectedTotalIncome(), 0.001);
        assertEquals(1500.0, result.getProjectedTotalExpenses(), 0.001);
        assertEquals(1500.0, result.getEstimatedBalance(), 0.001);
        assertEquals(ForecastCalculator.ForecastStatus.STABLE, result.getStatus());
    }

    @Test
    @DisplayName("calculateForecast() scenario WARNING")
    void forecastScenarioWarning() {
        // Entrate 600€, Uscite 500€ in 15 giorni, mese di 30 giorni
        // Media entrate: 40€/giorno, Media uscite: 33.33€/giorno
        // Proiezione entrate: 600 + (40 * 15) = 1200€
        // Proiezione uscite: 500 + (33.33 * 15) = 1000€
        // Saldo: 200€ -> WARNING

        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            600.0, 500.0, 10, 15, 30);

        assertTrue(result.isValid());
        assertEquals(ForecastCalculator.ForecastStatus.WARNING, result.getStatus());
        assertEquals(100.0, result.getEstimatedBalance(), 0.1);
    }

    @Test
    @DisplayName("calculateForecast() scenario CRITICAL")
    void forecastScenarioCritical() {
        // Entrate 200€, Uscite 600€ in 10 giorni, mese di 30 giorni
        // Media entrate: 20€/giorno, Media uscite: 60€/giorno
        // Proiezione entrate: 200 + (20 * 20) = 600€
        // Proiezione uscite: 600 + (60 * 20) = 1800€
        // Saldo: -1200€ -> CRITICAL

        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            200.0, 600.0, 10, 10, 30);

        assertTrue(result.isValid());
        assertEquals(ForecastCalculator.ForecastStatus.CRITICAL, result.getStatus());
        assertTrue(result.getEstimatedBalance() < -100);
    }

    @Test
    @DisplayName("calculateForecast() ultimo giorno del mese")
    void forecastOnLastDayOfMonth() {
        // Ultimo giorno: nessuna proiezione, saldo = entrate - uscite attuali
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            1500.0, 1200.0, 25, 30, 30);

        assertTrue(result.isValid());
        assertEquals(0, result.getRemainingDays());
        assertEquals(1500.0, result.getProjectedTotalIncome(), 0.001);
        assertEquals(1200.0, result.getProjectedTotalExpenses(), 0.001);
        assertEquals(300.0, result.getEstimatedBalance(), 0.001);
    }

    // ============================================================
    // TEST DI INTEGRAZIONE - Scenari realistici
    // ============================================================

    @Test
    @DisplayName("Scenario realistico: Inizio mese con poche transazioni")
    void scenarioEarlyMonth() {
        // Giorno 5 del mese, 3 giorni con movimenti
        // Stipendio ricevuto: 1500€
        // Spese: 150€
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            1500.0, 150.0, 3, 5, 31);

        assertTrue(result.isValid());
        assertEquals(26, result.getRemainingDays());
        assertTrue(result.getEstimatedBalance() > 0, "Saldo previsto positivo");
        assertEquals(ForecastCalculator.ForecastStatus.STABLE, result.getStatus());
    }

    @Test
    @DisplayName("Scenario realistico: Meta' mese con spese elevate")
    void scenarioMidMonthHighExpenses() {
        // Giorno 15, stipendio 1800€ ricevuto
        // Spese gia' a 1200€ (media 80€/giorno!)
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            1800.0, 1200.0, 15, 15, 30);

        assertTrue(result.isValid());
        // Media uscite: 80€/giorno, proiezione: 1200 + (80*15) = 2400€
        // Media entrate: 120€/giorno, proiezione: 1800 + (120*15) = 3600€
        // Saldo: 3600 - 2400 = 1200€
        assertEquals(ForecastCalculator.ForecastStatus.STABLE, result.getStatus());
    }

    @Test
    @DisplayName("Scenario realistico: Fine mese in deficit")
    void scenarioEndMonthDeficit() {
        // Giorno 25 di 30, spese superano le entrate
        // Entrate: 1000€, Uscite: 1500€
        ForecastCalculator.ForecastResult result = calculator.calculateForecast(
            1000.0, 1500.0, 20, 25, 30);

        assertTrue(result.isValid());
        assertEquals(5, result.getRemainingDays());
        assertTrue(result.getEstimatedBalance() < 0, "Saldo previsto negativo");
    }

    @Test
    @DisplayName("Verifica costanti di classe")
    void verifyClassConstants() {
        assertEquals(200.0, ForecastCalculator.STABLE_THRESHOLD);
        assertEquals(-100.0, ForecastCalculator.CRITICAL_THRESHOLD);
        assertEquals(3, ForecastCalculator.MINIMUM_DAYS_FOR_FORECAST);
    }
}
