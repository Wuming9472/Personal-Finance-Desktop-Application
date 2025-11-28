package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.dao.UserDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOMySQLImpl implements UserDAO {

    private Connection getConnection() throws SQLException {
        DAOMySQLSettings settings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        String connectionString =
                "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                        + "?user=" + settings.getUserName()
                        + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }

    @Override
    public boolean authenticate(String username, String password) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean register(String username, String password) throws SQLException {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Aggiorna la password sapendo user_id e vecchia password.
     * Restituisce true se è stata aggiornata esattamente una riga.
     */
    public boolean updatePassword(int userId, String oldPwd, String newPwd) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPwd);
            ps.setInt(2, userId);
            ps.setString(3, oldPwd);

            int rows = ps.executeUpdate();
            return rows == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina l'utente con lo user_id indicato.
     * Restituisce true se è stata eliminata esattamente una riga.
     */
    public boolean deleteUser(int userId) {
        String[] tablesToDelete = {
                "DELETE FROM movements WHERE user_id = ?",
                "DELETE FROM budgets WHERE user_id = ?",
                "DELETE FROM users WHERE user_id = ?"
        };

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);  // inizio transazione

            for (String sql : tablesToDelete) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            }

            conn.commit();  // conferma transazione
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();  // annulla transazione
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);  // ritorna al default
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


}
