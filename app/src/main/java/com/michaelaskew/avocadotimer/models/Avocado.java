package com.michaelaskew.avocadotimer.models;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Avocado implements Serializable {
    private int id;
    private String name;
    private String imagePath;
    private String creationTime;
    private int squishiness;

    // Constructors, getters, and setters...
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getSquishiness() { return squishiness; }

    public void setSquishiness(int squishiness) { this.squishiness = squishiness; }
}
