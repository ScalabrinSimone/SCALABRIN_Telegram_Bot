package org.example.keyboard;

import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface MenuKeyboard {
    //Manda il messaggio
    void sendInlineKeyboard(long chatId);
}
