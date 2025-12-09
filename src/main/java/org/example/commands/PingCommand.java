package org.example.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

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
}
