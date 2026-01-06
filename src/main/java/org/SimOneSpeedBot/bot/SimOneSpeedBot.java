package org.SimOneSpeedBot.bot;

import org.SimOneSpeedBot.api.ergast.ErgastAPI;
import org.SimOneSpeedBot.callback.CallbackHandler;
import org.SimOneSpeedBot.callback.MainMenuCallbackHandler;
import org.SimOneSpeedBot.callback.RaceCallback.RaceCallbackHandler;
import org.SimOneSpeedBot.callback.RaceCallback.SeasonCallbackHandler;
import org.SimOneSpeedBot.commands.*;
import org.SimOneSpeedBot.service.MyConfiguration;
import org.SimOneSpeedBot.service.UserStateManager;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimOneSpeedBot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient(MyConfiguration.getInstance().getProperty("BOT_TOKEN"));
    private final CommandHub hub = new CommandHub();
    private final UserStateManager stateManager;
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
        hub.register("constructor", new ConstructorCommand(telegramClient, userStates));
        hub.register("season", new SeasonCommand(telegramClient));
        hub.register("showmenu", new ShowMenuCommand(telegramClient, menuMessageIds));

        this.stateManager = new UserStateManager(telegramClient, hub, userStates);
    }

    @Override
    public void consume(Update update) {
        //Se l'update ha un callback
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            long chatId = callbackQuery.getMessage().getChatId();
            Integer savedMessageId = menuMessageIds.get(chatId);

            List<CallbackHandler> handlers = List.of(
                    new MainMenuCallbackHandler(telegramClient, savedMessageId != null ?
                            savedMessageId : callbackQuery.getMessage().getMessageId(), userStates), //Passp lo userStates per permettere di scrivere piú drivers.
                    new RaceCallbackHandler(telegramClient, savedMessageId != null ?
                            savedMessageId : callbackQuery.getMessage().getMessageId(), hub),
                    new SeasonCallbackHandler(telegramClient, savedMessageId != null ?
                            savedMessageId : callbackQuery.getMessage().getMessageId(), hub, userStates)
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

        //Gestione stati
        if (stateManager.handleMessageState(update)) {
            return; //lo stato ha gestito il messaggio
        }

        //Se l'update ha un messaggio e quest'ultimo ha un testo:
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText(); //Ha sia il nome che il cognome
            long chatId = update.getMessage().getChatId();

            //Gestione comandi normali (se non è in nessuno stato)
            boolean handled = hub.handle(messageText, chatId);

            if (!handled) {
                SendMessage message = SendMessage
                        .builder()
                        .chatId(chatId)
                        .text("ℹ Scusa, non riconosco il comando. Riprova")
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