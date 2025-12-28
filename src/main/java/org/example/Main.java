package org.example;

import org.example.bot.SimOneSpeedBot;
import org.example.service.MyConfiguration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        //Recupero la configurazione
        MyConfiguration myConfiguration = MyConfiguration.getInstance();

        //Registro il bot Telegram
        try {
            String botToken = myConfiguration.getProperty("BOT_TOKEN"); //Recupero il contenuto dalla configuration.
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new SimOneSpeedBot());
            System.out.println("SimOneSpeedBot successfully started!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}