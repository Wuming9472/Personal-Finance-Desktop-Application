package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Budget;
import it.unicas.project.template.address.model.dao.mysql.BudgetDAOMySQLImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
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

    // ====== CARD ALIMENTARI (ID 1) ======
    @FXML private Label foodCategoryLabel;
    @FXML private Label foodRemainingLabel;
    @FXML private Label foodPercentageLabel;
    @FXML private Label foodSpentLabel;
    @FXML private Label foodLimitLabel;
    @FXML private ProgressBar foodProgressBar;

    // ====== CARD TRASPORTI (ID 2) ======
    @FXML private Label transportCategoryLabel;
    @FXML private Label transportRemainingLabel;
    @FXML private Label transportPercentageLabel;
    @FXML private Label transportSpentLabel;
    @FXML private Label transportLimitLabel;
    @FXML private ProgressBar transportProgressBar;

    // ====== CARD SVAGO (ID 4) ======
    @FXML private Label leisureCategoryLabel;
    @FXML private Label leisureRemainingLabel;
    @FXML private Label leisurePercentageLabel;
    @FXML private Label leisureSpentLabel;
    @FXML private Label leisureLimitLabel;
    @FXML private ProgressBar leisureProgressBar;

    // ====== CARD BOLLETTE (ID 3) ======
    @FXML private Label billsCategoryLabel;
    @FXML private Label billsRemainingLabel;
    @FXML private Label billsPercentageLabel;
    @FXML private Label billsSpentLabel;
    @FXML private Label billsLimitLabel;
    @FXML private ProgressBar billsProgressBar;

    // ====== CARD SALUTE (ID 5) ======
    @FXML private Label healthCategoryLabel;
    @FXML private Label healthRemainingLabel;
    @FXML private Label healthPercentageLabel;
    @FXML private Label healthSpentLabel;
    @FXML private Label healthLimitLabel;
    @FXML private ProgressBar healthProgressBar;

    // ====== CARD INVESTIMENTI (ID 7) ======
    @FXML private Label investCategoryLabel;
    @FXML private Label investRemainingLabel;
    @FXML private Label investPercentageLabel;
    @FXML private Label investSpentLabel;
    @FXML private Label investLimitLabel;
    @FXML private ProgressBar investProgressBar;

    // ====== CARD ALTRO (ID 8) ======
    @FXML private Label otherCategoryLabel;
    @FXML private Label otherRemainingLabel;
    @FXML private Label otherPercentageLabel;
    @FXML private Label otherSpentLabel;
    @FXML private Label otherLimitLabel;
    @FXML private ProgressBar otherProgressBar;

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

    private void loadBudgetsForCurrentUser() {
        if (mainApp == null || mainApp.getLoggedUser() == null) return;

        currentUserId = mainApp.getLoggedUser().getUser_id();
        refreshBudgetsFromDb();
    }

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

    private void createDefaultBudgetsForUser(int userId, int month, int year) throws SQLException {
        budgetDAO.setOrUpdateBudget(userId, 1, month, year, 400.0);
        budgetDAO.setOrUpdateBudget(userId, 2, month, year, 150.0);
        budgetDAO.setOrUpdateBudget(userId, 3, month, year, 300.0);
        budgetDAO.setOrUpdateBudget(userId, 4, month, year, 200.0);
        budgetDAO.setOrUpdateBudget(userId, 5, month, year, 100.0);
        budgetDAO.setOrUpdateBudget(userId, 6, month, year, 0.0);
        budgetDAO.setOrUpdateBudget(userId, 7, month, year, 200.0);
        budgetDAO.setOrUpdateBudget(userId, 8, month, year, 100.0);
    }

    private void updateUIFromBudgets() {
        resetCard(foodRemainingLabel, foodSpentLabel, foodLimitLabel, foodPercentageLabel, foodProgressBar);
        resetCard(transportRemainingLabel, transportSpentLabel, transportLimitLabel, transportPercentageLabel, transportProgressBar);
        resetCard(leisureRemainingLabel, leisureSpentLabel, leisureLimitLabel, leisurePercentageLabel, leisureProgressBar);
        resetCard(billsRemainingLabel, billsSpentLabel, billsLimitLabel, billsPercentageLabel, billsProgressBar);
        resetCard(healthRemainingLabel, healthSpentLabel, healthLimitLabel, healthPercentageLabel, healthProgressBar);
        resetCard(investRemainingLabel, investSpentLabel, investLimitLabel, investPercentageLabel, investProgressBar);
        resetCard(otherRemainingLabel, otherSpentLabel, otherLimitLabel, otherPercentageLabel, otherProgressBar);

        if (currentBudgets == null) return;

        for (Budget b : currentBudgets) {
            switch (b.getCategoryId()) {
                case 1 -> {
                    foodCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, foodRemainingLabel, foodSpentLabel, foodLimitLabel, foodPercentageLabel, foodProgressBar);
                }
                case 2 -> {
                    transportCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, transportRemainingLabel, transportSpentLabel, transportLimitLabel, transportPercentageLabel, transportProgressBar);
                }
                case 3 -> {
                    billsCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, billsRemainingLabel, billsSpentLabel, billsLimitLabel, billsPercentageLabel, billsProgressBar);
                }
                case 4 -> {
                    leisureCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, leisureRemainingLabel, leisureSpentLabel, leisureLimitLabel, leisurePercentageLabel, leisureProgressBar);
                }
                case 5 -> {
                    healthCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, healthRemainingLabel, healthSpentLabel, healthLimitLabel, healthPercentageLabel, healthProgressBar);
                    if (b.getProgress() <= 1.0) healthProgressBar.setStyle("-fx-accent: #db2777;");
                }
                case 7 -> {
                    investCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, investRemainingLabel, investSpentLabel, investLimitLabel, investPercentageLabel, investProgressBar);
                    if (b.getProgress() <= 1.0) investProgressBar.setStyle("-fx-accent: #7c3aed;");
                }
                case 8 -> {
                    otherCategoryLabel.setText(b.getCategoryName());
                    updateCardFromBudget(b, otherRemainingLabel, otherSpentLabel, otherLimitLabel, otherPercentageLabel, otherProgressBar);
                    if (b.getProgress() <= 1.0) otherProgressBar.setStyle("-fx-accent: #475569;");
                }
            }
        }
    }

    private void resetCard(Label remaining, Label spent, Label limit, Label percentage, ProgressBar bar) {
        if (remaining != null) remaining.setText("Rimasti: € 0.00");
        if (spent != null) spent.setText("Spesi: € 0.00");
        if (limit != null) limit.setText("Limite: € 0.00");
        if (percentage != null) percentage.setText("0%");
        if (bar != null) {
            bar.setProgress(0);
            bar.setStyle("-fx-accent: #10b981;");
        }
    }

    private void updateCardFromBudget(Budget b, Label remaining, Label spent, Label limit, Label percentage, ProgressBar bar) {
        double budgetAmount = b.getBudgetAmount();
        double spentAmount = b.getSpentAmount();
        double remainingAmount = b.getRemaining();
        double ratio = b.getProgress();

        double percentageValue = ratio * 100.0;
        double clamped = Math.max(0.0, Math.min(1.0, ratio));

        if (spent != null) spent.setText(String.format("Spesi: € %.2f", spentAmount));
        if (limit != null) limit.setText(String.format("Limite: € %.2f", budgetAmount));
        if (remaining != null) remaining.setText(String.format("Rimasti: € %.2f", Math.max(0, remainingAmount)));
        if (percentage != null) percentage.setText(String.format("%.0f%%", percentageValue));

        if (bar != null) {
            bar.setProgress(clamped);
            String color;
            if (ratio < 0.75) color = "#10b981";
            else if (ratio <= 1.0) color = "#f59e0b";
            else color = "#ef4444";
            bar.setStyle("-fx-accent: " + color + ";");
        }
    }

    // ====== MODIFICA SINGOLA CATEGORIA (ICONA MATITA) ======

    @FXML private void handleEditFood() { editSingleBudget(1, "Alimentari"); }
    @FXML private void handleEditTransport() { editSingleBudget(2, "Trasporti"); }
    @FXML private void handleEditBills() { editSingleBudget(3, "Bollette"); }
    @FXML private void handleEditLeisure() { editSingleBudget(4, "Svago"); }
    @FXML private void handleEditHealth() { editSingleBudget(5, "Salute"); }
    @FXML private void handleEditInvest() { editSingleBudget(7, "Investimenti"); }
    @FXML private void handleEditOther() { editSingleBudget(8, "Altro"); }

    private void editSingleBudget(int categoryId, String categoryName) {
        double currentLimit = 0;
        for (Budget b : currentBudgets) {
            if (b.getCategoryId() == categoryId) {
                currentLimit = b.getBudgetAmount();
                break;
            }
        }

        TextInputDialog dialog = new TextInputDialog(String.format("%.2f", currentLimit));
        dialog.setTitle("Modifica Budget");
        dialog.setHeaderText("Imposta limite per " + categoryName);
        dialog.setContentText("Nuovo limite (€):");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(value -> {
            try {
                double newLimit = Double.parseDouble(value.replace(",", "."));

                if (newLimit < 0) {
                    showError("Valore non valido", "Il limite deve essere positivo.");
                    return;
                }

                budgetDAO.setOrUpdateBudget(currentUserId, categoryId, currentMonth, currentYear, newLimit);
                refreshBudgetsFromDb();

            } catch (NumberFormatException e) {
                showError("Valore non valido", "Inserisci un numero valido.");
            } catch (SQLException e) {
                showError("Errore database", e.getMessage());
            }
        });
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Errore");
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}