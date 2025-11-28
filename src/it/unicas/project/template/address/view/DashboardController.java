package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    @FXML private LineChart<String, Number> chartAndamento;
    @FXML private ComboBox<String> cmbRange;

    // Contenitore per la lista dinamica
    @FXML private VBox boxUltimiMovimenti;

    private MainApp mainApp;

    @FXML
    private void initialize() {
        resetLabels("...");
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
            // 1. Aggiorna KPI (Saldo, Entrate, Uscite)
            float totalEntrate = dao.getSumByMonth(userId, now.getMonthValue(), now.getYear(), "Entrata");
            float totalUscite = dao.getSumByMonth(userId, now.getMonthValue(), now.getYear(), "Uscita");
            float saldo = totalEntrate - totalUscite;

            lblEntrate.setText(String.format("€ %.2f", totalEntrate));
            lblUscite.setText(String.format("€ %.2f", totalUscite));
            lblSaldo.setText(String.format("€ %.2f", saldo));
            lblSaldo.setStyle(saldo >= 0 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #ef4444;");

            // 2. Aggiorna Lista Ultimi Movimenti
            List<Movimenti> recent = dao.selectLastByUser(userId, 5); // Ultimi 5
            populateRecentMovements(recent);
            populateChart(dao, userId);

        } catch (Exception e) {
            e.printStackTrace();
            lblSaldo.setText("Err DB");
        }
    }

    /**
     * Crea graficamente le righe della tabella movimenti.
     */
    private void populateRecentMovements(List<Movimenti> list) {
        boxUltimiMovimenti.getChildren().clear(); // Pulisce la lista vecchia

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

    /**
     * Helper per creare una riga grafica.
     */
    private HBox createMovementRow(Movimenti m) {
        boolean isExpense = m.getType().equalsIgnoreCase("Uscita") || m.getType().equalsIgnoreCase("Expense");
        Color color = isExpense ? Color.web("#fee2e2") : Color.web("#dcfce7"); // Sfondo pallino
        Color iconColor = isExpense ? Color.web("#dc2626") : Color.web("#16a34a"); // Colore icona/testo
        String symbol = isExpense ? "↓" : "↑";
        String sign = isExpense ? "- " : "+ ";

        // 1. Icona (Cerchio con freccia)
        Circle circle = new Circle(18, color);
        Label arrow = new Label(symbol);
        arrow.setTextFill(iconColor);
        arrow.setFont(Font.font("System", FontWeight.BOLD, 16));
        StackPane icon = new StackPane(circle, arrow);

        // 2. Testi (Descrizione e Data)
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

        // 3. Spaziatore
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Importo
        Label amount = new Label(sign + String.format("€ %.2f", m.getAmount()));
        amount.setTextFill(iconColor);
        amount.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Assemblaggio HBox
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
        chartAndamento.getData().clear(); // Pulisci dati vecchi

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
            chartAndamento.setAnimated(true);
            stylizeSeries(serieEntrate, "#16a34a");
            stylizeSeries(serieUscite, "#dc2626");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore caricamento grafico: " + e.getMessage());
        }
    }

    private void attachTooltip(XYChart.Data<String, Number> data, String label, String period, Float value) {
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                String.format("%s\n%s: € %.2f", label, period, value)
        );
        tooltip.setShowDelay(Duration.millis(100));
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                javafx.scene.control.Tooltip.install(newNode, tooltip);
            }
        });
    }

    private void stylizeSeries(XYChart.Series<String, Number> series, String colorHex) {
        series.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle(String.format("-fx-stroke: %s; -fx-stroke-width: 2px;", colorHex));
            }
        });

        for (XYChart.Data<String, Number> item : series.getData()) {
            item.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(String.format("-fx-background-color: white, %s;", colorHex));
                    newNode.setScaleX(1.2);
                    newNode.setScaleY(1.2);
                }
            });
        }
    }

}