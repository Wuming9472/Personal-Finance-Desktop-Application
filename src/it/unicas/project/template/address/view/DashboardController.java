package it.unicas.project.template.address.view;

import javafx.animation.PauseTransition;
import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
// IMPORTANTE: Usa SmoothAreaChart invece di AreaChart
import it.unicas.project.template.address.view.SmoothAreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DashboardController {

    @FXML private Label lblSaldo;
    @FXML private Label lblEntrate;
    @FXML private Label lblUscite;
    @FXML private Label lblPrevisione;

    // CAMBIO TIPO QUI:
    @FXML private SmoothAreaChart<String, Number> chartAndamento;

    @FXML private ComboBox<String> cmbRange;
    @FXML private VBox boxUltimiMovimenti;

    private MainApp mainApp;

    @FXML
    private void initialize() {
        resetLabels("...");
        // Animazione di ingresso per il grafico
        FadeTransition fade = new FadeTransition(Duration.millis(1000), chartAndamento);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        setupChartAppearance();
        initRangeSelector();
        refreshDashboardData();
    }

    public void refreshDashboardData() {
        if (mainApp == null || mainApp.getLoggedUser() == null) {
            resetLabels("-");
            return;
        }

        int userId = mainApp.getLoggedUser().getUser_id();
        LocalDate now = LocalDate.now();
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();

        try {
            float totalEntrate = dao.getSumByMonth(userId, now.getMonthValue(), now.getYear(), "Entrata");
            float totalUscite = dao.getSumByMonth(userId, now.getMonthValue(), now.getYear(), "Uscita");
            float saldo = totalEntrate - totalUscite;

            lblEntrate.setText(String.format("€ %.2f", totalEntrate));
            lblUscite.setText(String.format("€ %.2f", totalUscite));
            lblSaldo.setText(String.format("€ %.2f", saldo));
            lblSaldo.setStyle(saldo >= 0 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #ef4444;");

            List<Movimenti> recent = dao.selectLastByUser(userId, 5);
            populateRecentMovements(recent);

            populateChart(dao, userId);

        } catch (Exception e) {
            e.printStackTrace();
            lblSaldo.setText("Err DB");
        }
    }

    private void populateRecentMovements(List<Movimenti> list) {
        boxUltimiMovimenti.getChildren().clear();

        if (list.isEmpty()) {
            Label placeholder = new Label("Nessun movimento recente.");
            placeholder.setTextFill(Color.GRAY);
            boxUltimiMovimenti.getChildren().add(placeholder);
            return;
        }

        for (Movimenti m : list) {
            HBox row = createMovementRow(m);
            boxUltimiMovimenti.getChildren().add(row);
        }
    }

    private HBox createMovementRow(Movimenti m) {
        boolean isExpense = m.getType().equalsIgnoreCase("Uscita") || m.getType().equalsIgnoreCase("Expense");
        Color color = isExpense ? Color.web("#fee2e2") : Color.web("#dcfce7");
        Color iconColor = isExpense ? Color.web("#dc2626") : Color.web("#16a34a");
        String symbol = isExpense ? "↓" : "↑";
        String sign = isExpense ? "- " : "+ ";

        Circle circle = new Circle(18, color);
        Label arrow = new Label(symbol);
        arrow.setTextFill(iconColor);
        arrow.setFont(Font.font("System", FontWeight.BOLD, 16));
        StackPane icon = new StackPane(circle, arrow);

        Label desc = new Label(m.getTitle());
        desc.setTextFill(Color.web("#334155"));
        desc.setFont(Font.font("System", FontWeight.BOLD, 14));
        if(desc.getText().isEmpty()) {
            desc = new Label(m.getCategoryName());
            desc.setTextFill(Color.web("#334155"));
            desc.setFont(Font.font("System", FontWeight.BOLD, 14));
        }

        Label date = new Label(m.getDate().toString());
        date.setTextFill(Color.web("#94a3b8"));
        date.setFont(Font.font("System", 11));

        VBox texts = new VBox(2, desc, date);
        texts.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amount = new Label(sign + String.format("€ %.2f", m.getAmount()));
        amount.setTextFill(iconColor);
        amount.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox row = new HBox(15, icon, texts, spacer, amount);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void resetLabels(String text) {
        if(lblSaldo!=null) lblSaldo.setText(text);
        if(lblEntrate!=null) lblEntrate.setText(text);
        if(lblUscite!=null) lblUscite.setText(text);
    }

    private void initRangeSelector() {
        cmbRange.getItems().setAll(
                "Ultimo mese",
                "Ultimi 3 mesi",
                "Ultimi 6 mesi",
                "Ultimo anno"
        );
        cmbRange.setValue("Ultimo mese");
        cmbRange.setOnAction(event -> refreshDashboardData());
    }

    private int resolveMonthsBack() {
        String selected = cmbRange.getValue();
        if (selected == null) {
            return 1;
        }
        switch (selected) {
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

    private void populateChart(MovimentiDAOMySQLImpl dao, int userId) {
        chartAndamento.setAnimated(false);
        chartAndamento.setCreateSymbols(true);
        chartAndamento.getData().clear();

        XYChart.Series<String, Number> serieEntrate = new XYChart.Series<>();
        serieEntrate.setName("Entrate");
        XYChart.Series<String, Number> serieUscite = new XYChart.Series<>();
        serieUscite.setName("Uscite");

        try {
            int monthsBack = resolveMonthsBack();
            List<Pair<String, Pair<Float, Float>>> trendData = dao.getIncomeExpenseTrend(userId, monthsBack);

            if (trendData.isEmpty()) {
                return;
            }

            // Flag per tracciare quando ogni serie ha il suo primo valore reale
            boolean entrataStarted = false;
            boolean uscitaStarted = false;
            boolean bothStarted = false;

            // Calcolo valori CUMULATIVI
            float cumulativeEntrate = 0f;
            float cumulativeUscite = 0f;

            for (Pair<String, Pair<Float, Float>> point : trendData) {
                String label = point.getKey();
                String abbreviated = abbreviateLabel(label);
                Float entrata = point.getValue().getKey();
                Float uscita = point.getValue().getValue();

                // Aggiorno i flag PRIMA di processare questo punto
                if (entrata > 0) entrataStarted = true;
                if (uscita > 0) uscitaStarted = true;

                // Verifico se da questo momento entrambe le serie hanno dati
                if (entrataStarted && uscitaStarted && !bothStarted) {
                    bothStarted = true;
                }

                // Aggiorno i cumulativi
                cumulativeEntrate += entrata;
                cumulativeUscite += uscita;

                if (!bothStarted) {
                    // === FASE 1: Solo una serie è attiva ===
                    // L'altra serie ha punti a 0 per garantire continuità visiva

                    if (entrataStarted) {
                        // Ho entrate attive
                        XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, cumulativeEntrate);
                        attachCumulativeTooltip(incomeData, "Entrate", abbreviated, entrata, cumulativeEntrate);
                        serieEntrate.getData().add(incomeData);

                        // Aggiungo punto uscita a 0 per continuità (la curva rossa partirà da qui)
                        XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, cumulativeUscite);
                        attachCumulativeTooltip(expenseData, "Uscite", abbreviated, uscita, cumulativeUscite);
                        serieUscite.getData().add(expenseData);
                    } else if (uscitaStarted) {
                        // Ho uscite attive
                        XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, cumulativeUscite);
                        attachCumulativeTooltip(expenseData, "Uscite", abbreviated, uscita, cumulativeUscite);
                        serieUscite.getData().add(expenseData);

                        // Aggiungo punto entrata a 0 per continuità (la curva verde partirà da qui)
                        XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, cumulativeEntrate);
                        attachCumulativeTooltip(incomeData, "Entrate", abbreviated, entrata, cumulativeEntrate);
                        serieEntrate.getData().add(incomeData);
                    }
                } else {
                    // === FASE 2: Entrambe le serie hanno almeno un movimento ===
                    // Le serie sono COMPLETAMENTE INDIPENDENTI
                    // Aggiungo punti SOLO dove ci sono movimenti reali

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

            // Aggiungo le serie solo se hanno almeno un punto
            if (!serieEntrate.getData().isEmpty()) {
                chartAndamento.getData().add(serieEntrate);
            }
            if (!serieUscite.getData().isEmpty()) {
                chartAndamento.getData().add(serieUscite);
            }

            // Animazione
            Platform.runLater(() -> {
                chartAndamento.setAnimated(true);
                for (XYChart.Series<String, Number> series : chartAndamento.getData()) {
                    animateChartDataAppearance(series);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore caricamento grafico: " + e.getMessage());
        }
    }

    /**
     * Tooltip per grafico cumulativo - mostra valore giornaliero e totale
     */
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

    // Aggiungi dopo il metodo populateChart()
    private void setupChartAppearance() {
        chartAndamento.setLegendVisible(true);
        chartAndamento.setCreateSymbols(true);
        chartAndamento.setAnimated(true);

        // Rimuovi padding eccessivo
        chartAndamento.setPadding(new javafx.geometry.Insets(0));

        // Configura l'asse X (CategoryAxis) per rimuovere il gap ai bordi
        if (chartAndamento.getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) chartAndamento.getXAxis();
            xAxis.setTickLabelRotation(0);
            xAxis.setStartMargin(0);  // Rimuove lo spazio a sinistra
            xAxis.setEndMargin(0);    // Rimuove lo spazio a destra
            xAxis.setGapStartAndEnd(false);  // Disabilita il gap automatico ai bordi
        }

        if (chartAndamento.getYAxis() instanceof NumberAxis) {
            NumberAxis yAxis = (NumberAxis) chartAndamento.getYAxis();
            yAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
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

    /**
     * Anima l'ingresso progressivo dei dati del grafico con delay scaglionato
     */
    private void animateChartDataAppearance(XYChart.Series<String, Number> series) {
        int delay = 0;
        for (XYChart.Data<String, Number> data : series.getData()) {
            // Inizia con opacità 0 e scala piccola
            if (data.getNode() != null) {
                data.getNode().setOpacity(0);
                data.getNode().setScaleX(0.8);
                data.getNode().setScaleY(0.8);
            }

            // Crea transizione con delay progressivo
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

            delay += 50; // Incrementa il delay per ogni punto (50ms)
        }
    }

    private void attachTooltip(XYChart.Data<String, Number> data, String seriesName, String label, Float value) {
        Tooltip tooltip = new CustomTooltip(String.format("%s\n%s: € %.2f", label, seriesName, value));
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip.install(newNode, tooltip);
            }
        });
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
}