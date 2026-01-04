package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;


import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DriverCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, String> userStates; //chatId -> stato attuale. Serve per aspettare un
                                               //input dall'utente.
    private final String textToSend = """
                                🏁 Inserisci il nome del pilota...
                                
                                ℹ️ Scrivi solo il cognome (es: Verstappen).
                                """;

    public DriverCommand(TelegramClient client, Map<Long, String> userStates) {
        this.client = client;
        this.userStates = userStates;
    }

    @Override
    public void execute(long chatId, String[] args) {
        if(args.length == 0) {
            //Imposta stato e chiede il nome
            userStates.put(chatId, "AWAITING_DRIVER_NAME");

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
        else { //Processa il nome del pilota se ci sono args
            //Controlla quanti args ha lo string[] (deve avere solo il cognome)
            String driverName = String.join(" ", args[0]).toLowerCase(); //Ergast -> Solo cognome in minuscolo
            String driverInfo = new ErgastAPI().fetchDriver(driverName);

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(driverInfo)
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
        userStates.put(chatId, "AWAITING_DRIVER_NAME_EDIT:" + messageId); //Stato speciale che contiene il messageId

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

    public void processDriver(long chatId, String driverSurname, Integer messageId) {
        userStates.remove(chatId);
        //Gestisci nome driver --> solo cognome in minuscolo.
        driverSurname = driverSurname.toLowerCase();
        String driverInfo = new ErgastAPI().fetchDriver(driverSurname);

        if (messageId != null) {
            //Menu -> edita il messaggio con bottone back
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Back To Race Menu")
                    .callbackData("menu:race")
                    .build();

            //Bottone
            InlineKeyboardRow row = new InlineKeyboardRow(List.of(backButton));

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(row))
                    .build();

            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(driverInfo)
                    .replyMarkup(keyboard)
                    .build();

            try {
                client.execute(edit);
            } catch (Exception e) {
                if (!e.getMessage().contains("message is not modified")) {
                    e.printStackTrace();
                }
            }
        }
        else {
            //Comando -> manda nuovo messaggio senza bottoni
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(driverInfo)
                    .build();

            try {
                client.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
