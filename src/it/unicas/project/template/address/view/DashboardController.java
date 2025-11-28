package it.unicas.project.template.address.view;

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
import java.util.List;

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
        chartAndamento.getData().clear();

        XYChart.Series<String, Number> serieEntrate = new XYChart.Series<>();
        serieEntrate.setName("Entrate");
        XYChart.Series<String, Number> serieUscite = new XYChart.Series<>();
        serieUscite.setName("Uscite");

        try {
            int monthsBack = resolveMonthsBack();
            List<Pair<String, Pair<Float, Float>>> trendData = dao.getIncomeExpenseTrend(userId, monthsBack);

            for (Pair<String, Pair<Float, Float>> point : trendData) {
                String label = point.getKey();
                Float entrata = point.getValue().getKey();
                Float uscita = point.getValue().getValue();

                XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, entrata);
                XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, uscita);

                attachTooltip(incomeData, "Entrate", label, entrata);
                attachTooltip(expenseData, "Uscite", label, uscita);

                serieEntrate.getData().add(incomeData);
                serieUscite.getData().add(expenseData);
            }

            chartAndamento.getData().addAll(serieEntrate, serieUscite);

            // Riabilita l'animazione e applica lo stile ai nodi (punti)
            chartAndamento.setAnimated(true);
            stylizeSeriesNodes(serieEntrate);
            stylizeSeriesNodes(serieUscite);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore caricamento grafico: " + e.getMessage());
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

    private void stylizeSeriesNodes(XYChart.Series<String, Number> series) {
        int index = 0;
        for (XYChart.Data<String, Number> item : series.getData()) {
            final int nodeIndex = index++;
            item.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setOpacity(0);
                    newNode.setScaleX(0);
                    newNode.setScaleY(0);

                    ParallelTransition appear = new ParallelTransition(
                            createFadeTransition(newNode),
                            createScaleTransition(newNode)
                    );
                    appear.setDelay(Duration.millis(100 * nodeIndex));
                    appear.play();
                }
            });
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