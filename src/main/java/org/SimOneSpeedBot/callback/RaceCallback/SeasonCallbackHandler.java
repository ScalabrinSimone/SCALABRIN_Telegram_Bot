package org.SimOneSpeedBot.callback.RaceCallback;

import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.commands.CommandHub;
import org.SimOneSpeedBot.commands.ConstructorCommand;
import org.SimOneSpeedBot.commands.DriverCommand;
import org.SimOneSpeedBot.commands.SeasonCommand;
import org.SimOneSpeedBot.keyboard.MainMenuKeyboard;
import org.SimOneSpeedBot.keyboard.RaceKeyboards.SeasonKeyboard;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;

public class SeasonCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final int messageId;
    private final CommandHub hub;

    public SeasonCallbackHandler(TelegramClient client, int messageId, CommandHub hub) {
        this.client = client;
        this.mainMenuKeyboard = new MainMenuKeyboard(client);
        this.messageId = messageId;
        this.hub = hub;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        switch (data) {
            case "menu:race" -> {
                //Edita il messaggio per tornare al menu principale
                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("ℹ️ Seleziona la categoria di informazioni da ottenere:")
                        .replyMarkup(mainMenuKeyboard.getKeyboard())
                        .build();
                try {
                    client.execute(edit);
                } catch (Exception e) {
                    //Ignora l'errore se il messaggio è già uguale
                    if (!e.getMessage().contains("message is not modified")) {
                        e.printStackTrace();
                    }
                }
            }

            case "race:season:now" -> {
                SeasonCommand seasonCommand = (SeasonCommand) hub.getCommand("season");
                if (seasonCommand != null) {
                    seasonCommand.executeEdit(chatId, messageId, LocalDate.now().getYear());
                }
            }
            case "race:season:last" -> {
                SeasonCommand seasonCommand = (SeasonCommand) hub.getCommand("season");
                if (seasonCommand != null) {
                    seasonCommand.executeEdit(chatId, messageId, (LocalDate.now().getYear() - 1));
                }
            }

            case "race:season:select" -> {
                SeasonCommand seasonCommand = (SeasonCommand) hub.getCommand("season");
                if (seasonCommand != null) {
                    seasonCommand.executeEdit(chatId, messageId, selectedYear);
                }
            }

            default -> {return false;} //Se non inizia per queste, non riesce a gestirlo e ritorna false
        }

        answerCallback(callbackQuery);
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
