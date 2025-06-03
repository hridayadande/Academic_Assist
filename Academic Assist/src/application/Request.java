package application;

public class Request {
    private String username;
    private String reason;
    private String date;
    private int id;
    private String description;
    private int reopenedFromId; // ID of original request that this was reopened from

    public Request(String username, String reason, String date) {
        this.username = username;
        this.reason = reason;
        this.date = date;
        this.description = "";
        this.reopenedFromId = -1; // Default value indicating not reopened
    }

    public Request(int id, String username, String reason, String date, String description) {
        this.id = id;
        this.username = username;
        this.reason = reason;
        this.date = date;
        this.description = description;
        this.reopenedFromId = -1; // Default value indicating not reopened
    }

    public Request(int id, String username, String reason, String date, String description, int reopenedFromId) {
        this.id = id;
        this.username = username;
        this.reason = reason;
        this.date = date;
        this.description = description;
        this.reopenedFromId = reopenedFromId;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }

    public String getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getReopenedFromId() {
        return reopenedFromId;
    }
    
    public void setReopenedFromId(int reopenedFromId) {
        this.reopenedFromId = reopenedFromId;
    }
    
    public boolean isReopened() {
        return reopenedFromId > 0;
    }
} 