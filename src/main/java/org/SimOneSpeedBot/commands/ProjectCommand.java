package org.SimOneSpeedBot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class ProjectCommand implements Command {
    private final TelegramClient client;
    private final String messageToSend = """
            ℹ️ <b>Scopri il progetto su GitHub al seguente al link!</b>
            
            🔗 Link a <a href="https://github.com/ScalabrinSimone/SCALABRIN_Telegram_Bot/blob/master/README.md">GitHub pubblico</a>.
            """;

    public ProjectCommand(TelegramClient client) {
        this.client = client;
    }

    @Override
    public void execute(long chatId, String[] args) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageToSend)
                .parseMode("HTML")
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
                .text(messageToSend)
                .parseMode("HTML")
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
