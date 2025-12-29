package org.example.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class PingCommand implements Command {
    private final TelegramClient client;
    private final String messageToSend = """
            Pong 🏓!
            Bot online con i seguenti paramentri:
            ChatId: """;

    public PingCommand(TelegramClient client) {
        this.client = client;
    }

    @Override
    public void execute(long chatId, String[] args) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageToSend + " " + chatId + ".")
                .build();

        try {
            client.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeEdit(long chatId, int messageId) {
        //Crea bottone per tornare al menu utils
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Torna al menu Utils")
                .callbackData("menu:utils")
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(List.of(backButton))))
                .build();

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(messageToSend + " " + chatId + ".")
                .replyMarkup(keyboard) //Aggiungi la keyboard con il bottone indietro
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }
}
