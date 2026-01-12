package org.SimOneSpeedBot.database.Bookmarks;

public class Bookmark {
    private int id;
    private long userId;
    private String type;
    private String entityId;
    private String entityName;
    private String message;
    private String savedAt;

    public Bookmark(int id, long userId, String type, String entityId, String entityName, String message, String savedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.entityId = entityId;
        this.entityName = entityName;
        this.message = message;
        this.savedAt = savedAt;
    }

    //Getters
    public int getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getMessage() {
        return message;
    }

    public String getSavedAt() {
        return savedAt;
    }
}
