package org.SimOneSpeedBot.api;

import org.SimOneSpeedBot.service.MyConfiguration;

import java.net.http.HttpClient;

public class Ergast {
    private final String baseUrl = MyConfiguration.getInstance().getProperty("API_KEY_Ergast"); //Base URL da configurazione
    private final HttpClient client = HttpClient.newHttpClient();


    public Ergast(String convertedCall) {
        String finalUrl = baseUrl + convertedCall;
    }

    //Continuo
}
