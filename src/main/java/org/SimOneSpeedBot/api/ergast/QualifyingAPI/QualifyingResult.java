package org.SimOneSpeedBot.api.ergast.QualifyingAPI;

import org.SimOneSpeedBot.api.ergast.ConstructorAPI.Constructor;
import org.SimOneSpeedBot.api.ergast.DriverAPI.Driver;

public class QualifyingResult {
    private String number;
    private String position;
    private Driver Driver;
    private Constructor Constructor;
    private String Q1;
    private String Q2;
    private String Q3;

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

    public String getQ1() {
        return Q1;
    }

    public void setQ1(String q1) {
        Q1 = q1;
    }

    public String getQ2() {
        return Q2;
    }

    public void setQ2(String q2) {
        Q2 = q2;
    }

    public String getQ3() {
        return Q3;
    }

    public void setQ3(String q3) {
        Q3 = q3;
    }

    @Override
    public String toString() {
        String emoji = switch (position) {
            case "1" -> "🥇";
            case "2" -> "🥈";
            case "3" -> "🥉";
            default -> "▪️";
        };

        StringBuilder timeInfo = new StringBuilder();
        if (Q3 != null && !Q3.isEmpty()) {
            timeInfo.append(" - Q3: ").append(Q3);
        } else if (Q2 != null && !Q2.isEmpty()) {
            timeInfo.append(" - Q2: ").append(Q2);
        } else if (Q1 != null && !Q1.isEmpty()) {
            timeInfo.append(" - Q1: ").append(Q1);
        }

        return emoji + " P" + position + " - " + Driver.getGivenName() + " " +
                Driver.getFamilyName() + " (" + Constructor.getName() + ")" + timeInfo;
    }
}