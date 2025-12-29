package org.SimOneSpeedBot.keyboard;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface MenuKeyboard {
    //Manda il messaggio
    Message sendInlineKeyboard(long chatId);
}
