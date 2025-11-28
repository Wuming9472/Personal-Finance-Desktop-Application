package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
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

    public List<javafx.util.Pair<String, Float>> getMonthlyTrend(int userId, int month, int year) throws SQLException {
        List<javafx.util.Pair<String, Float>> data = new ArrayList<>();

        String query = "SELECT DATE(date) as giorno, " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE -amount END) as saldo " +
                "FROM movements " +
                "WHERE user_id = ? AND MONTH(date) = ? AND YEAR(date) = ? " +
                "GROUP BY DATE(date) " +
                "ORDER BY giorno ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);

            try (ResultSet rs = pstmt.executeQuery()) {
                float runningBalance = 0f;
                boolean firstPointAdded = false;

                while (rs.next()) {
                    LocalDate giorno = rs.getDate("giorno").toLocalDate();
                    float saldo = rs.getFloat("saldo");

                    if (!firstPointAdded) {
                        // Punto iniziale per dare ancoraggio al grafico
                        LocalDate firstDay = LocalDate.of(year, month, 1);
                        data.add(new javafx.util.Pair<>(firstDay.getDayOfMonth() + "", 0f));
                        firstPointAdded = true;
                    }

                    runningBalance += saldo;
                    data.add(new javafx.util.Pair<>(giorno.getDayOfMonth() + "", runningBalance));
                }

                // Se non ci sono movimenti, restituiamo un punto base per evitare grafici vuoti
                if (!firstPointAdded) {
                    LocalDate firstDay = LocalDate.of(year, month, 1);
                    data.add(new javafx.util.Pair<>(firstDay.getDayOfMonth() + "", 0f));
                }
            }
        }
        return data;
    }

    public List<javafx.util.Pair<String, javafx.util.Pair<Float, Float>>> getIncomeExpenseTrend(int userId, int monthsBack) throws SQLException {
        if (monthsBack <= 0) {
            throw new IllegalArgumentException("monthsBack must be positive");
        }

        LocalDate startDate = LocalDate.now().minusMonths(monthsBack).withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        boolean groupByDay = monthsBack == 1;

        String selectClause;
        String groupByClause;
        String orderByClause;

        if (groupByDay) {
            selectClause = "DATE(date) as periodo_date";
            groupByClause = "periodo_date";
            orderByClause = "periodo_date ASC";
        } else {
            selectClause = "YEAR(date) as periodo_year, MONTH(date) as periodo_month";
            groupByClause = "periodo_year, periodo_month";
            orderByClause = "periodo_year ASC, periodo_month ASC";
        }

        String query = "SELECT " + selectClause + ", " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as entrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as uscite " +
                "FROM movements " +
                "WHERE user_id = ? AND date >= ? AND date <= ? " +
                "GROUP BY " + groupByClause + " " +
                "ORDER BY " + orderByClause;

        List<javafx.util.Pair<String, javafx.util.Pair<Float, Float>>> data = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(today));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    float entrate = rs.getFloat("entrate");
                    float uscite = rs.getFloat("uscite");

                    String label;
                    if (groupByDay) {
                        LocalDate day = rs.getDate("periodo_date").toLocalDate();
                        label = day.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ITALIAN));
                    } else {
                        int year = rs.getInt("periodo_year");
                        int month = rs.getInt("periodo_month");
                        YearMonth ym = YearMonth.of(year, month);
                        label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN) + " " + ym.getYear();
                    }

                    data.add(new javafx.util.Pair<>(label, new javafx.util.Pair<>(entrate, uscite)));
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