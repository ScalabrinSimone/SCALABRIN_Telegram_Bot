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
                permanentNumber,
                code
        );
    }
}
