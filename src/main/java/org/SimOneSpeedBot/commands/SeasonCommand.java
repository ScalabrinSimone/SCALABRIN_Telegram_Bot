package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

public class SeasonCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, String> userStates; //chatId -> stato attuale. Serve per aspettare un
                                               //input dall'utente.
    private final String textToSend = """
                                🏁 Inserisci l'anno della stagione...
                                
                                ℹ️ Non scrivere anni superiori a quello attuale e ricorda che la prima stagione di formula 1 risale al 1950.
                                Scrivi solo il numero dell'anno.
                                """;

    public SeasonCommand(TelegramClient client, Map<Long, String> userStates) {
        this.client = client;
        this.userStates = userStates;
    }

    @Override
    public void execute(long chatId, String[] args) {
        if(args.length == 0) {
            //Imposta stato e chiede il nome
            userStates.put(chatId, "AWAITING_SEASON_YEAR");

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(textToSend)
                    .build();

            try {
                client.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {

            //Ergast -> nome e cognome in minuscolo
            String year = String.join(" ", args[0]).toLowerCase();
            String seasonInfo = new ErgastAPI().fetchSeason(year);

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

    @Override
    public void executeEdit(long chatId, int messageId) {
        //Chiamato dal menu, imposta uno stato speciale
        userStates.put(chatId, "AWAITING_SEASON_YEAR_EDIT:" + messageId); //Stato speciale che contiene il messageId

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(textToSend)
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }
}
