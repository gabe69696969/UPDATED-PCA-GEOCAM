package com.example.locationcamera.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
<<<<<<< HEAD
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
=======
<<<<<<< HEAD
>>>>>>> parent of 65c0cc0 (fix)
import android.location.Location;
=======
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import java.io.File;
>>>>>>> parent of 902dc8f (Satellite date and time)
=======

>>>>>>> parent of 65c0cc0 (fix)
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoMetadataUtils {

    private static final String TAG = "PhotoMetadataUtils";

    /**
     * Saves photo with location metadata embedded in EXIF and description
     */
    public static boolean savePhotoWithLocationMetadata(String photoPath, double latitude,
                                                        double longitude, String address,
                                                        long timestamp) {
        try {
            // First, add EXIF location data to the photo file
            boolean exifSuccess = addLocationToExif(photoPath, latitude, longitude);

            // Create description with coordinates and timestamp
<<<<<<< HEAD
            long accurateTimestamp = GPSTimeUtils.getAccurateTimestamp(location);
            String description = createLocationDescription(latitude, longitude, address, accurateTimestamp, location);
=======
            String description = createLocationDescription(latitude, longitude, address, timestamp);
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d

            // Add description to file metadata
            boolean descriptionSuccess = addDescriptionToFile(photoPath, description);

            Log.d(TAG, "Photo metadata saved - EXIF: " + exifSuccess + ", Description: " + descriptionSuccess);
            return exifSuccess || descriptionSuccess; // Success if either method works

        } catch (Exception e) {
            Log.e(TAG, "Error saving photo metadata", e);
            return false;
        }
    }


    /**
     * Adds GPS coordinates to EXIF data of the photo
     */
    private static boolean addLocationToExif(String photoPath, double latitude, double longitude) {
        try {
            ExifInterface exif = new ExifInterface(photoPath);

            // Convert latitude to DMS (Degrees, Minutes, Seconds) format
            String latitudeRef = latitude >= 0 ? "N" : "S";
            String longitudeRef = longitude >= 0 ? "E" : "W";

            // Use absolute values for calculations
            double absLatitude = Math.abs(latitude);
            double absLongitude = Math.abs(longitude);

            // Convert to DMS format required by EXIF
            String latitudeDMS = convertToDMS(absLatitude);
            String longitudeDMS = convertToDMS(absLongitude);

            // Set GPS coordinates in EXIF
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeDMS);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeDMS);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRef);

            // Set additional GPS EXIF data
            exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, "GPS");
<<<<<<< HEAD

            // Use accurate GPS satellite time
            if (location != null) {
                // Use GPS satellite time in UTC (standard for GPS)
                exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, GPSTimeUtils.getGPSTimeUTCForExif(location));
                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, GPSTimeUtils.getGPSDateUTCForExif(location));

                // Add GPS accuracy and provider information
                if (location.hasAccuracy()) {
                    // Store accuracy in GPS DOP (Dilution of Precision) field
                    String accuracy = String.format(Locale.US, "%.2f", location.getAccuracy());
                    exif.setAttribute(ExifInterface.TAG_GPS_DOP, accuracy);
                }

                // Add provider information to processing method
                String provider = location.getProvider();
                if (provider != null) {
                    exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, provider.toUpperCase());
                }
            } else {
                // Fallback to system time in UTC
                exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, GPSTimeUtils.getGPSTimeUTCForExif(null));
                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, GPSTimeUtils.getGPSDateUTCForExif(null));
            }
