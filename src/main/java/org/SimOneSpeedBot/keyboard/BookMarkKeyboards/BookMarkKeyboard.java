package org.SimOneSpeedBot.keyboard.BookMarkKeyboards;

import org.SimOneSpeedBot.keyboard.MenuKeyboard;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

public class BookMarkKeyboard implements MenuKeyboard {
    private final TelegramClient client;
    private final int messageId;
    private final User user;

    public BookMarkKeyboard(TelegramClient client, int messageId, User user) {this.client = client; this.messageId = messageId; this.user = user;}

    @Override
    public Message sendInlineKeyboard(long chatId) {
        return editInlineKeyboard(chatId); //Chiama il metodo che modifica
    }

    public Message editInlineKeyboard(long chatId) {
        //Dovrebbe crearli in base a quante info ha l'utente (gli passo il database e il suo metodo) e se ha troppe cose dentro mette frecce per navigare
        //Crea i bottoni
        InlineKeyboardButton modificaButton = InlineKeyboardButton.builder()
                .text("Errore")
                .callbackData("bookMark:errore") //Modifica
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Utils Menu")
                .callbackData("menu:utils")
                .build();

        //Crea la prima riga di bottoni
        InlineKeyboardRow upperRow = new InlineKeyboardRow(
                List.of(modificaButton)
        );
        //Crea la seconda riga di bottoni
        InlineKeyboardRow bottomRow = new InlineKeyboardRow(
                List.of(backButton)
        );

        //Crea la tastiera
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(upperRow, bottomRow))
                .build();

        //Modifica il messaggio
        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text("🏡 Benvenuto " + (user.getUserName() != null ? "@" + user.getUserName() : user.getFirstName()) +
                        "\n\nℹ️ Seleziona un elemento salvato:") //Controlla se ci sono elementi salvati. Se non c'é il nome utente dell'utente, usa il nome.
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
