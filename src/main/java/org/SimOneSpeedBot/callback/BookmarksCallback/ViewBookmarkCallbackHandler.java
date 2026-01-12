package org.SimOneSpeedBot.callback.BookmarksCallback;

import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.database.Bookmarks.Bookmark;
import org.SimOneSpeedBot.database.Bookmarks.BookmarkManager;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class ViewBookmarkCallbackHandler implements CallbackHandler {
    private final TelegramClient telegramClient;
    private final int messageId;

    public ViewBookmarkCallbackHandler(TelegramClient telegramClient, int messageId) {
        this.telegramClient = telegramClient;
        this.messageId = messageId;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {
        String data =  callbackQuery.getData();

        if (!data.startsWith("view:")) {
            return false; //Deve iniziare per view il callback.
        }

        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        //Formato: view:tipo:entityId
        String[] parts = data.split(":", 3);
        if (parts.length < 3) {
            return false; //Gestisce massimo 3 parti del callback
        }

        String type = parts[1];
        String entityId = parts[2];

        //Recupera tutti i bookmarks di un tipo per trovare quello specifico
        List<Bookmark> bookmarks = BookmarkManager.getBookmarkByType(userId, type);
        Bookmark selectedBookmark = null; //Inizializzo il bookmark selezionato

        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getEntityId().equals(entityId)) {
                selectedBookmark = bookmark;
                break;
            }
        }

        if (selectedBookmark == null) {
            answerCallback(callbackQuery);
            return true;
        }

        //Mostra il messaggio salvato
        String messageText = selectedBookmark.getMessage();

        //Bottoni
        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder()
                .text("🗑️ Elimina")
                .callbackData("delete:" + type + ":" + entityId)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To " + getCategoryName(type))
                .callbackData("bookmark:" + type + ":1")
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        new InlineKeyboardRow(deleteButton),
                        new InlineKeyboardRow(backButton)
                ))
                .build();

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(messageText)
                .replyMarkup(keyboard)
                .build();

        try {
            telegramClient.execute(edit);
            answerCallback(callbackQuery);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }

        return true;
    }

    //Metodo per recuperare i nomi delle categorie (sono pochi, quindi lo ricreo)
    private String getCategoryName(String type) {
        return switch (type) {
            case "driver" -> "🏎️ Piloti";
            case "constructor" -> "🏗️ Costruttori";
            case "season" -> "📅 Stagioni";
            default -> "";
        };
    }

    private void answerCallback(CallbackQuery callbackQuery) {
        try {
            telegramClient.execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
