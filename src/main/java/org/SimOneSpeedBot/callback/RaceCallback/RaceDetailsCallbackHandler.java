package org.SimOneSpeedBot.callback.RaceCallback;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.SimOneSpeedBot.api.ergast.GridAPI.GridPosition;
import org.SimOneSpeedBot.api.ergast.RaceResultAPI.Result;
import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.service.RaceService;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class RaceDetailsCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final int messageId;

    public RaceDetailsCallbackHandler(TelegramClient client, int messageId) {
        this.client = client;
        this.messageId = messageId;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        //Formato: race:details:year:round o race:grid:year:round o race:results:year:round
        if (data.startsWith("race:details:") || data.startsWith("race:grid:") || data.startsWith("race:results:")) {
            String[] parts = data.split(":");

            if (parts.length != 4) {
                return false;
            }

            String action = parts[1]; //details, grid, results
            int year = Integer.parseInt(parts[2]);
            int round = Integer.parseInt(parts[3]);

            switch (action) {
                case "details" -> showRaceDetails(chatId, year, round);
                case "grid" -> showStartingGrid(chatId, year, round);
                case "results" -> showRaceResults(chatId, year, round);
            }

            answerCallback(callbackQuery);
            return true;
        }

        return false;
    }

    //Mostra dettagli gara con bottoni per griglia/risultati
    private void showRaceDetails(long chatId, int year, int round) {
        ErgastAPI api = new ErgastAPI();
        String seasonInfo = api.fetchSeason(year);

        //Estrai la gara specifica (dovrai modificare fetchSeason per ritornare List<Race>)
        //Per ora uso un workaround
        String message = "<b>🏁 Gara Round " + round + "</b>\n\n<i>Caricamento dettagli...</i>";

        //Bottoni
        InlineKeyboardButton gridButton = InlineKeyboardButton.builder()
                .text("🏁 Griglia di Partenza")
                .callbackData("race:grid:" + year + ":" + round)
                .build();

        InlineKeyboardButton resultsButton = InlineKeyboardButton.builder()
                .text("🏆 Risultati Finali")
                .callbackData("race:results:" + year + ":" + round)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Torna alla Stagione")
                .callbackData("race:season:view:" + year)
                .build();

        InlineKeyboardRow row1 = new InlineKeyboardRow(gridButton);
        InlineKeyboardRow row2 = new InlineKeyboardRow(resultsButton);
        InlineKeyboardRow row3 = new InlineKeyboardRow(backButton);

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3))
                .build();

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(message)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }

    //Mostra griglia di partenza
    private void showStartingGrid(long chatId, int year, int round) {
        ErgastAPI api = new ErgastAPI();
        List<GridPosition> grid = api.fetchStartingGrid(year, round);

        String message = RaceService.formatStartingGrid(grid, "GP Round " + round, year, round);

        //Bottoni
        InlineKeyboardButton resultsButton = InlineKeyboardButton.builder()
                .text("🏆 Vai ai Risultati")
                .callbackData("race:results:" + year + ":" + round)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Torna ai Dettagli")
                .callbackData("race:details:" + year + ":" + round)
                .build();

        InlineKeyboardRow row1 = new InlineKeyboardRow(resultsButton);
        InlineKeyboardRow row2 = new InlineKeyboardRow(backButton);

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(message)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }

    //Mostra risultati finali
    private void showRaceResults(long chatId, int year, int round) {
        ErgastAPI api = new ErgastAPI();
        List<Result> results = api.fetchRaceResults(year, round);

        String message = RaceService.formatRaceResults(results, "GP Round " + round, year, round);

        //Bottoni
        InlineKeyboardButton gridButton = InlineKeyboardButton.builder()
                .text("🏁 Vai alla Griglia")
                .callbackData("race:grid:" + year + ":" + round)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Torna ai Dettagli")
                .callbackData("race:details:" + year + ":" + round)
                .build();

        InlineKeyboardRow row1 = new InlineKeyboardRow(gridButton);
        InlineKeyboardRow row2 = new InlineKeyboardRow(backButton);

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .build();

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(message)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }

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
