package org.example;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        //Recupero la configurazione
        MyConfiguration myConfiguration = MyConfiguration.getInstance();
        //System.out.println("Api key = " + myConfiguration.getProperty("API_KEY"));

        //Registro il bot Telegram
        try {
            String botToken = myConfiguration.getProperty("BOT_TOKEN");
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new SimOneSpeedBot());
            System.out.println("SimOneSpeedBot successfully started!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}