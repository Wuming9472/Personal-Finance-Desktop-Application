package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Movimenti;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MovimentiDAOMySQLImpl {


    // INSERT di un movimento

    public static void insert(Movimenti m, int userId, int categoryId) {
        Statement st = null;
        try {
            st = DAOMySQLSettings.getStatement();

            String sql =
                    "INSERT INTO movements " +
                            "(category_id, type, date, amount, description, payment_method, user_id) VALUES (" +
                            categoryId + ", '" +
                            m.getType() + "', '" +
                            m.getDate().toString() + "', " +
                            m.getAmount() + ", '" +
                            m.getDescription() + "', '" +
                            m.getPayment_method() + "', " +
                            userId + ");";

            st.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (st != null) {
                try {
                    DAOMySQLSettings.closeStatement(st);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // SELECT di tutti i movimenti di un utente

    public static List<Movimenti> findByUser(int userId) {
        List<Movimenti> result = new ArrayList<>();
        Statement st = null;

        try {
            st = DAOMySQLSettings.getStatement();

            String sql =
                    "SELECT movement_id, type, date, amount, description, payment_method " +
                            "FROM movements WHERE user_id = " + userId + " ORDER BY date DESC;";

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Movimenti m = new Movimenti(
                        rs.getInt("movement_id"),
                        rs.getString("type"),
                        rs.getDate("date").toLocalDate(),
                        rs.getFloat("amount"),
                        rs.getString("description"),
                        rs.getString("payment_method")
                );
                result.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (st != null) {
                try {
                    DAOMySQLSettings.closeStatement(st);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }
}
