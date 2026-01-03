package org.SimOneSpeedBot.api.ergast;


import org.SimOneSpeedBot.service.MyConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ErgastAPI {
    //Base URLs da configurazione
    private final String baseUrl;
    private final HttpClient client;

    public ErgastAPI() {
        this.baseUrl = MyConfiguration.getInstance().getProperty("API_KEY_Ergast") + "f1/";
        this.client = HttpClient.newHttpClient();
    }

    //Metodi
    public String fetchDriver(String driverSurname)
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "drivers/" + driverSurname))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Gson deserializzatore = new Gson();
            Driver pilota = deserializzatore.fromJson(response.body(), Driver.class);

            return pilota.toString(); //Oppure qualcosa con la classe formatter
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per il pilota: " + e.getMessage());

            return null;
        }
    }
}
