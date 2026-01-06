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

        String query = "SELECT * FROM users WHERE userId = ?";
        PreparedStatement stmt = null;

        try {
            stmt = connection.prepareStatement(query);
            stmt.setLong(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return true; //Se esiste un utente allora vuol dire che é gia presente nel databa: non devo aggiungerlo.
            }

        } catch (SQLException e) {
            System.err.println("Errore nella query");
            throw new SQLException();
        }

        return false; //Non esiste l'utente, sará da aggiungere.
    }

    public void insertUser(User utente) throws SQLException {
        try {
            if (!connection.isValid(5))
                throw new SQLException();
        } catch (SQLException e) {
            System.err.println("Errore nella connessione al database");
            return;
        }

        String query = "INSERT INTO users(userId, username, firstName, lastName, languageCode, isBot, isPremium) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(query);
            stmt.setLong(1, utente.getId()); //ID univoco utente
            stmt.setString(2, utente.getUserName()); //Username (es. @simone)
            stmt.setString(3, utente.getFirstName()); //Nome (sempre presente)
            stmt.setString(4, utente.getLastName()); //Cognome
            stmt.setString(5, utente.getLanguageCode()); //Lingua (es. "it", "en")
            stmt.setInt(6, utente.getIsBot() == true ? 1 : 0); //0 = false. Se è un bot
            stmt.setInt(7, utente.getIsPremium() == true ? 1 : 0); //0 = false. Se ha Telegram Premium

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nella query");
            throw new SQLException();
        }
    }
}
