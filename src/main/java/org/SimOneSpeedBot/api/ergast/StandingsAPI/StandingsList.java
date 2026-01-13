package org.SimOneSpeedBot.api.ergast.StandingsAPI;

import java.util.List;

public class StandingsList {
    private String season;

    private List<DriverStanding> DriverStandings;

    private List<ConstructorStanding> ConstructorStandings;

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public List<DriverStanding> getDriverStandings() {
        return DriverStandings;
    }

    public void setDriverStandings(List<DriverStanding> DriverStandings) {
        this.DriverStandings = DriverStandings;
    }

    public List<ConstructorStanding> getConstructorStandings() {
        return ConstructorStandings;
    }

    public void setConstructorStandings(List<ConstructorStanding> ConstructorStandings) {
        this.ConstructorStandings = ConstructorStandings;
    }
}
