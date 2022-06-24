package com.example.test3.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String displayName;
    private String userName;
    private String hostName;
    private String password;

//    public LoggedInUser(String userName, String password, String hostName, String userId, String displayName) {
    public LoggedInUser(String hostName, String userName, String password, String displayName) {
        this.userName=userName;
        this.hostName=hostName;
        this.displayName = displayName;
        this.password=password;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getUserName() { return userName; }
    public String getHostName() { return hostName; }
    public String getPassword() { return password; }
}