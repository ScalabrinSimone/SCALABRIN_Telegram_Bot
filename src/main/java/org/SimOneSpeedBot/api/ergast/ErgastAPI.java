package org.SimOneSpeedBot.api.ergast;


import com.google.gson.Gson;
import org.SimOneSpeedBot.api.ergast.ConstructorAPI.Constructor;
import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;
import org.SimOneSpeedBot.service.MyConfiguration;
import org.SimOneSpeedBot.api.ergast.StandingsAPI.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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
    //Ritorna la lista di gare della stagione come oggetti Race
    public List<org.SimOneSpeedBot.api.ergast.SeasonAPI.Race> fetchSeasonRaces(int year) {
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
                return List.of();
            }

            return mrData.getRaceTable().getRaces();
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per la stagione " + year + ": " + e.getMessage());
            return List.of();
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
            if (response.statusCode() == 200) {
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
            }

            return null;

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

            if (response.statusCode() == 200) {
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
            }

            return null;

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per il pilota: " + e.getMessage());

            return null;
        }
    }

    //Griglia di partenza
    public List<org.SimOneSpeedBot.api.ergast.GridAPI.GridPosition> fetchStartingGrid(int year, int round) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + year + "/" + round + "/grid.json"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Gson deserializzatore = new Gson();

                org.SimOneSpeedBot.api.ergast.GridAPI.RootResponse root =
                        deserializzatore.fromJson(response.body(), org.SimOneSpeedBot.api.ergast.GridAPI.RootResponse.class);

                org.SimOneSpeedBot.api.ergast.GridAPI.MRData mrData = root.getMRData();
                if (mrData == null || mrData.getRaceTable() == null) {
                    return new ArrayList<>();
                }

                List<org.SimOneSpeedBot.api.ergast.GridAPI.Race> races = mrData.getRaceTable().getRaces();
                if (races == null || races.isEmpty()) {
                    return new ArrayList<>();
                }

                org.SimOneSpeedBot.api.ergast.GridAPI.Race race = races.get(0);
                return race.getQualifyingResults() != null ? race.getQualifyingResults() : new ArrayList<>();
            }

            return new ArrayList<>();

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per la griglia di partenza: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //Risultati finali della gara
    public List<org.SimOneSpeedBot.api.ergast.RaceResultAPI.Result> fetchRaceResults(int year, int round) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + year + "/" + round + "/results.json"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Gson deserializzatore = new Gson();

                org.SimOneSpeedBot.api.ergast.RaceResultAPI.RootResponse root =
                        deserializzatore.fromJson(response.body(), org.SimOneSpeedBot.api.ergast.RaceResultAPI.RootResponse.class);

                org.SimOneSpeedBot.api.ergast.RaceResultAPI.MRData mrData = root.getMRData();
                if (mrData == null || mrData.getRaceTable() == null) {
                    return new ArrayList<>();
                }

                List<org.SimOneSpeedBot.api.ergast.RaceResultAPI.Race> races = mrData.getRaceTable().getRaces();
                if (races == null || races.isEmpty()) {
                    return new ArrayList<>();
                }

                org.SimOneSpeedBot.api.ergast.RaceResultAPI.Race race = races.get(0);
                return race.getResults() != null ? race.getResults() : new ArrayList<>();
            }

            return new ArrayList<>();

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per i risultati della gara: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //Risultati qualifiche
    public List<org.SimOneSpeedBot.api.ergast.QualifyingAPI.QualifyingResult> fetchQualifying(int year, int round) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + year + "/" + round + "/qualifying.json"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Gson deserializzatore = new Gson();

                org.SimOneSpeedBot.api.ergast.QualifyingAPI.RootResponse root =
                        deserializzatore.fromJson(response.body(), org.SimOneSpeedBot.api.ergast.QualifyingAPI.RootResponse.class);

                org.SimOneSpeedBot.api.ergast.QualifyingAPI.MRData mrData = root.getMRData();
                if (mrData == null || mrData.getRaceTable() == null) {
                    return new ArrayList<>();
                }

                List<org.SimOneSpeedBot.api.ergast.QualifyingAPI.Race> races = mrData.getRaceTable().getRaces();
                if (races == null || races.isEmpty()) {
                    return new ArrayList<>();
                }

                org.SimOneSpeedBot.api.ergast.QualifyingAPI.Race race = races.get(0);
                return race.getQualifyingResults() != null ? race.getQualifyingResults() : new ArrayList<>();
            }

            return new ArrayList<>();

        } catch (IOException | InterruptedException e) {
            System.err.println("Errore in richiesta API per le qualifiche: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //Standings

    //Recupera classifica piloti per una stagione
    public List<DriverStanding> fetchDriverStandings(int year) {
        String url = baseUrl + year + "/driverStandings.json";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                RootResponse rootResponse = gson.fromJson(response.body(), RootResponse.class);

                if (rootResponse != null &&
                        rootResponse.getMRData() != null &&
                        rootResponse.getMRData().getStandingsTable() != null &&
                        rootResponse.getMRData().getStandingsTable().getStandingsLists() != null &&
                        !rootResponse.getMRData().getStandingsTable().getStandingsLists().isEmpty()) {

                    return rootResponse.getMRData().getStandingsTable().getStandingsLists().get(0).getDriverStandings();
                }
            }

            return new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Errore recupero classifica piloti: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //Recupera classifica costruttori per una stagione
    public List<ConstructorStanding> fetchConstructorStandings(int year) {
        String url = baseUrl + year + "/constructorStandings.json";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                RootResponse rootResponse = gson.fromJson(response.body(), RootResponse.class);

                if (rootResponse != null &&
                        rootResponse.getMRData() != null &&
                        rootResponse.getMRData().getStandingsTable() != null &&
                        rootResponse.getMRData().getStandingsTable().getStandingsLists() != null &&
                        !rootResponse.getMRData().getStandingsTable().getStandingsLists().isEmpty()) {

                    return rootResponse.getMRData().getStandingsTable().getStandingsLists().get(0).getConstructorStandings();
                }
            }

            return new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Errore recupero classifica costruttori: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
