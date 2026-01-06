package org.SimOneSpeedBot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Database instance; //NON VERSIONATO
    private Connection connection;

    private Database() throws SQLException {
        String url = "jdbc:sqlite:src/main/java/org.SimOneSpeedBot/database/usersDatabase.db"; //Inserisce il database nella cartella database.
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
}
