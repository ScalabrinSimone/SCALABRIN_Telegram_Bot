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
    public String getXmlns() { return xmlns; }
    public String getSeries() { return series; }
    public String getUrl() { return url; }
    public String getLimit() { return limit; }
    public String getOffset() { return offset; }
    public String getTotal() { return total; }
    public RaceTable getRaceTable() { return RaceTable; }
}