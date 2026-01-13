package org.SimOneSpeedBot.database;

import org.telegram.telegrambots.meta.api.objects.User;

import java.io.File;
import java.sql.*;

public class Database {
    private static Database instance; //NON VERSIONATO
    private Connection connection;
    private static String DbUrl;

    /* Soluzione non adottata perché pattern singleton crea una istanza
    static {
        //Eseguito UNA SOLA VOLTA al caricamento della classe
        initializeDatabase(); //Mi assicuro di creare le tabelle (serve per evitare di creare a mano il database ogni volta che pullo il codice)
    }*/

    private Database() throws SQLException {
        //Directory di lavoro (locale: root progetto, server: /app). Prima era assoluta ma non c'èra nel server
        String workingDir = System.getProperty("user.dir");
        System.out.println("Working dir: " + workingDir); //Debug

        //Cartella e nome file DB RELATIVI alla working dir
        String dbFolder = "database";
        String dbFileName = "usersDatabase.db";

        //Costruisco il path completo del file (solo per creare la cartella in modo sicuro)
        File dir = new File(workingDir, dbFolder);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Impossibile creare la cartella database: " + dir.getAbsolutePath());
        }

        //URL JDBC relativo alla working dir (funziona sia locale che server)
        DbUrl = "jdbc:sqlite:" + dbFolder + "/" + dbFileName;

        connection = DriverManager.getConnection(DbUrl);
        System.out.println("Connessione di Database a: " + DbUrl);

        initializeDatabase(); //Viene eseguito una volta sola per pattern singleton
    }

    //Metodo per creare il database per la prima volta
    private static void initializeDatabase() {
        try (Statement stmt = getConnection().createStatement()) {

            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        userId INTEGER NOT NULL PRIMARY KEY,
                        username TEXT,
                        firstName TEXT NOT NULL,
                        lastName TEXT,
                        languageCode TEXT,
                        isBot INTEGER DEFAULT 0,
                        isPremium INTEGER DEFAULT 0
                    )
                    """;
            if (stmt.execute(createUsersTable)) {
                System.out.println("Users table creata correttamente.");
            } else {
                System.out.println("Users table giá presente.");
            }

            String createBookmarksTable = """
                    CREATE TABLE IF NOT EXISTS bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        type TEXT NOT NULL, -- "driver", "constructor", "season"
                        entityId TEXT NOT NULL, -- "hamilton", "ferrari", "2024"
                        entityName TEXT, -- "Lewis Hamilton", "Ferrari", "Stagione 2024"
                        message TEXT, -- Testo
                        savedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (userId) REFERENCES users(userId)
                    )
                    """;
            if (stmt.execute(createBookmarksTable)) {
                System.out.println("Bookmarks table creata correttamente.");
            } else {
                System.out.println("Bookmarks table giá presente.");
            }
            System.out.println("Tabelle DB inizializzate correttamente."); //Debug server https
        } catch (SQLException e) {
            System.err.println("Errore init DB: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DbUrl);
    }

    public static Database getInstance() throws SQLException {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }


    //Metodi di inserimento...
    //User
    public boolean isUserPresent(long userId) throws SQLException {
        try {
            if (!connection.isValid(5)) { //Controlla la connesione con timeout 5
                throw new SQLException();
            }
        } catch (SQLException e) {
            System.err.println("Errore di timeout del database: " + e.getMessage());
            throw new SQLException();
        }

        String query = "SELECT 1 FROM users WHERE userId = ? LIMIT 1"; //Seleziona 1 per efficienza e deve essercene 1
        //Uso try-with-resources per chiudere automaticamente PreparedStatement e ResultSet per evitare memory leak.
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); //Ritorna true se trova l'utente (1 = true)
            }
        }
    }

    public void insertUser(User utente) throws SQLException {
        try {
            if (!connection.isValid(5))
                throw new SQLException();
        } catch (SQLException e) {
            System.err.println("Errore nella connessione al database");
            return;
        }

        String query = "INSERT INTO users(userId, username, firstName, lastName, languageCode, isBot, isPremium) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, utente.getId()); //ID univoco utente
            stmt.setString(2, utente.getUserName()); //Username (es. @simone). Puó essere null
            stmt.setString(3, utente.getFirstName()); //Nome (sempre presente)
            stmt.setString(4, utente.getLastName()); //Cognome. Puó essere null
            stmt.setString(5, utente.getLanguageCode()); //Lingua (es. "it", "en"). Puó essere null
            stmt.setInt(6, Boolean.TRUE.equals(utente.getIsBot()) ? 1 : 0); //1 = true. Se è un bot
            stmt.setInt(7, Boolean.TRUE.equals(utente.getIsPremium()) ? 1 : 0); //1 = true. Se ha Telegram Premium

            stmt.executeUpdate();
            System.out.println("User inserito"); //Debug
        }
    }

    public User getUser(long userId) throws SQLException {
        try {
            if (!connection.isValid(5))
                throw new SQLException();
        } catch (SQLException e) {
            System.err.println("Errore di timeout del database: " + e.getMessage());
            throw new SQLException();
        }

        String query = "SELECT * FROM users WHERE userId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1, User.class); //Se esiste prende l'user
                }

                return null;
            }
        }
    }

    //Info User
    public void getUserInfos(User user) throws SQLException { //Sarebbe da rendere static
        try {
            if (!connection.isValid(5))
                throw new SQLException();
        } catch (SQLException e) {
            System.err.println("Errore di timeout del database: " + e.getMessage());
            throw new SQLException();
        }

        //Voglio predere tutte le cose che ha salvatao l'user (bookmarks), tramite le foreign ekys.
    }
}
