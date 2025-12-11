package it.unicas.project.template.address.model.dao.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe di configurazione e utilità per l'accesso al database MySQL.
 * <p>
 * Questa classe centralizza:
 * <ul>
 *     <li>i parametri di connessione (host, schema, credenziali, driver, ecc.);</li>
 *     <li>la configurazione corrente utilizzata per aprire nuove connessioni;</li>
 *     <li>metodi statici per ottenere connessioni e statement JDBC.</li>
 * </ul>
 * <p>
 * La configurazione di default è definita tramite le costanti statiche
 * ({@link #HOST}, {@link #USERNAME}, {@link #PWD}, {@link #SCHEMA}) e può
 * essere sovrascritta a runtime mediante
 * {@link #setCurrentDAOMySQLSettings(DAOMySQLSettings)}.
 */
public class DAOMySQLSettings {

    /**
     * Nome della classe driver JDBC di MySQL.
     */
    public static final String DRIVERNAME = "com.mysql.cj.jdbc.Driver";

    /**
     * Host predefinito del server MySQL.
     */
    public static final String HOST = "localhost";

    /**
     * Nome utente predefinito per la connessione al database.
     */
    public static final String USERNAME = "user";

    /**
     * Password predefinita per la connessione al database.
     */
    public static final String PWD = "password";

    /**
     * Nome dello schema (database) predefinito.
     */
    public static final String SCHEMA = "personal_finance_db";

    /**
     * Parametri aggiuntivi per la connessione JDBC (SSL, timezone, ecc.).
     */
    public static final String PARAMETERS =
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Esempio di URL originale utilizzato in un contesto differente:
    // String url = "jdbc:mysql://localhost:3306/amici?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Parametri istanza (possono differire dai default, ad esempio in test o altri ambienti)

    /** Host del server MySQL per questa istanza di configurazione. */
    private String host = "localhost";

    /** Nome utente per la connessione al database per questa istanza. */
    private String userName = "amici";

    /** Password per la connessione al database per questa istanza. */
    private String pwd = "$Prova2022$";

    /** Nome dello schema (database) per questa istanza. */
    private String schema = "amici";

    /**
     * Restituisce l'host configurato per questa istanza.
     *
     * @return host del server MySQL
     */
    public String getHost() {
        return host;
    }

    /**
     * Restituisce lo username configurato per questa istanza.
     *
     * @return nome utente per la connessione al database
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Restituisce la password configurata per questa istanza.
     *
     * @return password per la connessione al database
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * Restituisce il nome dello schema configurato per questa istanza.
     *
     * @return nome dello schema (database)
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Imposta l'host del server MySQL per questa istanza.
     *
     * @param host nuovo host da utilizzare
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Imposta lo username per questa istanza.
     *
     * @param userName nuovo nome utente da utilizzare
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Imposta la password per questa istanza.
     *
     * @param pwd nuova password da utilizzare
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    /**
     * Imposta il nome dello schema (database) per questa istanza.
     *
     * @param schema nuovo schema da utilizzare
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Blocco di inizializzazione statica che carica il driver JDBC di MySQL.
     * <p>
     * Viene eseguito una sola volta alla prima referenza della classe.
     */
    static {
        try {
            Class.forName(DRIVERNAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configurazione attualmente utilizzata per creare le connessioni.
     * <p>
     * Se non impostata esplicitamente, viene inizializzata con i valori di
     * default tramite {@link #getDefaultDAOSettings()}.
     */
    private static DAOMySQLSettings currentDAOMySQLSettings = null;

    /**
     * Restituisce l'istanza di configurazione attualmente in uso.
     * <p>
     * Se non è ancora stata inizializzata, viene creata a partire dai
     * valori di default definiti nelle costanti della classe.
     *
     * @return istanza corrente di {@link DAOMySQLSettings}
     */
    public static DAOMySQLSettings getCurrentDAOMySQLSettings() {
        if (currentDAOMySQLSettings == null) {
            currentDAOMySQLSettings = getDefaultDAOSettings();
        }
        return currentDAOMySQLSettings;
    }

    /**
     * Crea una nuova istanza di {@link DAOMySQLSettings} inizializzata
     * con i valori di default definiti dalle costanti
     * {@link #HOST}, {@link #USERNAME}, {@link #SCHEMA} e {@link #PWD}.
     *
     * @return nuova istanza di {@link DAOMySQLSettings} con valori di default
     */
    public static DAOMySQLSettings getDefaultDAOSettings() {
        DAOMySQLSettings daoMySQLSettings = new DAOMySQLSettings();
        daoMySQLSettings.host = HOST;
        daoMySQLSettings.userName = USERNAME;
        daoMySQLSettings.schema = SCHEMA;
        daoMySQLSettings.pwd = PWD;
        return daoMySQLSettings;
    }

    /**
     * Imposta la configurazione attuale da utilizzare per creare
     * nuove connessioni al database.
     * <p>
     * Questo metodo permette, ad esempio, di modificare la configurazione
     * in base all'ambiente (sviluppo, test, produzione) o alle preferenze
     * impostate a runtime.
     *
     * @param daoMySQLSettings nuova configurazione da impostare come corrente
     */
    public static void setCurrentDAOMySQLSettings(DAOMySQLSettings daoMySQLSettings) {
        currentDAOMySQLSettings = daoMySQLSettings;
    }

    /**
     * Restituisce una nuova connessione JDBC verso il database MySQL,
     * utilizzando la configurazione attualmente impostata.
     * <p>
     * Se la configurazione corrente non è stata ancora inizializzata,
     * viene creato automaticamente un set di parametri di default tramite
     * {@link #getDefaultDAOSettings()}.
     *
     * @return una nuova {@link Connection} verso il database
     * @throws SQLException se si verifica un errore nella creazione della connessione
     */
    public static Connection getConnection() throws SQLException {
        if (currentDAOMySQLSettings == null) {
            currentDAOMySQLSettings = getDefaultDAOSettings();
        }

        return DriverManager.getConnection(
                "jdbc:mysql://" + currentDAOMySQLSettings.host + "/" +
                        currentDAOMySQLSettings.schema + PARAMETERS,
                currentDAOMySQLSettings.userName,
                currentDAOMySQLSettings.pwd
        );
    }

    /**
     * Crea e restituisce uno {@link Statement} JDBC utilizzando
     * una nuova connessione ottenuta da {@link #getConnection()}.
     * <p>
     * La connessione associata allo statement è quella appena creata
     * e dovrà essere chiusa esplicitamente (ad esempio tramite
     * {@link #closeStatement(Statement)}).
     *
     * @return un oggetto {@link Statement} pronto per l'esecuzione di query
     * @throws SQLException se si verifica un errore durante la creazione dello statement
     */
    public static Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }

    /**
     * Chiude lo {@link Statement} passato e la connessione associata.
     * <p>
     * L'ordine di chiusura è:
     * <ol>
     *     <li>chiusura della connessione ottenuta tramite {@link Statement#getConnection()};</li>
     *     <li>chiusura dello statement stesso.</li>
     * </ol>
     *
     * @param st statement da chiudere insieme alla sua connessione
     * @throws SQLException se si verifica un errore durante la chiusura
     */
    public static void closeStatement(Statement st) throws SQLException {
        st.getConnection().close();
        st.close();
    }

}
