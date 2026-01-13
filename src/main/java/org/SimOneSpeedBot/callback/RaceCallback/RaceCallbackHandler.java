package org.SimOneSpeedBot.callback.RaceCallback;

import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.commands.CommandHub;
import org.SimOneSpeedBot.commands.ConstructorCommand;
import org.SimOneSpeedBot.commands.DriverCommand;
import org.SimOneSpeedBot.keyboard.MainMenuKeyboard;
import org.SimOneSpeedBot.keyboard.RaceKeyboards.SeasonKeyboard;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

public class RaceCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final int messageId;
    private final CommandHub hub;
    private final Map<Long, String> userStates;

    public RaceCallbackHandler(TelegramClient client, int messageId, CommandHub hub, Map<Long, String> userStates) {
        this.client = client;
        this.mainMenuKeyboard = new MainMenuKeyboard(client);
        this.messageId = messageId;
        this.hub = hub;
        this.userStates = userStates;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        switch (data) {
            case "race:season" -> {
                //Rimuove lo stato per continuare a scrivere la season se attivo
                if (userStates.get(chatId) != null &&
                        (userStates.get(chatId).startsWith("AWAITING_SEASON_YEAR"))) {
                    userStates.remove(chatId);
                }
                SeasonKeyboard keyboard = new SeasonKeyboard(client, messageId);
                keyboard.editInlineKeyboard(chatId);
            }
            case "race:driver" -> {
                DriverCommand driverCommand = (DriverCommand) hub.getCommand("driver");
                if (driverCommand != null) {
                    driverCommand.executeEdit(chatId, messageId);
                }
            }

            case "race:team" -> {
                ConstructorCommand constructorCommand = (ConstructorCommand) hub.getCommand("constructor");
                if (constructorCommand != null) {
                    constructorCommand.executeEdit(chatId, messageId);
                }
            }

            default -> {
                return false;
            } //Se non inizia per queste, non riesce a gestirlo e ritorna false
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
