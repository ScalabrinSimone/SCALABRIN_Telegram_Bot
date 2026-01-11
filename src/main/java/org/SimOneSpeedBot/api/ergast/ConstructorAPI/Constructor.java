package org.SimOneSpeedBot.api.ergast.ConstructorAPI;

public class Constructor {
    private String constructorId;
    private String url;
    private String name;
    private String nationality;

    public Constructor(String constructorId, String url, String name, String nationality) {
        this.constructorId = constructorId;
        this.url = url;
        this.name = name;
        this.nationality = nationality;
    }

    public String getConstructorId() { return constructorId; }
    public String getUrl() { return url; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }

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
