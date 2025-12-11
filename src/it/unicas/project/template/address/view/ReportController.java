package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.animation.*;
import it.unicas.project.template.address.util.ForecastCalculator;
import javafx.scene.Cursor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;
import it.unicas.project.template.address.util.ForecastQueryProvider;

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
    @FXML private Label lblEntrateMese;
    @FXML private Label lblMediaSpeseGiornaliera;
    @FXML private Label lblSpeseProiettateTotali;
    @FXML private Label lblDisclaimer;

    @FXML
    private void initialize() {
        if (lineChartAndamento != null) {
            lineChartAndamento.setAnimated(false); // Usiamo animazione custom
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
            loadPieChartData();
            loadLineChartData();
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
        double totalAmount = 0.0;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String categoria = rs.getString("name");
                    double totale = rs.getDouble("totale");
                    pieData.add(new PieChart.Data(categoria, totale));
                    totalAmount += totale;
                }
            }
        }

        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Nessuna spesa", 1));
            totalAmount = 1.0;
        }

        pieChart.setData(pieData);

        // Aggiungi tooltip e animazione hover per ogni fetta
        final double total = totalAmount;
        for (PieChart.Data data : pieData) {
            Node node = data.getNode();
            if (node != null) {
                setupSliceInteraction(node, data, total);
            } else {
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        setupSliceInteraction(newNode, data, total);
                    }
                });
            }
        }

        animatePieChart();
    }

    /**
     * Configura l'interazione per ogni fetta del grafico a torta:
     * - Tooltip con categoria, importo e percentuale
     * - Animazione hover delicata che evidenzia la fetta
     */
    private void setupSliceInteraction(Node node, PieChart.Data data, double totalAmount) {
        // Calcola la percentuale
        double percentage = (data.getPieValue() / totalAmount) * 100;

        // Crea il tooltip con importo e percentuale
        Tooltip tooltip = new Tooltip(String.format("%s\n€ %.2f (%.1f%%)",
                data.getName(),
                data.getPieValue(),
                percentage));
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setStyle("-fx-font-size: 13px; -fx-padding: 8px;");
        Tooltip.install(node, tooltip);

        // Crea l'effetto ombra per il sollevamento
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(15);
        dropShadow.setOffsetY(3);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        // Animazione hover: leggero ingrandimento e ombra
        node.setOnMouseEntered(e -> {
            // Salva la scala originale se non è già salvata
            if (node.getUserData() == null) {
                node.setUserData(new double[]{node.getScaleX(), node.getScaleY()});
            }

            // Effetto ombra per dare profondità
            node.setEffect(dropShadow);

            // Leggero ingrandimento (solo 3%)
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), node);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            scaleUp.setInterpolator(Interpolator.EASE_OUT);
            scaleUp.play();

            // Cambia il cursore per indicare che è interattivo
            node.setCursor(javafx.scene.Cursor.HAND);
        });

        node.setOnMouseExited(e -> {
            // Rimuovi l'ombra
            node.setEffect(null);

            // Ripristina la dimensione originale
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), node);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.setInterpolator(Interpolator.EASE_IN);
            scaleDown.play();

            // Ripristina il cursore
            node.setCursor(javafx.scene.Cursor.DEFAULT);
        });
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
            return;
        }

        for (Pair<String, Pair<Float, Float>> point : trendData) {
            String label = point.getKey();
            Float entrata = point.getValue().getKey();
            Float uscita = point.getValue().getValue();

            XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, entrata);
            XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, uscita);

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

        if (lineChartAndamento.getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) lineChartAndamento.getXAxis();
            xAxis.setTickLabelRotation(-45);
        }

        // Avvia animazione da sinistra a destra
        Platform.runLater(() -> animateChartReveal());
    }

    /**
     * Anima il grafico rivelando progressivamente da sinistra a destra
     */
    private void animateChartReveal() {
        if (lineChartAndamento == null) return;

        // Aspetta che il layout sia completo
        Platform.runLater(() -> {
            try {
                Region plotBackground = (Region) lineChartAndamento.lookup(".chart-plot-background");
                if (plotBackground == null || plotBackground.getParent() == null) {
                    return;
                }

                Node plotArea = lineChartAndamento.lookup(".plot-content");
                // Limitiamo il clip solo all'area del grafico, non agli assi
                if (plotArea == null) {
                    plotArea = findPlotArea(plotBackground);
                }
                if (plotArea == null) {
                    plotArea = plotBackground;
                }

                Rectangle clip = new Rectangle();
                clip.widthProperty().set(0);
                clip.heightProperty().bind(plotBackground.heightProperty());

                if (plotArea instanceof Region) {
                    clip.heightProperty().bind(((Region) plotArea).heightProperty());
                }

                plotArea.setClip(clip);

                double targetWidth = plotBackground.getWidth();
                if (targetWidth <= 0) {
                    targetWidth = plotBackground.getBoundsInParent().getWidth();
                }

                Timeline timeline = new Timeline();

                KeyValue kv = new KeyValue(clip.widthProperty(), targetWidth, Interpolator.EASE_OUT);
                KeyFrame kf = new KeyFrame(Duration.millis(1200), kv);

                timeline.getKeyFrames().add(kf);

                Node finalPlotArea = plotArea;
                timeline.setOnFinished(e -> finalPlotArea.setClip(null));

                // Piccolo delay per assicurarsi che il grafico sia renderizzato
                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(e -> timeline.play());
                pause.play();

            } catch (Exception e) {
                // Se qualcosa va storto, assicurati che il grafico sia visibile
                lineChartAndamento.setClip(null);
            }
        });
    }

    private Node findPlotArea(Node startNode) {
        Node current = startNode;
        while (current != null) {
            if (current.getStyleClass().contains("chart-plot-area")) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Anima il grafico a torta facendo "sbocciare" ogni fetta con un leggero rimbalzo
     * e una piccola oscillazione per dare un effetto fiera.
     */
    private void animatePieChart() {
        if (pieChart == null) return;

        ObservableList<PieChart.Data> data = pieChart.getData();
        if (data == null || data.isEmpty()) return;

        Platform.runLater(() -> {
            final double delayIncrement = 90; // ms tra una fetta e l'altra

            for (int i = 0; i < data.size(); i++) {
                PieChart.Data slice = data.get(i);
                double delay = i * delayIncrement;

                Node node = slice.getNode();
                if (node == null) {
                    final int index = i;
                    slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            playSliceAnimation(newNode, index * delayIncrement);
                        }
                    });
                } else {
                    playSliceAnimation(node, delay);
                }
            }
        });
    }

    private void playSliceAnimation(Node node, double delayMs) {
        node.setScaleX(0.92);
        node.setScaleY(0.92);
        node.setOpacity(0);

        ScaleTransition grow = new ScaleTransition(Duration.millis(420), node);
        grow.setFromX(0.92);
        grow.setFromY(0.92);
        grow.setToX(1.02);
        grow.setToY(1.02);
        grow.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(420), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition settle = new ScaleTransition(Duration.millis(180), node);
        settle.setFromX(1.02);
        settle.setFromY(1.02);
        settle.setToX(1.0);
        settle.setToY(1.0);
        settle.setInterpolator(Interpolator.EASE_OUT);

        SequentialTransition sequence = new SequentialTransition(
                new PauseTransition(Duration.millis(delayMs)),
                new ParallelTransition(grow, fadeIn),
                settle
        );
        sequence.play();
    }

    private void loadForecastData() throws SQLException {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate startOfMonth = currentMonth.atDay(1);

        String query = ForecastQueryProvider.MONTHLY_FORECAST_AGGREGATE;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setDate(2, Date.valueOf(startOfMonth));
            pstmt.setDate(3, Date.valueOf(today));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double totaleEntrate = rs.getDouble("totaleEntrate");
                    double totaleUscite = rs.getDouble("totaleUscite");
                    int giorniConMovimenti = rs.getInt("giorniConMovimenti");

                    ForecastCalculator calculator = new ForecastCalculator();
                    ForecastCalculator.ForecastResult result =
                            calculator.calculateForecast(
                                    totaleEntrate,
                                    totaleUscite,
                                    giorniConMovimenti,
                                    today.getDayOfMonth(),
                                    daysInMonth
                            );

                    if (!result.isValid()) {
                        displayInsufficientDataMessage();
                        return;
                    }

                    updateForecastUI(
                            result.getCurrentDay(),
                            result.getRemainingDays(),
                            result.getDailyExpenseAverage(),      // media spese giornaliera
                            result.getProjectedTotalExpenses(),   // uscite proiettate
                            result.getEstimatedBalance(),         // saldo stimato
                            totaleEntrate,
                            totaleUscite
                    );
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
            lblStatusMessaggio.setText("Inserisci almeno 7 giorni di movimenti per visualizzare una previsione affidabile.");
            lblStatusIcon.setText("⚠");

            paneStatus.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 12; " +
                    "-fx-border-color: #f59e0b; -fx-border-radius: 12; -fx-border-width: 2;");
            lblStatusTitolo.setStyle("-fx-text-fill: #92400e;");
            lblStatusMessaggio.setStyle("-fx-text-fill: #b45309;");

            lblGiorniRimanenti.setText("--");
            lblEntrateMese.setText("--");
            lblMediaSpeseGiornaliera.setText("--");
            lblSpeseProiettateTotali.setText("--");
        });
    }

    private void updateForecastUI(int currentDay, int remainingDays,
                                  double mediaSpeseGiornaliera,
                                  double speseProiettate, double saldoStimato,
                                  double totaleEntrate, double totaleUscite) {
        Platform.runLater(() -> {
            lblPeriodoCalcolo.setText(String.format("Calcolata sui movimenti reali dal giorno 1 al %d", currentDay));

            lblSaldoStimato.setText(String.format("€ %.2f", saldoStimato));

            lblGiorniRimanenti.setText(String.format("%d gg", remainingDays));
            lblEntrateMese.setText(String.format("€ %.2f", totaleEntrate));

            // Uscite: media e proiezione
            lblMediaSpeseGiornaliera.setText(String.format("Uscite: € %.2f", mediaSpeseGiornaliera));
            lblSpeseProiettateTotali.setText(String.format("Uscite: € %.2f", speseProiettate));

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
                statusIcon = "⚠";
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
        return it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings.getConnection();
    }
}