package org.SimOneSpeedBot.api.ergast.ConstructorAPI;

import java.util.List;

public class ConstructorTable {
    private String constructorId;
    private List<Constructor> Constructors;

    public List<Constructor> getConstructors() {
        return Constructors;
    }
    public String getConstructorId() { return constructorId; }
}
