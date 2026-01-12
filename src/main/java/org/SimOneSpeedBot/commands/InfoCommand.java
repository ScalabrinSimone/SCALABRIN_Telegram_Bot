package org.SimOneSpeedBot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class InfoCommand implements Command {
    private final TelegramClient client;
    private final String messageToSend = """
        <b>📖 Informazioni su <a href="t.me/SimOneSpeedBot">@SimOneSpeedBot</a></b>
        
        ━━━━━━━━━━━━━━━━━━━━
        
        <b>🏎️ MENU - Race Info</b>
        Cerca e visualizza informazioni su:
        
        <b>👤 Piloti</b>
        • Ricerca per lettera iniziale del cognome
        • Dettagli: nazionalità, data di nascita, numero
        • Link a Wikipedia per approfondimenti
        
        <b>🏗️ Costruttori</b>
        • Lista completa team F1 (attuali e storici)
        • Nazionalità e link ufficiali
        • Dati dal 1950 ad oggi
        
        <b>📅 Stagioni</b>
        • Consulta dati di stagioni specifiche
        • Circuiti, gare e risultati
        • Statistiche complete
        
        ━━━━━━━━━━━━━━━━━━━━
        <b>⚙️ MENU - Utils</b>
        Trova queste pagine e impostazioni (tra cui anche test di popup di errore) del bot non collegate con la formula 1, come:
        
        <b>💾 BOOKMARKS</b>
        Salva i tuoi elementi preferiti <u>(solo con menu!)</u>:
        • Organizzazione automatica per categoria
        • Massimo 10 elementi per pagina
        • Eliminazione rapida
        
        <b>Come salvare:</b>
        1. Cerca un pilota/costruttore/stagione
        2. Premi il pulsante <b>💾 Salva</b>
        3. Accedi ai salvati da <b>⚙️ Utils → 🔖 Bookmarks</b>
        
        ━━━━━━━━━━━━━━━━━━━━
        
        <b>👩‍💻 COMANDI</b>
        Tutti i comandi sono disponibili nella <b>tendina in basso a sinistra</b>, ma te li ho riportati anche qui, copiabili con un click 😲:
        <code>/start</code> - Mostra messaggio di benvenuto
        <code>/info</code> - Mostra questa guida
        <code>/showmenu</code> - Riporta il menu a bottoni in basso
        <code>/driver</code> *args/vuoto* - Ricerca info per un pilota, puoi cercarlo subito mettendo argomenti
        <code>/constructor</code> *args* - Ricerca info per una scuderia, puoi cercarla subito mettendo argomenti
        <code>/season</code> *args* - Ricerca info per una scuderia, puoi cercarla subito mettendo argomenti oppure lasciare vuoto e restituirá la stagione corrente
        <code>/ping</code> - Controlla se il bot é online
        <code>/project</code> - Ottieni le informazioni del progetto online
        
        ━━━━━━━━━━━━━━━━━━━━
        
        <i>💡 Suggerimento: Tutti i dati provengono dall'API <a href="https://api.jolpi.ca/ergast/">Ergast</a>, il reupload del database più completo per la Formula 1!</i>
        """;;

    public InfoCommand(TelegramClient client) {
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
