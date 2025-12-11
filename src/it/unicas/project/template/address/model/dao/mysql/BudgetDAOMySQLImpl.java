package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione MySQL delle operazioni di accesso ai dati per l'entità {@link Budget}.
 * <p>
 * Questa classe fornisce metodi per:
 * <ul>
 *     <li>recuperare i budget mensili di un utente, includendo l'importo speso
 *         calcolato "al volo" dalla tabella {@code movements};</li>
 *     <li>inserire o aggiornare un budget (upsert) per una combinazione
 *         utente/categoria/mese/anno.</li>
 * </ul>
 * Si appoggia alle impostazioni di connessione fornite da
 * {@link DAOMySQLSettings#getConnection()}.
 */
public class BudgetDAOMySQLImpl {

    /**
     * Restituisce una nuova connessione al database MySQL utilizzando
     * le impostazioni definite in {@link DAOMySQLSettings}.
     *
     * @return una connessione JDBC aperta verso il database
     * @throws SQLException se si verifica un errore durante l'apertura della connessione
     */
    private Connection getConnection() throws SQLException {
        return DAOMySQLSettings.getConnection();
    }

    /**
     * Recupera la lista dei budget per un dato utente e per uno specifico mese/anno.
     * <p>
     * Per ogni riga della tabella {@code budgets} viene calcolato anche l'importo
     * effettivamente speso ({@code spentAmount}) tramite una subquery sulla tabella
     * {@code movements}, filtrando:
     * <ul>
     *     <li>per {@code user_id} dell'utente;</li>
     *     <li>per {@code category_id} del budget;</li>
     *     <li>per mese e anno del movimento ({@code MONTH(m.date)} e {@code YEAR(m.date)});</li>
     *     <li>solo movimenti di tipo {@code 'Uscita'}.</li>
     * </ul>
     * Il risultato viene mappato in una lista di oggetti {@link Budget}.
     *
     * @param userId identificativo dell'utente
     * @param month  mese di riferimento (1–12)
     * @param year   anno di riferimento (es. 2025)
     * @return lista di {@link Budget} per l'utente nel mese/anno richiesti;
     *         la lista può essere vuota se non esistono budget
     * @throws SQLException se si verifica un errore durante l'esecuzione della query
     */
    public List<Budget> getBudgetsForMonth(int userId, int month, int year) throws SQLException {
        List<Budget> budgetList = new ArrayList<>();

        // QUERY:
        // 1. Seleziona i dati del budget e il nome della categoria.
        // 2. Usa una subquery (SELECT SUM...) per calcolare il totale speso prendendolo dai movimenti.
        String sql = "SELECT " +
                "   b.budget_id, " +
                "   b.category_id, " +
                "   c.name AS cat_name, " +
                "   b.amount AS limit_amount, " +
                "   COALESCE((SELECT SUM(m.amount) " +
                "             FROM movements m " +
                "             WHERE m.category_id = b.category_id " +
                "               AND m.user_id = b.user_id " +
                "               AND MONTH(m.date) = b.month " +
                "               AND YEAR(m.date) = b.year " +
                "               AND m.type = 'Uscita'" +
                "            ), 0) AS spent_amount " +
                "FROM budgets b " +
                "JOIN categories c ON b.category_id = c.category_id " +
                "WHERE b.user_id = ? AND b.month = ? AND b.year = ?";


        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Creiamo l'oggetto Budget con tutti i dati recuperati
                    budgetList.add(new Budget(
                            rs.getInt("budget_id"),
                            rs.getInt("category_id"),
                            userId, // userId che abbiamo passato
                            month,
                            year,
                            rs.getDouble("limit_amount"),
                            rs.getString("cat_name"),
                            rs.getDouble("spent_amount")
                    ));
                }
            }
        }
        return budgetList;
    }

    /**
     * Inserisce o aggiorna un budget per una specifica combinazione
     * utente/categoria/mese/anno.
     * <p>
     * La logica è:
     * <ol>
     *     <li>verificare se esiste già una riga nella tabella {@code budgets}
     *         per la combinazione ({@code user_id}, {@code category_id},
     *         {@code month}, {@code year});</li>
     *     <li>se esiste, effettuare un {@code UPDATE} dell'importo;</li>
     *     <li>se non esiste, effettuare un {@code INSERT} di una nuova riga.</li>
     * </ol>
     *
     * @param userId     identificativo dell'utente
     * @param categoryId identificativo della categoria del budget
     * @param month      mese di riferimento (1–12)
     * @param year       anno di riferimento (es. 2025)
     * @param amount     importo del budget da impostare
     * @throws SQLException se si verifica un errore durante le operazioni SQL
     */
    public void setOrUpdateBudget(int userId, int categoryId, int month, int year, double amount) throws SQLException {
        // 1. Controllo se esiste già un budget per questa combinazione
        String checkSql = "SELECT budget_id FROM budgets WHERE user_id=? AND category_id=? AND month=? AND year=?";
        Integer existingId = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, month);
            pstmt.setInt(4, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) existingId = rs.getInt("budget_id");
            }
        }

        if (existingId != null) {
            // UPDATE: Se esiste, aggiorniamo solo l'importo
            String updateSql = "UPDATE budgets SET amount = ? WHERE budget_id = ?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, existingId);
                pstmt.executeUpdate();
            }
        } else {
            // INSERT: Se non esiste, creiamo una nuova riga
            String insertSql = "INSERT INTO budgets (user_id, category_id, month, year, amount) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, categoryId);
                pstmt.setInt(3, month);
                pstmt.setInt(4, year);
                pstmt.setDouble(5, amount);
                pstmt.executeUpdate();
            }
        }
    }
}
