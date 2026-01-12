package org.SimOneSpeedBot.api.ergast.GridAPI;

import org.SimOneSpeedBot.api.ergast.ConstructorAPI.Constructor;
import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;

public class GridPosition {
    private String number;
    private String position;
    private Driver Driver;
    private Constructor Constructor;

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

    @Override
    public String toString() {
        String emoji = switch (position) {
            case "1" -> "🥇";
            case "2" -> "🥈";
            case "3" -> "🥉";
            default -> "🏁";
        };

        return emoji + " P" + position + " - " + Driver.getGivenName() + " " + Driver.getFamilyName() +
                " (" + Constructor.getName() + ")";
    }
}
