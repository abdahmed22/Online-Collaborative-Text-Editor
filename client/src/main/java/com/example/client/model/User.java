package com.example.client.model;

public class User {
    private final String userId;
    private final String userName;
    private final String userColor;
    private UserRole userRole;
    private int userCursor;

    public User(String userId, String userName, String userColor, UserRole userRole) {
        this.userId = userId;
        this.userName = userName;
        this.userColor = userColor;
        this.userRole = userRole;
        this.userCursor = 0;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserColor() {
        return userColor;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public int getUserPosition() {
        return userCursor;
    }

    public void setUserPosition(int userCursor) {
        this.userCursor = userCursor;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }
}