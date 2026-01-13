package org.SimOneSpeedBot.api.ergast.StandingsAPI;

import java.util.List;

public class StandingsTable {
    private List<StandingsList> standingsLists;

    public List<StandingsList> getStandingsLists() {
        return standingsLists;
    }

    public void setStandingsLists(List<StandingsList> standingsLists) {
        this.standingsLists = standingsLists;
    }
}
