package org.example.keyboard;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class MainMenuKeyboard implements  MenuKeyboard {

    private final TelegramClient client;

    public MainMenuKeyboard(TelegramClient client) {this.client = client;}

    @Override
    public void sendInlineKeyboard(long chatId) {

        //Crea i bottoni
        InlineKeyboardButton utilsButton = InlineKeyboardButton.builder()
                .text("Utils ⚙")
                .callbackData("menu:utils")
                .build();

        InlineKeyboardButton raceButton = InlineKeyboardButton.builder()
                .text("Let's race 🏎")
                .callbackData("menu:race")
                .build();

        //Crea una riga di bottoni
        InlineKeyboardRow row = new InlineKeyboardRow(
                List.of(utilsButton, raceButton)
        );

        //Crea la tastiera
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();

        //Crea il messaggio
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Seleziona una categoria per iniziare:")
                .replyMarkup(keyboard)
                .build();

        try {
            client.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public InlineKeyboardMarkup getKeyboard() {
        // Crea i bottoni
        InlineKeyboardButton utilsButton = InlineKeyboardButton.builder()
                .text("Utils ⚙")
                .callbackData("menu:utils")
                .build();

        InlineKeyboardButton raceButton = InlineKeyboardButton.builder()
                .text("Let's race 🏎")
                .callbackData("menu:race")
                .build();

        // Crea una riga di bottoni
        InlineKeyboardRow row = new InlineKeyboardRow(
                List.of(utilsButton, raceButton)
        );

        // Crea e ritorna la tastiera
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }
}