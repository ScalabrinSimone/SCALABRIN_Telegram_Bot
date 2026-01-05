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
        String year;

        //Se non ci sono argomenti, usa la stagione corrente
        if (args.length == 0) {
            year = String.valueOf(LocalDate.now().getYear());
        }
        else {
            year = args[0];
        }

        int yearInt = Integer.parseInt(year); //anno in int
        //Valida che sia un anno valido
        try {
            if (yearInt < 1950 || yearInt > LocalDate.now().getYear()) {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("❌ Anno non valido. Inserisci un anno tra 1950 e " + (LocalDate.now().getYear()))
                        .build();

                try {
                    client.execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        catch (NumberFormatException e) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Inserisci un anno valido (es: 2024, 2023)")
                    .build();

            try {
                client.execute(message);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        //Recupera le info della stagione
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
