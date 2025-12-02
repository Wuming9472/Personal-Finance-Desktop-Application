package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController {

    @FXML private Label lblSaldo;
    @FXML private Label lblEntrate;
    @FXML private Label lblUscite;
    @FXML private Label lblPrevisione;
    @FXML private Label lblMeseCorrente;

    @FXML private BarChart<String, Number> barChartAndamento;

    @FXML private VBox boxUltimiMovimenti;
    @FXML private GridPane gridBudgetList;

    private MainApp mainApp;

    // Mappa per memorizzare i valori per ogni periodo (per il tooltip combinato)
    private Map<String, float[]> periodData = new HashMap<>();

    // Popup per tooltip custom
    private Popup customTooltip;
    private VBox tooltipContent;

    @FXML
    private void initialize() {
        resetLabels("...");
        setupCustomTooltip();
    }

    private void setupCustomTooltip() {
        customTooltip = new Popup();
        customTooltip.setAutoHide(true);

        tooltipContent = new VBox(8);
        tooltipContent.setPadding(new Insets(12, 16, 12, 16));
        tooltipContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.15));
        shadow.setRadius(15);
        shadow.setOffsetY(4);
        tooltipContent.setEffect(shadow);

        customTooltip.getContent().add(tooltipContent);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        setupChartAppearance();
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

            calculateForecast(saldo, totalEntrate, totalUscite, now);

            if (lblMeseCorrente != null) {
                String nomeMese = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
                lblMeseCorrente.setText(nomeMese.substring(0, 1).toUpperCase() + nomeMese.substring(1) + " " + now.getYear());
            }

            List<Movimenti> recent = dao.selectLastByUser(userId, 5);
            populateRecentMovements(recent);

            populateBudgetStatus(userId, now.getMonthValue(), now.getYear());

            populateBarChart(userId, now.getMonthValue(), now.getYear());

        } catch (Exception e) {
            e.printStackTrace();
            lblSaldo.setText("Err DB");
        }
    }

    /**
     * Popola il BarChart con animazione delle barre che salgono dal basso
     */
    private void populateBarChart(int userId, int month, int year) throws SQLException {
        barChartAndamento.getData().clear();
        periodData.clear();

        // Disabilita animazione built-in per usare la nostra custom
        barChartAndamento.setAnimated(false);

        XYChart.Series<String, Number> seriesEntrate = new XYChart.Series<>();
        seriesEntrate.setName("Entrate");

        XYChart.Series<String, Number> seriesUscite = new XYChart.Series<>();
        seriesUscite.setName("Uscite");

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();

        String query = "SELECT DAY(date) as giorno, " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as entrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as uscite " +
                "FROM movements " +
                "WHERE user_id = ? AND MONTH(date) = ? AND YEAR(date) = ? " +
                "GROUP BY DAY(date) " +
                "ORDER BY giorno";

        float[] entrateGiornaliere = new float[32];
        float[] usciteGiornaliere = new float[32];

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int giorno = rs.getInt("giorno");
                    entrateGiornaliere[giorno] = rs.getFloat("entrate");
                    usciteGiornaliere[giorno] = rs.getFloat("uscite");
                }
            }
        }

        // Array per memorizzare i valori finali per l'animazione
        float[] entrateFinali = new float[10];
        float[] usciteFinali = new float[10];
        String[] labels = new String[10];

        for (int periodo = 0; periodo < 10; periodo++) {
            int giornoInizio = periodo * 3 + 1;
            int giornoFine = Math.min(giornoInizio + 2, daysInMonth);

            if (periodo == 9) {
                giornoFine = daysInMonth;
            }

            float sommaEntrate = 0;
            float sommaUscite = 0;

            for (int g = giornoInizio; g <= giornoFine; g++) {
                sommaEntrate += entrateGiornaliere[g];
                sommaUscite += usciteGiornaliere[g];
            }

            String label = giornoInizio + "-" + giornoFine;
            labels[periodo] = label;
            entrateFinali[periodo] = sommaEntrate;
            usciteFinali[periodo] = sommaUscite;

            // Salva i dati per il tooltip
            periodData.put(label, new float[]{sommaEntrate, sommaUscite});

            // Inizializza a 0 per l'animazione
            XYChart.Data<String, Number> dataEntrate = new XYChart.Data<>(label, 0);
            XYChart.Data<String, Number> dataUscite = new XYChart.Data<>(label, 0);

            seriesEntrate.getData().add(dataEntrate);
            seriesUscite.getData().add(dataUscite);
        }

        barChartAndamento.getData().addAll(seriesEntrate, seriesUscite);

        // Fissa il range dell'asse Y per evitare scatti durante l'animazione
        NumberAxis yAxis = (NumberAxis) barChartAndamento.getYAxis();
        double maxValue = 0;
        for (float v : entrateFinali) {
            maxValue = Math.max(maxValue, v);
        }
        for (float v : usciteFinali) {
            maxValue = Math.max(maxValue, v);
        }

        double padding = maxValue * 0.1;
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(Math.max(10, maxValue + padding));
        yAxis.setTickUnit(yAxis.getUpperBound() / 5);

        // Applica stili e tooltip dopo che i nodi sono stati creati
        Platform.runLater(() -> {
            // Applica colori
            for (XYChart.Data<String, Number> data : seriesEntrate.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #10b981;");
                    setupBarHoverEffect(data.getNode(), data.getXValue());
                }
            }
            for (XYChart.Data<String, Number> data : seriesUscite.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #ef4444;");
                    setupBarHoverEffect(data.getNode(), data.getXValue());
                }
            }

            // Avvia animazione delle barre
            animateBars(seriesEntrate, seriesUscite, entrateFinali, usciteFinali);
        });
    }

    /**
     * Configura l'effetto hover su una barra con tooltip custom
     */
    private void setupBarHoverEffect(Node node, String period) {
        node.setOnMouseEntered(e -> {
            // Scala la barra
            ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
            st.setToX(1.05);
            st.setToY(1.02);
            st.play();

            // Mostra tooltip custom
            showCustomTooltip(period, e.getScreenX(), e.getScreenY());
        });

        node.setOnMouseExited(e -> {
            // Ripristina scala
            ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();

            // Nascondi tooltip
            customTooltip.hide();
        });

        node.setOnMouseMoved(e -> {
            if (customTooltip.isShowing()) {
                customTooltip.setX(e.getScreenX() + 15);
                customTooltip.setY(e.getScreenY() - 60);
            }
        });
    }

    /**
     * Mostra il tooltip custom con entrate e uscite
     */
    private void showCustomTooltip(String period, double x, double y) {
        float[] values = periodData.get(period);
        if (values == null) return;

        tooltipContent.getChildren().clear();

        // Titolo periodo
        Label titleLabel = new Label("Periodo: " + period);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.web("#1e293b"));

        // Riga Entrate
        HBox entrateRow = new HBox(8);
        entrateRow.setAlignment(Pos.CENTER_LEFT);
        Circle greenDot = new Circle(5, Color.web("#10b981"));
        Label entrateLabel = new Label(String.format("Entrate: € %.2f", values[0]));
        entrateLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        entrateLabel.setTextFill(Color.web("#10b981"));
        entrateRow.getChildren().addAll(greenDot, entrateLabel);

        // Riga Uscite
        HBox usciteRow = new HBox(8);
        usciteRow.setAlignment(Pos.CENTER_LEFT);
        Circle redDot = new Circle(5, Color.web("#ef4444"));
        Label usciteLabel = new Label(String.format("Uscite: € %.2f", values[1]));
        usciteLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        usciteLabel.setTextFill(Color.web("#ef4444"));
        usciteRow.getChildren().addAll(redDot, usciteLabel);

        // Separatore
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #e2e8f0;");

        // Saldo periodo
        float saldo = values[0] - values[1];
        Label saldoLabel = new Label(String.format("Saldo: € %.2f", saldo));
        saldoLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        saldoLabel.setTextFill(saldo >= 0 ? Color.web("#10b981") : Color.web("#ef4444"));

        tooltipContent.getChildren().addAll(titleLabel, entrateRow, usciteRow, separator, saldoLabel);

        customTooltip.setX(x + 15);
        customTooltip.setY(y - 80);

        if (barChartAndamento.getScene() != null && barChartAndamento.getScene().getWindow() != null) {
            customTooltip.show(barChartAndamento.getScene().getWindow());
        }
    }

    /**
     * Anima le barre dal basso verso l'alto
     */
    private void animateBars(XYChart.Series<String, Number> seriesEntrate,
                             XYChart.Series<String, Number> seriesUscite,
                             float[] entrateFinali, float[] usciteFinali) {

        Duration animDuration = Duration.millis(800);

        Timeline timeline = new Timeline();

        for (int i = 0; i < seriesEntrate.getData().size(); i++) {
            final int index = i;
            final XYChart.Data<String, Number> dataEntrata = seriesEntrate.getData().get(i);
            final XYChart.Data<String, Number> dataUscita = seriesUscite.getData().get(i);
            final float targetEntrata = entrateFinali[i];
            final float targetUscita = usciteFinali[i];

            // Delay crescente per effetto onda
            Duration delay = Duration.millis(i * 50);

            // KeyFrame per entrate
            KeyValue kvEntrata = new KeyValue(dataEntrata.YValueProperty(), targetEntrata, Interpolator.EASE_OUT);
            KeyFrame kfEntrata = new KeyFrame(animDuration.add(delay), kvEntrata);

            // KeyFrame per uscite
            KeyValue kvUscita = new KeyValue(dataUscita.YValueProperty(), targetUscita, Interpolator.EASE_OUT);
            KeyFrame kfUscita = new KeyFrame(animDuration.add(delay), kvUscita);

            timeline.getKeyFrames().addAll(kfEntrata, kfUscita);
        }

        timeline.play();
    }

    private Connection getConnection() throws SQLException {
        it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings settings =
                it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }

    private void setupChartAppearance() {
        if (barChartAndamento != null) {
            barChartAndamento.setLegendVisible(true);
            barChartAndamento.setAnimated(false); // Usiamo animazione custom
            barChartAndamento.setBarGap(2);
            barChartAndamento.setCategoryGap(8);
        }
    }

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
            double progress = b.getProgress();
            double remaining = b.getBudgetAmount() - b.getSpentAmount();
            boolean isOver = remaining < 0;

            String bgColor, accentColor, textColor;

            if (progress >= 1.0) {
                bgColor = "#fff1f2";
                accentColor = "#e11d48";
                textColor = "#be123c";
            } else if (progress > 0.80) {
                bgColor = "#fffbeb";
                accentColor = "#d97706";
                textColor = "#b45309";
            } else {
                bgColor = "#ecfdf5";
                accentColor = "#059669";
                textColor = "#047857";
            }

            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-background-radius: 15; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

            HBox topRow = new HBox();
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label lblName = new Label(b.getCategoryName());
            lblName.setFont(Font.font("System", FontWeight.BOLD, 15));
            lblName.setTextFill(Color.web("#1e293b"));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            topRow.getChildren().addAll(lblName, spacer);

            if (isOver || progress > 0.9) {
                Label icon = new Label("!");
                icon.setFont(Font.font("System", FontWeight.BOLD, 14));
                icon.setTextFill(Color.web(accentColor));
                icon.setStyle("-fx-border-color: " + accentColor + "; -fx-border-radius: 10; -fx-padding: 0 5 0 5;");
                topRow.getChildren().add(icon);
            }

            HBox detailsRow = new HBox(10);
            detailsRow.setAlignment(Pos.CENTER_LEFT);

            Label lblSpesi = new Label("Spesi: €" + String.format("%.0f", b.getSpentAmount()));
            lblSpesi.setTextFill(Color.web("#64748b"));
            lblSpesi.setFont(Font.font("System", 12));

            String leftText = isOver ? "Superato di: €" + String.format("%.0f", Math.abs(remaining)) : "Rimasti: €" + String.format("%.0f", remaining);
            Label lblLeft = new Label(leftText);
            lblLeft.setTextFill(Color.web(accentColor));
            lblLeft.setFont(Font.font("System", FontWeight.BOLD, 12));

            detailsRow.getChildren().addAll(lblSpesi, lblLeft);

            ProgressBar pb = new ProgressBar(progress > 1.0 ? 1.0 : progress);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.setPrefHeight(6);
            pb.setStyle("-fx-accent: " + accentColor + "; " +
                    "-fx-control-inner-background: rgba(0,0,0,0.05); " +
                    "-fx-text-box-border: transparent; -fx-background-insets: 0;");

            card.getChildren().addAll(topRow, detailsRow, pb);

            gridBudgetList.add(card, column, row);

            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
    }

    private void calculateForecast(float saldoAttuale, float entrate, float uscite, LocalDate now) {
        if (lblPrevisione == null) return;

        try {
            int userId = mainApp.getLoggedUser().getUser_id();
            int daysInMonth = now.lengthOfMonth();

            String query = "SELECT " +
                    "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as totaleEntrate, " +
                    "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as totaleUscite, " +
                    "COUNT(DISTINCT DATE(date)) as giorniConMovimenti, " +
                    "MAX(DATE(date)) as dataRecente " +
                    "FROM movements " +
                    "WHERE user_id = ? " +
                    "AND YEAR(date) = ? " +
                    "AND MONTH(date) = ?";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setInt(1, userId);
                pstmt.setInt(2, now.getYear());
                pstmt.setInt(3, now.getMonthValue());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double totaleEntrate = rs.getDouble("totaleEntrate");
                        double totaleUscite = rs.getDouble("totaleUscite");
                        int giorniConMovimenti = rs.getInt("giorniConMovimenti");
                        java.sql.Date dataRecenteSQL = rs.getDate("dataRecente");

                        if (giorniConMovimenti < 3 || dataRecenteSQL == null) {
                            lblPrevisione.setText("N/A");
                            return;
                        }

                        LocalDate dataRecente = dataRecenteSQL.toLocalDate();
                        int currentDay = dataRecente.getDayOfMonth();
                        int remainingDays = daysInMonth - currentDay;

                        double mediaSpeseGiornaliera = totaleUscite / currentDay;
                        double mediaEntrateGiornaliera = totaleEntrate / currentDay;

                        double speseProiettate = totaleUscite + (mediaSpeseGiornaliera * remainingDays);
                        double entrateProiettate = totaleEntrate + (mediaEntrateGiornaliera * remainingDays);

                        double saldoStimato = entrateProiettate - speseProiettate;

                        lblPrevisione.setText(String.format("€ %.2f", saldoStimato));
                    } else {
                        lblPrevisione.setText("N/A");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblPrevisione.setText("Errore");
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
}