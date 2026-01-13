package org.SimOneSpeedBot.callback.BookmarksCallback;

import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.commands.CommandHub;
import org.SimOneSpeedBot.commands.SeasonCommand;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class SeasonPaginationCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final int messageId;
    private final CommandHub hub;

    public SeasonPaginationCallbackHandler(TelegramClient client, int messageId, CommandHub hub) {
        this.client = client;
        this.messageId = messageId;
        this.hub = hub;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        //Gestisce season:page:year:pageNumber
        if (!data.startsWith("season:page:")) {
            return false;
        }

        String[] parts = data.split(":");
        if (parts.length != 4) {
            return false;
        }

        int year = Integer.parseInt(parts[2]);
        int page = Integer.parseInt(parts[3]);

        //Richiama SeasonCommand con la pagina specifica
        SeasonCommand seasonCmd = (SeasonCommand) hub.getCommand("season");
        if (seasonCmd != null) {
            seasonCmd.processSeasonWithPage(chatId, year, messageId, page);
        }

        answerCallback(callbackQuery);
        return true;
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