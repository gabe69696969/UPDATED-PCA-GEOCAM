package com.example.locationcamera.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import com.example.locationcamera.model.PhotoLocation;

@Database(entities = {PhotoLocation.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PhotoLocationDao photoLocationDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                        AppDatabase.class, "photo_location_database")
                                .fallbackToDestructiveMigration() // Handle schema changes gracefully
                                .build();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to create database", e);
                    }
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}