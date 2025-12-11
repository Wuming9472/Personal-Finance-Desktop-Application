package it.unicas.project.template.address.model;

/**
 * Rappresenta un utente dell'applicazione.
 * <p>
 * Contiene le informazioni minime necessarie per la gestione
 * dell'autenticazione (id, username e password).
 */
public class User {

    private int user_id;
    private String username;
    private String password;

    /**
     * Costruttore completo.
     *
     * @param user_id  identificativo univoco dell'utente nel database.
     * @param username nome utente utilizzato per il login.
     * @param password password associata all'account.
     */
    public User(int user_id, String username, String password) {
        this.user_id = user_id;
        this.username = username;
        this.password = password;
    }


    public int getUser_id() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Setter (se servono)
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
