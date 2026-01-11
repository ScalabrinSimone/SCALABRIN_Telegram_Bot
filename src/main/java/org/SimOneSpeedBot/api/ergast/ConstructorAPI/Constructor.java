package org.SimOneSpeedBot.api.ergast.ConstructorAPI;

public class Constructor {
    private String constructorId;
    private String url;
    private String name;
    private String nationality;

    @Override
    public String toString(){
        return String.format(
                "ℹ Informazioni:\n\n🏠 Nome: %s.\n🌍 Nazionalitá: %s.\n\n🔗 Url (wikipedia): %s.",
                name,
                nationality,
                url != null ? url : "*dato non disponibile*"
        );
    }
}
