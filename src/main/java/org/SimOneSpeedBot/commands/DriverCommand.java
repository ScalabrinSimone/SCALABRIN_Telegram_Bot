package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.bot.SimOneSpeedBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, String> userStates; //chatId -> stato attuale. Serve per aspettare un
                                               //input dall'utente.
    private final String textToSend = """
                                ℹ️ Inserisci il nome del pilota Puoi scrivere nome e cognome separati 
                                (es: Max Verstappen) oppure il cognome del pilota (es: Verstappen)
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
        else { //Processa il nome del pilota
            processDriverName(chatId, String.join(" ", args), null); //Guarda come deve essere per l'api.
        }
    }

    public void processDriverName(long chatId, String driverName, InlineKeyboardMarkup keyboard) {
        userStates.remove(chatId);

        //String driverInfo = fetchDriverInfo(driverName); CHIAMA API!!!

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(driverInfo)
                .replyMarkup(createBackToRaceMenuButton())
                .build();

        try {
            client.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Metodo per creare il bottone per tornare al menu race
    private InlineKeyboardMarkup createBackToRaceMenuButton()
    {
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Race Menu")
                .callbackData("menu:race")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow(List.of(backButton));

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }
}
