package org.SimOneSpeedBot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Database instance;
    private Connection connection;

    private Database() throws SQLException {
        String url = "jdbc:sqlite:database/usersDatabase.db";
        connection = DriverManager.getConnection(url);
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
