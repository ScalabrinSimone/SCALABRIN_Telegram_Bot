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
        //Se non ci sono argomenti, usa la stagione corrente
        if (args.length == 0) {
            int currentYear = LocalDate.now().getYear();
            processSeason(chatId, currentYear, null, false);
        } else {
            //Processa l'anno fornito
            try {
                int year = Integer.parseInt(args[0].trim());
                processSeason(chatId, year, null, false);
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
            }
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
                //Menu -> edita il messaggio con bottone back per tornare a season
                InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                        .text("⬅️ Back To Season Menu\n(Concludi Inserimento)")
                        .callbackData("race:season")
                        .build();

                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(new InlineKeyboardRow(List.of(backButton))))
                        .build();

                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(errorText)
                        .replyMarkup(keyboard) //Aggiungi la tastiera
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
                    .text("⬅️ Back To Season Menu\n(Concludi Inserimento)")
                    .callbackData("race:season")
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow(List.of(backButton));
            InlineKeyboardMarkup keyboard;

            //Aggiungi pulsante salva solo se dal menu
            if (!seasonInfo.contains("❌")) {
                InlineKeyboardButton saveButton = InlineKeyboardButton.builder()
                        .text("💾 Salva")
                        .callbackData("save:season:" + year + ":stagione " + year) //Formato save:season:anno:stagione anno
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
        } else {
            //Comando -> nuovo messaggio
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
    }
}