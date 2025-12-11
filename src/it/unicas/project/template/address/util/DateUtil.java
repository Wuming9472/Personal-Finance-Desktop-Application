package it.unicas.project.template.address.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Metodi di utilità per la gestione delle date.
 * <p>
 * La classe fornisce funzioni statiche per:
 * <ul>
 *     <li>formattare un {@link LocalDate} in {@link String} secondo un pattern predefinito;</li>
 *     <li>convertire una stringa in {@link LocalDate};</li>
 *     <li>verificare se una stringa rappresenta una data valida.</li>
 * </ul>
 * Il pattern utilizzato per la conversione è definito da
 * {@link DateUtil#DATE_PATTERN}.
 *
 * @author Mario Molinara
 */
public class DateUtil {

    /**
     * Pattern della data utilizzato per le conversioni.
     * <p>
     * Formato: {@code dd-MM-yyyy}, ad esempio {@code 05-10-2025}.
     * Può essere modificato se si desidera supportare un formato diverso,
     * avendo cura di aggiornare di conseguenza l'eventuale documentazione.
     */
    private static final String DATE_PATTERN = "dd-MM-yyyy";

    /**
     * Formatter condiviso per la conversione tra {@link LocalDate}
     * e {@link String} secondo il pattern {@link #DATE_PATTERN}.
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * Restituisce la data fornita come stringa formattata.
     * <p>
     * Il formato utilizzato è quello definito in {@link #DATE_PATTERN}.
     * Se la data è {@code null}, il metodo restituisce {@code null}.
     *
     * @param date la data da convertire in stringa (può essere {@code null})
     * @return rappresentazione testuale della data secondo il pattern
     *         {@link #DATE_PATTERN}, oppure {@code null} se {@code date} è {@code null}
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMATTER.format(date);
    }

    /**
     * Converte una stringa in un oggetto {@link LocalDate} utilizzando il
     * pattern definito in {@link #DATE_PATTERN}.
     * <p>
     * Se la stringa non rispetta il formato previsto o non rappresenta una
     * data valida, il metodo restituisce {@code null}.
     *
     * @param dateString la data in formato testuale, da interpretare secondo {@link #DATE_PATTERN}
     * @return l'oggetto {@link LocalDate} corrispondente, oppure {@code null}
     *         se la stringa non può essere convertita
     */
    public static LocalDate parse(String dateString) {
        try {
            return DATE_FORMATTER.parse(dateString, LocalDate::from);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Verifica se la stringa fornita rappresenta una data valida nel formato
     * definito da {@link #DATE_PATTERN}.
     * <p>
     * Internamente utilizza il metodo {@link #parse(String)}: se la conversione
     * ha successo, la data è considerata valida.
     *
     * @param dateString stringa da validare come data
     * @return {@code true} se la stringa rappresenta una data valida,
     *         {@code false} altrimenti
     */
    public static boolean validDate(String dateString) {
        // Prova a effettuare il parse della stringa.
        return DateUtil.parse(dateString) != null;
    }
}
