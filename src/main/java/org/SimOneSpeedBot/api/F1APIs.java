package org.SimOneSpeedBot.api;

import org.SimOneSpeedBot.service.MyConfiguration;

import java.net.http.HttpClient;

/*
Ergast (https://api.jolpi.ca/ergast/) usata per tutte le stagioni con info generali.
OpenF1 (https://openf1.org/) usata per ottenere info, dal 2023 in poi, precise.
*/
public class F1APIs {
    //Base URLs da configurazione
    private final String Ergast_baseUrl;
    private final String OpenF1_baseUrl;
    private final HttpClient client;

    public F1APIs() {
        this.Ergast_baseUrl = MyConfiguration.getInstance().getProperty("API_KEY_Ergast");
        this.OpenF1_baseUrl = MyConfiguration.getInstance().getProperty("API_KEY_OpenF1");
        this.client = HttpClient.newHttpClient();
    }

    //Metodi

    public String fetchDriversCurrentYear() //MODIFICA: CAPIRE L'ANNO ATTUALE
    {

    }

}
