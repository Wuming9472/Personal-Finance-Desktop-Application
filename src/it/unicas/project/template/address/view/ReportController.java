package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import it.unicas.project.template.address.view.SmoothAreaChart;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class ReportController {

    private MainApp mainApp;
    private int currentUserId = -1;

    @FXML private PieChart pieChart;
    @FXML private SmoothAreaChart<String, Number> chartAndamento;
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
        if (pieChart != null) {
            pieChart.setLegendVisible(true);
        }
        setupChartAppearance();
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

    /**
     * Metodo pubblico per aggiornare i dati del report.
     * Può essere chiamato da altri controller quando i movimenti vengono modificati.
     */
    public void refreshReportData() {
        updateUIFromData();
    }

    private void updateUIFromData() {
        if (currentUserId <= 0) return;

        try {
            // 1. PieChart - Distribuzione spese per categoria
            loadPieChartData();

            // 2. Line chart - Andamento finanziario
            populateChart();

            // 3. Forecast section - Previsione Fine Mese
            loadForecastData();

        } catch (SQLException e) {
            e.printStackTrace();
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

    private void initRangeSelector() {
        if (cmbRange == null) return;
        cmbRange.getItems().setAll(
                "Ultimo mese",
                "Ultimi 3 mesi",
                "Ultimi 6 mesi",
                "Ultimo anno"
        );
        cmbRange.setValue("Ultimo mese");
        cmbRange.setOnAction(event -> updateUIFromData());
    }

    private int resolveMonthsBack() {
        if (cmbRange == null || cmbRange.getValue() == null) {
            return 1;
        }
        switch (cmbRange.getValue()) {
            case "Ultimi 3 mesi":
                return 3;
            case "Ultimi 6 mesi":
                return 6;
            case "Ultimo anno":
                return 12;
            default:
                return 1;
        }
    }

    private void populateChart() throws SQLException {
        if (chartAndamento == null || currentUserId <= 0) return;

        chartAndamento.setAnimated(false);
        chartAndamento.setCreateSymbols(true);
        chartAndamento.getData().clear();

        XYChart.Series<String, Number> serieEntrate = new XYChart.Series<>();
        serieEntrate.setName("Entrate");
        XYChart.Series<String, Number> serieUscite = new XYChart.Series<>();
        serieUscite.setName("Uscite");

        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        int monthsBack = resolveMonthsBack();
        List<Pair<String, Pair<Float, Float>>> trendData = dao.getIncomeExpenseTrend(currentUserId, monthsBack);

        if (trendData.isEmpty()) {
            return;
        }

        boolean groupByDay = monthsBack == 1;
        float cumulativeEntrate = 0f;
        float cumulativeUscite = 0f;

        for (Pair<String, Pair<Float, Float>> entry : trendData) {
            String label = entry.getKey();
            Pair<Float, Float> values = entry.getValue();
            Float entrata = values.getKey();
            Float uscita = values.getValue();

            String abbreviated = groupByDay ? label : abbreviateLabel(label);

            cumulativeEntrate += entrata;
            cumulativeUscite += uscita;

            if (groupByDay) {
                if (entrata > 0 || uscita > 0) {
                    XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, cumulativeUscite);
                    attachCumulativeTooltip(expenseData, "Uscite", abbreviated, uscita, cumulativeUscite);
                    serieUscite.getData().add(expenseData);

                    XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, cumulativeEntrate);
                    attachCumulativeTooltip(incomeData, "Entrate", abbreviated, entrata, cumulativeEntrate);
                    serieEntrate.getData().add(incomeData);
                }
            } else {
                if (entrata > 0) {
                    XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, cumulativeEntrate);
                    attachCumulativeTooltip(incomeData, "Entrate", abbreviated, entrata, cumulativeEntrate);
                    serieEntrate.getData().add(incomeData);
                }

                if (uscita > 0) {
                    XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, cumulativeUscite);
                    attachCumulativeTooltip(expenseData, "Uscite", abbreviated, uscita, cumulativeUscite);
                    serieUscite.getData().add(expenseData);
                }
            }
        }

        if (!serieEntrate.getData().isEmpty()) {
            chartAndamento.getData().add(serieEntrate);
        }
        if (!serieUscite.getData().isEmpty()) {
            chartAndamento.getData().add(serieUscite);
        }

        Platform.runLater(() -> {
            chartAndamento.setAnimated(true);
            for (XYChart.Series<String, Number> series : chartAndamento.getData()) {
                animateChartDataAppearance(series);
            }
        });
    }

    private void loadForecastData() throws SQLException {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        int currentDay = today.getDayOfMonth();
        int daysInMonth = currentMonth.lengthOfMonth();
        int remainingDays = daysInMonth - currentDay;

        // Query per ottenere i movimenti del mese corrente
        String query = "SELECT " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as totaleEntrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as totaleUscite, " +
                "COUNT(DISTINCT DATE(date)) as giorniConMovimenti " +
                "FROM movements " +
                "WHERE user_id = ? " +
                "AND YEAR(date) = ? " +
                "AND MONTH(date) = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, today.getYear());
            pstmt.setInt(3, today.getMonthValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double totaleEntrate = rs.getDouble("totaleEntrate");
                    double totaleUscite = rs.getDouble("totaleUscite");
                    int giorniConMovimenti = rs.getInt("giorniConMovimenti");

                    // Verifica se ci sono dati sufficienti (almeno 3 giorni con movimenti)
                    if (giorniConMovimenti < 3) {
                        displayInsufficientDataMessage();
                        return;
                    }

                    // Calcola la media giornaliera delle spese
                    double mediaGiornaliera = totaleUscite / currentDay;

                    // Proietta le spese totali per il mese
                    double speseProiettate = totaleUscite + (mediaGiornaliera * remainingDays);

                    // Calcola il saldo stimato (assumendo che le entrate non cambino)
                    double saldoStimato = totaleEntrate - speseProiettate;

                    // Aggiorna l'UI
                    updateForecastUI(currentDay, remainingDays, mediaGiornaliera, speseProiettate,
                                   saldoStimato, totaleEntrate, totaleUscite);
                } else {
                    displayInsufficientDataMessage();
                }
            }
        }
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
            // Aggiorna periodo di calcolo
            lblPeriodoCalcolo.setText(String.format("Calcolata sui movimenti reali dal giorno 1 al %d", currentDay));

            // Aggiorna saldo stimato
            lblSaldoStimato.setText(String.format("€ %.2f", saldoStimato));

            // Aggiorna dettagli
            lblGiorniRimanenti.setText(String.format("%d gg", remainingDays));
            lblMediaSpeseGiornaliera.setText(String.format("€ %.2f", mediaGiornaliera));
            lblSpeseProiettateTotali.setText(String.format("€ %.2f", speseProiettate));

            // Determina lo stato e i colori
            String statusIcon;
            String statusTitolo;
            String statusMessaggio;
            String backgroundColor;
            String borderColor;
            String titleColor;
            String messageColor;
            String saldoColor;

            // Logica per determinare lo stato
            if (saldoStimato > 200) {
                // Verde - Situazione positiva
                statusIcon = "📈";
                statusTitolo = "Situazione Stabile";
                statusMessaggio = "Previsione positiva. Mantenendo questo trend, chiuderai il mese in attivo.";
                backgroundColor = "#d1fae5";
                borderColor = "#10b981";
                titleColor = "#065f46";
                messageColor = "#047857";
                saldoColor = "#10b981";
            } else if (saldoStimato >= -100 && saldoStimato <= 200) {
                // Arancione - Situazione limite
                statusIcon = "⚠️";
                statusTitolo = "Attenzione";
                statusMessaggio = "Previsione vicina al limite. Monitora le spese per evitare di chiudere in negativo.";
                backgroundColor = "#fef3c7";
                borderColor = "#f59e0b";
                titleColor = "#92400e";
                messageColor = "#b45309";
                saldoColor = "#f59e0b";
            } else {
                // Rosso - Situazione critica
                statusIcon = "📉";
                statusTitolo = "Situazione Critica";
                statusMessaggio = "Previsione negativa. Riduci le spese o aumenta le entrate per evitare un deficit.";
                backgroundColor = "#fee2e2";
                borderColor = "#ef4444";
                titleColor = "#991b1b";
                messageColor = "#dc2626";
                saldoColor = "#ef4444";
            }

            // Applica gli stili
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

    private void attachCumulativeTooltip(XYChart.Data<String, Number> data, String seriesName,
                                         String label, Float dailyValue, Float cumulativeValue) {
        String tooltipText = String.format("%s\n%s: € %.2f\nTotale: € %.2f",
                label, seriesName, dailyValue, cumulativeValue);
        Tooltip tooltip = new CustomTooltip(tooltipText);
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip.install(newNode, tooltip);
            }
        });
    }

    private void setupChartAppearance() {
        if (chartAndamento == null) {
            return;
        }

        chartAndamento.setLegendVisible(true);
        chartAndamento.setCreateSymbols(true);
        chartAndamento.setAnimated(true);
        chartAndamento.setPadding(new javafx.geometry.Insets(0));

        if (chartAndamento.getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) chartAndamento.getXAxis();
            xAxis.setTickLabelRotation(0);
            xAxis.setStartMargin(0);
            xAxis.setEndMargin(0);
            xAxis.setGapStartAndEnd(false);
        }

        if (chartAndamento.getYAxis() instanceof NumberAxis) {
            NumberAxis yAxis = (NumberAxis) chartAndamento.getYAxis();
            yAxis.setTickLabelFormatter(new StringConverter<Number>() {
                @Override
                public String toString(Number n) {
                    return String.format("€%.0f", n.doubleValue());
                }

                @Override
                public Number fromString(String s) {
                    return null;
                }
            });
        }
    }

    private void animateChartDataAppearance(XYChart.Series<String, Number> series) {
        int delay = 0;
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setOpacity(0);
                data.getNode().setScaleX(0.8);
                data.getNode().setScaleY(0.8);
            }

            final int currentDelay = delay;
            PauseTransition pause = new PauseTransition(Duration.millis(currentDelay));
            pause.setOnFinished(event -> {
                if (data.getNode() != null) {
                    ParallelTransition transition = new ParallelTransition(
                            createFadeTransition(data.getNode()),
                            createScaleTransition(data.getNode())
                    );
                    transition.play();
                }
            });
            pause.play();
            delay += 50;
        }
    }

    private FadeTransition createFadeTransition(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(600), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        return fade;
    }

    private ScaleTransition createScaleTransition(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), node);
        scale.setFromX(0);
        scale.setFromY(0);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        return scale;
    }

    private String abbreviateLabel(String label) {
        if (label == null) {
            return "";
        }
        String trimmed = label.trim();
        return trimmed.length() > 3 ? trimmed.substring(0, 3) : trimmed;
    }

    private static class CustomTooltip extends Tooltip {
        public CustomTooltip(String text) {
            super(text);
            this.getStyleClass().add("chart-tooltip");
            this.setShowDelay(Duration.millis(50));
            this.setHideDelay(Duration.millis(50));
            this.setAutoHide(true);
        }
    }

    private Connection getConnection() throws SQLException {
        // Usa le stesse impostazioni del tuo DAO esistente
        it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings settings =
                it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }
}