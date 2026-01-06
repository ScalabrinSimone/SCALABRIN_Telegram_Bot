package org.SimOneSpeedBot.api.ergast.SeasonAPI;

public class MRData {
    private String xmlns;
    private String series;
    private String url;
    private String limit;
    private String offset;
    private String total;
    private RaceTable RaceTable;

    //Getter
    public String getTotal() { return total; }
    public RaceTable getRaceTable() { return RaceTable; }
}