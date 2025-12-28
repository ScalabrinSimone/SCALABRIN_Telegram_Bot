package org.example.keyboard;

import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface MenuKeyboard {
    //Manda il messaggio
    Message sendInlineKeyboard(long chatId);
}
