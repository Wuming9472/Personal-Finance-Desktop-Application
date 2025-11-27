package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Movimenti;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                            rs.getString("description"),
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
        String query = "INSERT INTO movements (type, date, amount, description, payment_method, user_id, category_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, m.getType());
            pstmt.setDate(2, Date.valueOf(m.getDate()));
            pstmt.setFloat(3, m.getAmount());
            pstmt.setString(4, m.getDescription());
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

    // Metodi dell'interfaccia DAO generica (non usati direttamente qui ma richiesti)
    @Override public List<Movimenti> select(Movimenti m) throws DAOException { return null; }
    @Override public void update(Movimenti m) throws DAOException {}
    @Override public void insert(Movimenti m) throws DAOException {}
    @Override public void delete(Movimenti m) throws DAOException {}
}