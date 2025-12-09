package org.example.bot;

import org.example.commands.CommandHub;
import org.example.commands.InfoCommand;
import org.example.commands.PingCommand;
import org.example.commands.StartCommand;
import org.example.service.MyConfiguration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class SimOneSpeedBot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));
    private final CommandHub hub = new CommandHub();

    public SimOneSpeedBot() //Qui registro i vari comandi
    {
        hub.register("start", new StartCommand(telegramClient));
        hub.register("info", new InfoCommand(telegramClient));
        hub.register("ping", new PingCommand(telegramClient));
    }

    @Override
    public void consume(Update update)
    {
        //Se l'update ha un messaggio e quest'ultimo ha un testo:
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            boolean handled = hub.handle(messageText, chatId);

            if(!handled)
            {
                SendMessage message = SendMessage
                        .builder()
                        .chatId(chatId)
                        .text("Scusa, non riconosco il comando. Riprova")
                        .build();

                try {
                    telegramClient.execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}