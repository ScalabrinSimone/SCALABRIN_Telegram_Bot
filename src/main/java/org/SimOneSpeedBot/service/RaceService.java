package org.SimOneSpeedBot.service;

import org.SimOneSpeedBot.api.ergast.GridAPI.GridPosition;
import org.SimOneSpeedBot.api.ergast.RaceResultAPI.Result;
import org.SimOneSpeedBot.api.ergast.SeasonAPI.Race;

import java.util.List;

public class RaceService {
    //Formatta la lista delle gare della stagione
    public static String formatSeasonRaces(List<Race> races, int year) {
        StringBuilder message = new StringBuilder();
        message.append("<b>🏎️ Stagione Formula 1 ").append(year).append("</b>\n\n");
        message.append("📊 <b>Totale gare:</b> ").append(races.size()).append("\n\n");
        message.append("<i>Seleziona una gara per visualizzare i dettagli:</i>");

        return message.toString();
    }

    //Formatta la griglia di partenza
    public static String formatStartingGrid(List<GridPosition> grid, String raceName, int year, int round) {
        if (grid.isEmpty()) {
            return "❌ <b>Griglia di partenza non disponibile</b>\n\n" +
                    "<i>I dati potrebbero non essere ancora disponibili per questa gara.</i>";
        }

        StringBuilder message = new StringBuilder();
        message.append("<b>🏁 Griglia di Partenza</b>\n");
        message.append("<b>").append(raceName).append("</b> (").append(year).append(" - Round ").append(round).append(")\n\n");

        //Top 3 con emoji speciali
        for (int i = 0; i < Math.min(3, grid.size()); i++) {
            GridPosition pos = grid.get(i);
            message.append(pos.toString()).append("\n");
        }

        if (grid.size() > 3) {
            message.append("\n");
            //Resto della griglia
            for (int i = 3; i < grid.size(); i++) {
                GridPosition pos = grid.get(i);
                message.append(pos.toString()).append("\n");
            }
        }

        return message.toString();
    }

    //Formatta i risultati finali
    public static String formatRaceResults(List<Result> results, String raceName, int year, int round) {
        if (results.isEmpty()) {
            return "❌ <b>Risultati non disponibili</b>\n\n" +
                    "<i>I dati potrebbero non essere ancora disponibili per questa gara.</i>";
        }

        StringBuilder message = new StringBuilder();
        message.append("<b>🏆 Risultati Finali</b>\n");
        message.append("<b>").append(raceName).append("</b> (").append(year).append(" - Round ").append(round).append(")\n\n");

        //Podio
        message.append("<b>🏆 Podio:</b>\n");
        for (int i = 0; i < Math.min(3, results.size()); i++) {
            Result result = results.get(i);
            message.append(result.toString()).append("\n");
        }

        //Verifica se ci sono altri piloti classificati
        if (results.size() > 3) {
            message.append("\n<b>📊 Classificati:</b>\n");
            for (int i = 3; i < Math.min(10, results.size()); i++) { //Mostra fino a P10
                Result result = results.get(i);
                message.append(result.toString()).append("\n");
            }
        }

        //Conta ritiri
        long ritiri = results.stream()
                .filter(r -> !r.getStatus().equals("Finished") &&
                        !r.getStatus().contains("+"))
                .count();

        if (ritiri > 0) {
            message.append("\n⚠️ <b>Ritiri:</b> ").append(ritiri);
        }

        //Giro veloce
        Result fastestLapDriver = results.stream()
                .filter(r -> r.getFastestLap() != null &&
                        r.getFastestLap().getRank() != null &&
                        r.getFastestLap().getRank().equals("1"))
                .findFirst()
                .orElse(null);

        if (fastestLapDriver != null && fastestLapDriver.getFastestLap().getTime() != null) {
            message.append("\n\n⚡ <b>Giro più veloce:</b> ")
                    .append(fastestLapDriver.getDriver().getGivenName())
                    .append(" ")
                    .append(fastestLapDriver.getDriver().getFamilyName())
                    .append(" - ")
                    .append(fastestLapDriver.getFastestLap().getTime().getTime());
        }

        return message.toString();
    }

    //Formatta info base della gara
    public static String formatRaceInfo(Race race) {
        StringBuilder message = new StringBuilder();
        message.append("<b>🏁 ").append(race.getRaceName()).append("</b>\n\n");
        message.append("📍 <b>Circuito:</b> ").append(race.getCircuit().getCircuitName()).append("\n");
        message.append("🌍 <b>Località:</b> ").append(race.getCircuit().getLocation().getLocality())
                .append(", ").append(race.getCircuit().getLocation().getCountry()).append("\n");
        message.append("📅 <b>Data:</b> ").append(race.getDate()).append("\n");

        if (race.getTime() != null && !race.getTime().isEmpty()) {
            message.append("⏰ <b>Ora:</b> ").append(race.getTime()).append(" UTC\n");
        }

        message.append("\n<i>Seleziona un'opzione:</i>");

        return message.toString();
    }

    //Formatta risultati qualifiche
    public static String formatQualifying(List<org.SimOneSpeedBot.api.ergast.QualifyingAPI.QualifyingResult> qualifying, String raceName, int year, int round) {
        if (qualifying.isEmpty()) {
            return "❌ <b>Risultati qualifiche non disponibili</b>\n\n" +
                    "<i>I dati potrebbero non essere ancora disponibili per questa gara.</i>";
        }

        StringBuilder message = new StringBuilder();
        message.append("<b>⏱️ Risultati Qualifiche</b>\n");
        message.append("<b>").append(raceName).append("</b> (").append(year).append(" - Round ").append(round).append(")\n\n");

        //Top 3
        message.append("<b>🏆 Top 3:</b>\n");
        for (int i = 0; i < Math.min(3, qualifying.size()); i++) {
            message.append(qualifying.get(i).toString()).append("\n");
        }

        //Q3 (dal 4° al 10°)
        if (qualifying.size() > 3) {
            message.append("\n<b>🟢 Q3 (P4-P10):</b>\n");
            for (int i = 3; i < Math.min(10, qualifying.size()); i++) {
                message.append(qualifying.get(i).toString()).append("\n");
            }
        }

        //Q2 (dall'11° al 15°)
        if (qualifying.size() > 10) {
            message.append("\n<b>🟡 Q2 (P11-P15):</b>\n");
            for (int i = 10; i < Math.min(15, qualifying.size()); i++) {
                message.append(qualifying.get(i).toString()).append("\n");
            }
        }

        //Q1 (dal 16° in poi)
        if (qualifying.size() > 15) {
            message.append("\n<b>🔴 Q1 (P16+):</b>\n");
            for (int i = 15; i < qualifying.size(); i++) {
                message.append(qualifying.get(i).toString()).append("\n");
            }
        }

        return message.toString();
    }
}
