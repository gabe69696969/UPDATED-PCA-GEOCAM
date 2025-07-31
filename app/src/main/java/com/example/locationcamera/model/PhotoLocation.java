package com.example.locationcamera.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo_locations")
public class PhotoLocation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String photoPath;
    public double latitude;
    public double longitude;
    public float accuracy;
    public double altitude;
    public String address;
    public long timestamp;

    public PhotoLocation() {}

    public PhotoLocation(String photoPath, double latitude, double longitude,
                        float accuracy, double altitude, String address, long timestamp) {
        this.photoPath = photoPath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.address = address;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getAccuracy() { return accuracy; }
    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

    public double getAltitude() { return altitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}