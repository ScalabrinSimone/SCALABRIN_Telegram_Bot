# 🏎️ SimOneSpeedBot

Bot Telegram per ottenere informazioni dettagliate sulla Formula 1, inclusi dati storici e stagioni complete dal 1950 ad oggi.

## 📱 Come usare il bot

Cerca **[@SimOneSpeedBot](https://t.me/SimOneSpeedBot)** su Telegram e avvia una chat con `/start`

## ✨ Funzionalità

### 🏁 Ricerca F1

#### Comandi diretti
- `/driver <nome> <cognome>` - Informazioni complete su un pilota (carriera, vittorie, podi, team, nazionalità)
- `/constructor <nome>` - Statistiche di una scuderia (vittorie, campionati, anni attivi, nazionalità)
- `/season <anno>` - Dettagli completi di una stagione (campioni piloti con podio, campione costruttori, calendario gare). Se lasciato vuoto restituisce la stagione corrente.

#### Menu interattivo
Usa `/showmenu` per aprire il menu principale (o eliminare quello precedente e riportarlo in fondo).

Naviga tra le categorie usando tastiere inline:

**Race**
- **Driver**: Ricerca piloti con inserimento guidato (nome e cognome separati)
- **Constructor**: Ricerca scuderie con gestione input flessibile (es. "Red Bull", "redbull", "bull red")
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
- Navigazione contestuale con bottoni "Back"

### 🔖 Sistema Bookmarks
- Salva fino a 10 elementi per categoria (piloti, scuderie, stagioni)
- Persistenza su database SQLite
- Accesso rapido tramite menu dedicato
- Visualizzazione contenuti salvati con tutti i dettagli originali

### 🔧 Utility
- `/info` - Informazioni sul bot e API utilizzate
- `/ping` - Verifica stato e tempi di risposta del bot
- `/showmenu` - Riapre il menu principale in fondo alla chat

## 🎯 Particolarità tecniche

### Architettura
- **Pattern Command**: Ogni comando è una classe separata registrata in un `CommandHub` per modularità e manutenibilità
- **Callback Handler**: Sistema modulare per gestire interazioni con tastiere inline tramite routing basato su prefissi
- **User State Manager**: Gestisce stati conversazionali per input multi-step (es. inserimento nome pilota → cognome)
- **Singleton Database**: Unica istanza condivisa per tutte le operazioni su SQLite con lazy initialization

### Database SQLite
Struttura con foreign key per relazioni tra entità:

**Tabella `users`**
- `id` (PRIMARY KEY): Chat ID Telegram
- `username`: Nome utente Telegram
- `created_at`: Timestamp creazione

**Tabella `bookmarks`**
- `id` (PRIMARY KEY): ID univoco bookmark
- `user_id` (FOREIGN KEY): Riferimento a `users(id)` con `ON DELETE CASCADE`
- `type`: Categoria bookmark (`driver`, `constructor`, `season`)
- `identifier`: ID univoco entità (driverId, constructorId, anno)
- `content`: Testo completo salvato (HTML)
- `created_at`: Timestamp salvataggio
- **UNIQUE constraint**: `(user_id, type, identifier)` per evitare duplicati

### API utilizzate

#### Ergast API via Jolpica (Dati storici 1950-2025)
- **Endpoint**: `https://api.jolpi.ca/ergast/`
- **Fornisce**: Statistiche complete su piloti, scuderie, stagioni, gare, qualifiche, risultati e classifiche
- **Gestione errori**: Retry logic per risposte 200 con dati vuoti, parsing robusto con Gson
- [Documentazione](https://ergast.com/mrd/)

#### OpenF1 (Dati live e telemetrie 2023-2025) - *In sviluppo*
- **Endpoint**: `https://openf1.org/`
- **Fornisce**: Telemetrie in tempo reale, posizioni GPS, dati radio team
- [Documentazione](https://openf1.org/)

### Gestione messaggi
- **Edit invece di send**: Il menu principale viene modificato invece di inviare nuovi messaggi per ridurre spam
- **Message ID tracking**: Mappa `chatId → messageId` per modificare sempre lo stesso messaggio nel menu
- **Callback acknowledgment**: Ogni interazione con button inline viene confermata a Telegram per evitare timeout
- **Paginazione intelligente**: Calendario stagioni diviso in pagine da 10 gare con navigazione avanti/indietro

### Gestione API Ergast
- **Classi JSON separate**: Struttura modulare con package per ogni endpoint (SeasonAPI, DriverAPI, ConstructorAPI, GridAPI, RaceResultAPI, QualifyingAPI, StandingsAPI)
- **Deserializzazione Gson**: Parsing JSON con match case-sensitive dei campi (es. `MRData`, `StandingsTable`)
- **Input flessibile**: Algoritmi per gestire variazioni input utente (es. "Aston Martin", "martin aston", "aston")
- **Controlli response 200**: Verifica presenza dati effettivi anche con status code 200

### 🚧 Funzionalità in sviluppo
- **Notifiche gare**: Alert personalizzati per inizio gare e qualifiche
- **Statistiche utente**: Dashboard personale con cronologia ricerche
- **Deploy 24/7**: Pubblicazione su server cloud (Railway/Heroku) per disponibilità continua
- **OpenF1 Integration**: Dati telemetrici e posizioni live durante le gare
- **Supporto multi-lingua**: Italiano e Inglese

### 🛠️ Tecnologie
- **Java 17+**
- **TelegramBots API** (libreria `telegrambots-longpolling` 10.2.0)
- **SQLite** (database embedded)
- **Maven** (gestione dipendenze)
- **JDBC** (connessione database)
- **Gson** (deserializzazione JSON)

---

## 🏫 Progetto scolastico

Sviluppato come progetto di TPSIT per il quinto anno di Informatica - ITIS "G. Marconi"

**Obiettivi didattici**:
- Architettura software modulare e scalabile
- Integrazione API REST esterne
- Database relazionale con vincoli di integrità
- Design pattern (Command, Singleton, State)
- Gestione asincrona eventi e callback

## 📝 Licenza

Progetto didattico - Simone Scalabrin © 2026