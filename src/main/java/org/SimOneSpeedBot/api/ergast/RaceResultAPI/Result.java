package org.SimOneSpeedBot.api.ergast.RaceResultAPI;

import org.SimOneSpeedBot.api.ergast.ConstructorAPI.Constructor;
import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;

public class Result {
    private String number;
    private String position;
    private String positionText;
    private String points;
    private Driver Driver;
    private Constructor Constructor;
    private String grid;
    private String laps;
    private String status;
    private Time Time;
    private FastestLap FastestLap;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPositionText() {
        return positionText;
    }

    public void setPositionText(String positionText) {
        this.positionText = positionText;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public Driver getDriver() {
        return Driver;
    }

    public void setDriver(Driver driver) {
        Driver = driver;
    }

    public Constructor getConstructor() {
        return Constructor;
    }

    public void setConstructor(Constructor constructor) {
        Constructor = constructor;
    }

    public String getGrid() {
        return grid;
    }

    public void setGrid(String grid) {
        this.grid = grid;
    }

    public String getLaps() {
        return laps;
    }

    public void setLaps(String laps) {
        this.laps = laps;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Time getTime() {
        return Time;
    }

    public void setTime(Time time) {
        Time = time;
    }

    public FastestLap getFastestLap() {
        return FastestLap;
    }

    public void setFastestLap(FastestLap fastestLap) {
        FastestLap = fastestLap;
    }

    @Override
    public String toString() {
        String emoji = switch (position) {
            case "1" -> "🥇";
            case "2" -> "🥈";
            case "3" -> "🥉";
            default -> "▪️";
        };

        String timeStr = (Time != null && Time.getTime() != null) ?
                " - " + Time.getTime() :
                " - " + status;

        return emoji + " P" + position + " - " + Driver.getGivenName() + " " +
                Driver.getFamilyName() + " (" + Constructor.getName() + ")" +
                timeStr + " [" + points + " pts]";
    }
}
