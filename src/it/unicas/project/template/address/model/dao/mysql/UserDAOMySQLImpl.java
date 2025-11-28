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
        String connectionString = "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName()
                + "&password=" + settings.getPwd();
        return DriverManager.getConnection(connectionString);
    }

    @Override
    public boolean authenticate(String username, String password) throws SQLException {
        String query = "SELECT count(*) FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
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

    public boolean updatePassword(int userId, String oldPwd, String newPwd) throws SQLException {

        String url = getConnectionUrl();
        String sql = "UPDATE user SET password = ? WHERE user_id = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPwd);
            ps.setInt(2, userId);
            ps.setString(3, oldPwd);

            int rows = ps.executeUpdate();
            return rows == 1; // true se password cambiata
        }
    }

    public boolean deleteUser(int userId) throws SQLException {

        String url = getConnectionUrl();
        String sql = "DELETE FROM user WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            int rows = ps.executeUpdate();

            return rows == 1; // true se utente eliminato
        }
    }

    /** Metodo di utilità se già esiste, altrimenti aggiungilo */
    private String getConnectionUrl() {
        DAOMySQLSettings settings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        return "jdbc:mysql://" + settings.getHost() + ":3306/" + settings.getSchema()
                + "?user=" + settings.getUserName() + "&password=" + settings.getPwd();
    }

}