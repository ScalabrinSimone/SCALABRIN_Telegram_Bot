package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.keyboard.MainMenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

public class StartCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, Integer> menuMessageIds; //Mappa per memorizzare il primo comando mandato
    private final String messageToSend = """
            <b>🏁 Benvenuto in <a href="t.me/SimOneSpeedBot">@SimOneSpeedBot</a>!</b>
            
            <i>Il tuo assistente personale per le statistiche della Formula 1</i>
            
            ━━━━━━━━━━━━━━━━━━━━
            
            📊 <b>Cosa puoi fare:</b>
            • <i>Cerca informazioni</i> su piloti, costruttori e stagioni
            • <i>Salva i tuoi preferiti</i> con i Bookmarks
            • <i>Esplora statistiche</i> dettagliate
            
            ⚙️ <b>Comandi disponibili:</b>
            <code>/start</code> - Mostra questo messaggio
            <code>/info</code> - Guida completa
            
            👇 Usa il menu qui sotto per iniziare (ha piú funzionalitá, come bookmarks e inserimento continuo 😉)!
            """;

    public StartCommand(TelegramClient client, Map<Long, Integer> menuMessageIds) {
        this.menuMessageIds = menuMessageIds;
        this.client = client;
    }

    @Override
    public void execute(long chatId, String[] args) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageToSend)
                .parseMode("HTML") //Per formattare in html
                .build();

        try {
            client.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Invio i pulsanti per una miglior UI
        MainMenuKeyboard mainMenuKeyboard = new MainMenuKeyboard(client);
        try {
            Message sent = mainMenuKeyboard.sendInlineKeyboard(chatId);
            /*//Debug per controllare il messaggio salvato
            if (sent != null) {
                menuMessageIds.put(chatId, sent.getMessageId());
                System.out.println("Salvato messageId: " + sent.getMessageId() + " per chatId: " + chatId);
            } else {
                System.out.println("ERRORE: sent è null!");
            }*/
            //Aggiunge a menuMessageIds per far si che venga eliminato anche il primo menui con il comando /showmenu
            if (sent != null) {
                menuMessageIds.put(chatId, sent.getMessageId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
