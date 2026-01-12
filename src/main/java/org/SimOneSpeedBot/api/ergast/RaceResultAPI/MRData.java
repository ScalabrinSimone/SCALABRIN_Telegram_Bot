package org.SimOneSpeedBot.api.ergast.RaceResultAPI;

public class MRData {
    private String series;
    private String url;
    private String limit;
    private String offset;
    private String total;
    private RaceTable RaceTable;

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public RaceTable getRaceTable() {
        return RaceTable;
    }

    public void setRaceTable(RaceTable raceTable) {
        RaceTable = raceTable;
    }
}
