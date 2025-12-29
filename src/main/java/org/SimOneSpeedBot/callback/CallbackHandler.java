package org.SimOneSpeedBot.callback;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

/**
 * /Interfaccia comune per la gestione dei callback dei bottoni
 */
public interface CallbackHandler {

    /**
     * /Gestisce un CallbackQuery se compatibile
     * /Ritorna true se il callback è stato gestito
     */
    boolean handle(CallbackQuery callbackQuery);
}