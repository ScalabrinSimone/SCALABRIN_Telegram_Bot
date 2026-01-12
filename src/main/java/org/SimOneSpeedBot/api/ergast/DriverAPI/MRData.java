package org.SimOneSpeedBot.api.ergast.DriverAPI;

public class MRData {
    private String series;
    private String url;
    private String limit;
    private String offset;
    private String total;
    private DriverTable DriverTable;

    public String getTotal() {
        return total;
    }

    public DriverTable getDriverTable() {
        return DriverTable;
    }
}
