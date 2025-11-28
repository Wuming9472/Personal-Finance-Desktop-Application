package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BudgetController {

    private MainApp mainApp;
    private final BudgetDAOMySQLImpl budgetDAO = new BudgetDAOMySQLImpl();

    private List<Budget> currentBudgets;
    private int currentUserId = -1;
    private int currentMonth;
    private int currentYear;

    // ====== CARD ALIMENTARI ======
    @FXML private Label foodCategoryLabel;
    @FXML private Label foodRemainingLabel;
    @FXML private Label foodPercentageLabel;
    @FXML private Label foodSpentLabel;
    @FXML private Label foodLimitLabel;
    @FXML private ProgressBar foodProgressBar;

    // ====== CARD TRASPORTI ======
    @FXML private Label transportCategoryLabel;
    @FXML private Label transportRemainingLabel;
    @FXML private Label transportPercentageLabel;
    @FXML private Label transportSpentLabel;
    @FXML private Label transportLimitLabel;
    @FXML private ProgressBar transportProgressBar;

    // ====== CARD SVAGO ======
    @FXML private Label leisureCategoryLabel;
    @FXML private Label leisureRemainingLabel;
    @FXML private Label leisurePercentageLabel;
    @FXML private Label leisureSpentLabel;
    @FXML private Label leisureLimitLabel;
    @FXML private ProgressBar leisureProgressBar;

    // ====== CARD BOLLETTE ======
    @FXML private Label billsCategoryLabel;
    @FXML private Label billsRemainingLabel;
    @FXML private Label billsPercentageLabel;
    @FXML private Label billsSpentLabel;
    @FXML private Label billsLimitLabel;
    @FXML private ProgressBar billsProgressBar;

    @FXML
    private void initialize() {
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue();
        currentYear = now.getYear();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        loadBudgetsForCurrentUser();
    }

    /** Ricarica i budget dal DB */
    private void loadBudgetsForCurrentUser() {
        if (mainApp == null || mainApp.getLoggedUser() == null) return;

        currentUserId = mainApp.getLoggedUser().getUser_id();
        refreshBudgetsFromDb();
    }

    /** Ricarica i budget e aggiorna le card */
    private void refreshBudgetsFromDb() {
        if (currentUserId <= 0) return;

        try {
            currentBudgets = budgetDAO.getBudgetsForMonth(currentUserId, currentMonth, currentYear);

            if (currentBudgets == null || currentBudgets.isEmpty()) {
                System.out.println("Nessun budget trovato, creo limiti predefiniti...");
                createDefaultBudgetsForUser(currentUserId, currentMonth, currentYear);
                currentBudgets = budgetDAO.getBudgetsForMonth(currentUserId, currentMonth, currentYear);
            }

            updateUIFromBudgets();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Errore nel caricamento dei budget", e.getMessage());
        }
    }

    /** Crea limiti di default per le categorie reali dell’utente */
    private void createDefaultBudgetsForUser(int userId, int month, int year) throws SQLException {
        // Imposta limiti predefiniti per le categorie note
        budgetDAO.setOrUpdateBudget(userId, 1, month, year, 400.0); // Alimentari
        budgetDAO.setOrUpdateBudget(userId, 2, month, year, 150.0); // Trasporti
        budgetDAO.setOrUpdateBudget(userId, 3, month, year, 300.0); // Bollette
        budgetDAO.setOrUpdateBudget(userId, 4, month, year, 200.0); // Svago
        budgetDAO.setOrUpdateBudget(userId, 5, month, year, 100.0); // Salute
        budgetDAO.setOrUpdateBudget(userId, 6, month, year, 0.0);   // Stipendio (non serve limite ma lo creiamo)
        budgetDAO.setOrUpdateBudget(userId, 7, month, year, 200.0); // Investimenti
        budgetDAO.setOrUpdateBudget(userId, 8, month, year, 100.0); // Altro

    }

    /** Aggiorna le card grafiche */
    private void updateUIFromBudgets() {

        // Reset iniziale
        resetCard(foodRemainingLabel, foodSpentLabel, foodLimitLabel,
                foodPercentageLabel, foodProgressBar);
        resetCard(transportRemainingLabel, transportSpentLabel, transportLimitLabel,
                transportPercentageLabel, transportProgressBar);
        resetCard(leisureRemainingLabel, leisureSpentLabel, leisureLimitLabel,
                leisurePercentageLabel, leisureProgressBar);
        resetCard(billsRemainingLabel, billsSpentLabel, billsLimitLabel,
                billsPercentageLabel, billsProgressBar);

        if (currentBudgets == null) return;

        for (Budget b : currentBudgets) {
            switch (b.getCategoryId()) {
                case 1 -> { // Alimentari
                    foodCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, foodRemainingLabel, foodSpentLabel,
                            foodLimitLabel, foodPercentageLabel, foodProgressBar);
                }
                case 2 -> { // Trasporti
                    transportCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, transportRemainingLabel, transportSpentLabel,
                            transportLimitLabel, transportPercentageLabel, transportProgressBar);
                }
                case 3 -> { // Bollette
                    billsCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, billsRemainingLabel, billsSpentLabel,
                            billsLimitLabel, billsPercentageLabel, billsProgressBar);
                }
                case 4 -> { // Svago
                    leisureCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, leisureRemainingLabel, leisureSpentLabel,
                            leisureLimitLabel, leisurePercentageLabel, leisureProgressBar);
                }

                default -> { /* altre categorie per ora non visualizzate */ }
            }
        }
    }

    private void resetCard(Label remaining, Label spent, Label limit,
                           Label percentage, ProgressBar bar) {
        if (remaining != null) remaining.setText("Rimasti: € 0.00");
        if (spent != null) spent.setText("Spesi: € 0.00");
        if (limit != null) limit.setText("Limite: € 0.00");
        if (percentage != null) percentage.setText("0%");
        if (bar != null) {
            bar.setProgress(0);
            bar.setStyle("-fx-accent: #10b981;");
        }
    }

    private void updateCardFromBudget(Budget b,
                                      Label remaining, Label spent, Label limit,
                                      Label percentage, ProgressBar bar) {

        double budgetAmount = b.getBudgetAmount();
        double spentAmount = b.getSpentAmount();
        double remainingAmount = b.getRemaining();
        double ratio = b.getProgress();

        double percentageValue = ratio * 100.0;
        double clamped = Math.max(0.0, Math.min(1.0, ratio));

        spent.setText(String.format("Spesi: € %.2f", spentAmount));
        limit.setText(String.format("Limite: € %.2f", budgetAmount));
        remaining.setText(String.format("Rimasti: € %.2f", Math.max(0, remainingAmount)));
        percentage.setText(String.format("%.0f%%", percentageValue));
        bar.setProgress(clamped);

        String color;
        if (ratio < 0.75) color = "#10b981"; // verde
        else if (ratio <= 1.0) color = "#f59e0b"; // giallo
        else color = "#ef4444"; // rosso

        bar.setStyle("-fx-accent: " + color + ";");
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleEditBudget() {
        if (currentBudgets == null || currentBudgets.isEmpty()) {
            showError("Nessun budget trovato",
                    "Non ci sono budget definiti per questo mese.");
            return;
        }

        for (Budget b : currentBudgets) {
            TextInputDialog dialog = new TextInputDialog(
                    String.format("%.2f", b.getBudgetAmount()));
            dialog.setTitle("Imposta limite budget");
            dialog.setHeaderText("Limite per categoria: " + b.getCategoryName());
            dialog.setContentText("Nuovo importo (€):");

            Optional<String> res = dialog.showAndWait();
            if (res.isEmpty()) continue;

            try {
                double newLimit = Double.parseDouble(res.get().replace(",", "."));
                if (newLimit < 0) {
                    showError("Valore non valido", "Il limite deve essere positivo.");
                    continue;
                }

                budgetDAO.setOrUpdateBudget(currentUserId, b.getCategoryId(),
                        currentMonth, currentYear, newLimit);

            } catch (NumberFormatException | SQLException ex) {
                showError("Errore", ex.getMessage());
            }
        }

        refreshBudgetsFromDb();
    }
}
