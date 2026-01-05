package org.SimOneSpeedBot.keyboard.RaceKeyboards;

import org.SimOneSpeedBot.keyboard.MenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class SeasonKeyboard implements MenuKeyboard {
    private final TelegramClient client;
    private final int messageId;

    public SeasonKeyboard(TelegramClient client, int messageId) {this.client = client; this.messageId = messageId;}

    @Override
    public Message sendInlineKeyboard(long chatId) {
        return editInlineKeyboard(chatId); //Chiama il metodo che modifica
    }

    public Message editInlineKeyboard(long chatId) {
        int currentYear = LocalDate.now().getYear(); //Prendo l'anno attuale
        //Crea i bottoni
        InlineKeyboardButton nowButton = InlineKeyboardButton.builder()
                .text("Stagione attuale - " + currentYear + " ⌚")
                .callbackData("race:season:now")
                .build();

        InlineKeyboardButton lastYearButton = InlineKeyboardButton.builder()
                .text("Stagione scorsa - " + (currentYear - 1) + " 🕰")
                .callbackData("race:season:last")
                .build();

        InlineKeyboardButton selectYearButton = InlineKeyboardButton.builder()
                .text("Seleziona una stagione 📅")
                .callbackData("race:season:select")
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Race Menu")
                .callbackData("menu:race")
                .build();

        //Crea la riga dei primi bottoni
        InlineKeyboardRow upperRow = new InlineKeyboardRow(
                List.of(nowButton)
        );
        //Crea la seconda riga di bottoni
        InlineKeyboardRow firstMiddleRow = new InlineKeyboardRow(
                List.of(lastYearButton)
        );
        //Crea la terza riga di bottoni
        InlineKeyboardRow secondMiddleRow = new InlineKeyboardRow(
                List.of(selectYearButton)
        );
        //Crea la quarta riga di bottoni
        InlineKeyboardRow bottomRow = new InlineKeyboardRow(
                List.of(backButton)
        );

        //Crea la tastiera
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(upperRow, firstMiddleRow, secondMiddleRow, bottomRow))
                .build();

        //Modifica il messaggio
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("ℹ️ Seleziona la stagione desiderata (sono giá presenti alcuni shortcut):")
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
