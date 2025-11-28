package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import  javafx.util.Pair;

public class MovimentiDAOMySQLImpl implements DAO<Movimenti> {

    private Connection getConnection() throws SQLException {
        DAOMySQLSettings settings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }

    // --- METODO SELECT AGGIORNATO (JOIN) ---
    public static List<Movimenti> findByUser(int userId) throws DAOException, SQLException {
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        return dao.selectByUser(userId);
    }

    public List<Movimenti> selectByUser(int userId) throws DAOException, SQLException {
        ArrayList<Movimenti> lista = new ArrayList<>();

        // JOIN per prendere anche il nome della categoria!
        String query = "SELECT m.*, c.name as cat_name " +
                "FROM movements m " +
                "LEFT JOIN categories c ON m.category_id = c.category_id " +
                "WHERE m.user_id = ? " +
                "ORDER BY m.date DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Creiamo l'oggetto con il costruttore standard
                    Movimenti mov = new Movimenti(
                            rs.getInt("movement_id"),
                            rs.getString("type"),
                            rs.getDate("date").toLocalDate(),
                            rs.getFloat("amount"),
                            rs.getString("title"),
                            rs.getString("payment_method")
                    );

                    // IMPORTANTE: Settiamo i dati della categoria recuperati dalla JOIN
                    mov.setCategoryId(rs.getInt("category_id"));
                    mov.setCategoryName(rs.getString("cat_name")); // Questo riempirà la colonna!

                    lista.add(mov);
                }
            }
        }
        return lista;
    }

    // --- INSERT (Mantenuto) ---
    public static void insert(Movimenti m, int userId, int categoryId) throws DAOException, SQLException {
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        dao.insertInternal(m, userId, categoryId);
    }

    private void insertInternal(Movimenti m, int userId, int categoryId) throws SQLException {
        String query = "INSERT INTO movements (type, date, amount, title, payment_method, user_id, category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, m.getType());
            pstmt.setDate(2, Date.valueOf(m.getDate()));
            pstmt.setFloat(3, m.getAmount());
            pstmt.setString(4, m.getTitle());
            pstmt.setString(5, m.getPayment_method());
            pstmt.setInt(6, userId);
            pstmt.setInt(7, categoryId);
            pstmt.executeUpdate();
        }
    }

    // --- DELETE ---
    public static void delete(int movementId) throws DAOException, SQLException {
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        dao.deleteInternal(movementId);
    }

    private void deleteInternal(int id) throws SQLException {
        String query = "DELETE FROM movements WHERE movement_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Movimenti> selectLastByUser(int userId, int limit) throws SQLException {
        ArrayList<Movimenti> lista = new ArrayList<>();
        // Usa la tabella 'movements' come confermato
        String query = "SELECT m.*, c.name as cat_name FROM movements m " +
                "LEFT JOIN categories c ON m.category_id = c.category_id " +
                "WHERE m.user_id = ? " +
                "ORDER BY m.date DESC LIMIT ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Movimenti mov = new Movimenti(
                            rs.getInt("movement_id"),
                            rs.getString("type"),
                            rs.getDate("date").toLocalDate(),
                            rs.getFloat("amount"),
                            rs.getString("title"),
                            rs.getString("payment_method")
                    );
                    mov.setCategoryId(rs.getInt("category_id"));
                    mov.setCategoryName(rs.getString("cat_name"));
                    lista.add(mov);
                }
            }
        }
        return lista;
    }

    public float getSumByMonth(int userId, int month, int year, String type) throws SQLException {
        float total = 0f;
        String query = "SELECT SUM(amount) FROM movements " +
                "WHERE user_id = ? " +
                "AND MONTH(date) = ? AND YEAR(date) = ? " +
                "AND type = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            pstmt.setString(4, type);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getFloat(1); // Recupera il risultato della SUM()
                }
            }
        }
        return total;
    }

    /**
     * Restituisce l'andamento giornaliero (saldo netto) per un determinato mese.
     * Ogni elemento contiene il giorno del mese e il saldo (entrate - uscite) di quel giorno.
     */
    public List<javafx.util.Pair<Integer, Float>> getDailyTrend(int userId, int month, int year) throws SQLException {
        List<javafx.util.Pair<Integer, Float>> data = new ArrayList<>();

        String query = "SELECT DAY(date) as giorno, " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE -amount END) as saldo " +
                "FROM movements " +
                "WHERE user_id = ? AND MONTH(date) = ? AND YEAR(date) = ? " +
                "GROUP BY DAY(date) " +
                "ORDER BY giorno ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int giorno = rs.getInt("giorno");
                    float saldo = rs.getFloat("saldo");
                    data.add(new javafx.util.Pair<>(giorno, saldo));
                }
            }
        }
        return data;
    }

    public List<javafx.util.Pair<String, Float>> getMonthlyTrend(int userId) throws SQLException {
        List<javafx.util.Pair<String, Float>> data = new ArrayList<>();

        // QUERY CORRETTA E ROBUSTA
        // 1. Raggruppa per anno-mese formattato
        // 2. Gestisce sia italiano (entrata) che inglese (income) e maiuscole/minuscole
        String query = "SELECT DATE_FORMAT(date, '%Y-%m') as mese, " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE -amount END) as saldo " +
                "FROM movements " +
                "WHERE user_id = ? " +
                "GROUP BY DATE_FORMAT(date, '%Y-%m') " +
                "ORDER BY mese ASC LIMIT 12";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String mese = rs.getString("mese");
                    float saldo = rs.getFloat("saldo");
                    data.add(new javafx.util.Pair<>(mese, saldo));
                }
            }
        }
        return data;
    }

    // Metodi dell'interfaccia DAO generica (non usati direttamente qui ma richiesti)
    @Override public List<Movimenti> select(Movimenti m) throws DAOException { return null; }
    @Override public void update(Movimenti m) throws DAOException {}
    @Override public void insert(Movimenti m) throws DAOException {}
    @Override public void delete(Movimenti m) throws DAOException {}
}