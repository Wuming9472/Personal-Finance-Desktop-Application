package it.unicas.project.template.address.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Model class for a Amici.
 *
 * @author Mario Molinara
 */
public class Amici {

    private StringProperty nome;
    private StringProperty cognome;
    private StringProperty telefono;
    private StringProperty email;
    private StringProperty compleanno;
    private IntegerProperty idAmici;  //wrapper

    //private static String attributoStaticoDiEsempio;

    /**
     * Default constructor.
     */
    public Amici() {
        this(null, null);
    }

    public Amici(String nome, String cognome, String telefono, String email, String compleanno, Integer idColleghi) {
        this.nome = new SimpleStringProperty(nome);
        this.cognome = new SimpleStringProperty(cognome);
        this.telefono = new SimpleStringProperty(telefono);
        this.email = new SimpleStringProperty(email);
        this.compleanno = new SimpleStringProperty(compleanno);
        if (idColleghi != null){
            this.idAmici = new SimpleIntegerProperty(idColleghi);
        } else {
            this.idAmici = null;
        }
    }

    /**
     * Constructor with some initial data.
     *
     * @param nome
     * @param cognome
     */
    public Amici(String nome, String cognome) {
        this.nome = new SimpleStringProperty(nome);
        this.cognome = new SimpleStringProperty(cognome);
        // Some initial dummy data, just for convenient testing.
        this.telefono = new SimpleStringProperty("telefono");
        this.email = new SimpleStringProperty("email@email.com");
        this.compleanno = new SimpleStringProperty("24-10-2017");
        this.idAmici = null;
    }

    public Integer getIdAmici(){
        if (idAmici == null){
            idAmici = new SimpleIntegerProperty(-1);
        }
        return idAmici.get();
    }

    public void setIdAmici(Integer idAmici) {
        if (this.idAmici == null){
            this.idAmici = new SimpleIntegerProperty();
        }
        this.idAmici.set(idAmici);
    }

    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public StringProperty nomeProperty() {
        return nome;
    }

    public String getCognome() {
        return cognome.get();
    }

    public void setCognome(String cognome) {
        this.cognome.set(cognome);
    }

    public StringProperty cognomeProperty() {
        return cognome;
    }

    public String getTelefono() {
        return telefono.get();
    }

    public void setTelefono(String telefono) {
        this.telefono.set(telefono);
    }

    public StringProperty telefonoProperty() {
        return telefono;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }
    
    public String getCompleanno() {
        return compleanno.getValue();
    }

    public void setCompleanno(String compleanno) {
        this.compleanno.set(compleanno);
    }

    public StringProperty compleannoProperty() {
        return compleanno;
    }


    public String toString(){
        return nome.getValue() + ", " + cognome.getValue() + ", " + telefono.getValue() + ", " + email.getValue() + ", " + compleanno.getValue() + ", (" + idAmici.getValue() + ")";
    }


    public static void main(String[] args) {



        // https://en.wikipedia.org/wiki/Observer_pattern
        Amici collega = new Amici();
        collega.setNome("Ciao");
        MyChangeListener myChangeListener = new MyChangeListener();
        collega.nomeProperty().addListener(myChangeListener);
        collega.setNome("Mario");


        collega.compleannoProperty().addListener(myChangeListener);

        collega.compleannoProperty().addListener(
                (ChangeListener) (o, oldVal, newVal) -> System.out.println("Compleanno property has changed!"));

        collega.compleannoProperty().addListener(
                (o, old, newVal)-> System.out.println("Compleanno property has changed! (Lambda implementation)")
        );


        collega.setCompleanno("30-10-1971");



        // Use Java Collections to create the List.
        List<Amici> list = new ArrayList<>();

        // Now add observability by wrapping it with ObservableList.
        ObservableList<Amici> observableList = FXCollections.observableList(list);
        observableList.addListener(
          (ListChangeListener) change -> System.out.println("Detected a change! ")
        );

        Amici c1 = new Amici();
        Amici c2 = new Amici();

        c1.nomeProperty().addListener(
                (o, old, newValue)->System.out.println("Ciao")
        );

        c1.setNome("Pippo");

        // Changes to the observableList WILL be reported.
        // This line will print out "Detected a change!"
        observableList.add(c1);

        // Changes to the underlying list will NOT be reported
        // Nothing will be printed as a result of the next line.
        observableList.add(c2);


        observableList.get(0).setNome("Nuovo valore");

        System.out.println("Size: "+observableList.size());

    }


}