=======
            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, getCurrentTimeStamp());
            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, getCurrentDateStamp());
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d

            // Save EXIF data
            exif.saveAttributes();

            Log.d(TAG, "EXIF GPS data saved successfully: " + latitudeDMS + " " + latitudeRef + ", " + longitudeDMS + " " + longitudeRef);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error saving EXIF GPS data", e);
            return false;
        }
    }

    /**
     * Converts decimal degrees to DMS (Degrees, Minutes, Seconds) format for EXIF
     */
    private static String convertToDMS(double coordinate) {
        int degrees = (int) coordinate;
        double minutesFloat = (coordinate - degrees) * 60;
        int minutes = (int) minutesFloat;
        double seconds = (minutesFloat - minutes) * 60;

        // Format as "degrees/1,minutes/1,seconds/1000" for EXIF
        return String.format(Locale.US, "%d/1,%d/1,%d/1000",
                degrees, minutes, (int)(seconds * 1000));
    }

    /**
     * Creates a formatted description with location information
     */
    private static String createLocationDescription(double latitude, double longitude,
                                                    String address, long timestamp) {
        StringBuilder description = new StringBuilder();

<<<<<<< HEAD
        // Add timestamp information with source
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Use the accurate timestamp (GPS time if available)
        long accurateTimestamp = GPSTimeUtils.getAccurateTimestamp(location);
        String timeSource = GPSTimeUtils.getTimeSourceDescription(location);

        description.append("Captured: ").append(dateFormat.format(new Date(accurateTimestamp))).append("\n");
        description.append("Time Source: ").append(timeSource).append("\n");

        // Add GPS time details if available
        if (GPSTimeUtils.hasValidGPSTime(location)) {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.getDefault());
            utcFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            description.append("GPS UTC Time: ").append(utcFormat.format(new Date(location.getTime()))).append("\n");
        } else {
            description.append("GPS Time: Not available\n");
        }
=======
        // Add timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        description.append("Captured: ").append(dateFormat.format(new Date(timestamp))).append("\n");
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d

        // Add coordinates
        description.append("Coordinates: ").append(String.format(Locale.US, "%.6f, %.6f", latitude, longitude)).append("\n");

        // Add address if available
        if (address != null && !address.isEmpty() && !address.equals("Location unavailable")) {
            description.append("Location: ").append(address).append("\n");
        }

<<<<<<< HEAD
        // Add final note
<<<<<<< HEAD
<<<<<<< HEAD
        description.append("GPS Location Data Embedded by LocationCamera");
=======
        description.append("Satellite GPS Location Data Embedded");
>>>>>>> parent of 902dc8f (Satellite date and time)
=======
        description.append("Satellite GPS Location and Time Data Embedded");
=======
        // Add accuracy note
        description.append("GPS Location Data Embedded");
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
>>>>>>> parent of 65c0cc0 (fix)

        return description.toString();
    }

    /**
     * Adds description to photo file using EXIF UserComment
     */
    private static boolean addDescriptionToFile(String photoPath, String description) {
        try {
            ExifInterface exif = new ExifInterface(photoPath);

            // Set description in EXIF UserComment field
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, description);

            // Set image description
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description);

            // Save EXIF data
            exif.saveAttributes();

            Log.d(TAG, "Photo description saved successfully: " + description);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error saving photo description", e);
            return false;
        }
    }

    /**
     * Saves photo to MediaStore with location metadata in description
     */
    public static Uri savePhotoToMediaStoreWithMetadata(ContentResolver resolver, String photoPath,
                                                        double latitude, double longitude,
                                                        String address, long timestamp) {
        try {
            // First add metadata to the file itself
            savePhotoWithLocationMetadata(photoPath, latitude, longitude, address, timestamp);

            // Create content values for MediaStore
            ContentValues contentValues = new ContentValues();

            // Use accurate timestamp for MediaStore
            long accurateTimestamp = timestamp; // This should already be the GPS time from the calling method

            // Basic file information
            String fileName = "LocationCamera_" + accurateTimestamp + ".jpg";
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, accurateTimestamp / 1000);
            contentValues.put(MediaStore.MediaColumns.DATE_MODIFIED, accurateTimestamp / 1000);

            // Add location data to MediaStore
            if (latitude != 0 && longitude != 0) {
                contentValues.put(MediaStore.Images.Media.LATITUDE, latitude);
                contentValues.put(MediaStore.Images.Media.LONGITUDE, longitude);
            }

            // Create description with location information
            String description = createLocationDescription(latitude, longitude, address, accurateTimestamp);

            // Add description to MediaStore (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Images.Media.DESCRIPTION, description);
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/LocationCamera");
            }

            // For older versions, use TITLE field for description
            contentValues.put(MediaStore.Images.Media.TITLE, "Photo with GPS: " +
                    String.format(Locale.US, "%.6f, %.6f", latitude, longitude));

            // Insert into MediaStore
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            if (imageUri != null) {
                // Copy file content to MediaStore
                try (FileOutputStream out = (FileOutputStream) resolver.openOutputStream(imageUri);
                     java.io.FileInputStream in = new java.io.FileInputStream(photoPath)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();

                    Log.d(TAG, "Photo saved to MediaStore with location metadata: " + imageUri);
                    return imageUri;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving photo to MediaStore with metadata", e);
        }

        return null;
    }

    /**
     * Reads location metadata from a photo file
     */
    public static LocationMetadata readLocationMetadata(String photoPath) {
        try {
            ExifInterface exif = new ExifInterface(photoPath);

            // Read GPS coordinates using the proper EXIF method
            float[] latLong = new float[2];
            boolean hasGPS = exif.getLatLong(latLong);

            // Read description
            String description = exif.getAttribute(ExifInterface.TAG_USER_COMMENT);
            if (description == null) {
                description = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
            }

            // Read timestamp
            String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);

            LocationMetadata metadata = new LocationMetadata();
            if (hasGPS) {
                metadata.latitude = latLong[0];
                metadata.longitude = latLong[1];
                metadata.hasLocation = true;
            }
            metadata.description = description;
            metadata.dateTime = dateTime;

            Log.d(TAG, "Location metadata read - GPS: " + hasGPS + ", Description: " + (description != null));
            return metadata;

        } catch (IOException e) {
            Log.e(TAG, "Error reading location metadata", e);
            return new LocationMetadata();
        }
    }

    /**
<<<<<<< HEAD
=======
     * Gets current timestamp for EXIF
     */
    private static String getCurrentTimeStamp() {
<<<<<<< HEAD
=======
        // Use GPS time if available, otherwise system time
>>>>>>> parent of 902dc8f (Satellite date and time)
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(new Date());
    }

    /**
     * Gets current date stamp for EXIF
     */
    private static String getCurrentDateStamp() {
<<<<<<< HEAD
=======
        // Use GPS time if available, otherwise system time
>>>>>>> parent of 902dc8f (Satellite date and time)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.getDefault());
<<<<<<< HEAD
        return dateFormat.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Gets GPS-based timestamp if location has time information
     */
