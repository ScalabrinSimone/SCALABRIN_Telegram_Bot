package org.SimOneSpeedBot.api.ergast.DriverAPI;

import java.util.List;

public class DriverTable {
    private String driverId;
    private List<Driver> Drivers;

    public List<Driver> getDrivers() {
        return Drivers;
    }

    public String getDriverId() {
        return driverId;
    }
}
