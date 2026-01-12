package org.SimOneSpeedBot.api.ergast.GridAPI;

public class RaceTable {
    private String season;
    private String round;
    private List<Race> Races;

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public List<Race> getRaces() {
        return Races;
    }

    public void setRaces(List<Race> races) {
        Races = races;
    }
}
