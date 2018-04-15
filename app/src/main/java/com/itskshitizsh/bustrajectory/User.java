package com.itskshitizsh.bustrajectory;

public class User {
    private String userId;
    private String coordinates;     // 'Latitude , Longitude'
    private String updateTime;

    public User() {
        // Constructor with no Parameters
    }

    public User(String userId, String coordinates, String updateTime) {
        this.userId = userId;
        this.coordinates = coordinates;
        this.updateTime = updateTime;
    }

    public String getUserId() {
        return userId;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getUpdateTime() {
        return updateTime;
    }
}