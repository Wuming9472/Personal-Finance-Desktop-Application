package it.unicas.project.template.address.util;

import it.unicas.project.template.address.model.Budget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class per gestire le notifiche di superamento budget.
 * Mostra popup di allarme quando un budget mensile viene superato.
 */
public class BudgetNotificationHelper {

    /**
     * Controlla se ci sono budget superati nella lista fornita e mostra una notifica di allarme.
     * @param budgets Lista di budget da controllare
     * @return true se almeno un budget è stato superato, false altrimenti
     */
    public static boolean checkAndNotifyBudgetExceeded(List<Budget> budgets) {
        if (budgets == null || budgets.isEmpty()) {
            return false;
        }

        List<Budget> exceededBudgets = new ArrayList<>();

        for (Budget b : budgets) {
            // Ignora la categoria Stipendio (ID 6)
            if (b.getCategoryId() == 6) continue;

            // Controlla se il budget è stato superato
            if (b.getSpentAmount() > b.getBudgetAmount() && b.getBudgetAmount() > 0) {
                exceededBudgets.add(b);
            }
        }

        if (!exceededBudgets.isEmpty()) {
            showBudgetExceededAlert(exceededBudgets);
            return true;
        }

        return false;
    }

    /**
     * Mostra un popup di allarme personalizzato per i budget superati.
     * Stile: rosso/arancione con icone di warning.
     */
    private static void showBudgetExceededAlert(List<Budget> exceededBudgets) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("⚠️ ALLARME BUDGET SUPERATO");
        alert.setHeaderText(null);
        alert.initStyle(StageStyle.DECORATED);

        // Crea il contenuto personalizzato
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #fff5f5;");

        // Titolo con icona
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);

        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 36px;");

        Label titleLabel = new Label("ATTENZIONE: Budget Superato!");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #dc2626;");

        titleBox.getChildren().addAll(warningIcon, titleLabel);

        // Messaggio principale
        Label messageLabel = new Label("Hai superato il budget mensile per le seguenti categorie:");
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setStyle("-fx-text-fill: #991b1b;");
        messageLabel.setWrapText(true);

        // Box per le categorie superate
        VBox categoriesBox = new VBox(10);
        categoriesBox.setPadding(new Insets(10));
        categoriesBox.setStyle("-fx-background-color: white; -fx-border-color: #f87171; " +
                              "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        for (Budget b : exceededBudgets) {
            HBox categoryRow = new HBox(10);
            categoryRow.setAlignment(Pos.CENTER_LEFT);

            // Icona di allarme
            Label exclamation = new Label("❗");
            exclamation.setStyle("-fx-font-size: 20px;");

            // Dettagli categoria
            VBox details = new VBox(3);

            Label categoryName = new Label(b.getCategoryName());
            categoryName.setFont(Font.font("System", FontWeight.BOLD, 15));
            categoryName.setStyle("-fx-text-fill: #dc2626;");

            double exceeded = b.getSpentAmount() - b.getBudgetAmount();
            double percentage = (b.getSpentAmount() / b.getBudgetAmount()) * 100;

            Label amountInfo = new Label(String.format(
                "Budget: €%.2f | Speso: €%.2f | Superato di: €%.2f (%.0f%%)",
                b.getBudgetAmount(), b.getSpentAmount(), exceeded, percentage
            ));
            amountInfo.setFont(Font.font("System", FontWeight.NORMAL, 12));
            amountInfo.setStyle("-fx-text-fill: #7f1d1d;");

            details.getChildren().addAll(categoryName, amountInfo);
            categoryRow.getChildren().addAll(exclamation, details);
            categoriesBox.getChildren().add(categoryRow);
        }

        // Messaggio di suggerimento
        Label suggestionLabel = new Label("💡 Suggerimento: Rivedi le tue spese o aumenta il limite del budget.");
        suggestionLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        suggestionLabel.setStyle("-fx-text-fill: #ea580c; -fx-font-style: italic;");
        suggestionLabel.setWrapText(true);

        content.getChildren().addAll(titleBox, messageLabel, categoriesBox, suggestionLabel);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle("-fx-background-color: #fff5f5;");

        // Personalizza il pulsante
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px;"
        );

        alert.showAndWait();
    }

    /**
     * Verifica se un singolo budget è stato superato.
     */
    public static boolean isBudgetExceeded(Budget budget) {
        if (budget == null || budget.getCategoryId() == 6) {
            return false;
        }
        return budget.getSpentAmount() > budget.getBudgetAmount() && budget.getBudgetAmount() > 0;
    }
}
