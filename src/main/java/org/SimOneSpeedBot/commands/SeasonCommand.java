package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.Map;

public class SeasonCommand implements Command {
    private final TelegramClient client;
    private final String textToSend = """
                                🏁 Inserisci l'anno della stagione...
                                
                                ℹ️ Non scrivere anni superiori a quello attuale e ricorda che la prima stagione di formula 1 risale al 1950.
                                Scrivi solo il numero dell'anno.
                                """;

    public SeasonCommand(TelegramClient client) {
        this.client = client;
    }

    @Override
    public void execute(long chatId, String[] args) {
        String yearString;

        //Se non ci sono argomenti, usa la stagione corrente
        if (args.length == 0) {
            yearString = String.valueOf(LocalDate.now().getYear());
        } else {
            yearString = args[0].trim();
        }

        int yearInt;

        //Controlla che sia un numero
        try {
            yearInt = Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            SendMessage errorMsg = SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Input errato. Inserisci un anno valido (es: 2024, 2023)")
                    .build();

            try {
                client.execute(errorMsg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        //Controlla il range
        int currentYear = LocalDate.now().getYear();
        if (yearInt < 1950 || yearInt > currentYear) {
            SendMessage errorMsg = SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Anno non valido. Inserisci un anno tra 1950 e " + currentYear)
                    .build();

            try {
                client.execute(errorMsg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        //Anno valido -> recupera info
        String seasonInfo = new ErgastAPI().fetchSeason(yearInt);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(seasonInfo)
                .build();

        try {
            client.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