<<<<<<< HEAD
    public static String getGPSTimeStamp(Location location) {
=======
    private static String getGPSTimeStamp(Location location) {
>>>>>>> parent of 902dc8f (Satellite date and time)
        if (location != null && location.getTime() > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            return timeFormat.format(new Date(location.getTime()));
        }
        return getCurrentTimeStamp();
    }

    /**
     * Gets GPS-based date stamp if location has time information
     */
<<<<<<< HEAD
    public static String getGPSDateStamp(Location location) {
=======
    private static String getGPSDateStamp(Location location) {
>>>>>>> parent of 902dc8f (Satellite date and time)
        if (location != null && location.getTime() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.getDefault());
            return dateFormat.format(new Date(location.getTime()));
        }
        return getCurrentDateStamp();
    }

    /**
<<<<<<< HEAD
     * Validates if a photo file has valid GPS metadata
     */
    public static boolean hasValidGPSMetadata(String photoPath) {
        try {
            LocationMetadata metadata = readLocationMetadata(photoPath);
            return metadata.hasLocation &&
                    metadata.latitude != 0 &&
                    metadata.longitude != 0;
        } catch (Exception e) {
            Log.e(TAG, "Error validating GPS metadata", e);
            return false;
        }
    }

    /**
     * Removes GPS metadata from a photo file
     */
    public static boolean removeGPSMetadata(String photoPath) {
        try {
            ExifInterface exif = new ExifInterface(photoPath);

            // Remove GPS-related EXIF tags
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, null);
            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null);
            exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null);
            exif.setAttribute(ExifInterface.TAG_GPS_DOP, null);

            // Save changes
            exif.saveAttributes();

            Log.d(TAG, "GPS metadata removed successfully from: " + photoPath);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error removing GPS metadata", e);
            return false;
        }
    }

    /**
=======
>>>>>>> parent of 902dc8f (Satellite date and time)
=======
        return dateFormat.format(new Date());
    }

    /**
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
>>>>>>> parent of 65c0cc0 (fix)
     * Data class for location metadata
     */
    public static class LocationMetadata {
        public double latitude = 0;
        public double longitude = 0;
        public boolean hasLocation = false;
        public String description = null;
        public String dateTime = null;
    }
}
