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
            <b>🔍 Cerca una stagione</b>
            
            <i>Inserisci l'anno in formato numerico per cercare:</i>
            
            💡 <b>Esempio:</b> <code>1950</code> per <i>Stagione F1 1950</i>.
            ⚠ <b>Inserisci anni tra il 1950 (prima stagione ufficiale di F1) e l'anno attuale</b>.
            ℹ <i>Per concludere l'inserimento <b>dal menu</b>, premere il pulsante "Back to Season Menu"</i>.
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
                        .text("<b>❌ Input errato.</b> <i>Inserisci un anno <u>valido</u> (es: 2024, 2023)</i>")
                        .parseMode("HTML")
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

    public void processSeason(long chatId, int year, Integer messageId, boolean keepState) {
        if (!keepState) {
            userStates.remove(chatId);
        }

        //Controlla il range
        int currentYear = LocalDate.now().getYear();
        if (year < 1950 || year > currentYear) {
            String errorText = "<b>❌ Anno non valido.</b> <i>Inserisci un anno <u>tra</u> 1950 e " + currentYear + "</i>";

            if (messageId != null) {
                //Menu -> edita il messaggio con bottone back per tornare a season
                InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                        .text("⬅️ Back To Season Menu\n (Concludi Inserimento)")
                        .callbackData("race:season")
                        .build();

                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(new InlineKeyboardRow(List.of(backButton))))
                        .build();

                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(errorText)
                        .parseMode("HTML")
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
                        .parseMode("HTML")
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
        //String seasonInfo = new ErgastAPI().fetchSeason(year);
        ErgastAPI api = new ErgastAPI();
        List<org.SimOneSpeedBot.api.ergast.SeasonAPI.Race> races = api.fetchSeasonRaces(year);

        if (races.isEmpty()) {
            String errorText = "❌ <b>Stagione " + year + " non trovata</b>\n\nℹ️ <i>Controlla di aver scritto correttamente l'anno (es: 2024, 2023)</i>.";
            return;
        }

        //Costruisco il testo principale
        StringBuilder seasonInfoBuilder = new StringBuilder();
        seasonInfoBuilder.append("<b>🏎️ Stagione Formula 1 ").append(year).append("</b>\n\n");
        seasonInfoBuilder.append("📊 <b>Totale gare:</b> ").append(races.size()).append("\n\n");
        seasonInfoBuilder.append("<i>Seleziona una gara dalla tastiera qui sotto per vedere i dettagli.</i>\n");
        String seasonInfo = seasonInfoBuilder.toString();

        if (messageId != null) {
            //Menu -> edita con bottone back e salva
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("⬅️ Back To Season Menu\n (Concludi Inserimento)")
                    .callbackData("race:season")
                    .build();

            //Righe con le gare (max es. 8 per evitare tastiera enorme)
            List<InlineKeyboardRow> rows = new java.util.ArrayList<>();

            int maxRacesToShow = Math.min(8, races.size());
            for (int i = 0; i < maxRacesToShow; i++) {
                org.SimOneSpeedBot.api.ergast.SeasonAPI.Race race = races.get(i);

                String buttonText = "🏁 " + race.getRound() + " - " + race.getRaceName();
                InlineKeyboardButton raceButton = InlineKeyboardButton.builder()
                        .text(buttonText)
                        .callbackData("race:details:" + year + ":" + race.getRound())
                        .build();

                rows.add(new InlineKeyboardRow(raceButton));
            }

            //Riga back
            rows.add(new InlineKeyboardRow(backButton));

            //Save button
            InlineKeyboardButton saveButton = null;
            if (!seasonInfo.contains("❌")) {
                saveButton = InlineKeyboardButton.builder()
                        .text("💾 Salva Stagione")
                        .callbackData("save:season:" + year + ":stagione " + year)
                        .build();
                rows.add(0, new InlineKeyboardRow(saveButton)); //Prima riga
            }

            InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                    .keyboard(rows)
                    .build();

            EditMessageText edit = EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(seasonInfo)
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
            //Comando -> nuovo messaggio
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(seasonInfo)
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