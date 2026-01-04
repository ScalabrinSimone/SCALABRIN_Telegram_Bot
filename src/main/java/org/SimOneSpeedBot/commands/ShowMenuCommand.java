package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.keyboard.MainMenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

public class ShowMenuCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, Integer> menuMessageIds;

    public ShowMenuCommand(TelegramClient client, Map<Long, Integer> menuMessageIds) {
        this.client = client;
        this.menuMessageIds = menuMessageIds;
    }

    @Override
    public void execute(long chatId, String[] args) {
        //Cancella il vecchio menu se esiste
        Integer oldMenuId = menuMessageIds.get(chatId);

        if (oldMenuId != null) {
            DeleteMessage delete = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(oldMenuId)
                    .build();

            try{
                client.execute(delete);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //Crea nuovo menu (non mantiene stato del menu per semplicitá)
        MainMenuKeyboard keyboard = new MainMenuKeyboard(client);
        Message newMenu = keyboard.sendInlineKeyboard(chatId);

        //Salva il nuovo messageId
        if (newMenu != null) {
            menuMessageIds.put(chatId, newMenu.getMessageId());
        }
    }
}
