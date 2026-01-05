package org.SimOneSpeedBot.api.ergast.SeasonAPI;

public class Race {
    private String season;
    private String round;
    private String url;
    private String raceName;
    private String date;
    private String time;
    private Circuit Circuit;

    //Getter
    public String getSeason() { return season; }
    public String getRound() { return round; }
    public String getUrl() { return url; }
    public String getRaceName() { return raceName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public Circuit getCircuit() { return Circuit; }

    @Override
    public String toString() {
        return "🏁 Round " + round + ": " + raceName + "\n" +
                "📍 " + Circuit.getCircuitName() + " (" + Circuit.getLocation().getCountry() + ")\n" +
                "📅 " + date + (time != null ? " " + time : "");
    }
}