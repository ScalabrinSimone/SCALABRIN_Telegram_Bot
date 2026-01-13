package org.SimOneSpeedBot.service;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class MyConfiguration {
    private static MyConfiguration instance;
    private Configuration config; //Può essere null se il file non esiste

    private MyConfiguration() {
        try {
            Configurations configs = new Configurations();
            config = configs.properties("config.properties");
        } catch (Exception e) {
            //Su Render il file non esiste: va bene così, uso solo le variabili d'ambiente
            config = null;
        }
    }
    public static synchronized MyConfiguration getInstance() {
        if (instance == null) {
            instance = new MyConfiguration();
        }
        return instance;
    }

    public String getProperty(String key) {
        //1. Prima prova da variabile d'ambiente (Render)
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        //2. Se non c'è, prova dal file di config (solo locale)
        if (config != null && config.containsKey(key)) {
            return config.getString(key);
        }

        //3. Se non trovato da nessuna parte -> errore
        throw new RuntimeException("Configurazione mancante per chiave: " + key);
    }
}
