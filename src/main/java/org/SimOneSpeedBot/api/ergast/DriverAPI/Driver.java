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

    @Override
    public String toString(){
        return String.format(
                "Nome: %s.\nCognome: %s.\nNazionalitá: %s.\nNascita: %s.\nNumero pilota: %s.\nAbbreviazione: %s.",
                givenName,
                familyName,
                nationality,
                dateOfBirth,
                permanentNumber != null ? permanentNumber : "*dato non disponibile*", //Gestisco caso di dati non disbonibili
                code != null ? code : "*dato non disponibile*" //es. Verstappen -> Jos Verstappen (no dati).
        );
    }
}
