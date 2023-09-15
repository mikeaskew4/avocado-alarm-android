package com.michaelaskew.avocadotimer.models;
import java.time.LocalDateTime;

public class Avocado {
    private String id;
    private String name;
    private String imagePath;
    private LocalDateTime creationTime;

    // Constructors, getters, and setters...
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }
}
