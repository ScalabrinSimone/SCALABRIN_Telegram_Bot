# 🏎️ SimOneSpeedBot

Bot Telegram per ottenere informazioni dettagliate sulla Formula 1, inclusi dati storici e stagioni complete dal 1950 ad oggi.

## 📱 Come usare il bot

Cerca **[@SimOneSpeedBot](https://t.me/SimOneSpeedBot)** su Telegram e avvia una chat con `/start`

***

## ✨ Funzionalità

### 🏁 Ricerca F1

#### Comandi diretti

- `/driver <nome> <cognome>` - Informazioni complete su un pilota (carriera, vittorie, podi, team, nazionalità)
- `/constructor <nome>` - Statistiche di una scuderia (vittorie, campionati, anni attivi, nazionalità)
- `/season <anno>` - Dettagli completi di una stagione (campioni piloti con podio, campione costruttori, calendario gare). Se lasciato vuoto restituisce la stagione corrente.
- `/info` - Informazioni sul bot e API utilizzate
- `/ping` - Verifica stato e tempi di risposta del bot
- `/showmenu` - Riapre il menu principale in fondo alla chat


#### Menu interattivo

Usa `/showmenu` per aprire il menu principale (o eliminare quello precedente e riportarlo in fondo).

Naviga tra le categorie usando tastiere inline:

**Race**

- **Driver**: Ricerca piloti con inserimento guidato (nome e cognome separati)
- **Constructor**: Ricerca scuderie con gestione input flessibile (es. `Red Bull`, `redbull`, `bull red`)
- **Season**: Esplora stagioni con:
    - Classifica finale piloti (podio completo)
    - Campione costruttori
    - Calendario completo con round e circuiti
    - Navigazione paginata (10 gare per pagina)
    - Dettagli singola gara: griglia di partenza, risultati finali, tempi qualifiche

**Bookmarks**

- Salva piloti, scuderie e stagioni preferite
- Accesso rapido ai contenuti salvati
- Gestione bookmark (visualizza ed elimina)

**Utils**

- Info sul bot e API utilizzate
- Ping per verificare stato e tempi di risposta

Il menu garantisce funzionalità avanzate come:

- Inserimento continuo di informazioni fino al ritorno al menu generale
- Sistema di salvataggio e recupero contenuti
- Navigazione contestuale con bottoni **Back**

***

## 🔖 Sistema Bookmarks

- Salva fino a 10 elementi per categoria (piloti, scuderie, stagioni)
- Persistenza su database SQLite
- Accesso rapido tramite menu dedicato
- Visualizzazione contenuti salvati con tutti i dettagli originali

***

## 🎯 Particolarità tecniche

### Architettura

- **Pattern Command**: Ogni comando è una classe separata registrata in un `CommandHub` per modularità e manutenibilità
- **Callback Handler**: Sistema modulare per gestire interazioni con tastiere inline tramite routing basato su prefissi
- **User State Manager**: Gestisce stati conversazionali per input multi-step (es. inserimento nome pilota → cognome)
- **Singleton Database**: Unica istanza condivisa per tutte le operazioni su SQLite con lazy initialization


### Database SQLite

Il bot utilizza un database SQLite locale, creato automaticamente alla prima esecuzione nella cartella `database/usersDatabase.db` (sia in locale che nel container Docker).

**Tabella `users`**

- `userId` (PRIMARY KEY): ID utente Telegram
- `username`: Username Telegram
- `firstName`: Nome
- `lastName`: Cognome
- `languageCode`: Codice lingua (es. `it`, `en`)
- `isBot`: Flag bot (0/1)
- `isPremium`: Flag Telegram Premium (0/1)

**Tabella `bookmarks`**

- `id` (PRIMARY KEY AUTOINCREMENT): ID univoco bookmark
- `userId` (FOREIGN KEY): Riferimento a `users(userId)` con relazione 1:N
- `type`: Categoria bookmark (`driver`, `constructor`, `season`)
- `entityId`: ID univoco entità (driverId, constructorId, anno)
- `entityName`: Nome leggibile dell’entità (es. “Lando Norris”, “McLaren”, “Stagione 2025”)
- `message`: Testo completo salvato (messaggio formattato da reinviare)
- `savedAt`: Timestamp salvataggio (default `CURRENT_TIMESTAMP`)

Vincoli:

- Relazione 1:N tra `users` e `bookmarks` (un utente può avere molti bookmarks)
- Possibile vincolo di unicità logica (gestito lato codice) per evitare duplicati per (`userId`, `type`, `entityId`)

***

## 🌐 API utilizzate

### Ergast API via Jolpica (Dati storici 1950-2025)

- **Endpoint base**: `https://api.jolpi.ca/ergast/`
- **Fornisce**:
    - Statistiche complete su piloti, scuderie, stagioni
    - Calendario gare, qualifiche, risultati e classifiche finali
- **Gestione errori**:
    - Retry logic per risposte 200 con dati vuoti
    - Parsing robusto con Gson e controlli sui campi principali (`MRData`, `StandingsTable`, ecc.)
- **Documentazione Ergast**: https://ergast.com/mrd/

***

## 📨 Gestione messaggi e logica bot

- **Edit invece di send**: Il menu principale viene modificato invece di inviare nuovi messaggi per ridurre spam
- **Message ID tracking**: Mappa `chatId → messageId` per modificare sempre lo stesso messaggio del menu
- **Callback acknowledgment**: Ogni interazione con button inline viene confermata a Telegram per evitare timeout
- **Paginazione intelligente**: Calendario stagioni diviso in pagine da 10 gare con navigazione avanti/indietro

***

## 🔍 Gestione API Ergast

- **Classi JSON separate**: Struttura modulare con package dedicati per ogni tipo di dato:
    - `SeasonAPI`, `DriverAPI`, `ConstructorAPI`, `GridAPI`, `RaceResultAPI`, `QualifyingAPI`, `StandingsAPI`, ecc.
- **Deserializzazione Gson**: Parsing JSON con mapping case-sensitive sui campi Ergast (`MRData`, `StandingsTable`, `StandingsLists`, …)
- **Input flessibile**:
    - Gestione di input “sporchi” (es. `Aston Martin`, `martin aston`, `aston`)
    - Normalizzazione e matching intelligente
- **Controlli su response 200**:
    - Verifica che i dati non siano vuoti anche se la response ha status 200
    - Gestione dei casi “nessun risultato” con messaggi chiari all’utente

***

## 🚧 Funzionalità in sviluppo

- **Notifiche gare**: Alert personalizzati per inizio gare e qualifiche
- **Statistiche utente**: Dashboard personale con cronologia ricerche
- **OpenF1 Integration**: Dati telemetrici e posizioni live durante le gare
- **Supporto multi-lingua**: Italiano e Inglese

*(La parte “Deploy 24/7” qui sotto è ora implementata con Docker + Render.)*

***

## ☁️ Deploy 24/7 con Docker + Render

Per tenere il bot online in modo continuo viene utilizzato un container Docker eseguito su una piattaforma cloud (es. Render).

### Cos’è Docker e cosa fa qui

**Docker** permette di impacchettare:

- il jar del bot,
- la JVM,
- le dipendenze,
- la configurazione di base

in un unico **container** eseguibile ovunque.
Nel tuo progetto il container:

- imposta la working directory su `/app`
- avvia il jar con `java -jar app.jar`
- crea/usa il database locale `database/usersDatabase.db` dentro `/app/database`
- legge la Telegram Bot Token da variabile d’ambiente

Questo garantisce che il comportamento sia lo stesso in locale e sul server.

### Servizi utilizzati

- **Telegram Bot API** – per ricevere aggiornamenti e inviare messaggi
- **Ergast API (via Jolpica)** – dati storici F1
- **Docker** – containerizzazione dell’applicazione
- **Render (o servizio analogo)** – esecuzione continua del container in cloud


### Dockerfile (semplificato)

Esempio coerente con la repo:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia il jar costruito da Maven
COPY target/SCALABRIN_Telegram_Bot-*.jar app.jar

# Crea (se serve) la cartella database a runtime
# (il codice Java comunque la crea se non esiste)

CMD ["java", "-jar", "app.jar"]
```


### Passi riassuntivi per il deploy

1. **Build del jar** in locale (o su CI):

```bash
mvn clean package
```

2. **Push su GitHub**
   Assicurarsi che `Dockerfile` e codice siano aggiornati.
3. **Creare servizio su Render**:
    - Tipo: Web Service / Background Worker con Docker
    - Collegare la repo GitHub `SCALABRIN_Telegram_Bot`
    - Impostare variabili d’ambiente:
        - `TELEGRAM_BOT_TOKEN` = token del bot
4. **Deploy**:
    - Render clona la repo, builda l’immagine Docker e avvia il container
    - All’avvio:
        - viene creato il DB `database/usersDatabase.db` in `/app`
        - il bot si registra su Telegram e inizia a ricevere update
5. **Verifica**:
    - Controllare log su Render (assenza errori SQLite, bot in ascolto)
    - Inviare `/start` a **@SimOneSpeedBot** e testare menù e comandi

***

## ⚙️ Setup locale

### Requisiti

- Java 21+
- Maven
- Account Telegram + Bot creato con [@BotFather](https://t.me/BotFather)


### Configurazione token

Imposta la Telegram Bot Token come variabile d’ambiente (consigliato):

```bash
export TELEGRAM_BOT_TOKEN=123456789:ABCDEF...
```

Il codice legge il token da `System.getenv("TELEGRAM_BOT_TOKEN")` (o dal metodo equivalente che hai usato nella classe `Main`).

### Database locale

Non serve creare a mano il DB:
all’avvio il codice Java:

- legge la working directory (`System.getProperty("user.dir")`)
- crea (se mancante) la cartella `database/`
- apre/crea il file `usersDatabase.db`:

```javascript
DbUrl = "jdbc:sqlite:database/usersDatabase.db";
```

- esegue `CREATE TABLE IF NOT EXISTS` per `users` e `bookmarks`


### Avvio

```bash
mvn clean package
java -jar target/SCALABRIN_Telegram_Bot-*.jar
```

Poi su Telegram:

- `/start`
- `/showmenu`
- prova ricerca piloti, costruttori, stagioni e bookmarks.

***

## 📊 Esempi di statistiche / query

Alcuni esempi di informazioni che il bot può fornire:

- Classifica piloti di una stagione (es. 2025) con punti finali e podio
- Campione costruttori di una stagione con punti totali
- Calendario completo delle gare:
    - nome Gran Premio
    - circuito
    - data
- Dettagli singola gara:
    - griglia di partenza
    - risultati finali
    - tempi di qualifica (Q1, Q2, Q3 se disponibili)

***

## 🛠️ Tecnologie

- **Java 21+**
- **TelegramBots API** (`telegrambots-longpolling` 10.2.0)
- **SQLite** (database embedded via JDBC)
- **Maven** (gestione dipendenze)
- **Gson** (deserializzazione JSON)
- **Docker** (containerizzazione per deploy 24/7)
- **Render / PaaS simile** (esecuzione container in cloud)

***

## 🏫 Progetto scolastico

Sviluppato come progetto di TPSIT per il quinto anno di Informatica - ITIS "G. Marconi"

**Obiettivi didattici**:

- Architettura software modulare e scalabile
- Integrazione API REST esterne
- Database relazionale con vincoli di integrità
- Design pattern (Command, Singleton, State)
- Gestione asincrona eventi e callback
- Deploy in produzione tramite Docker e piattaforma cloud

***

## 📝 Licenza

Progetto didattico - Simone Scalabrin © 2026