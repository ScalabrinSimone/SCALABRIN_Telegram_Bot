package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.api.ergast.ConstructorAPI.Constructor;
import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;

public class ConstructorCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, String> userStates;
    private final String textToSend = """
                                🏁 Inserisci il nome del team...
                                
                                ℹ️ Scrivi il nome della squadra come vuoi (solo 2 nomi accettati), se é composto da piú nomi separali con uno spazio. Se il nome della scuderia ha ino sponsor NON metterlo (es. Aston Martin Aramco Formula One Team → Aston Martin)
                                In caso di "visa cash app red bull racing team" inserisci semplicemente rb.
                                
                                Se vieni dal menu puoi continuare a scrivere i nomi dei piloti, per smettere l'inserimento, premere il pulsante "Back To Race Menu".
                                """;

    public ConstructorCommand(TelegramClient client, Map<Long, String> userStates) {
        this.client = client;
        this.userStates = userStates;
    }

    @Override
    public void execute(long chatId, String[] args) {
        if(args.length == 0) {
            //Imposta stato e chiede il nome
            userStates.put(chatId, "AWAITING_CONSTRUCTOR_NAME");

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(textToSend)
                    .build();

            try {
                client.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else { //Processa il nome della scuderia se ci sono args (ne gestisce solo 2!!!)
            //Controlla quanti args ha lo string[]

            //Ergast -> in minuscolo
            //Funziona come il comando driver, solo che invece di avere nome e congome ha primo nome e secondo nome (input 1 e input 2)
            String firstName = String.join(" ", args[0]).toLowerCase(); //Ci deve essere un nome
            String secondName = args.length != 1 ? String.join(" ", args[1]).toLowerCase() : ""; //Non é detto che ci sia il secondo
            Constructor constructor = new ErgastAPI().fetchConstructor(firstName, secondName);
            String constructorInfo = (constructor != null) ? constructor.toString() : "❌ Scuderia " + (StringUtils.capitalize(firstName) + (!secondName.equals("") ? StringUtils.capitalize(secondName) + " " : "")) + " non trovato\n\nℹ️ Controlla di aver scritto correttamente il nome (es: ferrari, Alfa romeo o aston Martin).";;

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(constructorInfo)
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
        userStates.put(chatId, "AWAITING_CONSTRUCTOR_NAME_EDIT:" + messageId); //Stato speciale che contiene il messageId

        EditMessageText edit = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(textToSend)
                .build();

        try {
            client.execute(edit);
        } catch (Exception e) {
            if (!e.getMessage().contains("message is not modified")) {
                e.printStackTrace();
            }
        }
    }

    public void processConstructor(long chatId, String firstName, String secondName, Integer messageId, boolean keepState) {
        if (!keepState) { //Rimuove lo stato solo se specificato, cosí da poter continuare a scrivere piloti
            userStates.remove(chatId);
        }

        //Gestisce nome scuderia --> firstName_secondName in minuscolo.
        firstName = firstName.toLowerCase();
        secondName = secondName.toLowerCase();

        Constructor constructor = new ErgastAPI().fetchConstructor(firstName, secondName);
        String constructorInfo = constructor != null ? constructor.toString() : "❌ Scuderia " + (StringUtils.capitalize(firstName) + (!secondName.equals("") ? StringUtils.capitalize(secondName) + " " : "")) + " non trovato\n\nℹ️ Controlla di aver scritto correttamente il nome (es: ferrari, Alfa romeo o aston Martin).";

        if (messageId != null) {
            //Menu -> edita il messaggio con bottone back

            //Bottone
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Back To Race Menu\n(Concludi Inserimento)")
                    .callbackData("menu:race")
                    .build();

            //Riga Sempre esistente
            InlineKeyboardRow row = new InlineKeyboardRow(List.of(backButton));

            //Keyboard sempre esistente
            InlineKeyboardMarkup keyboard;

            //Si puó salvare solo se esiste il pilota e solo se nel menu
            if(!constructorInfo.contains("❌ Scuderia")) {
                InlineKeyboardButton saveButton = InlineKeyboardButton.builder()
                        .text("💾 Salva")
                        .callbackData("save:constructor:" + firstName) //Formato: save:tipo:nome
                        .build();

                InlineKeyboardRow saveButtonRow = new InlineKeyboardRow(saveButton);

                keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(saveButtonRow, row))
                        .build();
            }
            else {
                keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(row))
                        .build();
            }

            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(constructorInfo)
                    .replyMarkup(keyboard)
                    .build();

            try {
                client.execute(edit);
            } catch (Exception e) {
                if (!e.getMessage().contains("message is not modified")) {
                    e.printStackTrace();
                }
            }
        }
        else {
            //Comando -> manda nuovo messaggio senza bottoni
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(constructorInfo)
                    .build();

            try {
                client.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}