package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Budget;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAOMySQLImpl {

    // Metodo helper per ottenere la connessione usando le tue impostazioni
    private Connection getConnection() throws SQLException {
        DAOMySQLSettings settings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }

    /**
     * Recupera la lista dei budget per un dato utente in un dato mese/anno.
     * Include anche quanto è stato speso (spentAmount) calcolandolo "al volo" dalla tabella movements.
     */
    public List<Budget> getBudgetsForMonth(int userId, int month, int year) throws SQLException {
        List<Budget> budgetList = new ArrayList<>();

        // QUERY:
        // 1. Seleziona i dati del budget e il nome della categoria.
        // 2. Usa una subquery (SELECT SUM...) per calcolare il totale speso prendendolo dai movimenti.
        String sql = "SELECT " +
                "   b.budget_id, " +
                "   b.category_id, " +
                "   c.name as cat_name, " +
                "   b.amount as limit_amount, " +
                "   COALESCE((SELECT SUM(m.amount) " +
                "             FROM movements m " +
                "             WHERE m.category_id = b.category_id " +
                "               AND m.user_id = b.user_id " +
                "               AND MONTH(m.date) = b.month " +
                "               AND YEAR(m.date) = b.year " +
                "               AND (m.type = 'Uscita' OR m.type = 'Entrata')), 0) as spent_amount " +
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
     * Imposta o Aggiorna un budget.
     * Se esiste già per quel mese/anno/categoria, lo aggiorna. Altrimenti lo crea nuovo.
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