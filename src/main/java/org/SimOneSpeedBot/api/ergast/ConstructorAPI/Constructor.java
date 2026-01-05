package org.SimOneSpeedBot.api.ergast.ConstructorAPI;

public class Constructor {
    private String constructorId;
    private String url;
    private String name;
    private String nationality;

    @Override
    public String toString(){
        return String.format(
                "ℹ Informazioni:\n\nNome: %s.\nNazionalitá: %s.\n\nUrl (wikipedia): %s.",
                name,
                nationality,
                url
        );
    }
}
