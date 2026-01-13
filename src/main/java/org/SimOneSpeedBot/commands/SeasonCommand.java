package org.SimOneSpeedBot.commands;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.SimOneSpeedBot.database.Bookmarks.BookmarkManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.ArrayList;
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
                        .text("⬅️ Back To Season Menu")
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
        ErgastAPI api = new ErgastAPI();
        List<org.SimOneSpeedBot.api.ergast.SeasonAPI.Race> races = api.fetchSeasonRaces(year);

        //Se l'API non ha gare per quell'anno
        if (races.isEmpty()) {
            String errorText = "❌ <b>Stagione " + year + " non trovata</b>\n\nℹ️ <i>Controlla di aver scritto correttamente l'anno (es: 2024, 2023)</i>.";

            if (messageId != null) {
                InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                        .text("⬅️ Back To Season Menu")
                        .callbackData("race:season")
                        .build();

                InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                        .keyboard(List.of(new InlineKeyboardRow(backButton)))
                        .build();

                EditMessageText edit = EditMessageText.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .text(errorText)
                        .parseMode("HTML")
                        .replyMarkup(keyboard)
                        .build();

                try {
                    client.execute(edit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
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

        //Stagione trovata → costruisco il testo completo (quello salvato nel database)
        StringBuilder seasonInfoBuilder = new StringBuilder();
        seasonInfoBuilder.append("<b>🏎️ Stagione Formula 1 ").append(year).append("</b>\n\n");

        //Aggiunge info campioni
        String championInfo = getChampionInfo(api, year);
        if (!championInfo.isEmpty()) {
            seasonInfoBuilder.append(championInfo).append("\n");
        }

        seasonInfoBuilder.append("📊 <b>Totale gare:</b> ").append(races.size()).append("\n\n");
        seasonInfoBuilder.append("<b>📅 Calendario gare:</b>\n\n");

        for (org.SimOneSpeedBot.api.ergast.SeasonAPI.Race race : races) {
            seasonInfoBuilder.append("• Round ")
                    .append(race.getRound())
                    .append(" - ")
                    .append(race.getRaceName())
                    .append(" (")
                    .append(race.getCircuit().getCircuitName())
                    .append(" - ")
                    .append(race.getDate())
                    .append(")\n");
        }

        seasonInfoBuilder.append("\n<i>Usa i bottoni qui sotto per vedere i dettagli di una singola gara.</i>");
        String seasonInfo = seasonInfoBuilder.toString();

        if (messageId != null) {
            //Costruisco tastiera con paginazione
            int currentPage = 1; //Prima pagina di default
            buildSeasonKeyboard(chatId, messageId, year, races, seasonInfo, currentPage);
        } else {
            //Comando /season -> solo testo
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

    //Metodo per costruire la tastiera con paginazione
    private void buildSeasonKeyboard(long chatId, int messageId, int year, List<org.SimOneSpeedBot.api.ergast.SeasonAPI.Race> races, String seasonInfo, int page) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        int racesPerPage = 10;
        int totalPages = (int) Math.ceil((double) races.size() / racesPerPage);

        //Limita la pagina al range valido
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        //Calcola indici per la pagina corrente
        int startIndex = (page - 1) * racesPerPage;
        int endIndex = Math.min(startIndex + racesPerPage, races.size());

        //Aggiungi gare della pagina corrente
        for (int i = startIndex; i < endIndex; i++) {
            org.SimOneSpeedBot.api.ergast.SeasonAPI.Race race = races.get(i);

            String buttonText = "🏁 " + race.getRound() + " - " + race.getRaceName();
            InlineKeyboardButton raceButton = InlineKeyboardButton.builder()
                    .text(buttonText)
                    .callbackData("race:details:" + year + ":" + race.getRound())
                    .build();

            rows.add(new InlineKeyboardRow(raceButton));
        }

        //Bottoni di navigazione se ci sono più pagine
        if (totalPages > 1) {
            List<InlineKeyboardButton> navButtons = new ArrayList<>();

            //Bottone pagina precedente
            if (page > 1) {
                InlineKeyboardButton prevButton = InlineKeyboardButton.builder()
                        .text("⬅️ Pagina " + (page - 1))
                        .callbackData("season:page:" + year + ":" + (page - 1))
                        .build();
                navButtons.add(prevButton);
            }

            //Indicatore pagina corrente
            InlineKeyboardButton pageIndicator = InlineKeyboardButton.builder()
                    .text("📄 " + page + "/" + totalPages)
                    .callbackData("page:indicator") //Non fa nulla
                    .build();
            navButtons.add(pageIndicator);

            //Bottone pagina successiva
            if (page < totalPages) {
                InlineKeyboardButton nextButton = InlineKeyboardButton.builder()
                        .text("Pagina " + (page + 1) + " ➡️")
                        .callbackData("season:page:" + year + ":" + (page + 1))
                        .build();
                navButtons.add(nextButton);
            }

            rows.add(new InlineKeyboardRow(navButtons));
        }

        //Pulsante salva
        boolean alreadySaved = BookmarkManager.bookmarkExists(chatId, "season", String.valueOf(year));
        InlineKeyboardButton saveButton;
        if (alreadySaved) {
            saveButton = InlineKeyboardButton.builder()
                    .text("⚠ Stagione " + year+ " giá salvata")
                    .callbackData("saved") //Non da errori
                    .build();
        } else {
            saveButton = InlineKeyboardButton.builder()
                    .text("💾 Salva Stagione")
                    .callbackData("save:season:" + year + ":stagione " + year)
                    .build();
        }
        rows.add(0, new InlineKeyboardRow(saveButton));

        //Back
        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("⬅️ Back To Season Menu")
                .callbackData("race:season")
                .build();
        rows.add(new InlineKeyboardRow(backButton));

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
    }

    //Metodo per permettere di avere un callback con pagina
    public void processSeasonWithPage(long chatId, int year, Integer messageId, int page) {
        //Recupera le gare
        ErgastAPI api = new ErgastAPI();
        List<org.SimOneSpeedBot.api.ergast.SeasonAPI.Race> races = api.fetchSeasonRaces(year);

        if (races.isEmpty()) {
            return; //Gestisci errore se necessario
        }

        //Recupera la classifica piloti e costruttori
        String championInfo = getChampionInfo(api, year);

        //Costruisci il testo (uguale a processSeason)
        StringBuilder seasonInfoBuilder = new StringBuilder();
        seasonInfoBuilder.append("<b>🏎️ Stagione Formula 1 ").append(year).append("</b>\n\n");

        //NUOVO: Aggiungi info campioni
        if (!championInfo.isEmpty()) {
            seasonInfoBuilder.append(championInfo).append("\n");
        }

        seasonInfoBuilder.append("📊 <b>Totale gare:</b> ").append(races.size()).append("\n\n");
        seasonInfoBuilder.append("<b>📅 Calendario gare:</b>\n\n");

        for (org.SimOneSpeedBot.api.ergast.SeasonAPI.Race race : races) {
            seasonInfoBuilder.append("• Round ")
                    .append(race.getRound())
                    .append(" - ")
                    .append(race.getRaceName())
                    .append(" (")
                    .append(race.getCircuit().getCircuitName())
                    .append(" - ")
                    .append(race.getDate())
                    .append(")\n");
        }

        seasonInfoBuilder.append("\n<i>Usa i bottoni qui sotto per vedere i dettagli di una singola gara.</i>");
        String seasonInfo = seasonInfoBuilder.toString();

        //Costruisci la tastiera con la pagina specifica
        buildSeasonKeyboard(chatId, messageId, year, races, seasonInfo, page);
    }

    //Recupera info sui campioni della stagione
    private String getChampionInfo(ErgastAPI api, int year) {
        try {
            //Recupera classifica piloti (top 3)
            List<org.SimOneSpeedBot.api.ergast.StandingsAPI.DriverStanding> driverStandings = api.fetchDriverStandings(year);

            //Recupera campione costruttori
            List<org.SimOneSpeedBot.api.ergast.StandingsAPI.ConstructorStanding> constructorStandings = api.fetchConstructorStandings(year);

            StringBuilder info = new StringBuilder();

            //Top 3 piloti (o solo campione se preferisci)
            if (!driverStandings.isEmpty()) {
                info.append("🏆 <b>Campione:</b> ")
                        .append(driverStandings.get(0).getDriver().getGivenName())
                        .append(" ")
                        .append(driverStandings.get(0).getDriver().getFamilyName())
                        .append(" (")
                        .append(driverStandings.get(0).getPoints())
                        .append(" pt)\n");

                //Top 3 completo (commentalo se vuoi solo il campione)
                if (driverStandings.size() > 1) {
                    info.append("🥈 ")
                            .append(driverStandings.get(1).getDriver().getGivenName())
                            .append(" ")
                            .append(driverStandings.get(1).getDriver().getFamilyName())
                            .append(" (")
                            .append(driverStandings.get(1).getPoints())
                            .append(" pt)");
                }

                if (driverStandings.size() > 2) {
                    info.append(" • 🥉 ")
                            .append(driverStandings.get(2).getDriver().getGivenName())
                            .append(" ")
                            .append(driverStandings.get(2).getDriver().getFamilyName())
                            .append(" (")
                            .append(driverStandings.get(2).getPoints())
                            .append(" pt)");
                }
                info.append("\n");
            }

            //Costruttore vincente
            if (!constructorStandings.isEmpty()) {
                info.append("🏁 <b>Costruttore:</b> ")
                        .append(constructorStandings.get(0).getConstructor().getName())
                        .append(" (")
                        .append(constructorStandings.get(0).getPoints())
                        .append(" pt)");
            }

            return info.toString();

        } catch (Exception e) {
            //Se API fallisce, ritorna stringa vuota
            return "";
        }
    }
}