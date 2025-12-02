# Manuale Utente - Personal Finance Desktop Application

## Avvio dell'applicazione
1. Avvia Java 21 o superiore e JavaFX installato nel sistema.
2. Assicurati che il database MySQL sia raggiungibile e che il file `PersonalFinanceDB.sql` sia importato.
3. Esegui la classe `it.unicas.project.template.address.MainApp` dall'IDE oppure con Ant usando `build/build.xml`.

## Autenticazione
- **Login**: inserisci username e password registrati. L'app verifica le credenziali tramite `UserDAO`.
- **Registrazione**: crea un nuovo account usando il pulsante di registrazione.

## Gestione Movimenti
- **Creazione**: premi "Nuovo" per aggiungere un movimento con titolo, importo, data e metodo di pagamento.
- **Modifica**: seleziona un movimento esistente e usa "Modifica".
- **Eliminazione**: seleziona un elemento e premi "Elimina".
- I campi sono validati e i dati vengono salvati nel database.

## Gestione Budget
- Imposta un limite mensile per ogni categoria.
- La colonna "Speso" mostra l'importo consumato; "Rimanente" calcola il saldo residuo.
- Le notifiche di superamento budget sono gestite tramite `BudgetNotificationHelper` e `BudgetNotificationPreferences`.
- Puoi disabilitare le notifiche per una categoria direttamente dal popup di allerta.

## Suggerimenti di utilizzo
- Aggiorna i movimenti con regolarità per tenere sincronizzati i calcoli del budget.
- Esporta periodicamente il database per avere un backup dei dati personali.
- Usa categorie coerenti: semplifica l'analisi delle spese ricorrenti.

## Risoluzione problemi comuni
- **La finestra non si apre**: verifica di aver configurato correttamente il path di JavaFX nel run configuration.
- **Credenziali rifiutate**: controlla la connessione al database e che la tabella utenti contenga l'account.
- **Notifiche ripetute**: se hai riabilitato una categoria, assicurati di non avere movimenti che superano di nuovo il limite.
