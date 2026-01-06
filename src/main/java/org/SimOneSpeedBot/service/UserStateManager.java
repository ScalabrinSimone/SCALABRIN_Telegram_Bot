package org.SimOneSpeedBot.service;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.SimOneSpeedBot.commands.CommandHub;
import org.SimOneSpeedBot.commands.ConstructorCommand;
import org.SimOneSpeedBot.commands.DriverCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class UserStateManager {
    private final TelegramClient client;
    private final CommandHub hub;
    private final Map<Long, String> userStates;

    public UserStateManager(TelegramClient client, CommandHub hub, Map<Long, String> userStates) {
        this.client = client;
        this.hub = hub;
        this.userStates = userStates;
    }

    public boolean handleMessageState(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String currentState = userStates.get(chatId);

        if (currentState == null) {
            return false; //Nessuno stato gestito qui
        }

        if (currentState.startsWith("AWAITING_DRIVER_NAME")) {
            handleDriverState(update, messageText, chatId, currentState);
            return true;
        }

        if (currentState.startsWith("AWAITING_CONSTRUCTOR_NAME")) {
            handleConstructorState(update, messageText, chatId, currentState);
            return true;
        }

        if (currentState.startsWith("AWAITING_SEASON_YEAR")) {
            handleSeasonState(update, messageText, chatId, currentState);
            return true;
        }

        return false;
    }

    //Stato attesa driver
    private void handleDriverState(Update update, String messageText, long chatId, String currentState) {
        //Chiama il comando driver con il nome come argomento
        DriverCommand driverCmd = (DriverCommand) hub.getCommand("driver");

        if (driverCmd != null) {
            //Prendo il messaggio in modo da separare nome e cognome
            String text = messageText.trim(); //Togliamo spazi inutili.
            String[] parts = text.split(" "); //Separa per spazi.

            String givenName = parts.length > 1 ? parts[0] : ""; //Nome
            String familyName = parts.length == 1 ? parts[0] : parts[parts.length - 1]; //Cognome

            //Elimina il messaggio dell'utente per avere una UI/UX migliore:
            DeleteMessage delete = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(update.getMessage().getMessageId())
                    .build();
            try {
                client.execute(delete);
            } catch (Exception e) {
                //Se fallisce la cancellazione, continua comunque
            }

            //Controlla se devo editare o mandare un nuovo messaggio
            if (currentState.contains("EDIT:")) { //Da menu
                //Estrae il messageId dallo stato
                int messageId = Integer.parseInt(currentState.split(":")[1]);
                driverCmd.processDriver(chatId, givenName, familyName, messageId, true); //Possiamo continuare a scrivere piloti.
            } else { //Nuovo messaggio
                driverCmd.processDriver(chatId, givenName, familyName, null, false); //Non ".
            }
        }
        return;
    }

    //Stato attesa constructor
    private void handleConstructorState(Update update, String messageText, long chatId, String currentState) {
        //Chiama il comando constructor con il firstNome come argomento
        ConstructorCommand constructorCmd = (ConstructorCommand) hub.getCommand("constructor");

        if (constructorCmd != null) {
            //Prendo il messaggio in modo da separare fisrtNome e secondNome
            String text = messageText.trim(); //Togliamo spazi inutili.
            String[] parts = text.split(" "); //Separa per spazi.

            String firstName = parts[0];
            String secondName = parts.length != 1 ? parts[parts.length - 1] : "";

            //Elimina il messaggio dell'utente per avere una UI/UX migliore:
            DeleteMessage delete = DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(update.getMessage().getMessageId())
                    .build();
            try {
                client.execute(delete);
            } catch (Exception e) {
                //Se fallisce la cancellazione, continua comunque
            }

            //Controlla se devo editare o mandare un nuovo messaggio
            if (currentState.contains("EDIT:")) { //Da menu
                //Estrae il messageId dallo stato
                int messageId = Integer.parseInt(currentState.split(":")[1]);
                constructorCmd.processConstructor(chatId, firstName, secondName, messageId, true); //Possiamo continuare a scrivere costruttori.
            } else { //Nuovo messaggio
                constructorCmd.processConstructor(chatId, firstName, secondName, null, false); //Non ".
            }

            return;
        }
    }

    //Stato attesa stagione
    private void handleSeasonState(Update update, String messageText, long chatId, String currentState) {
        String year = messageText.trim();

        //Valida che sia un numero
        try {
            int yearInt = Integer.parseInt(year);

            //Valida il range
            if (yearInt < 1950 || yearInt > LocalDate.now().getYear()) {
                //Anno fuori range
                if (currentState.contains("EDIT:")) {
                    //Dal menu -> edita il messaggio
                    DeleteMessage delete = DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(update.getMessage().getMessageId())
                            .build();

                    try {
                        client.execute(delete);
                    } catch (Exception e) {
                        //Ignora
                    }

                    int messageId = Integer.parseInt(currentState.split(":")[2]);

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
                            .text("❌ Anno non valido. Inserisci un anno tra 1950 e " + LocalDate.now().getYear())
                            .replyMarkup(keyboard)
                            .build();

                    try {
                        client.execute(edit);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Da comando -> invia nuovo messaggio
                    SendMessage errorMsg = SendMessage.builder()
                            .chatId(chatId)
                            .text("❌ Anno non valido. Inserisci un anno tra 1950 e " + LocalDate.now().getYear())
                            .build();

                    try {
                        client.execute(errorMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            //Anno valido -> recupera info
            if (currentState.contains("EDIT:")) {
                //Dal menu
                DeleteMessage delete = DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(update.getMessage().getMessageId())
                        .build();

                try {
                    client.execute(delete);
                } catch (Exception e) {
                    //Ignora
                }

                String seasonInfo = new ErgastAPI().fetchSeason(yearInt);

                int messageId = Integer.parseInt(currentState.split(":")[2]);

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
                        .text(seasonInfo)
                        .replyMarkup(keyboard)
                        .build();

                try {
                    client.execute(edit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //Da comando -> invia nuovo messaggio
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

                userStates.remove(chatId); //Rimuovi lo stato dopo comando
            }

        } catch (NumberFormatException e) {
            //Input non è un numero
            if (currentState.contains("EDIT:")) {
                //Dal menu -> edita il messaggio
                DeleteMessage delete = DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(update.getMessage().getMessageId())
                        .build();

                try {
                    client.execute(delete);
                } catch (Exception ex) {
                    //Ignora
                }

                int messageId = Integer.parseInt(currentState.split(":")[2]);

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
                        .text("❌ Inserisci un anno valido (es: 2024, 2023)")
                        .replyMarkup(keyboard)
                        .build();

                try {
                    client.execute(edit);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                //Da comando -> invia nuovo messaggio
                SendMessage errorMsg = SendMessage.builder()
                        .chatId(chatId)
                        .text("❌ Inserisci un anno valido (es: 2024, 2023)")
                        .build();

                try {
                    client.execute(errorMsg);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return;
    }
}
