package org.SimOneSpeedBot.api.ergast.StandingsAPI;

import java.util.List;

public class DriverStanding {
    private String position;

    private String points;

    private String wins;

    private Driver Driver;

    private List<Constructor> Constructors;

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public void setWins(String wins) {
        this.wins = wins;
    }

    public Driver getDriver() {
        return Driver;
    }

    public void setDriver(Driver Driver) {
        this.Driver = Driver;
    }

    public void setConstructors(List<Constructor> Constructors) {
        this.Constructors = Constructors;
    }
}