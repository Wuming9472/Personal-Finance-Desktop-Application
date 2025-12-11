package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.dao.UserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementazione MySQL dell'interfaccia {@link UserDAO}.
 * <p>
 * Questa classe gestisce le operazioni di accesso dati per gli utenti:
 * <ul>
 *     <li>autenticazione di un utente tramite username e password;</li>
 *     <li>registrazione di un nuovo utente;</li>
 *     <li>aggiornamento della password di un utente esistente;</li>
 *     <li>eliminazione di un utente e dei relativi dati (movimenti e budget)
 *         tramite transazione.</li>
 * </ul>
 * Le connessioni al database vengono ottenute tramite
 * {@link DAOMySQLSettings#getConnection()}.
 */
public class UserDAOMySQLImpl implements UserDAO {

    /**
     * Restituisce una connessione al database utilizzando le impostazioni
     * correnti definite in {@link DAOMySQLSettings}.
     *
     * @return una connessione aperta verso il database MySQL
     * @throws SQLException se si verifica un errore nella creazione della connessione
     */
    private Connection getConnection() throws SQLException {
        return DAOMySQLSettings.getConnection();
    }

    /**
     * Verifica le credenziali di accesso di un utente.
     * <p>
     * Esegue una query del tipo:
     * <pre>
     * SELECT COUNT(*) FROM users WHERE username = ? AND password = ?
     * </pre>
     * Se il conteggio è maggiore di zero, l'utente viene considerato autenticato.
     *
     * @param username username inserito dall'utente
     * @param password password inserita dall'utente
     * @return {@code true} se esiste almeno una riga con username e password corrispondenti,
     *         {@code false} altrimenti
     * @throws SQLException se si verifica un errore durante l'esecuzione della query
     */
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

    /**
     * Registra un nuovo utente nel sistema.
     * <p>
     * Esegue un {@code INSERT} nella tabella {@code users}:
     * <pre>
     * INSERT INTO users (username, password) VALUES (?, ?)
     * </pre>
     * In caso di violazione di vincolo di unicità (username già esistente)
     * viene rilanciata una {@link SQLException} con messaggio descrittivo
     * "Username già esistente".
     *
     * @param username username del nuovo utente
     * @param password password del nuovo utente
     * @return {@code true} se è stata inserita almeno una riga,
     *         {@code false} se per qualche motivo non è stato inserito nulla
     * @throws SQLException se si verifica un errore durante l'INSERT o se l'username è duplicato
     */
    @Override
    public boolean register(String username, String password) throws SQLException {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // errore MySQL per chiave duplicata: codice 1062, SQLState 23000
            if (e.getErrorCode() == 1062 || "23000".equals(e.getSQLState())) {
                throw new SQLException("Username già esistente", e);
            }
            // altri errori vengono rilanciati così come sono
            throw e;
        }
    }

    /**
     * Aggiorna la password di un utente conoscendo il suo {@code user_id}
     * e la vecchia password.
     * <p>
     * La query eseguita è:
     * <pre>
     * UPDATE users SET password = ? WHERE user_id = ? AND password = ?
     * </pre>
     * In questo modo l'aggiornamento avviene solo se la vecchia password
     * corrisponde a quella attuale.
     *
     * @param userId identificativo dell'utente
     * @param oldPwd password attuale dell'utente
     * @param newPwd nuova password da impostare
     * @return {@code true} se è stata aggiornata esattamente una riga,
     *         {@code false} se nessuna riga è stata modificata
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
     * Elimina l'utente identificato da {@code userId} e tutti i dati correlati.
     * <p>
     * L'operazione viene effettuata all'interno di una transazione esplicita
     * che esegue, in ordine:
     * <ol>
     *     <li>DELETE dalla tabella {@code movements} per l'utente;</li>
     *     <li>DELETE dalla tabella {@code budgets} per l'utente;</li>
     *     <li>DELETE dalla tabella {@code users} per l'utente.</li>
     * </ol>
     * Se tutte le operazioni vanno a buon fine viene eseguita una
     * {@link Connection#commit()}, altrimenti viene eseguito un
     * {@link Connection#rollback()}.
     *
     * @param userId identificativo dell'utente da eliminare
     * @return {@code true} se la transazione è andata a buon fine,
     *         {@code false} in caso di errore (con rollback)
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
