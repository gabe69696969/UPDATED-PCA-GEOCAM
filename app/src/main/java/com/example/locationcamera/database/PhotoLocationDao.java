package com.example.locationcamera.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.locationcamera.model.PhotoLocation;
import java.util.List;

@Dao
public interface PhotoLocationDao {

    @Query("SELECT * FROM photo_locations ORDER BY timestamp DESC")
    List<PhotoLocation> getAllPhotos();

    @Query("SELECT * FROM photo_locations WHERE id = :id")
    PhotoLocation getPhotoById(int id);

    @Insert
    long insertPhoto(PhotoLocation photoLocation);

    @Update
    void updatePhoto(PhotoLocation photoLocation);

    @Delete
    void deletePhoto(PhotoLocation photoLocation);

    @Query("DELETE FROM photo_locations WHERE id = :id")
    void deletePhotoById(int id);
}