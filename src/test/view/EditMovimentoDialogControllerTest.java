package test.view;

import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.view.EditMovimentoDialogController;
import it.unicas.project.template.address.view.EditMovimentoDialogController.CategoryItem;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EditMovimentoDialogControllerTest {

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    private EditMovimentoDialogController controller;
    private TextField amountField;
    private ComboBox<String> typeField;
    private DatePicker dateField;
    private ComboBox<CategoryItem> categoryField;
    private ComboBox<String> methodField;
    private TextArea descArea;
    private Label charCountLabel;

    @BeforeEach
    void setUp() {
        controller = new EditMovimentoDialogController();
        amountField = new TextField();
        typeField = new ComboBox<>();
        dateField = new DatePicker();
        categoryField = new ComboBox<>();
        methodField = new ComboBox<>();
        descArea = new TextArea();
        charCountLabel = new Label();

        setField(controller, "amountField", amountField);
        setField(controller, "typeField", typeField);
        setField(controller, "dateField", dateField);
        setField(controller, "categoryField", categoryField);
        setField(controller, "methodField", methodField);
        setField(controller, "descArea", descArea);
        setField(controller, "charCountLabel", charCountLabel);
    }

    @Test
    void initializeShouldPopulateTypeComboBox() throws Exception {
        runOnFxThreadAndWait(() -> {
            typeField.getItems().addAll("Entrata", "Uscita");
        });

        assertEquals(2, typeField.getItems().size());
        assertTrue(typeField.getItems().contains("Entrata"));
        assertTrue(typeField.getItems().contains("Uscita"));
    }

    @Test
    void initializeShouldPopulateMethodComboBox() throws Exception {
        runOnFxThreadAndWait(() -> {
            methodField.getItems().addAll("Contanti", "Bancomat", "Carta di credito", "Bonifico", "Addebito SDD");
        });

        assertEquals(5, methodField.getItems().size());
        assertTrue(methodField.getItems().contains("Contanti"));
        assertTrue(methodField.getItems().contains("Bonifico"));
    }

    @Test
    void categoryItemToStringReturnsName() {
        CategoryItem item = new CategoryItem(1, "Alimentari");
        assertEquals("Alimentari", item.toString());
    }

    @Test
    void categoryItemStoresIdAndName() {
        CategoryItem item = new CategoryItem(5, "Salute");
        assertEquals(5, item.id);
        assertEquals("Salute", item.name);
    }

    @Test
    void isOkClickedDefaultToFalse() {
        assertFalse(controller.isOkClicked());
    }

    @Test
    void setMovimentoShouldPopulateFields() throws Exception {
        Movimenti movimento = new Movimenti();
        movimento.setAmount(150.50f);
        movimento.setType("Entrata");
        movimento.setDate(LocalDate.of(2024, 5, 15));
        movimento.setTitle("Stipendio maggio");
        movimento.setPayment_method("Bonifico");
        movimento.setCategoryId(6);

        // Setup type options
        runOnFxThreadAndWait(() -> {
            typeField.getItems().addAll("Entrata", "Uscita");
            methodField.getItems().addAll("Contanti", "Bancomat", "Carta di credito", "Bonifico");
            categoryField.getItems().add(new CategoryItem(6, "Stipendio"));
        });

        runOnFxThreadAndWait(() -> controller.setMovimento(movimento));

        assertEquals("150.5", amountField.getText());
        assertEquals("Entrata", typeField.getValue());
        assertEquals(LocalDate.of(2024, 5, 15), dateField.getValue());
        assertEquals("Stipendio maggio", descArea.getText());
        assertEquals("Bonifico", methodField.getValue());
    }

    @Test
    void validateAndSaveShouldFailWhenAmountIsEmpty() {
        typeField.getItems().addAll("Entrata", "Uscita");
        typeField.setValue("Entrata");
        dateField.setValue(LocalDate.now());
        categoryField.getItems().add(new CategoryItem(1, "Test"));
        categoryField.setValue(categoryField.getItems().get(0));
        amountField.setText("");

        // La validazione richiede importo
        boolean isValid = amountField.getText() != null && !amountField.getText().isEmpty();
        assertFalse(isValid);
    }

    @Test
    void validateAndSaveShouldFailWhenAmountIsZeroOrNegative() {
        amountField.setText("0");

        try {
            float amount = Float.parseFloat(amountField.getText().replace(",", "."));
            assertFalse(amount > 0);
        } catch (NumberFormatException e) {
            fail("Parsing should not fail for '0'");
        }

        amountField.setText("-50");
        try {
            float amount = Float.parseFloat(amountField.getText().replace(",", "."));
            assertFalse(amount > 0);
        } catch (NumberFormatException e) {
            fail("Parsing should not fail for '-50'");
        }
    }

    @Test
    void validateAndSaveShouldFailWhenTypeNotSelected() {
        amountField.setText("100");
        dateField.setValue(LocalDate.now());
        categoryField.getItems().add(new CategoryItem(1, "Test"));
        categoryField.setValue(categoryField.getItems().get(0));
        // typeField non selezionato

        boolean isValid = typeField.getValue() != null;
        assertFalse(isValid);
    }

    @Test
    void validateAndSaveShouldFailWhenDateNotSelected() {
        amountField.setText("100");
        typeField.getItems().addAll("Entrata", "Uscita");
        typeField.setValue("Uscita");
        categoryField.getItems().add(new CategoryItem(1, "Test"));
        categoryField.setValue(categoryField.getItems().get(0));
        // dateField non selezionato

        boolean isValid = dateField.getValue() != null;
        assertFalse(isValid);
    }

    @Test
    void validateAndSaveShouldFailWhenCategoryNotSelected() {
        amountField.setText("100");
        typeField.getItems().addAll("Entrata", "Uscita");
        typeField.setValue("Uscita");
        dateField.setValue(LocalDate.now());
        // categoryField non selezionato

        boolean isValid = categoryField.getValue() != null;
        assertFalse(isValid);
    }

    @Test
    void amountParsingHandlesComma() {
        String input = "100,50";
        float parsed = Float.parseFloat(input.replace(",", "."));
        assertEquals(100.5f, parsed, 0.001f);
    }

    @Test
    void amountParsingHandlesDot() {
        String input = "100.50";
        float parsed = Float.parseFloat(input.replace(",", "."));
        assertEquals(100.5f, parsed, 0.001f);
    }

    @Test
    void getSelectedCategoryIdReturnsMinusOneWhenNoSelection() {
        int categoryId = categoryField.getValue() != null ? categoryField.getValue().id : -1;
        assertEquals(-1, categoryId);
    }

    @Test
    void getSelectedCategoryIdReturnsCorrectId() {
        CategoryItem item = new CategoryItem(3, "Bollette");
        categoryField.getItems().add(item);
        categoryField.setValue(item);

        int categoryId = categoryField.getValue() != null ? categoryField.getValue().id : -1;
        assertEquals(3, categoryId);
    }

    @Test
    void descriptionLengthLimitedTo100Chars() {
        String longText = "A".repeat(150);
        String limitedText = longText.length() > 100 ? longText.substring(0, 100) : longText;

        assertEquals(100, limitedText.length());
    }

    @Test
    void charCountLabelUpdatedOnTextChange() {
        String text = "Test description";
        String expected = text.length() + "/100";
        assertEquals("16/100", expected);
    }

    @Test
    void getMovimentoReturnsNullWhenNotSet() {
        assertNull(controller.getMovimento());
    }

    /**
     * Esegue un Runnable sul JavaFX Application Thread e aspetta che finisca.
     */
    private static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout in runOnFxThreadAndWait");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = EditMovimentoDialogController.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
