package org.SimOneSpeedBot.api.ergast.DriverAPI;

import java.util.List;

public class Driver {
    private String driverId;
    private String permanentNumber;
    private String code;
    private String url;
    private String givenName;
    private String familyName;
    private String dateOfBirth;
    private String nationality;

    public Driver(String driverId, String givenName, String familyName, String dateOfBirth, String nationality,
                  String permanentNumber, String code, String url) {
        this.driverId = driverId;
        this.givenName = givenName;
        this.familyName = familyName;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.permanentNumber = permanentNumber;
        this.code = code;
        this.url = url;
    }

    public String getDriverId() { return driverId; }

    public String getGivenName() { return givenName; }

    public String getFamilyName() { return familyName; }

    @Override
    public String toString(){
        return String.format(
                "ℹ Informazioni:\n\n👤 Nome e Cognome: %s %s.\n🌍 Nazionalitá: %s.\n🎂 Nascita: %s.\n🔢 Numero pilota: %s.\n🏷 Abbreviazione: %s.\n\n🔗 Url (wikipedia): %s.",
                givenName,
                familyName,
                nationality,
                dateOfBirth,
                permanentNumber != null ? permanentNumber : "*dato non disponibile*", //Gestisco caso di dati non disbonibili
                code != null ? code : "*dato non disponibile*", //es. Verstappen -> Jos Verstappen (no dati).
                url != null ? url : "*dato non disponibile*"
        );
    }
}
