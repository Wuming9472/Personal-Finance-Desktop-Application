package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.MovimentiDAOMySQLImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ReportController {

    private MainApp mainApp;
    private int currentUserId = -1;

    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
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
    @FXML private Label lblMediaSpeseGiornaliera;
    @FXML private Label lblSpeseProiettateTotali;
    @FXML private Label lblDisclaimer;

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

    /**
     * Metodo pubblico per aggiornare i dati del report.
     * Può essere chiamato da altri controller quando i movimenti vengono modificati.
     */
    public void refreshReportData() {
        updateUIFromData();
    }

    private void updateUIFromData() {
        if (currentUserId <= 0) return;

        try {
            // 1. PieChart - Distribuzione spese per categoria
            loadPieChartData();

            // 2. BarChart - Confronto mensile ultimi 6 mesi
            loadBarChartData();

            // 3. Forecast section - Previsione Fine Mese
            loadForecastData();

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

    private void loadForecastData() throws SQLException {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        int currentDay = today.getDayOfMonth();
        int daysInMonth = currentMonth.lengthOfMonth();
        int remainingDays = daysInMonth - currentDay;

        // Query per ottenere i movimenti del mese corrente
        String query = "SELECT " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as totaleEntrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as totaleUscite, " +
                "COUNT(DISTINCT DATE(date)) as giorniConMovimenti " +
                "FROM movements " +
                "WHERE user_id = ? " +
                "AND YEAR(date) = ? " +
                "AND MONTH(date) = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, today.getYear());
            pstmt.setInt(3, today.getMonthValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double totaleEntrate = rs.getDouble("totaleEntrate");
                    double totaleUscite = rs.getDouble("totaleUscite");
                    int giorniConMovimenti = rs.getInt("giorniConMovimenti");

                    // Verifica se ci sono dati sufficienti (almeno 3 giorni con movimenti)
                    if (giorniConMovimenti < 3) {
                        displayInsufficientDataMessage();
                        return;
                    }

                    // Calcola la media giornaliera delle spese
                    double mediaGiornaliera = totaleUscite / currentDay;

                    // Proietta le spese totali per il mese
                    double speseProiettate = totaleUscite + (mediaGiornaliera * remainingDays);

                    // Calcola il saldo stimato (assumendo che le entrate non cambino)
                    double saldoStimato = totaleEntrate - speseProiettate;

                    // Aggiorna l'UI
                    updateForecastUI(currentDay, remainingDays, mediaGiornaliera, speseProiettate,
                                   saldoStimato, totaleEntrate, totaleUscite);
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
            lblStatusMessaggio.setText("Inserisci almeno 3 giorni di movimenti per visualizzare una previsione affidabile.");
            lblStatusIcon.setText("⚠️");

            paneStatus.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 12; " +
                              "-fx-border-color: #f59e0b; -fx-border-radius: 12; -fx-border-width: 2;");
            lblStatusTitolo.setStyle("-fx-text-fill: #92400e;");
            lblStatusMessaggio.setStyle("-fx-text-fill: #b45309;");

            lblGiorniRimanenti.setText("--");
            lblMediaSpeseGiornaliera.setText("--");
            lblSpeseProiettateTotali.setText("--");
        });
    }

    private void updateForecastUI(int currentDay, int remainingDays, double mediaGiornaliera,
                                  double speseProiettate, double saldoStimato,
                                  double totaleEntrate, double totaleUscite) {
        Platform.runLater(() -> {
            // Aggiorna periodo di calcolo
            lblPeriodoCalcolo.setText(String.format("Calcolata sui movimenti reali dal giorno 1 al %d", currentDay));

            // Aggiorna saldo stimato
            lblSaldoStimato.setText(String.format("€ %.2f", saldoStimato));

            // Aggiorna dettagli
            lblGiorniRimanenti.setText(String.format("%d gg", remainingDays));
            lblMediaSpeseGiornaliera.setText(String.format("€ %.2f", mediaGiornaliera));
            lblSpeseProiettateTotali.setText(String.format("€ %.2f", speseProiettate));

            // Determina lo stato e i colori
            String statusIcon;
            String statusTitolo;
            String statusMessaggio;
            String backgroundColor;
            String borderColor;
            String titleColor;
            String messageColor;
            String saldoColor;

            // Logica per determinare lo stato
            if (saldoStimato > 200) {
                // Verde - Situazione positiva
                statusIcon = "📈";
                statusTitolo = "Situazione Stabile";
                statusMessaggio = "Previsione positiva. Mantenendo questo trend, chiuderai il mese in attivo.";
                backgroundColor = "#d1fae5";
                borderColor = "#10b981";
                titleColor = "#065f46";
                messageColor = "#047857";
                saldoColor = "#10b981";
            } else if (saldoStimato >= -100 && saldoStimato <= 200) {
                // Arancione - Situazione limite
                statusIcon = "⚠️";
                statusTitolo = "Attenzione";
                statusMessaggio = "Previsione vicina al limite. Monitora le spese per evitare di chiudere in negativo.";
                backgroundColor = "#fef3c7";
                borderColor = "#f59e0b";
                titleColor = "#92400e";
                messageColor = "#b45309";
                saldoColor = "#f59e0b";
            } else {
                // Rosso - Situazione critica
                statusIcon = "📉";
                statusTitolo = "Situazione Critica";
                statusMessaggio = "Previsione negativa. Riduci le spese o aumenta le entrate per evitare un deficit.";
                backgroundColor = "#fee2e2";
                borderColor = "#ef4444";
                titleColor = "#991b1b";
                messageColor = "#dc2626";
                saldoColor = "#ef4444";
            }

            // Applica gli stili
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
        // Usa le stesse impostazioni del tuo DAO esistente
        it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings settings =
                it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }
}