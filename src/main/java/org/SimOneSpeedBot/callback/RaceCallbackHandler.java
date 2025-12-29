package org.SimOneSpeedBot.callback;

import org.SimOneSpeedBot.keyboard.MainMenuKeyboard;
import org.SimOneSpeedBot.keyboard.RaceKeyboards.*;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class RaceCallbackHandler implements CallbackHandler{
    private final TelegramClient client;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final int messageId;

    public RaceCallbackHandler(TelegramClient client, int messageId) {
        this.client = client;
        this.mainMenuKeyboard = new MainMenuKeyboard(client);
        this.messageId = messageId;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

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

            case "race:after2023" -> {
                RaceAfter2023Keyboard keyboard = new RaceAfter2023Keyboard(client, messageId);
                keyboard.editInlineKeyboard(chatId);
            }
            case "race:pre2023" -> {
                RacePre2023Keyboard keyboard = new RacePre2023Keyboard(client, messageId);
                keyboard.editInlineKeyboard(chatId);
            }
            case "race:drivers" -> {
                RaceDriversKeyboard keyboard = new RaceDriversKeyboard(client, messageId);
                keyboard.editInlineKeyboard(chatId);
            }

            case "race:teams" -> {
                RaceTeamsKeyboard keyboard = new RaceTeamsKeyboard(client, messageId);
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
