package org.SimOneSpeedBot.database.Bookmarks;

import org.SimOneSpeedBot.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkManager {

    //Salva un bookmark
    public static boolean saveBookmark(long userId, String type, String entityId, String entityName, String message) {
        //Controlla se esiste già
        if (bookmarkExists(userId, type, entityId)) {
            return false; //Già salvato
        }

        String sql = "INSERT INTO bookmarks (userId, type, entityId, entityName, message) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, entityId);
            pstmt.setString(4, entityName);
            pstmt.setString(5, message);
            pstmt.executeUpdate();
            //System.out.println("Bookmark salvato con succeso"); //Debug
            return true;

        } catch (SQLException e) {
            System.err.println("Errore salvataggio bookmark: " + e.getMessage());
            return false;
        }
    }

    //Rimuovi un bookmark
    public static boolean removeBookmark(long userId, String type, String entityId) {
        String sql = "DELETE FROM bookmarks WHERE userId = ? AND type = ? AND entityId = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, entityId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Errore rimozione bookmark: " + e.getMessage());
            return false;
        }
    }

    //Controlla se un bookmark esiste
    public static boolean bookmarkExists(long userId, String type, String entityId) {
        String sql = "SELECT COUNT(*) FROM bookmarks WHERE userId = ? AND type = ? AND entityId = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, entityId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    //Ottieni tutti i bookmark di un utente per una categoria MODIFICA
    public static List<Bookmark> getBookmarkByType(long userId, String type) {
        List<Bookmark> bookmarks = new ArrayList<>();
        String sql = "SELECT * FROM bookmarks WHERE userId = ? AND type = ? ORDER BY savedAt DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                bookmarks.add(new Bookmark(
                        rs.getInt("id"),
                        rs.getLong("userId"),
                        rs.getString("type"),
                        rs.getString("entityId"),
                        rs.getString("entityName"),
                        rs.getString("message"),
                        rs.getString("savedAt")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Errore recupero bookmark: " + e.getMessage());
        }

        return bookmarks;
    }

    //Ottieni tutti i bookmark di un utente per categoria
    public static List<Bookmark> getBookmarksByType(long userId) {
        List<Bookmark> bookmarks = new ArrayList<>();
        String sql = "SELECT * FROM bookmarks WHERE userId = ? ORDER BY savedAt DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                bookmarks.add(new Bookmark(
                        rs.getInt("id"),
                        rs.getLong("userId"),
                        rs.getString("type"),
                        rs.getString("entityId"),
                        rs.getString("entityName"),
                        rs.getString("message"),
                        rs.getString("savedAt")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Errore recupero bookmark: " + e.getMessage());
        }

        return bookmarks;
    }

    //Ottiene le categorie che hanno bookmark salvati per l'utente
    public static List<String> getAvailableCategories(long userId) {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT type FROM bookmarks WHERE userId = ? ORDER BY type";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("type"));
            }

        } catch (SQLException e) {
            System.err.println("Errore recupero categorie: " + e.getMessage());
        }

        return categories;
    }

    //Conta bookmark per categoria
    public static Map<String, Integer> countBookmarksByType(long userId) {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT type, COUNT(*) as count FROM bookmarks WHERE userId = ? GROUP BY type";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                counts.put(rs.getString("type"), rs.getInt("count"));
            }

        } catch (SQLException e) {
            System.err.println("Errore conteggio bookmark: " + e.getMessage());
        }

        return counts;
    }
}