package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.fxml.FXML;

public class BudgetController {

    private MainApp mainApp;

    @FXML
    private void initialize() {
        // Qui potrai caricare i dati reali dei budget dal DB
        // e aggiornare le ProgressBar (es. progress bar.setProgress(0.5))
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleEditBudget() {
        System.out.println("Imposta Limiti cliccato");
        // mainApp.showBudgetEditDialog();
    }
}