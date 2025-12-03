package it.unicas.project.template.address.util;

import it.unicas.project.template.address.model.Budget;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Helper class per gestire le notifiche di superamento budget.
 * Mostra popup di allarme quando un budget mensile viene superato.
 */
public class BudgetNotificationHelper {

    /**
     * Controlla se una specifica categoria ha appena superato il budget e mostra la notifica.
     * La notifica viene mostrata solo se:
     * - Il budget è stato superato
     * - La categoria NON era già stata notificata questo mese
     * - Le notifiche per questa categoria NON sono disabilitate
     *
     * @param budgets Lista di tutti i budget del mese
     * @param categoryId ID della categoria da controllare (quella del movimento appena inserito)
     * @return true se la notifica è stata mostrata, false altrimenti
     */
    public static boolean checkAndNotifyForCategory(List<Budget> budgets, int categoryId) {
        if (budgets == null || budgets.isEmpty()) {
            return false;
        }

        // Ignora la categoria Stipendio (ID 6)
        if (categoryId == 6) {
            return false;
        }

        // Trova il budget per la categoria specifica
        Budget categoryBudget = budgets.stream()
            .filter(b -> b.getCategoryId() == categoryId)
            .findFirst()
            .orElse(null);

        if (categoryBudget == null) {
            return false;
        }

        // Controlla se il budget è stato superato
        boolean isExceeded = categoryBudget.getSpentAmount() > categoryBudget.getBudgetAmount()
            && categoryBudget.getBudgetAmount() > 0;

        BudgetNotificationPreferences prefs = BudgetNotificationPreferences.getInstance();

        if (!isExceeded) {
            // Se il budget NON è più superato, rimuovi la marcatura
            prefs.unmarkAsNotified(categoryId);
            return false;
        }

        // Il budget è superato: controlla se mostrare la notifica
        if (prefs.isNotificationDismissedForCurrentMonth(categoryId, categoryBudget.getBudgetAmount())) {
            // L'utente ha scelto "Non mostrare più" per questo budget nel mese corrente
            return false;
        }

        if (prefs.wasAlreadyNotifiedThisMonth(categoryId)) {
            // Già notificato questo mese, mostra comunque il popup
            // (l'utente ha inserito un altro movimento nella stessa categoria)
            showSingleBudgetExceededAlert(categoryBudget);
            return true;
        }

        // Prima volta che supera il budget questo mese: mostra notifica e segna
        prefs.markAsNotified(categoryId);
        showSingleBudgetExceededAlert(categoryBudget);
        return true;
    }

    /**
     * Controlla se ci sono budget superati nella lista fornita e mostra una notifica di allarme.
     * @param budgets Lista di budget da controllare
     * @return true se almeno un budget è stato superato, false altrimenti
     * @deprecated Usare checkAndNotifyForCategory per notifiche per singola categoria
     */
    @Deprecated
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
     * Mostra un popup di allarme per una singola categoria che ha superato il budget.
     * Include il bottone "Non mostrare più" per disabilitare le notifiche.
     */
    private static void showSingleBudgetExceededAlert(Budget budget) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("ALLARME BUDGET SUPERATO");
        alert.setHeaderText(null);
        alert.initStyle(StageStyle.DECORATED);

        // Crea il contenuto personalizzato
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #fff5f5;");

        // Titolo con icona
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);
        

        Label titleLabel = new Label("ATTENZIONE: Budget Superato!");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #dc2626;");

        titleBox.getChildren().addAll(titleLabel);

        // Messaggio principale
        Label messageLabel = new Label("Hai superato il budget mensile per la seguente categoria:");
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setStyle("-fx-text-fill: #991b1b;");
        messageLabel.setWrapText(true);

        // Box per la categoria superata
        VBox categoryBox = new VBox(10);
        categoryBox.setPadding(new Insets(15));
        categoryBox.setStyle("-fx-background-color: white; -fx-border-color: #f87171; " +
                           "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox categoryRow = new HBox(10);
        categoryRow.setAlignment(Pos.CENTER_LEFT);

        // Icona di allarme
        Label exclamation = new Label("❗");
        exclamation.setStyle("-fx-font-size: 24px;");

        // Dettagli categoria
        VBox details = new VBox(5);

        Label categoryName = new Label(budget.getCategoryName());
        categoryName.setFont(Font.font("System", FontWeight.BOLD, 16));
        categoryName.setStyle("-fx-text-fill: #dc2626;");

        double exceeded = budget.getSpentAmount() - budget.getBudgetAmount();
        double percentage = (budget.getSpentAmount() / budget.getBudgetAmount()) * 100;

        Label amountInfo = new Label(String.format(
            "Budget: €%.2f | Speso: €%.2f | Superato di: €%.2f (%.0f%%)",
            budget.getBudgetAmount(), budget.getSpentAmount(), exceeded, percentage
        ));
        amountInfo.setFont(Font.font("System", FontWeight.NORMAL, 13));
        amountInfo.setStyle("-fx-text-fill: #7f1d1d;");

        details.getChildren().addAll(categoryName, amountInfo);
        categoryRow.getChildren().addAll(exclamation, details);
        categoryBox.getChildren().add(categoryRow);

        // Messaggio di suggerimento
        Label suggestionLabel = new Label("💡 Suggerimento: Rivedi le tue spese o aumenta il limite del budget.");
        suggestionLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        suggestionLabel.setStyle("-fx-text-fill: #ea580c; -fx-font-style: italic;");
        suggestionLabel.setWrapText(true);

        content.getChildren().addAll(titleBox, messageLabel, categoryBox, suggestionLabel);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle("-fx-background-color: #fff5f5;");

        // Bottoni personalizzati: OK e "Non mostrare più"
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType disableButton = new ButtonType("Non mostrare più", ButtonBar.ButtonData.OTHER);

        alert.getButtonTypes().setAll(okButton, disableButton);

        // Stile bottone OK
        Button okBtn = (Button) alert.getDialogPane().lookupButton(okButton);
        okBtn.setStyle(
            "-fx-background-color: #dc2626; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px;"
        );

        // Stile bottone "Non mostrare più"
        Button disableBtn = (Button) alert.getDialogPane().lookupButton(disableButton);
        disableBtn.setStyle(
            "-fx-background-color: #6b7280; -fx-text-fill: white; " +
            "-fx-font-weight: normal; -fx-padding: 10 20; -fx-font-size: 13px;"
        );

        // Gestione click
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == disableButton) {
            // L'utente ha scelto "Non mostrare più" per il mese corrente
            BudgetNotificationPreferences.getInstance()
                .dismissNotificationForCurrentMonth(budget.getCategoryId(), budget.getBudgetAmount());
        }
    }

    /**
     * Mostra un popup di allarme personalizzato per i budget superati.
     * Stile: rosso/arancione con icone di warning.
     * @deprecated Usare showSingleBudgetExceededAlert per notifiche singole
     */
    @Deprecated
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
