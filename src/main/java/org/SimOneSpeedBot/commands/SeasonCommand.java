package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SeasonCommand implements Command {
    private final TelegramClient client;
    private final Map<Long, String> userStates;
    private final String textToSend = """
            🏁 Inserisci l'anno della stagione...
            
            ℹ️ Non scrivere anni superiori a quello attuale e ricorda che la prima stagione di formula 1 risale al 1950.
            Scrivi solo il numero dell'anno.
            """;

    public SeasonCommand(TelegramClient client, Map<Long, String> userStates) {
        this.client = client;
        this.userStates = userStates;
    }

    @Override
    public void execute(long chatId, String[] args) {
        String yearString;

        //Se non ci sono argomenti, usa la stagione corrente
        if (args.length == 0) {
            yearString = String.valueOf(LocalDate.now().getYear());
        } else {
            yearString = args[0].trim();
        }

        int yearInt;

        //Controlla che sia un numero
        try {
            yearInt = Integer.parseInt(yearString);
        } catch (NumberFormatException e) {
            SendMessage errorMsg = SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Input errato. Inserisci un anno valido (es: 2024, 2023)")
                    .build();

            try {
                client.execute(errorMsg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        //Controlla il range
        int currentYear = LocalDate.now().getYear();
        if (yearInt < 1950 || yearInt > currentYear) {
            SendMessage errorMsg = SendMessage.builder()
                    .chatId(chatId)
                    .text("❌ Anno non valido. Inserisci un anno tra 1950 e " + currentYear)
                    .build();

            try {
                client.execute(errorMsg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        //Anno valido -> recupera info
        String seasonInfo = new ErgastAPI().fetchSeason(yearInt);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(seasonInfo)
                .build();

        try {
            client.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void executeEdit(long chatId, int messageId) {
        //Chiamato dal menu, imposta stato speciale
        userStates.put(chatId, "AWAITING_SEASON_YEAR_EDIT:" + messageId);

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

    public void processSeason(long chatId, int year, Integer messageId, boolean keepState) {
        if (!keepState) {
            userStates.remove(chatId);
        }

        //Controlla il range
        int currentYear = LocalDate.now().getYear();
        if (year < 1950 || year > currentYear) {
            String errorText = "❌ Anno non valido. Inserisci un anno tra 1950 e " + currentYear;

            if (messageId != null) {
                //Menu -> edita il messaggio
                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(errorText)
                        .build();

                try {
                    client.execute(edit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //Comando -> nuovo messaggio
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(errorText)
                        .build();

                try {
                    client.execute(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        //Anno valido -> recupera info
        String seasonInfo = new ErgastAPI().fetchSeason(year);

        if (messageId != null) {
            //Menu -> edita con bottone back e salva
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Back To Race Menu\n(Concludi Inserimento)")
                    .callbackData("menu:race")
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow(List.of(backButton));
            InlineKeyboardMarkup keyboard;

            //Aggiungi pulsante salva solo se dal menu
            if (!seasonInfo.contains("❌")) {
                InlineKeyboardButton saveButton = InlineKeyboardButton.builder()
                        .text("💾 Salva")
                        .callbackData("save:season:" + year + ":Stagione " + year)
                        .build();

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
                    .text(seasonInfo)
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
    }
}