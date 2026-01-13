package org.SimOneSpeedBot.api.ergast.StandingsAPI;

import java.util.List;

public class StandingsTable {
    private List<StandingsList> StandingsLists;

    public List<StandingsList> getStandingsLists() {
        return StandingsLists;
    }

    public void setStandingsLists(List<StandingsList> StandingsLists) {
        this.StandingsLists = StandingsLists;
    }
}
