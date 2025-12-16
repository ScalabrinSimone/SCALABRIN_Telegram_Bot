package org.example.callback;

import org.example.commands.InfoCommand;
import org.example.commands.PingCommand;
import org.example.keyboard.MainMenuKeyboard;
import org.example.keyboard.UtilsMenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MainMenuCallbackHandler implements CallbackHandler {

    private final TelegramClient client;
    private final MainMenuKeyboard mainMenuKeyboard;
    private final int messageId;

    public MainMenuCallbackHandler(TelegramClient client, int messageId) {
        this.client = client;
        this.mainMenuKeyboard = new MainMenuKeyboard(client);
        this.messageId = messageId;
    }

    @Override
    public boolean handle(CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        if (!data.startsWith("menu:")) return false;

        switch (data) {
            case "menu:home" -> {
                // Edita il messaggio per tornare al menu principale
                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text("Seleziona una categoria per iniziare:")
                        .replyMarkup(mainMenuKeyboard.getKeyboard()) // Metodo da creare
                        .build();
                try {
                    client.execute(edit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case "menu:utils" -> {
                UtilsMenuKeyboard keyboard = new UtilsMenuKeyboard(client, messageId);
                keyboard.sendInlineKeyboard(chatId);
            }
            case "menu:utils:info" -> {
                new InfoCommand(client).execute(chatId, new String[]{});
            }
            case "menu:utils:ping" -> {
                new PingCommand(client).execute(chatId, new String[]{});
            }
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
