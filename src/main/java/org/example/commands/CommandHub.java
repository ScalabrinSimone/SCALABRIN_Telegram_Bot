package org.example.commands;

import java.util.HashMap;
import java.util.Map;

public class CommandHub {
    private final Map<String, Command> commands = new HashMap<>(); //HashMap per i comandi

    //Metodo per registrare i nuovi commandi
    public void register(String name, Command command) {
        commands.put(name, command);
    }

    //Gestione dei comandi
    public boolean handle(String text, long chatId) {
        if (!text.startsWith("/")) return false;

        String[] parts = text.split(" ");
        String cmdName = parts[0].substring(1); //Prende da una stringa tipo /driver leclerc 2023 solo driver
        String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length); //Prende il resto dell'array (leclerc, 2023) e ne fa diventare gli argomenti del comando

        Command command = commands.get(cmdName.toLowerCase()); //Cerca nella map quel comando (driver)
        if (command == null)
            return false;

        command.execute(chatId, args); //Esegue in classe driver execute(chatId, ["leclerc", "2023"])
        return true; //Gestito correttamente il comando
    }
}