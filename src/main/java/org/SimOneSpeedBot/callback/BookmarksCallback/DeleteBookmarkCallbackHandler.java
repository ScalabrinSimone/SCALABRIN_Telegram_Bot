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

public class DeleteBookmarkCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final int messageId;

    public DeleteBookmarkCallbackHandler(TelegramClient client, int messageId) {
        this.client = client;
        this.messageId = messageId;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        if (!data.startsWith("delete:")) {
            return false;
        }

        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        //Formato: delete:tipo:entityId
        String[] parts = data.split(":", 3);
        if (parts.length < 3) {
            return false;
        }

        String type = parts[1];
        String entityId = parts[2];

        //Elimina il bookmark
        boolean deleted = BookmarkManager.removeBookmark(userId, type, entityId);

        if (deleted) {
            //Controlla se ci sono ancora bookmark in quella categoria (evito problemi di non trovate categorie)
            List<Bookmark> remainingBookmarks = BookmarkManager.getBookmarkByType(userId, type);
            if (remainingBookmarks.isEmpty()) {
                //Non ci sono più bookmark in questa categoria, torna al menu principale bookmark
                InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                        .text("⬅️ Back To Bookmarks")
                        .callbackData("menu:utils:bookMark")
                        .build();

                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(new InlineKeyboardRow(backButton)))
                        .build();

                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("✅ Bookmark eliminato con successo!\n\nℹ️ Non ci sono più elementi in questa categoria.")
                        .replyMarkup(keyboard)
                        .build();

                try {
                    client.execute(edit);
                    answerCallback(callbackQuery, "✅ Eliminato");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //Torna alla lista dei bookmark di quella categoria
                InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                        .text("⬅️ Back To " + getCategoryName(type))
                        .callbackData("bookmark:" + type + ":1")
                        .build();

                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(new InlineKeyboardRow(backButton)))
                        .build();

                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("✅ Bookmark eliminato con successo!")
                        .replyMarkup(keyboard)
                        .build();

                try {
                    client.execute(edit);
                    answerCallback(callbackQuery, "✅ Eliminato");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            answerCallback(callbackQuery, "❌ Errore eliminazione");
        }

        return true;
    }

    private String getCategoryName(String type) {
        return switch (type) {
            case "driver" -> "🏎️ Piloti";
            case "constructor" -> "🏗️ Costruttori";
            case "season" -> "📅 Stagioni";
            default -> "";
        };
    }

    private void answerCallback(CallbackQuery callbackQuery, String text) {
        try {
            client.execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .text(text)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
