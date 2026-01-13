package org.SimOneSpeedBot.callback.BookmarksCallback;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
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

import java.util.ArrayList;
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
        String data = callbackQuery.getData();

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

        //Se é una stagione, gestisci diversamente
        if (type.equals("season")) {
            handleSeasonBookmark(callbackQuery, selectedBookmark);
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
                .parseMode("HTML")
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

    private void handleSeasonBookmark(CallbackQuery callbackQuery, Bookmark selectedBookmark) {
        long chatId = callbackQuery.getMessage().getChatId();
        String entityId = selectedBookmark.getEntityId();
        int year = Integer.parseInt(entityId);

        ErgastAPI api = new ErgastAPI();
        List<org.SimOneSpeedBot.api.ergast.SeasonAPI.Race> races = api.fetchSeasonRaces(year);

        List<InlineKeyboardRow> rows = new ArrayList<>();

        int maxRacesToShow = Math.min(10, races.size());
        for (int i = 0; i < maxRacesToShow; i++) {
            org.SimOneSpeedBot.api.ergast.SeasonAPI.Race race = races.get(i);

            String buttonText = "🏁 " + race.getRound() + " - " + race.getRaceName();
            InlineKeyboardButton raceButton = InlineKeyboardButton.builder()
                    .text(buttonText)
                    .callbackData("race:details:" + year + ":" + race.getRound() + ":bookmark") //Aggiunge :bookmark alla fine per tracciare che viene da bookmark
                    .build();

            rows.add(new InlineKeyboardRow(raceButton));
        }

        //Bottone elimina
        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder()
                .text("🗑️ Elimina")
                .callbackData("delete:season:" + entityId)
                .build();
        rows.add(0, new InlineKeyboardRow(deleteButton));

        //Bottone back che torna ai bookmarks
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To 📅 Stagioni")
                .callbackData("bookmark:season:1")
                .build();
        rows.add(new InlineKeyboardRow(backButton));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(selectedBookmark.getMessage())
                .parseMode("HTML")
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
