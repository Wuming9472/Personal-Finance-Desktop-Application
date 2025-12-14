# 💰 BalanceSuite - Personal Finance Desktop Application

[![Java](https://img.shields.io/badge/Java-14+-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![JavaDoc](https://img.shields.io/badge/JavaDoc-API-blue.svg)](https://wuming9472.github.io/Personal-Finance-Desktop-Application/)

Un'applicazione desktop moderna e intuitiva per la gestione delle finanze personali, sviluppata in Java con JavaFX. **BalanceSuite** ti aiuta a tenere traccia delle tue entrate e uscite, pianificare budget mensili per categoria e visualizzare report dettagliati con grafici interattivi.

## 📋 Indice

- [Caratteristiche](#-caratteristiche)
- [Screenshot](#-screenshot)
- [Tecnologie Utilizzate](#-tecnologie-utilizzate)
- [Architettura del Progetto](#-architettura-del-progetto)
- [Prerequisiti](#-prerequisiti)
- [Installazione](#-installazione)
- [Configurazione Database](#-configurazione-database)
- [Esecuzione](#-esecuzione)
- [Struttura del Database](#-struttura-del-database)
- [Funzionalità Dettagliate](#-funzionalità-dettagliate)
- [Documentazione API](#-documentazione-api)
- [Licenza](#-licenza)

## ✨ Caratteristiche

### 🏠 Dashboard Interattiva
- **Vista mensile completa** con navigazione tra mesi
- **Riepilogo finanziario**: saldo corrente, entrate totali, uscite totali
- **Previsione intelligente** del saldo a fine mese basata sui trend di spesa
- **Grafico a barre animato** per visualizzare entrate/uscite per periodi (3 giorni)
- **Lista movimenti recenti** con icone colorate e categorie
- **Stato budget in tempo reale** con barre di progresso e alert visivi

### 💸 Gestione Movimenti
- **Registrazione completa** di entrate e uscite
- **Categorizzazione** dei movimenti (Alimentari, Trasporti, Stipendio, etc.)
- Tracciamento di **data, importo, titolo e metodo di pagamento**
- **Modifica ed eliminazione** facile dei movimenti esistenti
- **Tabella filtrabile** per ricercare transazioni specifiche
- **Validazione input** per garantire dati corretti

### 📊 Budget Planning
- **Impostazione budget mensili** per categoria
- **Monitoraggio in tempo reale** della spesa per ogni categoria
- **Calcolo automatico** di: importo speso, rimanente e percentuale utilizzata
- **Alert visivi** per budget in via di esaurimento o superati
- **Sistema di notifiche** configurabile per avvisi di superamento budget
- **Disattivazione notifiche** per singola categoria

### 📈 Report e Analytics
- **Grafico a torta interattivo** per distribuzione spese per categoria
- **Grafico a linea temporale** (area chart) per trend mensili
- **Selezione periodo personalizzato** (ultimi 6 mesi, 1 anno, tutto)
- **Previsione avanzata** con calcolo di:
  - Saldo stimato a fine mese
  - Media spese/entrate giornaliere
  - Proiezione totali
  - Giorni rimanenti
- **Categoria critica** (quella con più spese)
- **Tooltip dettagliati** su tutti i grafici
- **Animazioni fluide** per una UX moderna

### 👤 Gestione Account
- **Sistema di autenticazione** sicuro
- **Registrazione nuovi utenti** con validazione
- **Dashboard personalizzata** per utente
- **Impostazioni database** configurabili dall'app
- **Multi-utente**: ogni utente ha i propri dati isolati

### 🎨 UI/UX Moderna
- **Design pulito e moderno** con colori consistenti
- **Tema personalizzabile** (8 temi disponibili: Cupertino Light/Dark, Nord Light/Dark, Primer Light/Dark, Dracula)
- **Animazioni fluide** su grafici e transizioni
- **Menu collapsibile** per ottimizzare lo spazio
- **Responsive** e adattivo alle dimensioni della finestra
- **Icone e indicatori visivi** per feedback immediato

## 📸 Screenshot

<img width="1920" height="1000" alt="Screenshot (1)" src="https://github.com/user-attachments/assets/a85edb41-05d0-4f64-ac71-f85fb530ed01" />
<img width="1920" height="1004" alt="Screenshot (3)" src="https://github.com/user-attachments/assets/e62f108e-9624-4a94-ae5b-4a8f7b0293a0" />
<img width="1920" height="993" alt="Screenshot (8)" src="https://github.com/user-attachments/assets/91632d5e-70c7-4eba-9398-f34b41fe28a8" />
<img width="1920" height="1003" alt="Screenshot (6)" src="https://github.com/user-attachments/assets/6d853eb1-2d47-43b3-8eba-056e4cc0cca2" />
<img width="1920" height="453" alt="Screenshot (7)" src="https://github.com/user-attachments/assets/a40a8b57-22ce-4a90-8c75-136d4f811f50" />





## 🛠 Tecnologie Utilizzate

### Backend
- **Java 14+** - Linguaggio di programmazione principale
- **JavaFX** - Framework UI per interfaccia grafica moderna
- **MySQL 8.0+** - Database relazionale per persistenza dati
- **JDBC** - Connettività database
- **MySQL Connector/J 8.0.27** - Driver JDBC per MySQL

### Pattern e Architetture
- **MVC (Model-View-Controller)** - Separazione logica business/presentazione
- **DAO (Data Access Object)** - Astrazione accesso ai dati
- **Factory Pattern** - Gestione istanze DAO
- **Observer Pattern** - Gestione eventi UI con JavaFX Properties
- **Singleton** - Configurazione database centralizzata

### Testing
- **JUnit 5** - Framework di testing
- **Custom Test Runner** - Runner minimale per esecuzione test

### Build & Development
- **Ant** - Build automation
- **IntelliJ IDEA / Eclipse** - IDE supportati
- **MySQL Workbench** - Design e gestione database


## 📦 Prerequisiti

Prima di iniziare, assicurati di avere installato:

- **Java JDK 14 o superiore**
  ```bash
  java -version  # Deve mostrare 14+
  ```

- **JavaFX SDK** (se non incluso nel JDK)
  - Download: [https://openjfx.io/](https://openjfx.io/)

- **MySQL 8.0 o superiore**
  ```bash
  mysql --version  # Deve mostrare 8.0+
  ```

- **IDE** (opzionale ma consigliato)
  - IntelliJ IDEA, Eclipse o NetBeans
  - Con supporto JavaFX

## 🚀 Installazione

### 1. Clona il Repository

```bash
git clone https://github.com/Wuming9472/Personal-Finance-Desktop-Application.git
cd Personal-Finance-Desktop-Application
```

### 2. Configura le Dipendenze

Assicurati che la libreria MySQL Connector sia presente in `lib/`:

```bash
ls lib/
# Dovresti vedere: mysql-connector-java-8.0.27.jar
```

### 3. Importa il Progetto nell'IDE

#### IntelliJ IDEA:
1. File → Open → Seleziona la directory del progetto
2. Imposta SDK Java 14+
3. Aggiungi JavaFX al classpath:
   - File → Project Structure → Libraries → + → Java
   - Seleziona la directory `lib` di JavaFX SDK
4. Configura Run Configuration:
   - Main class: `it.unicas.project.template.address.MainApp`
   - VM Options:
     ```
     --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
     ```

#### Eclipse:
1. File → Import → Existing Projects into Workspace
2. Seleziona la directory del progetto
3. Aggiungi JavaFX e MySQL Connector al Build Path
4. Run As → Java Application → MainApp

## 🗄 Configurazione Database

### 1. Crea il Database

```bash
mysql -u root -p
```

```sql
source /path/to/Personal-Finance-Desktop-Application/PersonalFinanceDB.sql
```

Oppure importa manualmente:

```bash
mysql -u root -p < PersonalFinanceDB.sql
```

### 2. Verifica Creazione

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

### 3. Configura Credenziali

Al primo avvio, l'applicazione mostrerà un dialog per configurare:
- **Host**: `localhost` (default)
- **Schema**: `personal_finance_db`
- **Username**: il tuo username MySQL
- **Password**: la tua password MySQL

Le credenziali vengono salvate in `DAOMySQLSettings` per gli avvii successivi.

## ▶️ Esecuzione

### Da IDE
1. Esegui la classe `MainApp.java`
2. Apparirà la schermata di Login
3. Registra un nuovo account o accedi con credenziali esistenti

### Da Command Line

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

## 🗃 Struttura del Database

### Schema: `personal_finance_db`

#### Tabella `users`
Memorizza gli utenti registrati.

| Campo      | Tipo         | Descrizione            |
|------------|--------------|------------------------|
| user_id    | INT (PK, AI) | ID univoco utente      |
| username   | VARCHAR(16)  | Nome utente (unique)   |
| password   | VARCHAR(32)  | Password in chiaro*    |

> **Nota Sicurezza**: La password è attualmente in chiaro. Per produzione, implementare hashing (BCrypt, SHA-256).

#### Tabella `categories`
Categorie predefinite per classificare i movimenti.

| Campo       | Tipo         | Descrizione                   |
|-------------|--------------|-------------------------------|
| category_id | INT (PK, AI) | ID univoco categoria          |
| name        | VARCHAR(100) | Nome (es. Alimentari, Stipendio) |

#### Tabella `movements`
Transazioni finanziarie degli utenti.

| Campo          | Tipo           | Descrizione                      |
|----------------|----------------|----------------------------------|
| movement_id    | INT (PK, AI)   | ID univoco movimento             |
| user_id        | INT (FK)       | Riferimento a users              |
| category_id    | INT (FK)       | Riferimento a categories         |
| type           | VARCHAR(20)    | "Entrata" o "Uscita"             |
| date           | DATETIME       | Data transazione                 |
| amount         | DECIMAL(10,2)  | Importo                          |
| title          | VARCHAR(255)   | Descrizione/nota                 |
| payment_method | VARCHAR(50)    | Metodo pagamento (es. Carta)     |

#### Tabella `budgets`
Budget mensili per categoria.

| Campo       | Tipo          | Descrizione                     |
|-------------|---------------|---------------------------------|
| budget_id   | INT (PK, AI)  | ID univoco budget               |
| user_id     | INT (FK)      | Riferimento a users             |
| category_id | INT (FK)      | Riferimento a categories        |
| month       | INT           | Mese (1-12)                     |
| year        | INT           | Anno (aggiunto da applicazione*)|
| amount      | DECIMAL(10,2) | Limite budget                   |

> **Nota**: Il campo `year` è stato aggiunto nell'implementazione Java per supportare budget multi-anno.

#### Tabella `security_questions`
Domande di sicurezza in caso di password dimenticata.

| Campo       | Tipo          | Descrizione                     |
|-------------|---------------|---------------------------------|
| question_id | INT (PK, AI)  | ID univoco budget               |
| user_id     | INT (FK)      | Riferimento a users             |
| question    | VARCHAR(32)   | Domanda di sicurezza            |
| answer      | VARCHAR       | Risposta associata              |


### Relazioni

```
users (1) ──< (N) movements
users (1) ──< (N) budgets
users (1) ──< (N) security_questions
categories (1) ──< (N) movements
categories (1) ──< (N) budgets

```

## 🎯 Funzionalità Dettagliate

### Dashboard

**Navigazione Temporale**
- Bottoni "Mese Precedente" / "Mese Successivo"
- Visualizzazione dinamica del periodo selezionato

**Card Riepilogo**
- **Saldo**: Entrate - Uscite (verde se positivo, rosso se negativo)
- **Entrate Totali**: Somma di tutte le entrate del mese
- **Uscite Totali**: Somma di tutte le uscite del mese
- **Previsione Saldo**: Proiezione intelligente del saldo a fine mese
  - Visibile solo per il mese corrente
  - Basata su media spese/entrate giornaliere
  - Calcolo: `(EntrateMedie * GiorniRimanenti) - (SpeseMedie * GiorniRimanenti) + SaldoAttuale`

**Grafico Andamento (BarChart)**
- Entrate (verde) vs Uscite (rosse) per periodo di 3 giorni
- 10 periodi totali per coprire il mese
- Animazione di crescita delle barre dal basso
- Tooltip custom su hover con:
  - Entrate del periodo
  - Uscite del periodo
  - Saldo del periodo
- Effetto zoom leggero sulle barre al passaggio del mouse
- Area hover trasparente per migliore UX

**Ultimi Movimenti**
- Lista scrollabile dei movimenti dell'ultimo mese
- Icone colorate (verde per entrate, rosso per uscite)
- Data, titolo (o categoria se titolo vuoto), importo

**Stato Budget**
- Grid 2x2 con card per ogni budget attivo
- Barra di progresso colorata (verde < 80%, gialla 80-100%, rossa > 100%)
- Badge "!" per budget in esaurimento o superati
- Visualizzazione speso, rimanente e percentuale

### Gestione Movimenti

**Creazione Movimento**
1. Click su "Nuovo Movimento"
2. Dialog con form:
   - **Tipo**: ComboBox (Entrata/Uscita)
   - **Categoria**: ComboBox con categorie da DB
   - **Data**: DatePicker
   - **Importo**: TextField numerico
   - **Titolo**: TextField (opzionale)
   - **Metodo Pagamento**: TextField (opzionale)
3. Validazione:
   - Importo > 0
   - Data valida
   - Categoria selezionata
4. Salvataggio su DB e refresh tabella

**Modifica Movimento**
1. Seleziona movimento dalla tabella
2. Click su "Modifica"
3. Dialog precompilato con dati esistenti
4. Conferma modifica → Update su DB

**Eliminazione Movimento**
1. Seleziona movimento
2. Click su "Elimina"
3. Conferma → Delete da DB

**Notifiche Budget**
- Al salvataggio di un movimento di tipo "Uscita":
  - Controllo automatico se il budget della categoria è superato
  - Popup di notifica con:
    - Budget limite
    - Spesa attuale
    - Superamento
    - Opzione "Non mostrare più per questa categoria"
- Preferenze salvate in `budget_notifications.json`

### Budget Planning

**Modifica Budget**
1. Seleziona budget dalla tabella
2. Click su "Modifica"
3. Aggiorna importo limite
4. Update su DB

**Alert Visivi**
- Budget < 80%: Barra verde
- Budget 80-100%: Barra gialla
- Budget > 100%: Barra rossa + icona warning

### Report & Analytics

**Grafico a Torta (PieChart)**
- Distribuzione percentuale spese per categoria
- Colori distinti per categoria
- Tooltip con importo e percentuale
- Animazione di apparizione slices
- Effetto hover con zoom e evidenziazione
- Legend con nomi categorie

**Grafico Temporale (SmoothAreaChart)**
- Due serie: Entrate (verde) e Uscite (rosse)
- Asse X: Mesi
- Asse Y: Importo (€)
- Area riempita con gradiente
- Tooltip con valori esatti
- Animazione di disegno linea da sinistra a destra

**Selezione Periodo**
- ComboBox con opzioni:
  - Ultimi 6 mesi
  - Ultimo anno
- Refresh automatico dei grafici alla selezione

**Pannello Previsione (Forecast)**  
Visibile solo se ci sono abbastanza dati (almeno 7 **giorni diversi** con movimenti nel **mese corrente**)

- **Periodo di Calcolo**: dal giorno **1** del mese corrente **fino a oggi**  
- **Saldo Stimato**: saldo previsto a fine mese  
  `SaldoStimato = EntrateTotali − SpeseProiettateTotali`
- **Giorni Trascorsi**: numero di giorni di calendario dall’inizio del mese a oggi  
  `GiorniTrascorsi = giorno_corrente_del_mese`
- **Giorni Rimanenti**: giorni di calendario dalla data odierna fino alla fine del mese  
  `GiorniRimanenti = GiorniNelMese − GiorniTrascorsi`
- **Media Spese Giornaliera**: spesa media giornaliera del mese corrente  
  `MediaSpeseGiornaliera = TotaleUscite / GiorniTrascorsi`
- **Spese Proiettate Totali**: spese stimate a fine mese  
  `SpeseProiettateTotali = TotaleUscite + (MediaSpeseGiornaliera * GiorniRimanenti)`
- **Status Badge**: indicatore visivo dello stato previsto del saldo a fine mese  
  - verde = situazione stabile (saldoStimato > 200)  
  - giallo = attenzione (−100 ≤ saldoStimato ≤ 200)  
  - rosso = situazione critica (saldoStimato < −100)
- **Disclaimer**: la previsione è una stima basata sull’andamento medio delle entrate e uscite **del mese corrente fino a oggi** e **non considera movimenti futuri non ancora registrati**.


**Categoria Critica**
- Identifica la categoria con più spese nel periodo
- Visualizzata con importo totale

**Risparmio Stimato**
- Calcolo: `Entrate Totali - Uscite Totali` del periodo

### Account Management

**Visualizzazione Profilo**
- Username loggato
- User ID
- Statistiche account:
  - Numero movimenti totali
  - Data registrazione (se disponibile)


**Impostazioni Database**
- Modifica credenziali connessione
- Test connessione prima di salvare
- Dialog separato per sicurezza

**Logout**
- Ritorno alla schermata di login
- Pulizia sessione utente


## 🔮 Roadmap Future

Possibili migliori futuri:

- [ ] **Sicurezza Password**: Hashing password con BCrypt
- [ ] **Export/Import Dati**: Export CSV/Excel di movimenti e budget
- [ ] **Multi-Currency**: Supporto valute multiple
- [ ] **Ricevute**: Upload e associazione ricevute/fatture ai movimenti
- [ ] **Obiettivi di Risparmio**: Impostazione e tracking obiettivi finanziari
- [ ] **Notifiche Desktop**: Notifiche native OS per alert budget
- [ ] **Dark Mode Auto**: Cambio automatico tema in base a orario
- [ ] **Machine Learning**: Previsioni spese basate su ML
- [ ] **Mobile Companion App**: App mobile per inserimento rapido spese
- [ ] **Categorizzazione Automatica**: Suggerimento categorie basato su titolo
- [ ] **Ricorrenze**: Gestione movimenti ricorrenti (stipendio, affitto)



## 📖 Documentazione API

La documentazione JavaDoc completa di tutte le classi e metodi è disponibile online:

🔗 **[Consulta la JavaDoc](https://wuming9472.github.io/Personal-Finance-Desktop-Application/)**

Include documentazione dettagliata per:
- Model classes (`User`, `Movimenti`, `Budget`, `Category`)
- DAO interfaces e implementazioni MySQL
- Controller JavaFX
- Utility classes



## 📄 Licenza

Questo progetto è rilasciato sotto licenza **MIT**. Vedi il file [LICENSE](LICENSE) per dettagli.

```
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

## 👥 Autori

- **Wuming9472** - [GitHub Profile](https://github.com/Wuming9472)
- **ingridcristiano** - [GitHub Profile](https://github.com/ingridcristiano)

