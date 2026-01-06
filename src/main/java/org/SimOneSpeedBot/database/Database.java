package org.SimOneSpeedBot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Database instance; //NON VERSIONATO
    private Connection connection;

    private Database() throws SQLException {
        String url = buildDbUrl(); //Inserisce il database nella cartella database.
        connection = DriverManager.getConnection(url); //Tabelle create da IntelliJ ultimate
        System.out.println("Connessione di Database"); //Debug
    }

    public static Database getInstance() throws SQLException {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    //Metodo pewr inserire il db nella cartella database (non root del progetto) - Generato dall'AI
    private String buildDbUrl() {
        try {
            //Prende il path del file .class di Database
            java.nio.file.Path classFile = java.nio.file.Paths.get(
                    Database.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
            //classFile è qualcosa tipo .../target/classes/  (non il singolo .class)

            //Ora aggiungo il package della classe + nome file DB
            //Se la tua classe è in package org.SimOneSpeedBot.database
            java.nio.file.Path dbDir = classFile
                    .resolve("org/SimOneSpeedBot/database");

            java.nio.file.Path dbPath = dbDir.resolve("usersDatabase.db");

            System.out.println("DB path: " + dbPath); //Debug

            return "jdbc:sqlite:" + dbPath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            //Fallback: db nella working dir
            return "jdbc:sqlite:usersDatabase.db";
        }
    }

    //Metodi di inserimento...
}
