package org.SimOneSpeedBot.keyboard.RaceKeyboards;

import org.SimOneSpeedBot.keyboard.MenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class RacePre2023Keyboard implements MenuKeyboard {
    private final TelegramClient client;
    private final int messageId;

    public RacePre2023Keyboard(TelegramClient client, int messageId) {this.client = client; this.messageId = messageId;}

    @Override
    public Message sendInlineKeyboard(long chatId) {
        return editInlineKeyboard(chatId); //Chiama il metodo che modifica
    }

    public Message editInlineKeyboard(long chatId) {
        //Crea i bottoni
        InlineKeyboardButton postButton = InlineKeyboardButton.builder()
                .text("Stagioni post 2023 ⌚")
                .callbackData("race:after2023")
                .build();

        InlineKeyboardButton preButton = InlineKeyboardButton.builder()
                .text("Stagioni pre 2023 🕰")
                .callbackData("race:pre2023")
                .build();

        InlineKeyboardButton pilotaButton = InlineKeyboardButton.builder()
                .text("Piloti 🧑")
                .callbackData("race:drivers")
                .build();

        InlineKeyboardButton teamButton = InlineKeyboardButton.builder()
                .text("Scuderie 🏠")
                .callbackData("race:teams")
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Race Menu")
                .callbackData("menu:race")
                .build();

        //Crea la riga dei primi bottoni
        InlineKeyboardRow upperRow = new InlineKeyboardRow(
                List.of(postButton, preButton)
        );
        //Crea la seconda riga di bottoni
        InlineKeyboardRow middleRow = new InlineKeyboardRow(
                List.of(pilotaButton, teamButton)
        );
        //Crea la terza riga di bottoni
        InlineKeyboardRow bottomRow = new InlineKeyboardRow(
                List.of(backButton)
        );

        //Crea la tastiera
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(upperRow, middleRow, bottomRow))
                .build();

        //Modifica il messaggio
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("ℹ️Seleziona la categoria di informazioni da ottenere:")
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
