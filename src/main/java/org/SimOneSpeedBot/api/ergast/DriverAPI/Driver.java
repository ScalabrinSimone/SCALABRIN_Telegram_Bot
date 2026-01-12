package org.SimOneSpeedBot.api.ergast.DriverAPI;

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

    public String getDriverId() {
        return driverId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    @Override
    public String toString() {
        return String.format(
                "ℹ <b>Informazioni:</b>\n\n👤 <b>Nome e Cognome:</b> %s %s.\n🌍 <b>Nazionalitá:</b> %s.\n🎂 <b>Nascita:</b> %s.\n🔢 <b>Numero pilota:</b> %s.\n🏷 <b>Abbreviazione:</b> %s.\n\n🔗 <b>Url (wikipedia):</b> %s.",
                givenName,
                familyName,
                nationality,
                dateOfBirth,
                permanentNumber != null ? permanentNumber : "*<i>dato non disponibile</i>*", //Gestisco caso di dati non disbonibili
                code != null ? code : "*<i>dato non disponibile</i>*", //es. Verstappen -> Jos Verstappen (no dati).
                url != null ? url : "*<i>dato non disponibile</i>*"
        );
    }
}
