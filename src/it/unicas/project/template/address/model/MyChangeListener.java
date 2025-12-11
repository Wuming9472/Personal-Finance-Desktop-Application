package it.unicas.project.template.address.model;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Implementazione semplice di {@link ChangeListener} utilizzata
 * per intercettare e loggare i cambiamenti di un valore osservabile.
 * <p>
 * Ogni volta che il valore osservato cambia, il listener stampa
 * su console il valore precedente e quello nuovo.
 */
public class MyChangeListener implements ChangeListener<Object> {

    /**
     * Metodo chiamato automaticamente quando il valore osservato cambia.
     *
     * @param observable sorgente del cambiamento (la proprietà osservata).
     * @param oldValue   valore precedente.
     * @param newValue   nuovo valore impostato.
     */
    @Override
    public void changed(ObservableValue<? extends Object> observable,
                        Object oldValue,
                        Object newValue) {
        System.out.println("Evento intercettato dalla classe con nome! " + oldValue + " " + newValue);
    }
}
