package org.SimOneSpeedBot.api.ergast.StandingsAPI;

import java.util.List;

public class DriverStanding {
    private String position;

    private String points;

    private String wins;

    private Driver Driver;

    private List<Constructor> Constructors;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getWins() {
        return wins;
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

    public List<Constructor> getConstructors() {
        return Constructors;
    }

    public void setConstructors(List<Constructor> Constructors) {
        this.Constructors = Constructors;
    }
}