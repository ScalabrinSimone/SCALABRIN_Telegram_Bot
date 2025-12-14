package org.example.commands;

import org.example.Main;
import org.example.keyboard.MainMenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand implements Command {
    private final TelegramClient client;
    private final String messageToSend = """
            Bevenuto nel bot di Telegram della formula 1 creato da Scalabrin Simone!
            
            Cosa può fare questo bot?
            Scopri subito tutti i comandi dal menu a tendina in basso a sinistra della chat!
            
            Cosa utilizza e come funziona il seguente bot?
            Scopri la documentazione e il progetto sul github ufficiale:
            https://github.com/ScalabrinSimone/SCALABRIN_Telegram_Bot/tree/master
            
            Divertiti!
            Questo bot è gratis da utilizzare e non servono permessi di alcun tipo. Importalo nelle tue chat e chiunque
            potrà usarlo!""";

    public StartCommand(TelegramClient client) {
        this.client = client;
    }

    @Override
    public void execute(long chatId, String[] args) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageToSend)
                .build();

        try {
            client.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Invio i pulsanti per una miglior UI
        MainMenuKeyboard mainMenuKeyboard = new MainMenuKeyboard(client);
        mainMenuKeyboard.sendInlineKeyboard(chatId);
    }
}
