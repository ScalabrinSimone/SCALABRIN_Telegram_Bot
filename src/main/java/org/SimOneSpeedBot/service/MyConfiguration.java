package org.SimOneSpeedBot.service;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class MyConfiguration {
    private static MyConfiguration instance;
    private Configurations configs = new Configurations();
    private Configuration config;

    private MyConfiguration() {
        try {
            config = configs.properties("config.properties");
        } catch (ConfigurationException e) {
            System.err.println("File non disponibile.");
            System.exit(-1);
        }
    }

    public static MyConfiguration getInstance() {
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

        //2. Se non c'è, usa il file di config (per uso locale)
        return config.getString(key);
    }
}
