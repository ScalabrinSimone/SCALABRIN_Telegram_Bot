package org.SimOneSpeedBot.api.ergast;


import com.google.gson.Gson;
import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;
import org.SimOneSpeedBot.api.ergast.DriverAPI.MRData;
import org.SimOneSpeedBot.api.ergast.DriverAPI.RootResponse;
import org.SimOneSpeedBot.service.MyConfiguration;
import org.apache.commons.lang3.StringUtils;

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

        //Voglio che l'utente sia libero di inserire il pilota come vuole, quindi controllo anche per cognome_nome
        String result = tryFetchDriver(driverId); //Primo controllo classico.
        if (result != null) {
            return result;
        }

        //Altrimenti non c'é nulla con nome_cognome o con cognome, proviamo con altri input come cognome_nome (input invertito da utente).
        //Tratto il driverName come se fosse il driverSurname
        else if (!driverName.equals("")) { //Solo il driverName puó essere vuoto ("")
            driverId = driverSurname + "_" + driverName;
            result = tryFetchDriver(driverId);

            if (result != null) {
                return result;
            }

            result = tryFetchDriver(driverName); //es. Charles Leclerc
            if (result != null) {
                return result;
            }

            result =  tryFetchDriver(driverSurname); //es. Leclerc Charles
            if (result != null) {
                return result;
            }
        }

        //Se ancora non trova, ritorna messaggio di errore
        //StringUtils.capitalize(*stringa*) rende la prima lettera maiuscola.
        return "❌ Pilota " + (!driverName.equals("") ? StringUtils.capitalize(driverName) + " " : "") + StringUtils.capitalize(driverSurname) + " non trovato\n\nℹ️ Controlla di aver scritto correttamente il nome (es: Verstappen, Max Verstappen o Verstappen Max).";

    }
    private String tryFetchDriver(String driverId) {
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
                return null; //Pilota non trovato. L'API risponde SEMPRE con 200 anche se il pilota non esiste.
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
