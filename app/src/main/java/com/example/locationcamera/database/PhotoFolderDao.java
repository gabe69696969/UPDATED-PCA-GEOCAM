package com.example.locationcamera.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.locationcamera.model.PhotoFolder;
import java.util.List;

@Dao
public interface PhotoFolderDao {

    @Query("SELECT * FROM photo_folders ORDER BY createdAt DESC")
    List<PhotoFolder> getAllFolders();

    @Query("SELECT * FROM photo_folders WHERE id = :id")
    PhotoFolder getFolderById(int id);

    @Insert
    long insertFolder(PhotoFolder folder);

    @Update
    void updateFolder(PhotoFolder folder);

    @Delete
    void deleteFolder(PhotoFolder folder);

    @Query("DELETE FROM photo_folders WHERE id = :id")
    void deleteFolderById(int id);

    @Query("UPDATE photo_folders SET photoCount = (SELECT COUNT(*) FROM photo_locations WHERE folderId = :folderId) WHERE id = :folderId")
    void updateFolderPhotoCount(int folderId);
}