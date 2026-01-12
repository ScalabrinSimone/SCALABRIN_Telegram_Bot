package org.SimOneSpeedBot.callback.BookmarksCallback;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.commands.CommandHub;
import org.SimOneSpeedBot.database.Bookmarks.Bookmark;
import org.SimOneSpeedBot.database.Bookmarks.BookmarkManager;
import org.SimOneSpeedBot.keyboard.MainMenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookmarkCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final int messageId;

    public BookmarkCallbackHandler(TelegramClient client, int messageId) {
        this.client = client;
        this.messageId = messageId;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        if (!data.startsWith("bookmark:")){
            return false;
        }

        //Formato: bookmark:tipo:pagina
        String[] parts = data.split(":");
        if (parts.length < 3) { //Alemno 3
            return false;
        }

        String type = parts[1];
        int page = Integer.parseInt(parts[2]); //Mi serve la pagina

        //Recupera tutti i bookmark per quella categoria
        List<Bookmark> allBookmarks = BookmarkManager.getBookmarkByType(userId, type);

        if (allBookmarks.isEmpty()) {
            answerCallback(callbackQuery); //Non dovrebbe succedere
            return true;
        }

        //Calcola paginazione (10 per pagina) - GENERATO CON AI
        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) allBookmarks.size() / itemsPerPage);

        //Assicurati che la pagina sia valida
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allBookmarks.size());
        List<Bookmark> pageBookmarks = allBookmarks.subList(startIndex, endIndex);

        //Categorie
        String categoryName = switch (type) {
            case "driver" -> "🏎️ Piloti";
            case "constructor" -> "🏗️ Costruttori";
            case "season" -> "📅 Stagioni";
            default -> type;
        };

        StringBuilder text = new StringBuilder(categoryName + " salvati (Pagina " + page + "/" + totalPages + " - 10 x pagina):\n\n");
        text.append("Totale: ").append(allBookmarks.size()).append(" elementi (slvati in ordine cronologico discendente)\n\n");

        List<InlineKeyboardRow> rows = new ArrayList<>();

        //Bottone per ogni bookmark
        for (Bookmark bookmark : pageBookmarks) {
            InlineKeyboardButton bookmarkButton = InlineKeyboardButton.builder()
                    .text(bookmark.getEntityName())
                    .callbackData("view:" + type + ":" + bookmark.getEntityId())
                    .build();

            rows.add(new InlineKeyboardRow(bookmarkButton));
        }

        //Paginazione
        List<InlineKeyboardButton> paginationButtons = new ArrayList<>();

        if (page > 1) {
            paginationButtons.add(InlineKeyboardButton.builder()
                    .text("⬅️ Indietro")
                    .callbackData("bookmark:" + type + ":" + (page - 1))
                    .build());
        }

        if (page < totalPages) {
            paginationButtons.add(InlineKeyboardButton.builder()
                    .text("Avanti ➡️")
                    .callbackData("bookmark:" + type + ":" + (page + 1))
                    .build());
        }

        if (!paginationButtons.isEmpty()) {
            rows.add(new InlineKeyboardRow(paginationButtons));
        }

        //Bottone back
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Bookmarks")
                .callbackData("menu:bookmark")
                .build();

        rows.add(new InlineKeyboardRow(backButton));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(text.toString())
                .replyMarkup(keyboard)
                .build();

        try {
            client.execute(edit);
            answerCallback(callbackQuery);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }

        return true;
    }

    //Ack per dire a telegram che ha ricevuto il callback
    private void answerCallback(CallbackQuery callbackQuery) {
        try {
            client.execute(
                    AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
