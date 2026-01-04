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
                                
                                ℹ️ Puoi scrivere nome e cognome, cognome nome oppure cognome, sconsigliato se esistono piú piloti con lo stesso cognome (es: Verstappen ritorna il padre; max verstappen, Verstappen Max ritorna il figlio).
                                Se vieni dal menu puoi continuare a scrivere i nomi dei piloti, per smettere l'inserimento, premere il pulsante "Back To Race Menu".
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

            //Ergast -> nome e cognome in minuscolo
            String driverName = args.length > 1 ? String.join(" ", args[0]).toLowerCase() : "";
            String driverSurname = args.length == 1 ? String.join(" ", args[0]).toLowerCase() : String.join(" ", args[args.length - 1]).toLowerCase();
            String driverInfo = new ErgastAPI().fetchDriver(driverName, driverSurname);

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

    public void processDriver(long chatId, String driverName, String driverSurname, Integer messageId, boolean keepState) {
        if (!keepState) { //Rimuove lo stato solo se specificato, cosí da poter continuare a scrivere piloti
            userStates.remove(chatId);
        }

        //Gestisci nome driver --> nome_cognome in minuscolo.
        driverName = driverName.toLowerCase();
        driverSurname = driverSurname.toLowerCase();

        String driverInfo = new ErgastAPI().fetchDriver(driverName, driverSurname);

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