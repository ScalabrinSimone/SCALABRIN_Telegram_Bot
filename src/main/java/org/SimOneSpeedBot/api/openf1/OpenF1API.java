package org.SimOneSpeedBot.api.openf1;


import org.SimOneSpeedBot.service.MyConfiguration;

import java.net.http.HttpClient;

public class OpenF1API {
    //Base URLs da configurazione
    private final String baseUrl;
    private final HttpClient client;

    public OpenF1API() {
        this.baseUrl = MyConfiguration.getInstance().getProperty("API_KEY_OpenF1");
        this.client = HttpClient.newHttpClient();
    }
}
