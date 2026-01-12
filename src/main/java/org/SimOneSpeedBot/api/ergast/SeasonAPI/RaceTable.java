package org.SimOneSpeedBot.api.ergast.SeasonAPI;

import java.util.List;

public class RaceTable {
    private String season;
    private List<Race> Races;

    //Getter
    public String getSeason() {
        return season;
    }

    public List<Race> getRaces() {
        return Races;
    }
}