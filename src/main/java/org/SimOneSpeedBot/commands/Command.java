package org.SimOneSpeedBot.commands;

//Interfaccia per implementare i comandi
public interface Command {
    void execute(long chatId, String[] args); //Esecuzione del comando

    default void executeEdit(long chatId, int messageId) { //Metodo per editare
        //Implementazione vuota di default, i comandi che non lo usano non devono implementarlo
    }
}
