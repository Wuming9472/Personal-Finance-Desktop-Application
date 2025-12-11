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
import javafx.util.Pair;

/**
 * Implementazione MySQL del DAO per l'entità {@link Movimenti}.
 * <p>
 * Fornisce metodi specializzati per:
 * <ul>
 *     <li>recuperare i movimenti di un utente (tutti, ultimi N, filtrati per mese/anno);</li>
 *     <li>inserire, aggiornare e cancellare movimenti;</li>
 *     <li>calcolare statistiche e trend (somma mensile per tipo, andamento giornaliero,
 *         trend entrate/uscite per periodo, aggregazioni a blocchi di 3 giorni).</li>
 * </ul>
 * Le query sono costruite direttamente sulla tabella {@code movements} del database
 * e spesso includono una JOIN con la tabella {@code categories} per recuperare
 * anche il nome della categoria.
 */
public class MovimentiDAOMySQLImpl implements DAO<Movimenti> {

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
     * Recupera tutti i movimenti relativi a un dato utente.
     * <p>
     * Metodo statico di comodo che istanzia internamente il DAO e delega
     * la logica a {@link #selectByUser(int)}.
     *
     * @param userId identificativo dell'utente
     * @return lista di {@link Movimenti} associati all'utente, ordinati per data decrescente
     * @throws DAOException per compatibilità con l'interfaccia DAO (non usato esplicitamente qui)
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public static List<Movimenti> findByUser(int userId) throws DAOException, SQLException {
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        return dao.selectByUser(userId);
    }

    /**
     * Recupera tutti i movimenti di un utente, includendo il nome della categoria.
     * <p>
     * La query esegue una JOIN tra le tabelle {@code movements} (alias {@code m})
     * e {@code categories} (alias {@code c}) per ottenere anche {@code c.name}
     * come {@code cat_name}. I risultati sono ordinati per data in ordine decrescente.
     *
     * @param userId identificativo dell'utente
     * @return lista di {@link Movimenti} con categoria e nome categoria valorizzati
     * @throws DAOException per compatibilità con l'interfaccia DAO
     * @throws SQLException se si verifica un errore durante l'esecuzione della query
     */
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

                    // Settiamo i dati della categoria recuperati dalla JOIN
                    mov.setCategoryId(rs.getInt("category_id"));
                    mov.setCategoryName(rs.getString("cat_name"));

                    lista.add(mov);
                }
            }
        }
        return lista;
    }

    /**
     * Inserisce un nuovo movimento per un determinato utente e categoria.
     * <p>
     * Metodo statico di comodo che istanzia il DAO e delega a
     * {@link #insertInternal(Movimenti, int, int)}.
     *
     * @param m          oggetto {@link Movimenti} da inserire
     * @param userId     identificativo dell'utente proprietario del movimento
     * @param categoryId identificativo della categoria associata
     * @throws DAOException per compatibilità con l'interfaccia DAO
     * @throws SQLException se si verifica un errore durante l'esecuzione dell'INSERT
     */
    public static void insert(Movimenti m, int userId, int categoryId) throws DAOException, SQLException {
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        dao.insertInternal(m, userId, categoryId);
    }

    /**
     * Inserisce un nuovo movimento nella tabella {@code movements}.
     *
     * @param m          oggetto {@link Movimenti} da inserire
     * @param userId     identificativo dell'utente
     * @param categoryId identificativo della categoria
     * @throws SQLException se si verifica un errore durante l'esecuzione dell'INSERT
     */
    private void insertInternal(Movimenti m, int userId, int categoryId) throws SQLException {
        String query = "INSERT INTO movements (type, date, amount, title, payment_method, user_id, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
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

    /**
     * Elimina un movimento dato il suo identificativo.
     * <p>
     * Metodo statico di comodo che istanzia il DAO e delega a
     * {@link #deleteInternal(int)}.
     *
     * @param movementId identificativo del movimento da cancellare
     * @throws DAOException per compatibilità con l'interfaccia DAO
     * @throws SQLException se si verifica un errore durante l'esecuzione del DELETE
     */
    public static void delete(int movementId) throws DAOException, SQLException {
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        dao.deleteInternal(movementId);
    }

    /**
     * Elimina un movimento dalla tabella {@code movements}.
     *
     * @param id identificativo del movimento da cancellare
     * @throws SQLException se si verifica un errore durante il DELETE
     */
    private void deleteInternal(int id) throws SQLException {
        String query = "DELETE FROM movements WHERE movement_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Recupera gli ultimi {@code limit} movimenti registrati da un utente,
     * ordinati dalla data più recente alla più vecchia.
     * <p>
     * La query utilizza la tabella {@code movements} e una LEFT JOIN su
     * {@code categories} per ottenere anche il nome della categoria.
     *
     * @param userId identificativo dell'utente
     * @param limit  numero massimo di movimenti da restituire
     * @return lista di {@link Movimenti} limitata a {@code limit} elementi
     * @throws SQLException se si verifica un errore durante l'esecuzione della query
     */
    public List<Movimenti> selectLastByUser(int userId, int limit) throws SQLException {
        ArrayList<Movimenti> lista = new ArrayList<>();
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

    /**
     * Recupera i movimenti di un utente filtrati per mese e anno,
     * includendo il nome della categoria.
     *
     * @param userId identificativo dell'utente
     * @param month  mese di riferimento (1–12)
     * @param year   anno di riferimento (es. 2025)
     * @return lista di {@link Movimenti} per quell'utente e quel mese/anno
     * @throws SQLException se si verifica un errore durante l'esecuzione della query
     */
    public List<Movimenti> selectByUserAndMonthYear(int userId, int month, int year) throws SQLException {
        ArrayList<Movimenti> lista = new ArrayList<>();
        String query = "SELECT m.*, c.name as cat_name FROM movements m " +
                "LEFT JOIN categories c ON m.category_id = c.category_id " +
                "WHERE m.user_id = ? AND MONTH(m.date) = ? AND YEAR(m.date) = ? " +
                "ORDER BY m.date DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);

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

    /**
     * Calcola la somma degli importi per un utente, mese, anno e tipo di movimento.
     * <p>
     * Viene eseguita una {@code SUM(amount)} sulla tabella {@code movements},
     * filtrando per:
     * <ul>
     *     <li>{@code user_id}</li>
     *     <li>mese ({@code MONTH(date)})</li>
     *     <li>anno ({@code YEAR(date)})</li>
     *     <li>{@code type} (es. "Entrata", "Uscita").</li>
     * </ul>
     *
     * @param userId identificativo dell'utente
     * @param month  mese di riferimento (1–12)
     * @param year   anno di riferimento
     * @param type   tipo di movimento ("Entrata"/"Uscita" o equivalenti)
     * @return somma degli importi corrispondenti ai filtri, 0 se non ci sono risultati
     * @throws SQLException se si verifica un errore durante l'esecuzione della query
     */
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
                    total = rs.getFloat(1); // risultato della SUM()
                }
            }
        }
        return total;
    }

    /**
     * Restituisce i dati di trend giornaliero per un mese specifico,
     * calcolando un saldo progressivo.
     * <p>
     * Per ogni giorno con movimenti viene calcolato:
     * <ul>
     *     <li>entrate come valore positivo;</li>
     *     <li>uscite come valore negativo;</li>
     *     <li>saldo cumulativo (running balance) nel tempo.</li>
     * </ul>
     * Le entrate sono riconosciute come {@code type} in
     * {@code ('entrata', 'income')}, le uscite come gli altri valori.
     * <p>
     * Il risultato è una lista di coppie {@code (labelGiorno, saldoCumulativo)},
     * dove la label è il giorno del mese come stringa.
     * Se non ci sono movimenti, viene comunque restituito un punto base
     * con giorno 1 e saldo 0.
     *
     * @param userId identificativo dell'utente
     * @param month  mese di riferimento (1–12)
     * @param year   anno di riferimento
     * @return lista di {@link Pair} con etichetta giorno e saldo cumulativo
     * @throws SQLException se si verifica un errore durante l'accesso al database
     */
    public List<Pair<String, Float>> getMonthlyTrend(int userId, int month, int year) throws SQLException {
        List<Pair<String, Float>> data = new ArrayList<>();

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
                        data.add(new Pair<>(firstDay.getDayOfMonth() + "", 0f));
                        firstPointAdded = true;
                    }

                    runningBalance += saldo;
                    data.add(new Pair<>(giorno.getDayOfMonth() + "", runningBalance));
                }

                // Se non ci sono movimenti, restituiamo un punto base per evitare grafici vuoti
                if (!firstPointAdded) {
                    LocalDate firstDay = LocalDate.of(year, month, 1);
                    data.add(new Pair<>(firstDay.getDayOfMonth() + "", 0f));
                }
            }
        }
        return data;
    }

    /**
     * Calcola il trend di entrate e uscite per un utente su un intervallo
     * temporale espresso in mesi a ritroso rispetto ad oggi.
     * <p>
     * Il comportamento dipende dal parametro {@code monthsBack}:
     * <ul>
     *     <li>se {@code monthsBack == 1}, i dati vengono raggruppati per giorno;</li>
     *     <li>se {@code monthsBack > 1}, i dati vengono raggruppati per mese.</li>
     * </ul>
     * In entrambi i casi si ottiene una lista di:
     * <pre>
     * (etichettaPeriodo, (totaleEntrate, totaleUscite))
     * </pre>
     * dove:
     * <ul>
     *     <li>l'etichetta è:
     *         <ul>
     *             <li>per giorno: data nel formato {@code "dd MMM"} (es. "05 gen");</li>
     *             <li>per mese: stringa breve tipo {@code "gen '25"};</li>
     *         </ul>
     *     </li>
     *     <li>entrate: somma di importi con {@code type} in
     *         {@code ('entrata','income')};</li>
     *     <li>uscite: somma di importi con {@code type} in
     *         {@code ('uscita','expense')}.</li>
     * </ul>
     *
     * @param userId     identificativo dell'utente
     * @param monthsBack numero di mesi da considerare a ritroso (deve essere &gt; 0)
     * @return lista di {@link Pair} con etichetta periodo e coppia (entrate, uscite)
     * @throws SQLException              se si verifica un errore durante l'esecuzione della query
     * @throws IllegalArgumentException se {@code monthsBack} non è positivo
     */
    public List<Pair<String, Pair<Float, Float>>> getIncomeExpenseTrend(int userId, int monthsBack) throws SQLException {
        if (monthsBack <= 0) {
            throw new IllegalArgumentException("monthsBack must be positive");
        }

        LocalDate startDate = LocalDate.now().minusMonths(monthsBack).withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        boolean groupByDay = monthsBack == 1;

        // When grouping by month, include the entire current month
        // When grouping by day, only include up to today
        LocalDate endDate;
        if (groupByDay) {
            endDate = today;
        } else {
            endDate = YearMonth.from(today).atEndOfMonth();
        }

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

        List<Pair<String, Pair<Float, Float>>> data = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, Date.valueOf(startDate));
            pstmt.setDate(3, Date.valueOf(endDate));

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
                        // Formato compatto: "gen '25" invece di "gen 2025"
                        String monthName = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN);
                        String yearShort = String.format("'%02d", year % 100);
                        label = monthName + " " + yearShort;
                    }

                    data.add(new Pair<>(label, new Pair<>(entrate, uscite)));
                }
            }
        }

        return data;
    }

    /**
     * Recupera, per un determinato mese di un utente, le somme di entrate e uscite
     * raggruppate in bucket di 3 giorni.
     * <p>
     * Il bucket viene calcolato come:
     * <pre>
     * bucket = FLOOR((DAY(date) - 1) / 3)
     * </pre>
     * quindi:
     * <ul>
     *     <li>bucket 0 → giorni 1–3;</li>
     *     <li>bucket 1 → giorni 4–6;</li>
     *     <li>ecc.</li>
     * </ul>
     * Il risultato è una lista di coppie:
     * <pre>
     * (bucketIndex, (entrateTotali, usciteTotali))
     * </pre>
     *
     * @param userId        identificativo dell'utente
     * @param referenceDate data di riferimento (viene usato solo mese/anno)
     * @return lista di {@link Pair} con indice del bucket e coppia (entrate, uscite)
     * @throws SQLException se si verifica un errore durante l'esecuzione della query
     */
    public List<Pair<Integer, Pair<Float, Float>>> getThreeDayBucketsForMonth(int userId, LocalDate referenceDate) throws SQLException {
        LocalDate monthDate = referenceDate.withDayOfMonth(1);
        int targetYear = monthDate.getYear();
        int targetMonth = monthDate.getMonthValue();

        String query = "SELECT FLOOR((DAY(date) - 1) / 3) AS bucket, " +
                "SUM(CASE WHEN LOWER(type) IN ('entrata', 'income') THEN amount ELSE 0 END) as entrate, " +
                "SUM(CASE WHEN LOWER(type) IN ('uscita', 'expense') THEN amount ELSE 0 END) as uscite " +
                "FROM movements " +
                "WHERE user_id = ? AND YEAR(date) = ? AND MONTH(date) = ? " +
                "GROUP BY bucket " +
                "ORDER BY bucket ASC " +
                "LIMIT 10";

        List<Pair<Integer, Pair<Float, Float>>> data = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, targetYear);
            pstmt.setInt(3, targetMonth);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int bucketIndex = rs.getInt("bucket");
                    float entrate = rs.getFloat("entrate");
                    float uscite = rs.getFloat("uscite");
                    data.add(new Pair<>(bucketIndex, new Pair<>(entrate, uscite)));
                }
            }
        }

        return data;
    }

    /**
     * Aggiorna un movimento esistente per una determinata categoria.
     * <p>
     * Metodo statico di comodo che istanzia internamente il DAO e delega
     * a {@link #updateInternal(Movimenti, int)}.
     *
     * @param m          oggetto {@link Movimenti} contenente i nuovi valori
     * @param categoryId identificativo della categoria da impostare
     * @throws DAOException per compatibilità con l'interfaccia DAO
     * @throws SQLException se si verifica un errore durante l'esecuzione dell'UPDATE
     */
    public static void update(Movimenti m, int categoryId) throws DAOException, SQLException {
        MovimentiDAOMySQLImpl dao = new MovimentiDAOMySQLImpl();
        dao.updateInternal(m, categoryId);
    }

    /**
     * Aggiorna i campi di un movimento esistente nella tabella {@code movements},
     * inclusa la categoria.
     *
     * @param m          oggetto {@link Movimenti} con i dati aggiornati
     * @param categoryId nuovo {@code category_id} da impostare
     * @throws SQLException se si verifica un errore durante l'UPDATE
     */
    private void updateInternal(Movimenti m, int categoryId) throws SQLException {
        String query = "UPDATE movements SET type = ?, date = ?, amount = ?, title = ?, " +
                "payment_method = ?, category_id = ? WHERE movement_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, m.getType());
            pstmt.setDate(2, Date.valueOf(m.getDate()));
            pstmt.setFloat(3, m.getAmount());
            pstmt.setString(4, m.getTitle());
            pstmt.setString(5, m.getPayment_method());
            pstmt.setInt(6, categoryId);
            pstmt.setInt(7, m.getMovement_id());
            pstmt.executeUpdate();
        }
    }

    // Metodi dell'interfaccia DAO generica (non utilizzati direttamente qui, ma richiesti dall'interfaccia)

    /**
     * Implementazione richiesta dall'interfaccia {@link DAO}, non utilizzata
     * in questa classe. Restituisce sempre {@code null}.
     */
    @Override
    public List<Movimenti> select(Movimenti m) throws DAOException {
        return null;
    }

    /**
     * Implementazione richiesta dall'interfaccia {@link DAO}, non utilizzata
     * in questa classe. Non esegue alcuna operazione.
     */
    @Override
    public void update(Movimenti m) throws DAOException {
        // Non utilizzato
    }

    /**
     * Implementazione richiesta dall'interfaccia {@link DAO}, non utilizzata
     * in questa classe. Non esegue alcuna operazione.
     */
    @Override
    public void insert(Movimenti m) throws DAOException {
        // Non utilizzato
    }

    /**
     * Implementazione richiesta dall'interfaccia {@link DAO}, non utilizzata
     * in questa classe. Non esegue alcuna operazione.
     */
    @Override
    public void delete(Movimenti m) throws DAOException {
        // Non utilizzato
    }
}
