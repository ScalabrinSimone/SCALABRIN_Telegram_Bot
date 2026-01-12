package org.SimOneSpeedBot.api.ergast.RaceResultAPI;

public class FastestLap {
    private String rank;
    private String lap;
    private LapTime Time;
    private AverageSpeed AverageSpeed;

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getLap() {
        return lap;
    }

    public void setLap(String lap) {
        this.lap = lap;
    }

    public LapTime getTime() {
        return Time;
    }

    public void setTime(LapTime time) {
        Time = time;
    }

    public AverageSpeed getAverageSpeed() {
        return AverageSpeed;
    }

    public void setAverageSpeed(AverageSpeed averageSpeed) {
        AverageSpeed = averageSpeed;
    }
}
