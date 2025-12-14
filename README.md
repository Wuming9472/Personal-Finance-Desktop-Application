# BalanceSuite - Personal Finance Desktop Application

[![Java](https://img.shields.io/badge/Java-14+-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![JavaDoc](https://img.shields.io/badge/JavaDoc-API-blue.svg)](https://wuming9472.github.io/Personal-Finance-Desktop-Application/)

BalanceSuite è un’applicazione desktop per la gestione delle finanze personali sviluppata in Java con JavaFX. Consente di registrare entrate e uscite, pianificare budget mensili per categoria e consultare report dettagliati tramite grafici interattivi.

## Indice

- [Caratteristiche](#caratteristiche)
- [Screenshot](#screenshot)
- [Tecnologie utilizzate](#tecnologie-utilizzate)
- [Architettura del progetto](#architettura-del-progetto)
- [Prerequisiti](#prerequisiti)
- [Installazione](#installazione)
- [Configurazione database](#configurazione-database)
- [Esecuzione](#esecuzione)
- [Struttura del database](#struttura-del-database)
- [Funzionalità dettagliate](#funzionalità-dettagliate)
- [Roadmap](#roadmap)
- [Documentazione API](#documentazione-api)
- [Licenza](#licenza)
- [Autori](#autori)

## Caratteristiche

### Dashboard interattiva
- Vista mensile con navigazione tra periodi
- Riepilogo: saldo corrente, entrate totali, uscite totali
- Stima del saldo a fine mese basata sui trend di spesa
- Grafico a barre per entrate/uscite su finestre temporali (periodi da 3 giorni)
- Elenco movimenti recenti con categorizzazione
- Monitoraggio budget in tempo reale con barre di avanzamento e avvisi visivi

### Gestione movimenti
- Registrazione completa di entrate e uscite
- Categorizzazione dei movimenti (es. Alimentari, Trasporti, Stipendio)
- Tracciamento di data, importo, titolo e metodo di pagamento
- Modifica ed eliminazione dei movimenti esistenti
- Tabella filtrabile per ricerca transazioni
- Validazione degli input

### Budget planning
- Definizione budget mensili per categoria
- Monitoraggio in tempo reale della spesa per categoria
- Calcolo automatico di speso, residuo e percentuale di utilizzo
- Avvisi visivi per budget prossimi al limite o superati
- Notifiche configurabili per superamento budget
- Disattivazione notifiche per singola categoria

### Report e analytics
- Grafico a torta per distribuzione spese per categoria
- Grafico temporale (area chart) per trend mensili
- Selezione periodo (ultimi 6 mesi, ultimo anno, completo)
- Modulo di previsione con calcolo di:
  - saldo stimato a fine mese
  - medie giornaliere di entrate/uscite
  - proiezione dei totali
  - giorni rimanenti
- Identificazione della categoria con maggiore spesa nel periodo
- Tooltip su grafici e animazioni per una migliore esperienza utente

### Gestione account
- Autenticazione utente
- Registrazione nuovi utenti con validazione
- Dati isolati per utente (multi-utenza)
- Parametri database configurabili dall’applicazione

### UI/UX
- Interfaccia moderna e coerente
- Tema selezionabile (8 temi: Cupertino Light/Dark, Nord Light/Dark, Primer Light/Dark, Dracula)
- Animazioni su grafici e transizioni
- Menu collassabile
- Layout adattivo alle dimensioni della finestra

## Screenshot

<img width="1920" height="1000" alt="Screenshot (1)" src="https://github.com/user-attachments/assets/a85edb41-05d0-4f64-ac71-f85fb530ed01" />
<img width="1920" height="1004" alt="Screenshot (3)" src="https://github.com/user-attachments/assets/e62f108e-9624-4a94-ae5b-4a8f7b0293a0" />
<img width="1920" height="993" alt="Screenshot (8)" src="https://github.com/user-attachments/assets/91632d5e-70c7-4eba-9398-f34b41fe28a8" />
<img width="1920" height="1003" alt="Screenshot (6)" src="https://github.com/user-attachments/assets/6d853eb1-2d47-43b3-8eba-056e4cc0cca2" />
<img width="1920" height="453" alt="Screenshot (7)" src="https://github.com/user-attachments/assets/a40a8b57-22ce-4a90-8c75-136d4f811f50" />

## Tecnologie utilizzate

### Backend
- **Java 14+** - Linguaggio di programmazione principale
- **JavaFX** - Framework UI per interfaccia grafica
- **MySQL 8.0+** - Database relazionale per persistenza dati
- **JDBC** - Connettività database
- **MySQL Connector/J 8.0.27** - Driver JDBC per MySQL

### Pattern e architetture
- **MVC (Model-View-Controller)** - Separazione tra logica di business e presentazione
- **DAO (Data Access Object)** - Astrazione dell’accesso ai dati
- **Factory Pattern** - Gestione delle istanze DAO
- **Observer Pattern** - Gestione degli eventi UI con JavaFX Properties
- **Singleton** - Configurazione database centralizzata

### Testing
- **JUnit 5** - Framework di testing
- **Custom Test Runner** - Runner minimale per esecuzione test

### Build e sviluppo
- **Ant** - Build automation
- **IntelliJ IDEA / Eclipse** - IDE supportati
- **MySQL Workbench** - Design e gestione database

## Architettura del progetto

Il progetto adotta un’architettura MVC con accesso ai dati tramite DAO. La configurazione di connessione al database è centralizzata e riutilizzata attraverso un approccio singleton. Le viste JavaFX e i controller gestiscono la presentazione e l’interazione con l’utente, mantenendo separata la logica applicativa.

## Prerequisiti

Assicurati di avere installato:

- **Java JDK 14 o superiore**
  ```bash
  java -version  # Deve mostrare 14+
  ```

- **JavaFX SDK** (se non incluso nel JDK)
  - Download: https://openjfx.io/

- **MySQL 8.0 o superiore**
  ```bash
  mysql --version  # Deve mostrare 8.0+
  ```

- **IDE** (opzionale ma consigliato)
  - IntelliJ IDEA, Eclipse o NetBeans
  - Con supporto JavaFX

## Installazione

### 1) Clona il repository
```bash
git clone https://github.com/Wuming9472/Personal-Finance-Desktop-Application.git
cd Personal-Finance-Desktop-Application
```

### 2) Configura le dipendenze
Assicurati che la libreria MySQL Connector sia presente in `lib/`:
```bash
ls lib/
# Dovresti vedere: mysql-connector-java-8.0.27.jar
```

### 3) Importa il progetto nell’IDE

#### IntelliJ IDEA
1. **File → Open** → seleziona la directory del progetto  
2. Imposta **SDK Java 14+**  
3. Aggiungi JavaFX al classpath:
   - **File → Project Structure → Libraries → + → Java**
   - Seleziona la directory `lib` di JavaFX SDK
4. Configura la Run Configuration:
   - **Main class**: `it.unicas.project.template.address.MainApp`
   - **VM Options**:
     ```
     --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
     ```

#### Eclipse
1. **File → Import → Existing Projects into Workspace**
2. Seleziona la directory del progetto
3. Aggiungi JavaFX e MySQL Connector al Build Path
4. **Run As → Java Application → MainApp**

## Configurazione database

### 1) Crea il database
```bash
mysql -u root -p
```

Esegui lo script:
```sql
source /path/to/Personal-Finance-Desktop-Application/PersonalFinanceDB.sql
```

Oppure importa manualmente:
```bash
mysql -u root -p < PersonalFinanceDB.sql
```

### 2) Verifica la creazione
```sql
USE personal_finance_db;
SHOW TABLES;
```

Dovresti vedere:
- `users`
- `categories`
- `movements`
- `budgets`
- `security_questions`

### 3) Configura le credenziali
Al primo avvio, l’applicazione mostra un dialog per configurare:
- Host (default `localhost`)
- Schema `personal_finance_db`
- Username MySQL
- Password MySQL

Le credenziali vengono salvate in `DAOMySQLSettings` per gli avvii successivi.

## Esecuzione

### Da IDE
1. Esegui la classe `MainApp.java`
2. Si apre la schermata di login
3. Registra un nuovo account o accedi con credenziali esistenti

### Da riga di comando
```bash
# Compila (se non già compilato)
javac -d bin -cp "lib/*:src" src/it/unicas/project/template/address/**/*.java

# Esegui
java -cp "bin:lib/*" --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     it.unicas.project.template.address.MainApp
```

### Con Ant
```bash
ant -f build/build.xml
```

## Struttura del database

### Schema: `personal_finance_db`

#### Tabella `users`
Memorizza gli utenti registrati.

| Campo    | Tipo         | Descrizione          |
|----------|--------------|----------------------|
| user_id  | INT (PK, AI) | ID univoco utente    |
| username | VARCHAR(16)  | Nome utente (unique) |
| password | VARCHAR(32)  | Password in chiaro*  |

Nota sicurezza: la password è attualmente memorizzata in chiaro. Per ambienti di produzione è necessario implementare hashing (es. BCrypt o SHA-256 con salt).

#### Tabella `categories`
Categorie predefinite per classificare i movimenti.

| Campo       | Tipo         | Descrizione                      |
|------------|--------------|----------------------------------|
| category_id | INT (PK, AI) | ID univoco categoria             |
| name        | VARCHAR(100) | Nome (es. Alimentari, Stipendio) |

#### Tabella `movements`
Transazioni finanziarie degli utenti.

| Campo          | Tipo          | Descrizione                |
|----------------|---------------|----------------------------|
| movement_id    | INT (PK, AI)  | ID univoco movimento       |
| user_id        | INT (FK)      | Riferimento a `users`      |
| category_id    | INT (FK)      | Riferimento a `categories` |
| type           | VARCHAR(20)   | `Entrata` o `Uscita`       |
| date           | DATETIME      | Data transazione           |
| amount         | DECIMAL(10,2) | Importo                    |
| title          | VARCHAR(255)  | Descrizione/nota           |
| payment_method | VARCHAR(50)   | Metodo pagamento           |

#### Tabella `budgets`
Budget mensili per categoria.

| Campo       | Tipo          | Descrizione                      |
|-------------|---------------|----------------------------------|
| budget_id   | INT (PK, AI)  | ID univoco budget                |
| user_id     | INT (FK)      | Riferimento a `users`            |
| category_id | INT (FK)      | Riferimento a `categories`       |
| month       | INT           | Mese (1-12)                      |
| year        | INT           | Anno (aggiunto da applicazione*) |
| amount      | DECIMAL(10,2) | Limite budget                    |

Nota: il campo `year` è gestito dall’implementazione Java per supportare budget multi-anno.

#### Tabella `security_questions`
Domande di sicurezza per recupero password.

| Campo       | Tipo         | Descrizione          |
|-------------|--------------|----------------------|
| question_id | INT (PK, AI) | ID univoco domanda   |
| user_id     | INT (FK)     | Riferimento a `users`|
| question    | VARCHAR(32)  | Domanda di sicurezza |
| answer      | VARCHAR      | Risposta associata   |

### Relazioni
```text
users (1) ──< (N) movements
users (1) ──< (N) budgets
users (1) ──< (N) security_questions
categories (1) ──< (N) movements
categories (1) ──< (N) budgets
```

## Funzionalità dettagliate

### Dashboard

**Navigazione temporale**
- Pulsanti “Mese precedente” / “Mese successivo”
- Visualizzazione dinamica del periodo selezionato

**Riepilogo**
- Saldo: entrate - uscite (positivo/negativo con indicatori visivi)
- Entrate totali del mese
- Uscite totali del mese
- Previsione saldo: proiezione del saldo a fine mese (solo mese corrente) basata su medie giornaliere

**Grafico andamento (BarChart)**
- Confronto entrate vs uscite su finestre temporali di 3 giorni
- 10 periodi per copertura del mese
- Tooltip con entrate, uscite e saldo del periodo
- Animazioni e interazioni (hover) per migliorare la leggibilità

**Movimenti recenti**
- Lista scorrevole dei movimenti dell’ultimo mese
- Data, titolo (o categoria se titolo assente), importo

**Stato budget**
- Card per ciascun budget attivo con progressione spesa
- Indicatori: stabile, in esaurimento, superato

### Gestione movimenti

**Creazione movimento**
1. Seleziona “Nuovo movimento”
2. Compila il form:
   - Tipo (Entrata/Uscita)
   - Categoria
   - Data
   - Importo
   - Titolo (opzionale)
   - Metodo pagamento (opzionale)
3. Validazione (importo > 0, data valida, categoria selezionata)
4. Salvataggio su database e aggiornamento tabella

**Modifica movimento**
- Selezione dalla tabella, apertura dialog precompilato, aggiornamento su DB

**Eliminazione movimento**
- Selezione dalla tabella, conferma, rimozione su DB

**Notifiche budget**
- Su salvataggio di un’uscita: controllo budget per categoria
- Notifica con limite, spesa attuale e superamento
- Opzione per disattivare notifiche per la categoria
- Preferenze salvate in `budget_notifications.json`

### Budget planning
- Modifica importo limite per categoria e mese
- Avvisi visivi basati su soglie di utilizzo (es. <80%, 80–100%, >100%)

### Report e analytics

**PieChart**
- Distribuzione percentuale spese per categoria
- Tooltip con importo e percentuale
- Animazioni e interazioni

**Grafico temporale (SmoothAreaChart)**
- Serie entrate e uscite nel tempo
- Tooltip e animazioni

**Selezione periodo**
- Ultimi 6 mesi
- Ultimo anno
- Intero storico (se previsto)

**Pannello previsione (Forecast)**
Visibile solo se sono presenti dati sufficienti (almeno 7 giorni distinti con movimenti nel mese corrente).

- Periodo di calcolo: dal giorno 1 del mese corrente fino a oggi
- Saldo stimato a fine mese:
  - `SaldoStimato = EntrateTotali − SpeseProiettateTotali`
- Giorni trascorsi:
  - `GiorniTrascorsi = giorno_corrente_del_mese`
- Giorni rimanenti:
  - `GiorniRimanenti = GiorniNelMese − GiorniTrascorsi`
- Media spese giornaliera:
  - `MediaSpeseGiornaliera = TotaleUscite / GiorniTrascorsi`
- Spese proiettate totali:
  - `SpeseProiettateTotali = TotaleUscite + (MediaSpeseGiornaliera * GiorniRimanenti)`
- Indicatore di stato:
  - verde: saldoStimato > 200
  - giallo: −100 ≤ saldoStimato ≤ 200
  - rosso: saldoStimato < −100
- Nota: la previsione è una stima basata sull’andamento medio del mese corrente fino a oggi e non considera movimenti futuri non ancora registrati.

**Categoria critica**
- Identifica la categoria con maggiore spesa nel periodo

**Risparmio stimato**
- Calcolo: entrate totali - uscite totali (nel periodo selezionato)

### Gestione account

**Profilo**
- Username e user ID
- Statistiche account (numero movimenti, eventuale data registrazione se disponibile)

**Impostazioni database**
- Modifica credenziali di connessione
- Test connessione prima del salvataggio
- Dialog dedicato

**Logout**
- Ritorno alla schermata di login e chiusura sessione

## Roadmap

Possibili evoluzioni:
- [ ] Hashing password (BCrypt)
- [ ] Export/import dati (CSV/Excel) per movimenti e budget
- [ ] Supporto multi-valuta
- [ ] Gestione ricevute (upload e associazione ai movimenti)
- [ ] Obiettivi di risparmio con tracking
- [ ] Notifiche desktop native
- [ ] Cambio tema automatico in base all’orario
- [ ] Previsioni con modelli ML
- [ ] Companion app mobile
- [ ] Suggerimento categorie automatico in base al titolo
- [ ] Gestione ricorrenze (stipendio, affitto, ecc.)

## Documentazione API

La documentazione JavaDoc è disponibile online:
- https://wuming9472.github.io/Personal-Finance-Desktop-Application/

Include dettagli per:
- Model (`User`, `Movimenti`, `Budget`, `Category`)
- Interfacce DAO e implementazioni MySQL
- Controller JavaFX
- Classi di utilità

## Licenza

Questo progetto è rilasciato sotto licenza **MIT**. Vedi il file [LICENSE](LICENSE) per dettagli.

```text
MIT License

Copyright (c) 2024 Wuming9472, ingridcristiano

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Autori

- **Wuming9472** - https://github.com/Wuming9472  
- **ingridcristiano** - https://github.com/ingridcristiano
