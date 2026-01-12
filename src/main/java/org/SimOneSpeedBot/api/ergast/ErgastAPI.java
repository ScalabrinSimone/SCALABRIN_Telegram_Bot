package org.SimOneSpeedBot.api.ergast;


import com.google.gson.Gson;
import org.SimOneSpeedBot.api.ergast.ConstructorAPI.Constructor;
import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;
import org.SimOneSpeedBot.api.ergast.SeasonAPI.Race;
import org.SimOneSpeedBot.service.MyConfiguration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ErgastAPI {
    //Base URLs da configurazione
    private final String baseUrl;
    private final HttpClient client;

    public ErgastAPI() {
        this.baseUrl = MyConfiguration.getInstance().getProperty("API_KEY_Ergast") + "f1/";
        this.client = HttpClient.newHttpClient();
    }

    //Metodi

    //Season
    public String fetchSeason(int year) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + year + ".json"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Gson deserializzatore = new Gson();

            org.SimOneSpeedBot.api.ergast.SeasonAPI.RootResponse root =
                    deserializzatore.fromJson(response.body(), org.SimOneSpeedBot.api.ergast.SeasonAPI.RootResponse.class);

            org.SimOneSpeedBot.api.ergast.SeasonAPI.MRData mrData = root.getMRData();
            if (mrData == null || mrData.getRaceTable() == null ||
                    mrData.getRaceTable().getRaces() == null ||
                    mrData.getRaceTable().getRaces().isEmpty()) {
                return "❌ Stagione " + year + " non trovata\n\nℹ️ Controlla di aver scritto correttamente l'anno (es: 2024, 2023).";
            }

            //Formatta le informazioni della stagione
            List<Race> races = mrData.getRaceTable().getRaces();
            StringBuilder info = new StringBuilder();
            info.append("🏎️ Stagione Formula 1 ").append(year).append("\n\n");
            info.append("📊 Totale gare: ").append(races.size()).append("\n\n");

            //Mostra solo le prime 3 gare come anteprima
            info.append("🥇 Prime gare:\n\n");
            for (int i = 0; i < Math.min(3, races.size()); i++) {
                info.append(races.get(i).toString()).append("\n\n");
            }

            if (races.size() > 3) {
                info.append("... e altre ").append(races.size() - 3).append(" gare");
            }

            return info.toString();
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per la stagione " + year + ": " + e.getMessage());
            return "❌ Errore nel recupero della stagione " + year;
        }
    }

    //Constructor
    public Constructor fetchConstructor(String firstName, String secondName) {
        String constructorId = secondName.equals("") ? firstName : firstName + "_" + secondName; //caso ferrari e aston_martin

        //Voglio che vengano gestiti altri inserimenti
        Constructor result = tryFetchConstructor(constructorId); //Primo controllo classico.
        if (result != null) {
            return result;
        }

        //caso martin_aston (input invertito da utente).
        //Tratto in modo contrario al primo
        else if (!secondName.equals("")) { //Solo il secondName puó essere vuoto ("")
            constructorId = secondName + "_" + firstName;
            result = tryFetchConstructor(constructorId);

            if (result != null) {
                return result;
            }

            result = tryFetchConstructor(firstName); //es. alfa romeo (api vuole solo alfa)
            if (result != null) {
                return result;
            }

            result = tryFetchConstructor(secondName); //es. romeo alfa
            if (result != null) {
                return result;
            }

            //es. nome-secondo_nome (alcune volte api vuole cosí, ma é il caso meno probabile, quindi in fondo)
            result = tryFetchConstructor(secondName + "-" + firstName);
            if (result != null) {
                return result;
            }
            result = tryFetchConstructor(firstName + "-" + secondName); //secondo_nome-nome
            if (result != null) {
                return result;
            }
        }

        //Se ancora non trova, ritorna messaggio di errore
        //StringUtils.capitalize(*stringa*) rende la prima lettera maiuscola.
        return null;

    }

    private Constructor tryFetchConstructor(String constructorId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "constructors/" + constructorId + ".json"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Gson deserializzatore = new Gson();

            //System.out.println("JSON ricevuto: " + response.body()); //Debug

            //Import specificato qui per non avere probelmi nella compilazione
            org.SimOneSpeedBot.api.ergast.ConstructorAPI.RootResponse root = deserializzatore.fromJson(response.body(),
                    org.SimOneSpeedBot.api.ergast.ConstructorAPI.RootResponse.class);

            org.SimOneSpeedBot.api.ergast.ConstructorAPI.MRData mrData = root.getMRData();
            if (mrData == null || mrData.getConstructorTable() == null ||
                    mrData.getConstructorTable().getConstructors() == null ||
                    mrData.getConstructorTable().getConstructors().isEmpty()) {
                return null; //Team non trovato. L'API risponde SEMPRE con 200 anche se la scuderia non esiste.
            } else {
                Constructor team = mrData.getConstructorTable().getConstructors().getFirst(); //Prende il primo (= get(0))
                return team;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per il team: " + e.getMessage());

            return null;
        }
    }

    //Driver
    public Driver fetchDriver(String driverName, String driverSurname) {
        String driverId = driverName.equals("") ? driverSurname : driverName + "_" + driverSurname; //nome_cognome per api e se non funziona solo cognome e se non funziona allora non esiste

        //Voglio che l'utente sia libero di inserire il pilota come vuole, quindi controllo anche per cognome_nome
        Driver result = tryFetchDriver(driverId); //Primo controllo classico.
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

            result = tryFetchDriver(driverSurname); //es. Leclerc Charles
            if (result != null) {
                return result;
            }
        }

        //Se ancora non trova, ritorna null. Uso un return di driver per il database
        return null;

    }

    private Driver tryFetchDriver(String driverId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "drivers/" + driverId + ".json"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Gson deserializzatore = new Gson();

            //System.out.println("JSON ricevuto: " + response.body()); //Debug

            //Import specificato qui per non avere probelmi nella compilazione
            org.SimOneSpeedBot.api.ergast.DriverAPI.RootResponse root = deserializzatore.fromJson(response.body(),
                    org.SimOneSpeedBot.api.ergast.DriverAPI.RootResponse.class);

            org.SimOneSpeedBot.api.ergast.DriverAPI.MRData mrData = root.getMRData();
            if (mrData == null || mrData.getDriverTable() == null ||
                    mrData.getDriverTable().getDrivers() == null ||
                    mrData.getDriverTable().getDrivers().isEmpty()) {
                return null; //Pilota non trovato. L'API risponde SEMPRE con 200 anche se il pilota non esiste.
            } else {
                Driver pilota = mrData.getDriverTable().getDrivers().getFirst(); //Prende il primo (= get(0))
                return pilota;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per il pilota: " + e.getMessage());

            return null;
        }
    }
}
