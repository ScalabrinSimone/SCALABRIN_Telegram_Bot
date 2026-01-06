package org.SimOneSpeedBot.database;

import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.*;

public class Database {
    private static Database instance; //NON VERSIONATO
    private Connection connection;

    private Database() throws SQLException {
        String url = "jdbc:sqlite:src/main/java/org/SimOneSpeedBot/database/usersDatabase.db"; //Inserisce il database nella cartella database.
        connection = DriverManager.getConnection(url); //Tabelle create da IntelliJ ultimate
        System.out.println("Connessione di Database"); //Debug
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
        try{
            if(!connection.isValid(5)){ //Controlla la connesione con timeout 5
                throw new SQLException();
            }
        }
        catch(SQLException e){
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
        }
        catch(SQLException e){
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

                return  null;
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
