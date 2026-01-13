package org.SimOneSpeedBot.callback.RaceCallback;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.SimOneSpeedBot.api.ergast.GridAPI.GridPosition;
import org.SimOneSpeedBot.api.ergast.QualifyingAPI.QualifyingResult;
import org.SimOneSpeedBot.api.ergast.RaceResultAPI.Result;
import org.SimOneSpeedBot.api.ergast.SeasonAPI.Race;
import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.commands.CommandHub;
import org.SimOneSpeedBot.commands.SeasonCommand;
import org.SimOneSpeedBot.service.RaceService;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class RaceDetailsCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final int messageId;
    private final CommandHub hub;

    public RaceDetailsCallbackHandler(TelegramClient client, int messageId, CommandHub hub) {
        this.client = client;
        this.messageId = messageId;
        this.hub = hub;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        if (!data.startsWith("race:")) {
            return false;
        }

        //Controlla se viene da bookmark
        boolean fromBookmark = data.endsWith(":bookmark");
        if (fromBookmark) {
            data = data.replace(":bookmark", ""); //Rimuove il flag per processare normalmente
        }

        //Per tornare subito alla season selezionata
        if (data.startsWith("race:season:year:")) {
            //Formato: race:season:year:2024
            String[] parts = data.split(":");
            int year = Integer.parseInt(parts[3]);

            //Richiama direttamente SeasonCommand, come quando l'utente ha inserito l'anno
            SeasonCommand seasonCmd = (SeasonCommand) hub.getCommand("season");
            seasonCmd.processSeason(chatId, year, messageId, false);

            answerCallback(callbackQuery);
            return true;
        }

        String[] parts = data.split(":");
        if (parts.length != 4) {
            return false;
        }

        String action = parts[1]; //details, grid, results, qualy
        int year = Integer.parseInt(parts[2]);
        int round = Integer.parseInt(parts[3]);

        switch (action) {
            case "details" -> showRaceDetails(chatId, year, round, fromBookmark);
            case "grid" -> showStartingGrid(chatId, year, round, fromBookmark);
            case "results" -> showRaceResults(chatId, year, round, fromBookmark);
            case "qualy" -> showQualifying(chatId, year, round, fromBookmark);
            default -> { return false; }
        }

        answerCallback(callbackQuery);
        return true;
    }

    //Mostra dettagli gara con bottoni per griglia/risultati
    private void showRaceDetails(long chatId, int year, int round, boolean fromBookmark) {
        ErgastAPI api = new ErgastAPI();
        List<Race> races = api.fetchSeasonRaces(year);

        Race target = races.stream()
                .filter(r -> String.valueOf(round).equals(r.getRound()))
                .findFirst()
                .orElse(null);

        String message;
        if (target == null) {
            message = "❌ <b>Gara non trovata</b>\n\n<i>I dati per il round " + round + " non sono disponibili.</i>";
        } else {
            message = RaceService.formatRaceInfo(target);
        }

        String bookmarkSuffix = fromBookmark ? ":bookmark" : "";

        InlineKeyboardButton gridButton = InlineKeyboardButton.builder()
                .text("🏁 Griglia di Partenza")
                .callbackData("race:grid:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton qualyButton = InlineKeyboardButton.builder()
                .text("⏱️ Qualifiche")
                .callbackData("race:qualy:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton resultsButton = InlineKeyboardButton.builder()
                .text("🏆 Risultati Gara")
                .callbackData("race:results:" + year + ":" + round + bookmarkSuffix)
                .build();

        //Cambia il back in base alla provenienza
        InlineKeyboardButton backButton;
        if (fromBookmark) {
            backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Torna alla Stagione Salvata")
                    .callbackData("view:season:" + year)
                    .build();
        } else {
            backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Torna alla Stagione")
                    .callbackData("race:season:year:" + year)
                    .build();
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(gridButton));
        rows.add(new InlineKeyboardRow(qualyButton));
        rows.add(new InlineKeyboardRow(resultsButton));
        rows.add(new InlineKeyboardRow(backButton));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
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
    private void showStartingGrid(long chatId, int year, int round, boolean fromBookmark) {
        ErgastAPI api = new ErgastAPI();
        List<GridPosition> grid = api.fetchStartingGrid(year, round);

        String message = RaceService.formatStartingGrid(grid, "GP Round " + round, year, round);

        String bookmarkSuffix = fromBookmark ? ":bookmark" : "";

        InlineKeyboardButton resultsButton = InlineKeyboardButton.builder()
                .text("🏆 Vai ai Risultati")
                .callbackData("race:results:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton qualyButton = InlineKeyboardButton.builder()
                .text("⏱️ Vai alle Qualifiche")
                .callbackData("race:qualy:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Torna ai Dettagli")
                .callbackData("race:details:" + year + ":" + round + bookmarkSuffix)
                .build();

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(resultsButton));
        rows.add(new InlineKeyboardRow(qualyButton));
        rows.add(new InlineKeyboardRow(backButton));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
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
    private void showRaceResults(long chatId, int year, int round, boolean fromBookmark) {
        ErgastAPI api = new ErgastAPI();
        List<Result> results = api.fetchRaceResults(year, round);

        String message = RaceService.formatRaceResults(results, "GP Round " + round, year, round);

        String bookmarkSuffix = fromBookmark ? ":bookmark" : "";

        InlineKeyboardButton gridButton = InlineKeyboardButton.builder()
                .text("🏁 Vai alla Griglia")
                .callbackData("race:grid:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton qualyButton = InlineKeyboardButton.builder()
                .text("⏱️ Vai alle Qualifiche")
                .callbackData("race:qualy:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Torna ai Dettagli")
                .callbackData("race:details:" + year + ":" + round + bookmarkSuffix)
                .build();

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(gridButton));
        rows.add(new InlineKeyboardRow(qualyButton));
        rows.add(new InlineKeyboardRow(backButton));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
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

    //Mostra le qualifiche
    private void showQualifying(long chatId, int year, int round, boolean fromBookmark) {
        ErgastAPI api = new ErgastAPI();
        List<QualifyingResult> qualy = api.fetchQualifying(year, round);

        String message = RaceService.formatQualifying(qualy, "GP Round " + round, year, round);

        String bookmarkSuffix = fromBookmark ? ":bookmark" : "";

        InlineKeyboardButton gridButton = InlineKeyboardButton.builder()
                .text("🏁 Vai alla Griglia")
                .callbackData("race:grid:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton resultsButton = InlineKeyboardButton.builder()
                .text("🏆 Vai ai Risultati")
                .callbackData("race:results:" + year + ":" + round + bookmarkSuffix)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Torna ai Dettagli")
                .callbackData("race:details:" + year + ":" + round + bookmarkSuffix)
                .build();

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(gridButton));
        rows.add(new InlineKeyboardRow(resultsButton));
        rows.add(new InlineKeyboardRow(backButton));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(rows)
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
