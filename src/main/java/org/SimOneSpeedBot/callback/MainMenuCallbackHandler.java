package org.SimOneSpeedBot.callback;

import org.SimOneSpeedBot.commands.InfoCommand;
import org.SimOneSpeedBot.commands.PingCommand;
import org.SimOneSpeedBot.keyboard.MainMenuKeyboard;
import org.SimOneSpeedBot.keyboard.RaceMenuKeyboard;
import org.SimOneSpeedBot.keyboard.UtilsMenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

public class MainMenuCallbackHandler implements CallbackHandler {
    private final TelegramClient client;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final int messageId;
    private final Map<Long, String> userStates;

    public MainMenuCallbackHandler(TelegramClient client, int messageId, Map<Long, String> userStates) {
        this.client = client;
        this.mainMenuKeyboard = new MainMenuKeyboard(client);
        this.messageId = messageId;
        this.userStates = userStates;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        if (!data.startsWith("menu:")) return false;

        switch (data) {
            case "menu:home" -> {
                //Edita il messaggio per tornare al menu principale
                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Seleziona una categoria per iniziare:")
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

            case "menu:utils" -> {
                //System.out.println("messageId salvato: " + messageId); //Debug per il message Id salvato
                UtilsMenuKeyboard keyboard = new UtilsMenuKeyboard(client, messageId);
                keyboard.editInlineKeyboard(chatId);
            }
            case "menu:utils:info" -> {
                new InfoCommand(client).executeEdit(chatId, messageId);
            }
            case "menu:utils:ping" -> {
                new PingCommand(client).executeEdit(chatId, messageId);
            }

            case "menu:race" -> {
                //Rimuove lo stato per continuare a scrivere il driver se attivo
                if (userStates.get(chatId) != null &&
                userStates.get(chatId).startsWith("AWAITING_DRIVER_NAME")){
                    userStates.remove(chatId);
                }

                RaceMenuKeyboard keyboard = new RaceMenuKeyboard(client, messageId);
                keyboard.editInlineKeyboard(chatId);
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
