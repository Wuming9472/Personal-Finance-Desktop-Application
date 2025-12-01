package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    @FXML private Label lblSaldo;
    @FXML private Label lblEntrate;
    @FXML private Label lblUscite;
    @FXML private Label lblPrevisione;

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis barChartCategoryAxis;
    @FXML private NumberAxis barChartNumberAxis;
    @FXML private VBox boxUltimiMovimenti;

    // --- MODIFICA: Ora usiamo GridPane invece di VBox per i budget ---
    @FXML private GridPane gridBudgetList;

    private MainApp mainApp;

    @FXML
    private void initialize() {
        resetLabels("...");
        if (barChart != null) {
            // Animazione di ingresso per il grafico
            FadeTransition fade = new FadeTransition(Duration.millis(1000), barChart);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        setupBarChartAppearance();
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

            populateBarChartData(userId);

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

    private void populateBarChartData(int userId) throws SQLException {
        if (barChart == null) {
            return;
        }

        String query = "SELECT YEAR(date) as anno, MONTH(date) as mese, " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as entrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as uscite " +
                "FROM movements " +
                "WHERE user_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                "GROUP BY YEAR(date), MONTH(date) " +
                "ORDER BY anno, mese";

        XYChart.Series<String, Number> seriesEntrate = new XYChart.Series<>();
        seriesEntrate.setName("Entrate");

        XYChart.Series<String, Number> seriesUscite = new XYChart.Series<>();
        seriesUscite.setName("Uscite");

        String[] mesi = {"", "Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"};

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int mese = rs.getInt("mese");
                    double entrate = rs.getDouble("entrate");
                    double uscite = rs.getDouble("uscite");

                    String label = mesi[mese];
                    seriesEntrate.getData().add(new XYChart.Data<>(label, entrate));
                    seriesUscite.getData().add(new XYChart.Data<>(label, uscite));
                }
            }
        }

        barChart.getData().clear();
        barChart.getData().addAll(seriesEntrate, seriesUscite);

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

    private void setupBarChartAppearance() {
        if (barChart == null) {
            return;
        }

        barChart.setLegendVisible(true);
        barChart.setAnimated(true);
        barChart.setBarGap(3);
        barChart.setCategoryGap(20);

        if (barChartNumberAxis != null) {
            barChartNumberAxis.setTickLabelFormatter(new StringConverter<Number>() {
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

    private Connection getConnection() throws SQLException {
        DAOMySQLSettings settings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }
}