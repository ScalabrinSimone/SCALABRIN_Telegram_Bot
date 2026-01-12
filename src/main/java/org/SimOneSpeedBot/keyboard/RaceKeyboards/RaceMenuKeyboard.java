package org.SimOneSpeedBot.keyboard.RaceKeyboards;

import org.SimOneSpeedBot.keyboard.MenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class RaceMenuKeyboard implements MenuKeyboard {
    private final TelegramClient client;
    private final int messageId;

    public RaceMenuKeyboard(TelegramClient client, int messageId) {
        this.client = client;
        this.messageId = messageId;
    }

    @Override
    public Message sendInlineKeyboard(long chatId) {
        return editInlineKeyboard(chatId); //Chiama il metodo che modifica
    }

    public Message editInlineKeyboard(long chatId) {
        //Crea i bottoni
        InlineKeyboardButton seasonButton = InlineKeyboardButton.builder()
                .text("Stagione 🏆")
                .callbackData("race:season")
                .build();

        InlineKeyboardButton pilotaButton = InlineKeyboardButton.builder()
                .text("Pilota 🧑")
                .callbackData("race:driver")
                .build();

        InlineKeyboardButton teamButton = InlineKeyboardButton.builder()
                .text("Scuderia 🏠")
                .callbackData("race:team")
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Main Menu")
                .callbackData("menu:home")
                .build();

        //Crea la riga dei primi bottoni
        InlineKeyboardRow upperRow = new InlineKeyboardRow(
                List.of(seasonButton)
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
                .text("ℹ️ Seleziona la categoria di informazioni da ottenere:")
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
