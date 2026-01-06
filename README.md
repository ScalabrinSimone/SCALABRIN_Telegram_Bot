# 🏎️ SimOneSpeedBot

Bot Telegram per ottenere informazioni dettagliate sulla Formula 1, inclusi dati storici e telemetrie live.

## 📱 Come usare il bot

Cerca **[@SimOneSpeedBot](https://t.me/SimOneSpeedBot)** su Telegram e avvia una chat con `/start`

## ✨ Funzionalità

### 🏁 Ricerca F1

#### Comandi diretti
- `/driver <nome>` - Informazioni complete su un pilota (carriera, vittorie, podi, team)
- `/constructor <nome>` - Statistiche di una scuderia (vittorie, campionati, anni attivi)
- `/season <anno>` - Dettagli di una stagione (campione piloti, campione costruttori, numero di gare). Se
lasciato senza argomenti restituisce quella piú recente.

#### Menu interattivo (`/showmenu` per eliminare quello precedente e riportarlo in fondo)
Naviga tra le categorie usando keyboard inline:
- **Race**: Ricerca piloti, scuderie e stagioni tramite menu
- **Utils**: Info sul bot e ping

Utilizzare il menu garantisce piú funzionalitá, come un inserimento continuo di informazioni
fino a quando l'uitente non torna al menu generale; oppure una 
funzionaliá per salvare e visualizzare i contenuti.

### 🔧 Utility
- `/info` - Informazioni sul bot e le API utilizzate
- `/ping` - Verifica lo stato e i tempi di risposta del bot

## 🎯 Particolarità tecniche

### Architettura
- **Pattern Command**: Ogni comando è una classe separata registrata in un `CommandHub`
- **Callback Handler**: Sistema modulare per gestire le interazioni con keyboard inline
- **User State Manager**: Gestisce stati conversazionali per input multi-step (es. inserimento nome pilota)
- **Singleton Database**: Unica istanza condivisa per tutte le operazioni sul database

### Database SQLite
Struttura con foreign key per relazioni tra entità:

**Tabella `users`**

***Altre tabelle in sviluppo***

### API utilizzate

#### Ergast API (Dati storici 1950-2024)

-   **Endpoint**:  `https://api.jolpi.ca/ergast/`

-   **Fornisce**: statistiche complete su piloti, scuderie, stagioni e gare

-   [Documentazione](https://ergast.com/mrd/)


#### OpenF1 (Dati live e telemetrie 2023-2025)

-   **Endpoint**:  `https://openf1.org/`

-   **Fornisce**: telemetrie in tempo reale, posizioni GPS, dati radio team

-   [Documentazione](https://openf1.org/)


### Gestione messaggi

-   **Edit invece di send**: Il menu principale viene modificato invece di inviare nuovi messaggi per ridurre lo spam

-   **Message ID tracking**: Mappa  `chatId -> messageId`  per modificare sempre lo stesso messaggio

-   **Callback acknowledgment**: Ogni interazione con button inline viene confermata a Telegram


### 🚧 Funzionalità in sviluppo

-   **Sistema Bookmarks**: Salvataggio di piloti, team e stagioni preferiti

-   **Notifiche gare**: Alert personalizzati per inizio gare e qualifiche

-   **Statistiche utente**: Dashboard personale con cronologia ricerche

-   **Deploy 24/7**: Pubblicazione su server cloud (Railway/Heroku) per disponibilità continua

-   **OpenF1 Integration**: Dati telemetrici e posizioni live durante le gare


### 🛠️ Tecnologie

-   Java 17+

-   TelegramBots API (libreria  `telegrambots-longpolling`)

-   SQLite (database embedded)

-   Maven (gestione dipendenze)

-   JDBC (connessione database)

---

## 🏫 Progetto scolastico

Sviluppato come progetto di TPSIT per il quinto anno di Informatica.

## 📝 Licenza

Progetto didattico - Simone Scalabrin © 2026  