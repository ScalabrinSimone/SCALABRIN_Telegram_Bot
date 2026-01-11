package org.SimOneSpeedBot.callback.BookmarksCallback;

import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.database.Bookmarks.BookmarkManager;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class SaveCallbackHandler implements CallbackHandler {
    private final TelegramClient client;

    public SaveCallbackHandler(TelegramClient client) {
        this.client = client;
        //Voglio salvare anche il messaggio
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        if (!data.startsWith("save:")) {
            return false; //Se non é save: non é gestita da questa classe
        }

        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();
        int messageId = callbackQuery.getMessage().getMessageId();

        //Formato sempre del tipo save:tipo:entityId:entityName
        String[] parts = data.split(":", 4);
        if (parts.length < 4) {
            answerCallback(callbackQuery, "❌ Errore formato callback");
            return true; //Gestito ma con errore nel formato di callback
        }

        String type = parts[1]; //Driver, constructor o season
        String entityId = parts[2]; //Da api
        String entityName = parts[3];

        //Salva nel database
        boolean saved = BookmarkManager.saveBookmark(userId, type, entityId, entityName); //Salva il bookmark

        //Risposte al callback diverse se per Season (torna al suo menu) oppure per driver e constructor (race menu)
        if (saved && type.equals("season")) {
            //Disattiva il pulsante salva
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Back To Season Menu\n(Concludi Inserimento)")
                    .callbackData("race:season")
                    .build();

            InlineKeyboardButton savedButton = InlineKeyboardButton.builder()
                    .text("✅ Salvato")
                    .callbackData("saved") //Callback vuoto, non fa nulla, e non deve generare il popup di errore.
                    .build();

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(
                            new InlineKeyboardRow(savedButton),
                            new InlineKeyboardRow(backButton)
                    ))
                    .build();

            EditMessageReplyMarkup edit = EditMessageReplyMarkup.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .replyMarkup(keyboard)
                    .build();

            try {
                client.execute(edit);
                answerCallback(callbackQuery, "✅ Salvato con successo!");
            } catch (Exception e) {
                e.printStackTrace();
                answerCallback(callbackQuery, "❌ Errore salvataggio");
            }
        }
        else if(saved) //Altri casi dove non ci sono comunque problemi di salvataggio
        {
            //Disattiva il pulsante salva
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Back To Race Menu\n(Concludi Inserimento)")
                    .callbackData("menu:race")
                    .build();

            InlineKeyboardButton savedButton = InlineKeyboardButton.builder()
                    .text("✅ Salvato")
                    .callbackData("saved") //Callback vuoto, non fa nulla
                    .build();

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(
                            new InlineKeyboardRow(savedButton),
                            new InlineKeyboardRow(backButton)
                    ))
                    .build();

            EditMessageReplyMarkup edit = EditMessageReplyMarkup.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .replyMarkup(keyboard)
                    .build();

            try {
                client.execute(edit);
                answerCallback(callbackQuery, "✅ Salvato con successo!");
            } catch (Exception e) {
                e.printStackTrace();
                answerCallback(callbackQuery, "❌ Errore salvataggio");
            }
        }
        else {
            answerCallback(callbackQuery, "❌ Errore salvataggio");
        }

        return true; //Gestito
    }

    private void answerCallback(CallbackQuery callbackQuery, String text) {
        try {
            client.execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .text(text)
                            .showAlert(false)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
