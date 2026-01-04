package org.SimOneSpeedBot.bot;

import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.callback.MainMenuCallbackHandler;
import org.SimOneSpeedBot.callback.RaceCallbackHandler;
import org.SimOneSpeedBot.commands.*;
import org.SimOneSpeedBot.service.MyConfiguration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimOneSpeedBot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));
    private final CommandHub hub = new CommandHub();
    private final Map<Long, Integer> menuMessageIds = new HashMap<>(); //chatId -> messageId. Serve per modificare
                                                                      //il messaggio invece che inviarne di nuovi.
                                                                     //Usato per la prima volta in classe StartCommand.

    private final Map<Long, String> userStates = new HashMap<>(); //chatId -> stato attuale. Serve per aspettare un
                                                                 //input dall'utente.

    public SimOneSpeedBot() //Qui registro i vari comandi
    {
        hub.register("start", new StartCommand(telegramClient, menuMessageIds));
        hub.register("info", new InfoCommand(telegramClient));
        hub.register("ping", new PingCommand(telegramClient));
        hub.register("driver", new DriverCommand(telegramClient, userStates));
        hub.register("showmenu", new ShowMenuCommand(telegramClient, menuMessageIds));
    }

    @Override
    public void consume(Update update)
    {
        //Se l'update ha un callback
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            long chatId = callbackQuery.getMessage().getChatId();
            Integer savedMessageId = menuMessageIds.get(chatId);

            List<CallbackHandler> handlers = List.of(
                    new MainMenuCallbackHandler(telegramClient, savedMessageId != null ?
                            savedMessageId : callbackQuery.getMessage().getMessageId(), userStates), //Passp lo userStates per permettere di scrivere piú drivers.
                    new RaceCallbackHandler(telegramClient, savedMessageId != null ?
                            savedMessageId : callbackQuery.getMessage().getMessageId(), hub)
            );

            boolean handled = false;
            //Ciclo per gestire il callback
            for (CallbackHandler handler : handlers) {
                if (handler.handle(callbackQuery)) {
                    handled = true;
                    break; //Se un handler ha gestito il callback esco
                }
            }

            //Se nessun handler ha gestito il callback
            if (!handled) {
                try {
                    telegramClient.execute(
                            AnswerCallbackQuery.builder()
                                    .callbackQueryId(callbackQuery.getId())
                                    .text("❗Avviso❗\n\nSpiacenti, la funzione non é ancora disponibile")
                                    .showAlert(true) //Mostra un popup invece del messaggio o la keyboard
                                    .build()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //Se l'update ha un messaggio e quest'ultimo ha un testo:
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText(); //Ha sia il nome che il cognome
            long chatId = update.getMessage().getChatId();

            //Controlla se l'utente é in uno stato particolare
            String currentState = userStates.get(chatId);
            if (currentState!= null && currentState.startsWith("AWAITING_DRIVER_NAME")) {
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
                        telegramClient.execute(delete);
                    } catch (Exception e) {
                        //Se fallisce la cancellazione, continua comunque
                    }

                    //Controlla se devo editare o mandare un nuovo messaggio
                    if (currentState.contains("EDIT:")) { //Da menu
                        //Estrae il messageId dallo stato
                        int messageId = Integer.parseInt(currentState.split(":")[1]);
                        driverCmd.processDriver(chatId, givenName, familyName, messageId, true); //Possiamo continuare a scrivere piloti.
                    }
                    else { //Nuovo messaggio
                        driverCmd.processDriver(chatId, givenName, familyName, null, false); //Non ".
                    }
                }
                return;
            }

            boolean handled = hub.handle(messageText, chatId);

            if(!handled)
            {
                SendMessage message = SendMessage
                        .builder()
                        .chatId(chatId)
                        .text("Scusa, non riconosco il comando. Riprova")
                        .build();

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}