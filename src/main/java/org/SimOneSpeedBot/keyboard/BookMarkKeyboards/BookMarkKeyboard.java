package org.SimOneSpeedBot.keyboard.BookMarkKeyboards;

import org.SimOneSpeedBot.database.Bookmarks.BookmarkManager;
import org.SimOneSpeedBot.keyboard.MenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class BookMarkKeyboard implements MenuKeyboard {
    private final TelegramClient client;
    private final int messageId;
    private final User user;

    public BookMarkKeyboard(TelegramClient client, int messageId, User user) {
        this.client = client;
        this.messageId = messageId;
        this.user = user;
    }

    @Override
    public Message sendInlineKeyboard(long chatId) {
        return editInlineKeyboard(chatId); //Chiama il metodo che modifica
    }

    public Message editInlineKeyboard(long chatId) {
        //Dovrebbe crearli in base a quante info ha l'utente (gli passo il database e il suo metodo) e se ha troppe cose dentro mette frecce per navigare
        //Crea i bottoni

        //Recupera le categorie disponibili
        List<String> categories = BookmarkManager.getAvailableCategories(user.getId());

        //Crea la keyboard
        InlineKeyboardMarkup keyboard = null;

        //Righe
        List<InlineKeyboardRow> rows = new ArrayList<>();

        //Bottone back sempre presente
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Utils Menu")
                .callbackData("menu:utils")
                .build();

        //Se non ci sono bookmarks salvati
        if (categories.isEmpty()) {
            InlineKeyboardButton modificaButton = InlineKeyboardButton.builder()
                    .text("📭 Nessun Bookmark salvato")
                    .callbackData("saved") //Non da errori di alcun tipo
                    .build();

            //Crea la prima riga di bottoni
            rows.add(new InlineKeyboardRow(modificaButton));

            //Crea la seconda riga di bottoni
            rows.add(new InlineKeyboardRow(backButton));

            //Crea la tastiera
            keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(rows)
                    .build();
        } else //C'é almeno 1 bookmark salvato
        {
            for (String category : categories) {
                String categoryName = switch (category) //Prende il tipo di ogni categoria
                {
                    case "driver" -> "🏎️ Piloti";
                    case "constructor" -> "🏗️ Costruttori";
                    case "season" -> "📅 Stagioni";
                    default -> ""; //Metto solo perché lo seitch deve coprire tutte le possibilitá
                };

                //Aggiunge il bottone
                InlineKeyboardButton categoryButton = InlineKeyboardButton.builder()
                        .text(categoryName)
                        .callbackData("bookmark:" + category + ":1") //Formato: bookmark:tipo:pagina
                        .build();

                rows.add(new InlineKeyboardRow(categoryButton)); //Aggiunge il pulsante alla riga
            }

            rows.add(new InlineKeyboardRow(backButton)); //Aggiungo il bottone per tornare indietro

            //Crea la tastiera
            keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(rows)
                    .build();
        }

        //Modifica il messaggio sia che abbia piú o meno bottoni
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("<b>🏡 Benvenuto nei tuoi Bookmarks " + (user.getUserName() != null ? "@" + user.getUserName() : user.getFirstName()) +
                        "!</b>\n\nℹ️ Seleziona un elemento salvato:") //Controlla se ci sono elementi salvati. Se non c'é il nome utente dell'utente, usa il nome.
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();

        try {
            return (Message) client.execute(edit);
        } catch (Exception e) {
            //Ignora l'errore se il messaggio è già uguale
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
            return null; //Ritorna null se non è stato modificato
        }
    }
}
