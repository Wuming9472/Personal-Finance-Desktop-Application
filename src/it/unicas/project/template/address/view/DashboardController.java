package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import it.unicas.project.template.address.util.ForecastQueryProvider;
import javafx.animation.*;
import it.unicas.project.template.address.util.ForecastCalculator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import java.util.function.Function;
import java.util.function.Supplier;
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

    @FXML private AnchorPane cardPrevisione;

    private MainApp mainApp;
    private MovimentiDAOMySQLImpl movimentiDAO = new MovimentiDAOMySQLImpl();
    private BudgetDAOMySQLImpl budgetDAO = new BudgetDAOMySQLImpl();
    private Supplier<it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings> settingsSupplier =
            it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings::getCurrentDAOMySQLSettings;
    private Function<String, Connection> connectionFactory = url -> {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException(e); // o la tua eccezione custom
        }
    };

    // Mappa per memorizzare i valori per ogni periodo (per il tooltip combinato)
    private Map<String, float[]> periodData = new HashMap<>();

    // Popup per tooltip custom
    private Popup customTooltip;
    private VBox tooltipContent;

    // Variabili per la navigazione tra i mesi
    private int selectedMonth;
    private int selectedYear;

    private XYChart.Series<String, Number> currentSeriesEntrate;
    private XYChart.Series<String, Number> currentSeriesUscite;
    private float[] currentEntrateFinali;
    private float[] currentUsciteFinali;
    private boolean hoverResizeListenerInitialized = false;

    @FXML
    private void initialize() {
        resetLabels("...");
        setupCustomTooltip();
        setupHoverAreaResizeListener();
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
        // Inizializza con il mese corrente
        LocalDate now = LocalDate.now();
        this.selectedMonth = now.getMonthValue();
        this.selectedYear = now.getYear();
        setupChartAppearance();
        refreshDashboardData();
    }

    public void setMovimentiDAO(MovimentiDAOMySQLImpl movimentiDAO) {
        this.movimentiDAO = movimentiDAO;
    }

    public void setBudgetDAO(BudgetDAOMySQLImpl budgetDAO) {
        this.budgetDAO = budgetDAO;
    }

    public void setSettingsSupplier(Supplier<it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
    }

    public void setConnectionFactory(Function<String, Connection> connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void refreshDashboardData() {
        if (mainApp == null || mainApp.getLoggedUser() == null) {
            resetLabels("-");
            return;
        }

        int userId = mainApp.getLoggedUser().getUser_id();
        LocalDate now = LocalDate.now();
        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth, 1);
        try {
            float totalEntrate = movimentiDAO.getSumByMonth(userId, selectedMonth, selectedYear, "Entrata");
            float totalUscite = movimentiDAO.getSumByMonth(userId, selectedMonth, selectedYear, "Uscita");
            float saldo = totalEntrate - totalUscite;

            lblEntrate.setText(String.format("€ %.2f", totalEntrate));
            lblUscite.setText(String.format("€ %.2f", totalUscite));
            lblSaldo.setText(String.format("€ %.2f", saldo));
            lblSaldo.setStyle(saldo >= 0 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #ef4444;");

            // Calcola la previsione solo se è il mese corrente
            boolean isCurrentMonth = (selectedMonth == now.getMonthValue() && selectedYear == now.getYear());
            if (cardPrevisione != null) {
                cardPrevisione.setVisible(isCurrentMonth);
                cardPrevisione.setManaged(isCurrentMonth);
            }
            if (isCurrentMonth) {
                calculateForecast(now);
            }

            if (lblMeseCorrente != null) {
                String nomeMese = selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN);
                lblMeseCorrente.setText(nomeMese.substring(0, 1).toUpperCase() + nomeMese.substring(1) + " " + selectedYear);
            }

            List<Movimenti> monthlyMovements = movimentiDAO.selectByUserAndMonthYear(userId, selectedMonth, selectedYear);
            populateRecentMovements(monthlyMovements);

            populateBudgetStatus(userId, selectedMonth, selectedYear);

            populateBarChart(userId, selectedMonth, selectedYear);

        } catch (Exception e) {
            e.printStackTrace();
            lblSaldo.setText("Err DB");
        }
    }

    /**
     * Naviga al mese precedente
     */
    @FXML
    private void handlePreviousMonth() {
        selectedMonth--;
        if (selectedMonth < 1) {
            selectedMonth = 12;
            selectedYear--;
        }
        refreshDashboardData();
    }

    /**
     * Naviga al mese successivo
     */
    @FXML
    private void handleNextMonth() {
        selectedMonth++;
        if (selectedMonth > 12) {
            selectedMonth = 1;
            selectedYear++;
        }
        refreshDashboardData();
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

        currentSeriesEntrate = seriesEntrate;
        currentSeriesUscite = seriesUscite;
        currentEntrateFinali = entrateFinali;
        currentUsciteFinali = usciteFinali;

        // Applica stili e tooltip dopo che i nodi sono stati creati
        Platform.runLater(() -> {
            // Crea liste dei nodi per l'hover unificato

            // Applica colori e raccogli i nodi
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

            refreshHoverAreas();

            // Avvia animazione delle barre
            animateBars(seriesEntrate, seriesUscite, entrateFinali, usciteFinali);
        });
    }

    /**
     * Aggiunge aree di hover trasparenti per ogni coppia di barre
     */
    private void addHoverAreas(java.util.List<Node> entrateNodes, java.util.List<Node> usciteNodes,
                                XYChart.Series<String, Number> seriesEntrate,
                                float[] entrateFinali, float[] usciteFinali) {

        // Trova il Pane che contiene le barre (plotContent)
        Node plotBackground = barChartAndamento.lookup(".chart-plot-background");
        if (plotBackground == null || plotBackground.getParent() == null) return;

        Pane plotContent = (Pane) plotBackground.getParent();
        javafx.geometry.Bounds plotBounds = plotBackground.getBoundsInParent();

        for (int i = 0; i < Math.min(entrateNodes.size(), usciteNodes.size()); i++) {
            Node nodeEntrate = entrateNodes.get(i);
            Node nodeUscite = usciteNodes.get(i);
            String period = seriesEntrate.getData().get(i).getXValue();

            // Crea area di hover solo se ci sono dati (almeno una colonna non è 0)
            if (entrateFinali[i] == 0 && usciteFinali[i] == 0) {
                continue;
            }

            // Converti i bounds delle barre in coordinate relative al plotContent
            javafx.geometry.Bounds entrateInPlot = plotContent.sceneToLocal(
                nodeEntrate.localToScene(nodeEntrate.getBoundsInLocal())
            );
            javafx.geometry.Bounds usciteInPlot = plotContent.sceneToLocal(
                nodeUscite.localToScene(nodeUscite.getBoundsInLocal())
            );

            // Calcola l'area che copre entrambe le colonne più metà del categoryGap ai lati
            double minX = Math.min(entrateInPlot.getMinX(), usciteInPlot.getMinX());
            double maxX = Math.max(entrateInPlot.getMaxX(), usciteInPlot.getMaxX());

            // Aggiungi padding: metà del categoryGap (20/2 = 10) su ogni lato
            double padding = 10;
            minX -= padding;
            maxX += padding;

            // Crea Rectangle che copre l'intera altezza del grafico
            Rectangle hoverArea = new Rectangle();
            hoverArea.setX(minX);
            hoverArea.setY(plotBounds.getMinY());
            hoverArea.setWidth(maxX - minX);
            hoverArea.setHeight(plotBounds.getHeight());

            // Inizialmente trasparente
            hoverArea.setFill(Color.TRANSPARENT);
            hoverArea.setStroke(Color.TRANSPARENT);

            // Porta dietro alle barre
            hoverArea.setMouseTransparent(false);
            hoverArea.toBack();

            hoverArea.getStyleClass().add("hover-area");

            // Applica effetto hover
            setupHoverAreaEffect(hoverArea, nodeEntrate, nodeUscite, period);

            // Aggiungi al plotContent
            plotContent.getChildren().add(hoverArea);
        }
    }

    /**
     * Configura l'effetto hover su un'area di hover
     */
    private void setupHoverAreaEffect(Rectangle hoverArea, Node nodeEntrate, Node nodeUscite, String period) {
        hoverArea.setOnMouseEntered(e -> {
            // Mostra area grigia
            hoverArea.setFill(Color.rgb(0, 0, 0, 0.05));

            // Scala le barre
            ScaleTransition stEntrate = new ScaleTransition(Duration.millis(150), nodeEntrate);
            stEntrate.setToX(1.05);
            stEntrate.setToY(1.02);
            stEntrate.play();

            ScaleTransition stUscite = new ScaleTransition(Duration.millis(150), nodeUscite);
            stUscite.setToX(1.05);
            stUscite.setToY(1.02);
            stUscite.play();

            // Mostra tooltip
            showCustomTooltip(period, e.getScreenX(), e.getScreenY());
        });

        hoverArea.setOnMouseExited(e -> {
            // Nascondi area grigia
            hoverArea.setFill(Color.TRANSPARENT);

            // Ripristina scala barre
            ScaleTransition stEntrate = new ScaleTransition(Duration.millis(150), nodeEntrate);
            stEntrate.setToX(1.0);
            stEntrate.setToY(1.0);
            stEntrate.play();

            ScaleTransition stUscite = new ScaleTransition(Duration.millis(150), nodeUscite);
            stUscite.setToX(1.0);
            stUscite.setToY(1.0);
            stUscite.play();

            // Nascondi tooltip
            customTooltip.hide();
        });

        hoverArea.setOnMouseMoved(e -> {
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
        return DAOMySQLSettings.getConnection();
    }

    private void setupChartAppearance() {
        if (barChartAndamento != null) {
            barChartAndamento.setLegendVisible(true);
            barChartAndamento.setAnimated(false); // Usiamo animazione custom
            barChartAndamento.setBarGap(8);
            barChartAndamento.setCategoryGap(20);
        }
    }

    private void setupHoverAreaResizeListener() {
        if (barChartAndamento == null || hoverResizeListenerInitialized) {
            return;
        }

        hoverResizeListenerInitialized = true;

        ChangeListener<Number> chartSizeListener = (obs, oldVal, newVal) -> refreshHoverAreas();
        barChartAndamento.widthProperty().addListener(chartSizeListener);
        barChartAndamento.heightProperty().addListener(chartSizeListener);

        Platform.runLater(() -> {
            Node plotArea = barChartAndamento.lookup(".chart-plot-background");
            if (plotArea != null) {
                plotArea.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> refreshHoverAreas());
            }
        });
    }

    private Pane getPlotContent() {
        Node plotArea = barChartAndamento.lookup(".chart-plot-background");
        if (plotArea == null || plotArea.getParent() == null) return null;
        return (Pane) plotArea.getParent();
    }

    private void clearHoverAreas(Pane plotContent) {
        plotContent.getChildren().removeIf(node -> node instanceof Rectangle && node.getStyleClass().contains("hover-area"));
    }

    private void refreshHoverAreas() {
        if (currentSeriesEntrate == null || currentSeriesUscite == null
                || currentEntrateFinali == null || currentUsciteFinali == null) {
            return;
        }

        Platform.runLater(() -> {
            Pane plotContent = getPlotContent();
            if (plotContent == null) {
                return;
            }

            clearHoverAreas(plotContent);

            java.util.List<Node> entrateNodes = new java.util.ArrayList<>();
            java.util.List<Node> usciteNodes = new java.util.ArrayList<>();

            for (XYChart.Data<String, Number> data : currentSeriesEntrate.getData()) {
                if (data.getNode() != null) {
                    entrateNodes.add(data.getNode());
                }
            }

            for (XYChart.Data<String, Number> data : currentSeriesUscite.getData()) {
                if (data.getNode() != null) {
                    usciteNodes.add(data.getNode());
                }
            }

            if (!entrateNodes.isEmpty() && !usciteNodes.isEmpty()) {
                addHoverAreas(entrateNodes, usciteNodes, currentSeriesEntrate, currentEntrateFinali, currentUsciteFinali);
            }
        });
    }

    private void populateBudgetStatus(int userId, int month, int year) {
        if (gridBudgetList == null) return;
        gridBudgetList.getChildren().clear();

        List<Budget> budgetList;

        try {
            budgetList = budgetDAO.getBudgetsForMonth(userId, month, year);
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

    private void calculateForecast(LocalDate today) {
        if (lblPrevisione == null || mainApp == null || mainApp.getLoggedUser() == null) {
            return;
        }

        try {
            int userId = mainApp.getLoggedUser().getUser_id();

            var currentMonth = java.time.YearMonth.from(today);
            int daysInMonth = currentMonth.lengthOfMonth();
            LocalDate startOfMonth = currentMonth.atDay(1);

            String query = ForecastQueryProvider.MONTHLY_FORECAST_AGGREGATE;

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setInt(1, userId);
                pstmt.setDate(2, java.sql.Date.valueOf(startOfMonth));
                pstmt.setDate(3, java.sql.Date.valueOf(today));

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
                            lblPrevisione.setText("N/A");
                            lblPrevisione.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 28px;");
                            return;
                        }

                        double saldoStimato = result.getEstimatedBalance();
                        lblPrevisione.setText(String.format("€ %.2f", saldoStimato));

                        // Colore coerente col segno
                        if (saldoStimato >= 0) {
                            lblPrevisione.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;-fx-font-size: 28px;");
                        } else {
                            lblPrevisione.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 28px;");
                        }

                    } else {
                        lblPrevisione.setText("N/A");
                        lblPrevisione.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 28px;");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lblPrevisione.setText("Errore");
            lblPrevisione.setStyle("-fx-text-fill: #ef4444;");
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