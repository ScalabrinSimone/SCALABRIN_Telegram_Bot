package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;
import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.SimOneSpeedBot.database.Bookmarks.BookmarkManager;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

public class DriverCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, String> userStates; //chatId -> stato attuale. Serve per aspettare un
    //input dall'utente.
    private final String textToSend = """
            <b>🔍 Cerca un pilota</b>
            
            <i>Inserisci il cognome (<b>obbligatorio</b>), nome cognome o cognome nome per cercare:</i>
            
            💡 <b>Esempio:</b> <code>Lewis Hamilton</code> per <i>Lewis Hamilton</i>, <code>Max Verstappen</code> per <i>Max Verstappen</i>.
            ⚠ <b>Per piloti che hanno un cognome comune (come verstappen con quello del padre), é <u>obbligatorio</u> inserire anche il nome</b>.
            ℹ <i>Per concludere l'inserimento <b>dal menu</b>, premere il pulsante "Back to Race Menu"</i>.
            """;

    public DriverCommand(TelegramClient client, Map<Long, String> userStates) {
        this.client = client;
        this.userStates = userStates;
    }

    @Override
    public void execute(long chatId, String[] args) {
        if (args.length == 0) {
            //Imposta stato e chiede il nome
            userStates.put(chatId, "AWAITING_DRIVER_NAME");

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(textToSend)
                    .parseMode("HTML")
                    .build();

            try {
                client.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { //Processa il nome del pilota se ci sono args
            //Controlla quanti args ha lo string[]

            //Ergast -> nome e cognome in minuscolo
            String driverName = args.length > 1 ? String.join(" ", args[0]).toLowerCase() : "";
            String driverSurname = args.length == 1 ? String.join(" ", args[0]).toLowerCase() : String.join(" ", args[args.length - 1]).toLowerCase();
            Driver driver = new ErgastAPI().fetchDriver(driverName, driverSurname);
            String driverInfo = (driver != null) ? driver.toString() : "❌ <b>Pilota " + (!driverName.equals("") ? StringUtils.capitalize(driverName) + " " : "") + StringUtils.capitalize(driverSurname) + " non trovato</b>\n\nℹ️ <i>Controlla di aver scritto <u>correttamente</u> il nome e cognome</i>.";

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(driverInfo)
                    .parseMode("HTML")
                    .build();

            try {
                client.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void executeEdit(long chatId, int messageId) {
        //Chiamato dal menu, imposta uno stato speciale
        userStates.put(chatId, "AWAITING_DRIVER_NAME_EDIT:" + messageId); //Stato speciale che contiene il messageId

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(textToSend)
                .parseMode("HTML")
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }

    public void processDriver(long chatId, String driverName, String driverSurname, Integer messageId, boolean keepState) {
        if (!keepState) { //Rimuove lo stato solo se specificato, cosí da poter continuare a scrivere piloti
            userStates.remove(chatId);
        }

        //Gestisci nome driver --> nome_cognome in minuscolo.
        driverName = driverName.toLowerCase();
        driverSurname = driverSurname.toLowerCase();

        //Se ancora non trova, ritorna messaggio di errore
        //StringUtils.capitalize(*stringa*) rende la prima lettera maiuscola.
        Driver driver = new ErgastAPI().fetchDriver(driverName, driverSurname);

        String driverInfo = (driver != null) ? driver.toString() : "❌ <b>Pilota " + (!driverName.equals("") ? StringUtils.capitalize(driverName) + " " : "") + StringUtils.capitalize(driverSurname) + " non trovato</b>\n\nℹ️ <i>Controlla di aver scritto <u>correttamente</u> il nome e cognome</i>.";

        if (messageId != null) {
            //Menu -> edita il messaggio con bottone back

            //Bottone
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Back To Race Menu\n (Concludi Inserimento)")
                    .callbackData("menu:race")
                    .build();

            //Riga Sempre esistente
            InlineKeyboardRow row = new InlineKeyboardRow(List.of(backButton));

            //Keyboard sempre esistente
            InlineKeyboardMarkup keyboard;

            //Si puó salvare solo se esiste il pilota e solo se nel menu
            if (!driverInfo.contains("❌ Pilota")) {

                //Controllo se é giá salvato e crea un pulsante di conseguenza
                boolean alreadySaved = BookmarkManager.bookmarkExists(chatId, "driver", driver.getDriverId());
                InlineKeyboardButton saveButton;
                if (alreadySaved) {
                    saveButton = InlineKeyboardButton.builder()
                            .text("⚠ Driver " + driver.getFamilyName() + " " + driver.getGivenName() + " giá salvato")
                            .callbackData("saved") //Non da errori
                            .build();
                } else {
                    saveButton = InlineKeyboardButton.builder()
                            .text("💾 Salva Driver")
                            .callbackData("save:driver:" + driver.getDriverId() + ":" + driver.getGivenName() + " " + driver.getFamilyName()) //Formato: save:tipo:if:nome cognome
                            .build();
                }

                InlineKeyboardRow saveButtonRow = new InlineKeyboardRow(saveButton);

                keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(saveButtonRow, row))
                        .build();
            } else {
                keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(row))
                        .build();
            }

            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(driverInfo)
                    .parseMode("HTML")
                    .replyMarkup(keyboard)
                    .build();

            try {
                client.execute(edit);
            } catch (Exception e) {
                if (!e.getMessage().contains("message is not modified")) {
                    e.printStackTrace();
                }
            }
        } else {
            //Comando -> manda nuovo messaggio senza bottoni
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(driverInfo)
                    .parseMode("HTML")
                    .build();

            try {
                client.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}