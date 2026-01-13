package org.SimOneSpeedBot;

import com.sun.net.httpserver.HttpServer; //Fa parte di Java SE (JDK standard), è già incluso. Non è una libreria esterna
import org.SimOneSpeedBot.bot.SimOneSpeedBot;
import org.SimOneSpeedBot.service.MyConfiguration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        System.out.println("Main starting..."); //Debug per server https

        //Avvia server HTTP per keep-alive (Render)
        startHealthCheckServer();

        //Recupero la configurazione
        MyConfiguration myConfiguration = MyConfiguration.getInstance();

        //Registro bot
        try {
            String botToken = myConfiguration.getProperty("BOT_TOKEN");
            System.out.println("BOT_TOKEN length: " + (botToken != null ? botToken.length() : -1));

            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new SimOneSpeedBot());
            System.out.println("SimOneSpeedBot successfully started!");
        } catch (Exception e) {
            System.out.println("Errore in Main:");
            e.printStackTrace();
        }
    }

    private static void startHealthCheckServer() {
        try {
            //Render fornisce la porta tramite variabile ambiente PORT, default 8080
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            //Gestione del server hhtps -> generato da AI
            server.createContext("/health", exchange -> {
                String response = "SimOneSpeedBot is running!"; //Risposta al ping bot
                boolean isHead = "HEAD".equalsIgnoreCase(exchange.getRequestMethod());
                if (isHead) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            });

            server.setExecutor(null);
            server.start();
            System.out.println("Health check server started on port " + port);
        } catch (IOException e) {
            System.err.println("Errore avvio health check server: " + e.getMessage());
        }
    }
}