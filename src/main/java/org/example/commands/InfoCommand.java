package org.example.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class InfoCommand implements Command {
    private final TelegramClient client;
    private final String messageToSend = """
            Il bot utilizza 2 API: 
            - Ergast (https://api.jolpi.ca/ergast/) --> Per i dati più generali, disponibili dal 1950
            ad oggi.
            
            - OpenF1 (https://openf1.org/) --> Per gare dal 2023 ad oggi, con dati e telemetrie in tempo reale.
            
            I dati possono essere salvati in un database SQLite e si può essere guidati dalla grafica oppure inserire un 
            comando con tutta la lista di cose da inserire (solo per alcune operazioni semplici, per tutti i comandi 
            completi usare l'interfaccia).
            
            Se non si sa come funziona o cosa fa un comando, si può scrivere /comando help. Il risultato sarà una breve
            guida a questo.
            
            Un esempio:
            /driver leclerc 2023
             
            Divertiti ad utilizzarlo!""";

    public InfoCommand(TelegramClient client) {
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
    }
}
