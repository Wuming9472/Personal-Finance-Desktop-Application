package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ReportController {

    private MainApp mainApp;
    private int currentUserId = -1;

    @FXML private PieChart pieChart;
    @FXML private SmoothAreaChart<String, Number> lineChartAndamento;
    @FXML private ComboBox<String> cmbRange;
    @FXML private Label lblRisparmioStimato;
    @FXML private Label lblCategoriaCritica;

    // Forecast section UI elements
    @FXML private Label lblPeriodoCalcolo;
    @FXML private Label lblSaldoStimato;
    @FXML private AnchorPane paneStatus;
    @FXML private Label lblStatusIcon;
    @FXML private Label lblStatusTitolo;
    @FXML private Label lblStatusMessaggio;
    @FXML private Label lblGiorniRimanenti;
    @FXML private Label lblMediaSpeseGiornaliera;
    @FXML private Label lblSpeseProiettateTotali;
    @FXML private Label lblDisclaimer;

    @FXML
    private void initialize() {
        if (lineChartAndamento != null) {
            lineChartAndamento.setAnimated(true);
            lineChartAndamento.setLegendVisible(true);
            lineChartAndamento.setCreateSymbols(true);
        }
        if (pieChart != null) {
            pieChart.setLegendVisible(true);
        }
        initRangeSelector();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        loadReportDataForCurrentUser();
    }

    private void loadReportDataForCurrentUser() {
        if (mainApp == null || mainApp.getLoggedUser() == null) return;
        currentUserId = mainApp.getLoggedUser().getUser_id();
        updateUIFromData();
    }

    public void refreshReportData() {
        updateUIFromData();
    }

    private void updateUIFromData() {
        if (currentUserId <= 0) return;

        try {
            // 1. PieChart - Distribuzione spese per categoria
            loadPieChartData();

            // 2. LineChart - Andamento finanziario 6/12 mesi
            loadLineChartData();

            // 3. Forecast section - Previsione Fine Mese
            loadForecastData();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initRangeSelector() {
        if (cmbRange != null) {
            cmbRange.getItems().setAll(
                    "Ultimi 6 mesi",
                    "Ultimo anno"
            );
            cmbRange.setValue("Ultimi 6 mesi");
            cmbRange.setOnAction(event -> {
                try {
                    loadLineChartData();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private int resolveMonthsBack() {
        if (cmbRange == null) return 6;
        String selected = cmbRange.getValue();
        if (selected == null) {
            return 6;
        }
        switch (selected) {
            case "Ultimo anno":
                return 12;
            case "Ultimi 6 mesi":
            default:
                return 6;
        }
    }

    private void loadPieChartData() throws SQLException {
        String query = "SELECT c.name, SUM(m.amount) as totale " +
                "FROM movements m " +
                "JOIN categories c ON m.category_id = c.category_id " +
                "WHERE m.user_id = ? AND LOWER(m.type) IN ('uscita', 'expense') " +
                "GROUP BY c.name " +
                "ORDER BY totale DESC";

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String categoria = rs.getString("name");
                    double totale = rs.getDouble("totale");
                    pieData.add(new PieChart.Data(categoria, totale));
                }
            }
        }

        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Nessuna spesa", 1));
        }

        pieChart.setData(pieData);
    }

    private void loadLineChartData() throws SQLException {
        if (lineChartAndamento == null) return;

        lineChartAndamento.setAnimated(false);
        lineChartAndamento.getData().clear();

        int monthsBack = resolveMonthsBack();

        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        List<Pair<String, Pair<Float, Float>>> trendData = dao.getIncomeExpenseTrend(currentUserId, monthsBack);

        XYChart.Series<String, Number> serieEntrate = new XYChart.Series<>();
        serieEntrate.setName("Entrate");
        XYChart.Series<String, Number> serieUscite = new XYChart.Series<>();
        serieUscite.setName("Uscite");

        if (trendData.isEmpty()) {
            lineChartAndamento.setAnimated(true);
            return;
        }

        for (Pair<String, Pair<Float, Float>> point : trendData) {
            String label = point.getKey();
            Float entrata = point.getValue().getKey();
            Float uscita = point.getValue().getValue();

            // Plot monthly values (not cumulative)
            XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, entrata);
            XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, uscita);

            // Tooltip
            final float entrataCorrente = entrata;
            final float uscitaCorrente = uscita;

            incomeData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip tooltip = new Tooltip(String.format("%s\nEntrate: € %.2f", label, entrataCorrente));
                    tooltip.setShowDelay(Duration.millis(50));
                    Tooltip.install(newNode, tooltip);
                }
            });

            expenseData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip tooltip = new Tooltip(String.format("%s\nUscite: € %.2f", label, uscitaCorrente));
                    tooltip.setShowDelay(Duration.millis(50));
                    Tooltip.install(newNode, tooltip);
                }
            });

            serieEntrate.getData().add(incomeData);
            serieUscite.getData().add(expenseData);
        }

        lineChartAndamento.getData().addAll(serieEntrate, serieUscite);

        // Force horizontal tick labels on X axis
        if (lineChartAndamento.getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) lineChartAndamento.getXAxis();
            xAxis.setTickLabelRotation(-45);
        }

        Platform.runLater(() -> {
            lineChartAndamento.setAnimated(true);
        });
    }

    private void loadForecastData() throws SQLException {
        LocalDate referenceDate = getLatestExpenseDate();
        if (referenceDate == null) {
            displayInsufficientDataMessage();
            return;
        }

        YearMonth referenceMonth = YearMonth.from(referenceDate);
        int currentDay = referenceDate.getDayOfMonth();
        int daysInMonth = referenceMonth.lengthOfMonth();
        int remainingDays = daysInMonth - currentDay;

        String query = "SELECT " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata') THEN amount ELSE 0 END) as totaleEntrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita') THEN amount ELSE 0 END) as totaleUscite, " +
                "COUNT(DISTINCT DATE(date)) as giorniConMovimenti " +
                "FROM movements " +
                "WHERE user_id = ? " +
                "AND YEAR(date) = ? " +
                "AND MONTH(date) = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, referenceDate.getYear());
            pstmt.setInt(3, referenceDate.getMonthValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double totaleEntrate = rs.getDouble("totaleEntrate");
                    double totaleUscite = rs.getDouble("totaleUscite");
                    int giorniConMovimenti = rs.getInt("giorniConMovimenti");

                    if (giorniConMovimenti < 3) {
                        displayInsufficientDataMessage();
                        return;
                    }

                    double mediaSpeseGiornaliera = totaleUscite / currentDay;
                    double mediaEntrateGiornaliera = totaleEntrate / currentDay;

                    double speseProiettate = totaleUscite + (mediaSpeseGiornaliera * remainingDays);
                    double entrateProiettate = totaleEntrate + (mediaEntrateGiornaliera * remainingDays);
                    double saldoStimato = entrateProiettate - speseProiettate;

                    updateForecastUI(currentDay, remainingDays, mediaSpeseGiornaliera, speseProiettate,
                            saldoStimato, totaleEntrate, totaleUscite);
                } else {
                    displayInsufficientDataMessage();
                }
            }
        }
    }

    private LocalDate getLatestExpenseDate() throws SQLException {
        String query = "SELECT MAX(DATE(date)) as latestExpense " +
                "FROM movements " +
                "WHERE user_id = ? " +
                "AND LOWER(type) IN ('uscita', 'expense')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date latestDate = rs.getDate("latestExpense");
                    if (latestDate != null) {
                        return latestDate.toLocalDate();
                    }
                }
            }
        }

        return null;
    }

    private void displayInsufficientDataMessage() {
        Platform.runLater(() -> {
            lblPeriodoCalcolo.setText("Dati insufficienti per calcolare una previsione");
            lblSaldoStimato.setText("N/A");
            lblSaldoStimato.setStyle("-fx-text-fill: #64748b;");

            lblStatusTitolo.setText("Dati Insufficienti");
            lblStatusMessaggio.setText("Inserisci almeno 3 giorni di movimenti per visualizzare una previsione affidabile.");
            lblStatusIcon.setText("⚠️");

            paneStatus.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 12; " +
                    "-fx-border-color: #f59e0b; -fx-border-radius: 12; -fx-border-width: 2;");
            lblStatusTitolo.setStyle("-fx-text-fill: #92400e;");
            lblStatusMessaggio.setStyle("-fx-text-fill: #b45309;");

            lblGiorniRimanenti.setText("--");
            lblMediaSpeseGiornaliera.setText("--");
            lblSpeseProiettateTotali.setText("--");
        });
    }

    private void updateForecastUI(int currentDay, int remainingDays, double mediaGiornaliera,
                                  double speseProiettate, double saldoStimato,
                                  double totaleEntrate, double totaleUscite) {
        Platform.runLater(() -> {
            lblPeriodoCalcolo.setText(String.format("Calcolata sui movimenti reali dal giorno 1 al %d", currentDay));

            lblSaldoStimato.setText(String.format("€ %.2f", saldoStimato));

            lblGiorniRimanenti.setText(String.format("%d gg", remainingDays));
            lblMediaSpeseGiornaliera.setText(String.format("€ %.2f", mediaGiornaliera));
            lblSpeseProiettateTotali.setText(String.format("€ %.2f", speseProiettate));

            String statusIcon;
            String statusTitolo;
            String statusMessaggio;
            String backgroundColor;
            String borderColor;
            String titleColor;
            String messageColor;
            String saldoColor;

            if (saldoStimato > 200) {
                statusIcon = "📈";
                statusTitolo = "Situazione Stabile";
                statusMessaggio = "Previsione positiva. Mantenendo questo trend, chiuderai il mese in attivo.";
                backgroundColor = "#d1fae5";
                borderColor = "#10b981";
                titleColor = "#065f46";
                messageColor = "#047857";
                saldoColor = "#10b981";
            } else if (saldoStimato >= -100 && saldoStimato <= 200) {
                statusIcon = "⚠️";
                statusTitolo = "Attenzione";
                statusMessaggio = "Previsione vicina al limite. Monitora le spese per evitare di chiudere in negativo.";
                backgroundColor = "#fef3c7";
                borderColor = "#f59e0b";
                titleColor = "#92400e";
                messageColor = "#b45309";
                saldoColor = "#f59e0b";
            } else {
                statusIcon = "📉";
                statusTitolo = "Situazione Critica";
                statusMessaggio = "Previsione negativa. Riduci le spese o aumenta le entrate per evitare un deficit.";
                backgroundColor = "#fee2e2";
                borderColor = "#ef4444";
                titleColor = "#991b1b";
                messageColor = "#dc2626";
                saldoColor = "#ef4444";
            }

            lblStatusIcon.setText(statusIcon);
            lblStatusTitolo.setText(statusTitolo);
            lblStatusMessaggio.setText(statusMessaggio);

            paneStatus.setStyle(String.format(
                    "-fx-background-color: %s; -fx-background-radius: 12; " +
                            "-fx-border-color: %s; -fx-border-radius: 12; -fx-border-width: 2;",
                    backgroundColor, borderColor));

            lblStatusTitolo.setStyle("-fx-text-fill: " + titleColor + ";");
            lblStatusMessaggio.setStyle("-fx-text-fill: " + messageColor + ";");
            lblSaldoStimato.setStyle("-fx-text-fill: " + saldoColor + ";");
        });
    }

    private Connection getConnection() throws SQLException {
        it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings settings =
                it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }
}