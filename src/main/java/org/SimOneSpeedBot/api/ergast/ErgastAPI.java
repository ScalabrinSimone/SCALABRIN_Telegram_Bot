package org.SimOneSpeedBot.api.ergast;


import com.google.gson.Gson;
import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;
import org.SimOneSpeedBot.api.ergast.DriverAPI.MRData;
import org.SimOneSpeedBot.api.ergast.DriverAPI.RootResponse;
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
    public String fetchDriver(String driverName, String driverSurname)
    {
        String driverId = driverName.equals("") ? driverSurname : driverName + "_" + driverSurname; //nome_cognome per api e se non funziona solo cognome e se non funziona allora non esiste
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "drivers/" + driverId + "/"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Gson deserializzatore = new Gson();

            //System.out.println("JSON ricevuto: " + response.body()); //Debug

            RootResponse root = deserializzatore.fromJson(response.body(), RootResponse.class);

            MRData mrData = root.getMRData();
            if (mrData == null || mrData.getDriverTable() == null ||
                    mrData.getDriverTable().getDrivers() == null ||
                    mrData.getDriverTable().getDrivers().isEmpty()) {
                return "Nessun pilota trovato con l'id " + mrData.getDriverTable().getDriverId() + ".";
            }
            else {
                Driver pilota = mrData.getDriverTable().getDrivers().getFirst(); //Prende il primo (= get(0))
                return pilota.toString();
            }
        }
        catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per il pilota: " + e.getMessage());

            return null;
        }
    }
}
