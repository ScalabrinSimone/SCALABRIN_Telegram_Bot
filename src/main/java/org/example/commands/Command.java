package org.example.commands;

//Interfaccia per implementare i comandi
public interface Command {
    void execute(long chatId, String[] args); //Esecuzione del comando
}
