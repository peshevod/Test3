package com.example.test3.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    private String userName;
    private String password;
    private String hostName;

    public LoggedInUser(String userName, String password, String hostName, String userId, String displayName) {
        this.userName=userName;
        this.password=password;
        this.hostName=hostName;
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getHostName() { return hostName; }
}