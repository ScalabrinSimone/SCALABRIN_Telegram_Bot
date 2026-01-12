package org.SimOneSpeedBot.api.ergast.ConstructorAPI;

public class MRData {
    private String series;
    private String url;
    private String limit;
    private String offset;
    private String total;
    private ConstructorTable ConstructorTable;

    public String getTotal() {
        return total;
    }

    public ConstructorTable getConstructorTable() {
        return ConstructorTable;
    }
}
