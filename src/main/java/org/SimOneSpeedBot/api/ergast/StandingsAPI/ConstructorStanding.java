package org.SimOneSpeedBot.api.ergast.StandingsAPI;

public class ConstructorStanding {
    private String position;

    private String points;

    private String wins;

    private Constructor Constructor;

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

    public Constructor getConstructor() {
        return Constructor;
    }

    public void setConstructor(Constructor Constructor) {
        this.Constructor = Constructor;
    }
}