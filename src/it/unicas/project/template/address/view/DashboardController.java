package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

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

    @FXML
    private void initialize() {
        resetLabels("...");
        if (barChartAndamento != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(1000), barChartAndamento);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
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

            // Aggiorna label mese corrente
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
     * Popola il BarChart con dati aggregati per periodi di 3 giorni.
     * Un mese viene diviso in 10 periodi (1-3, 4-6, 7-9, ..., 28-30/31).
     */
    private void populateBarChart(int userId, int month, int year) throws SQLException {
        barChartAndamento.getData().clear();

        XYChart.Series<String, Number> seriesEntrate = new XYChart.Series<>();
        seriesEntrate.setName("Entrate");

        XYChart.Series<String, Number> seriesUscite = new XYChart.Series<>();
        seriesUscite.setName("Uscite");

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();

        // Query per ottenere somme per ogni giorno
        String query = "SELECT DAY(date) as giorno, " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as entrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as uscite " +
                "FROM movements " +
                "WHERE user_id = ? AND MONTH(date) = ? AND YEAR(date) = ? " +
                "GROUP BY DAY(date) " +
                "ORDER BY giorno";

        // Array per memorizzare i valori giornalieri
        float[] entrateGiornaliere = new float[32]; // indice 1-31
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

        // Aggrega per periodi di 3 giorni (10 periodi totali)
        for (int periodo = 0; periodo < 10; periodo++) {
            int giornoInizio = periodo * 3 + 1;
            int giornoFine = Math.min(giornoInizio + 2, daysInMonth);

            // Se è l'ultimo periodo, includi tutti i giorni rimanenti
            if (periodo == 9) {
                giornoFine = daysInMonth;
            }

            float sommaEntrate = 0;
            float sommaUscite = 0;

            for (int g = giornoInizio; g <= giornoFine; g++) {
                sommaEntrate += entrateGiornaliere[g];
                sommaUscite += usciteGiornaliere[g];
            }

            // Label per il periodo (es. "1-3", "4-6", ecc.)
            String label = giornoInizio + "-" + giornoFine;

            XYChart.Data<String, Number> dataEntrate = new XYChart.Data<>(label, sommaEntrate);
            XYChart.Data<String, Number> dataUscite = new XYChart.Data<>(label, sommaUscite);

            // Tooltip
            final float entrateFinale = sommaEntrate;
            final float usciteFinale = sommaUscite;
            dataEntrate.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(String.format("Entrate: € %.2f", entrateFinale)));
                }
            });
            dataUscite.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(String.format("Uscite: € %.2f", usciteFinale)));
                }
            });

            seriesEntrate.getData().add(dataEntrate);
            seriesUscite.getData().add(dataUscite);
        }

        barChartAndamento.getData().addAll(seriesEntrate, seriesUscite);

        // Applica colori verde/rosso
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : seriesEntrate.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #10b981;");
                }
            }
            for (XYChart.Data<String, Number> data : seriesUscite.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #ef4444;");
                }
            }
        });
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
            barChartAndamento.setAnimated(true);
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
}