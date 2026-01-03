package org.SimOneSpeedBot.api.ergast;


import org.SimOneSpeedBot.service.MyConfiguration;

import java.net.http.HttpClient;

public class ErgastAPI {
    //Base URLs da configurazione
    private final String baseUrl;
    private final HttpClient client;

    public ErgastAPI() {
        this.baseUrl = MyConfiguration.getInstance().getProperty("API_KEY_Ergast");
        this.client = HttpClient.newHttpClient();
    }
}
