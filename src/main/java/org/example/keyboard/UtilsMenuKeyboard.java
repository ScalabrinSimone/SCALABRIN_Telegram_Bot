package org.example.keyboard;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class UtilsMenuKeyboard implements MenuKeyboard {
    private final TelegramClient client;
    private final int messageId;

    public UtilsMenuKeyboard(TelegramClient client, int messageId) {this.client = client; this.messageId = messageId;}

    @Override
    public void sendInlineKeyboard(long chatId) {

        //Crea i bottoni
        InlineKeyboardButton utilsButton = InlineKeyboardButton.builder()
                .text("Info ❓")
                .callbackData("menu:utils:info")
                .build();

        InlineKeyboardButton raceButton = InlineKeyboardButton.builder()
                .text("Ping 🏓")
                .callbackData("menu:utils:ping")
                .build();
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Main Menu")
                .callbackData("menu:home")
                .build();

        //Crea la riga dei primi bottoni
        InlineKeyboardRow upperRow = new InlineKeyboardRow(
                List.of(utilsButton, raceButton)
        );
        //Crea la seconda riga di bottoni
        InlineKeyboardRow bottomRow = new InlineKeyboardRow(
                List.of(backButton)
        );

        //Crea la tastiera
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(upperRow, bottomRow))
                .build();

        //Modifica il messaggio
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("Seleziona una utility:")
                .replyMarkup(keyboard)
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}