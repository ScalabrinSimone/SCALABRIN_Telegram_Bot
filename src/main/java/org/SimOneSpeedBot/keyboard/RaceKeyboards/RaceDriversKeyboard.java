package org.SimOneSpeedBot.keyboard.RaceKeyboards;

import org.SimOneSpeedBot.api.F1APIs;
import org.SimOneSpeedBot.keyboard.MenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class RaceDriversKeyboard implements MenuKeyboard {
    private final TelegramClient client;
    private final int messageId;

    public RaceDriversKeyboard(TelegramClient client, int messageId) {this.client = client; this.messageId = messageId;}

    @Override
    public Message sendInlineKeyboard(long chatId) {
        return editInlineKeyboard(chatId); //Chiama il metodo che modifica
    }

    public Message editInlineKeyboard(long chatId) {
        F1APIs api = new  F1APIs(); //Volgio prendere  il driver

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Race Menu")
                .callbackData("menu:race")
                .build();

        //Crea la riga di bottoni
        InlineKeyboardRow Row = new InlineKeyboardRow(
                List.of(backButton)
        );

        //Crea la tastiera
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(Row))
                .build();

        //Modifica il messaggio
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("ℹ️ Inserisci il nome del pilota:")
                .replyMarkup(keyboard)
                .build();

        try {
            return (Message) client.execute(edit);
        } catch (Exception e) {
            //Ignora l'errore se il messaggio è già uguale
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
            return null; //Ritorna null se non è stato modificato
        }
    }
}
