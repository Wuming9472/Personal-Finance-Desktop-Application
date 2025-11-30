package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReportController {

    private MainApp mainApp;
    private int currentUserId = -1;

    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private Label lblRisparmioStimato;
    @FXML private Label lblCategoriaCritica;

    @FXML
    private void initialize() {
        if (barChart != null) {
            barChart.setAnimated(true);
            barChart.setLegendVisible(true);
        }
        if (pieChart != null) {
            pieChart.setLegendVisible(true);
        }
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

    private void updateUIFromData() {
        if (currentUserId <= 0) return;

        try {
            // 1. PieChart - Distribuzione spese per categoria
            loadPieChartData();

            // 2. BarChart - Confronto mensile ultimi 6 mesi
            loadBarChartData();



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

    private void loadBarChartData() throws SQLException {
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

            pstmt.setInt(1, currentUserId);

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
        barChart.setBarGap(3);
        barChart.setCategoryGap(20);

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
        // Usa le stesse impostazioni del tuo DAO esistente
        it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings settings =
                it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }
}