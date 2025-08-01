package com.example.locationcamera.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo_folders")
public class PhotoFolder {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public long createdAt;
    public int photoCount;

    public PhotoFolder() {}

    public PhotoFolder(String name, long createdAt) {
        this.name = name;
        this.createdAt = createdAt;
        this.photoCount = 0;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getPhotoCount() { return photoCount; }
    public void setPhotoCount(int photoCount) { this.photoCount = photoCount; }
}