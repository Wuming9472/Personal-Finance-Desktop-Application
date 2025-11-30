package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane; // Importante per la griglia
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

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    @FXML private Label lblSaldo;
    @FXML private Label lblEntrate;
    @FXML private Label lblUscite;
    @FXML private Label lblPrevisione;

    @FXML private SmoothAreaChart<String, Number> chartAndamento;

    @FXML private ComboBox<String> cmbRange;
    @FXML private VBox boxUltimiMovimenti;

    // --- MODIFICA: Ora usiamo GridPane invece di VBox per i budget ---
    @FXML private GridPane gridBudgetList;

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

            // Calcolo Previsione (Semplice)
            calculateForecast(saldo, totalEntrate, totalUscite, now);

            List<Movimenti> recent = dao.selectLastByUser(userId, 5);
            populateRecentMovements(recent);

            // Popola Budget (Metodo Aggiornato per Card View)
            populateBudgetStatus(userId, now.getMonthValue(), now.getYear());

            populateChart(dao, userId);

        } catch (Exception e) {
            e.printStackTrace();
            lblSaldo.setText("Err DB");
        }
    }

    // =================================================================================
    // LOGICA BUDGET: CARD VIEW (GRIGLIA COLORATA)
    // =================================================================================
    private void populateBudgetStatus(int userId, int month, int year) {
        if (gridBudgetList == null) return;
        gridBudgetList.getChildren().clear();

        BudgetDAOMySQLImpl budgetDao = new BudgetDAOMySQLImpl();
        List<Budget> budgetList;

        try {
            budgetList = budgetDao.getBudgetsForMonth(userId, month, year);
        } catch (SQLException e) {
            e.printStackTrace();
            gridBudgetList.add(new Label("Errore DB"), 0, 0);
            return;
        }

        if (budgetList.isEmpty()) {
            Label lbl = new Label("Nessun budget impostato.");
            lbl.setTextFill(Color.GRAY);
            lbl.setFont(Font.font("System", 12));
            gridBudgetList.add(lbl, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;

        for (Budget b : budgetList) {
            // --- 1. Calcoli ---
            double progress = b.getProgress();
            double remaining = b.getBudgetAmount() - b.getSpentAmount();
            boolean isOver = remaining < 0;

            // --- 2. Definizione Colori (Stile Card "Pastello" come foto) ---
            String bgColor, accentColor, textColor;

            if (progress >= 1.0) {
                // ROSSO (Sforato/Critico)
                bgColor = "#fff1f2"; // Rosa chiarissimo
                accentColor = "#e11d48"; // Rosso acceso
                textColor = "#be123c";
            } else if (progress > 0.80) {
                // GIALLO (Attenzione)
                bgColor = "#fffbeb"; // Giallo chiarissimo
                accentColor = "#d97706"; // Ambra
                textColor = "#b45309";
            } else {
                // VERDE (Safe)
                bgColor = "#ecfdf5"; // Verde chiarissimo
                accentColor = "#059669"; // Smeraldo
                textColor = "#047857";
            }

            // --- 3. Creazione CARD (VBox) ---
            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            // Stile CSS Inline per il riquadro arrotondato
            card.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-background-radius: 15; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

            // -- RIGA A: Titolo Categoria --
            HBox topRow = new HBox();
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label lblName = new Label(b.getCategoryName());
            lblName.setFont(Font.font("System", FontWeight.BOLD, 15));
            lblName.setTextFill(Color.web("#1e293b")); // Grigio scuro

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            topRow.getChildren().addAll(lblName, spacer);

            // Se sforato, icona di warning
            if (isOver || progress > 0.9) {
                Label icon = new Label("!");
                icon.setFont(Font.font("System", FontWeight.BOLD, 14));
                icon.setTextFill(Color.web(accentColor));
                icon.setStyle("-fx-border-color: " + accentColor + "; -fx-border-radius: 10; -fx-padding: 0 5 0 5;");
                topRow.getChildren().add(icon);
            }

            // -- RIGA B: Dettagli Spesi / Rimanenti --
            HBox detailsRow = new HBox(10);
            detailsRow.setAlignment(Pos.CENTER_LEFT);

            Label lblSpesi = new Label("Spesi: €" + String.format("%.0f", b.getSpentAmount()));
            lblSpesi.setTextFill(Color.web("#64748b")); // Grigio medio
            lblSpesi.setFont(Font.font("System", 12));

            // Logica per testo "Left" o "Over"
            String leftText = isOver ? "Over: €" + String.format("%.0f", Math.abs(remaining)) : "Left: €" + String.format("%.0f", remaining);
            Label lblLeft = new Label(leftText);
            lblLeft.setTextFill(Color.web(accentColor)); // Colore dinamico
            lblLeft.setFont(Font.font("System", FontWeight.BOLD, 12));

            detailsRow.getChildren().addAll(lblSpesi, lblLeft);

            // -- RIGA C: Progress Bar Sottile --
            ProgressBar pb = new ProgressBar(progress > 1.0 ? 1.0 : progress);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.setPrefHeight(6); // Molto sottile
            // Colora la barra interna e nasconde il bordo
            pb.setStyle("-fx-accent: " + accentColor + "; " +
                    "-fx-control-inner-background: rgba(0,0,0,0.05); " +
                    "-fx-text-box-border: transparent; -fx-background-insets: 0;");

            // Assemblaggio Card
            card.getChildren().addAll(topRow, detailsRow, pb);

            // Aggiunta alla Griglia
            gridBudgetList.add(card, column, row);

            // Gestione colonne (Max 2 per riga)
            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
    }

    // =================================================================================
    // ALTRE FUNZIONI (Chart, Forecast, Movimenti) - INVARIATE
    // =================================================================================

    private void calculateForecast(float saldoAttuale, float entrate, float uscite, LocalDate now) {
        if (lblPrevisione == null) return;
        int daysInMonth = now.lengthOfMonth();
        int currentDay = now.getDayOfMonth();
        if (currentDay == 0) currentDay = 1;

        float netto = entrate - uscite;
        float proiezione = (netto / currentDay) * daysInMonth;
        float previsioneFinale = saldoAttuale + (proiezione - netto);

        lblPrevisione.setText(String.format("€ %.2f", previsioneFinale));
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

            boolean entrataStarted = false;
            boolean uscitaStarted = false;
            boolean bothStarted = false;

            float cumulativeEntrate = 0f;
            float cumulativeUscite = 0f;

            for (Pair<String, Pair<Float, Float>> point : trendData) {
                String label = point.getKey();
                String abbreviated = abbreviateLabel(label);
                Float entrata = point.getValue().getKey();
                Float uscita = point.getValue().getValue();

                if (entrata > 0) entrataStarted = true;
                if (uscita > 0) uscitaStarted = true;

                if (entrataStarted && uscitaStarted && !bothStarted) {
                    bothStarted = true;
                }

                cumulativeEntrate += entrata;
                cumulativeUscite += uscita;

                if (!bothStarted) {
                    if (entrataStarted) {
                        XYChart.Data<String, Number> incomeData = new XYChart.Data<>(label, cumulativeEntrate);
                        attachCumulativeTooltip(incomeData, "Entrate", abbreviated, entrata, cumulativeEntrate);
                        serieEntrate.getData().add(incomeData);

                        XYChart.Data<String, Number> expenseData = new XYChart.Data<>(label, cumulativeUscite);
                        attachCumulativeTooltip(expenseData, "Uscite", abbreviated, uscita, cumulativeUscite);
                        serieUscite.getData().add(expenseData);
                    } else if (uscitaStarted) {
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

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore caricamento grafico: " + e.getMessage());
        }
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