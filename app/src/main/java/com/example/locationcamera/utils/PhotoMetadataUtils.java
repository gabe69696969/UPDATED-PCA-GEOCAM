package com.example.locationcamera.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
<<<<<<< HEAD
=======
import java.io.File;
>>>>>>> parent of 902dc8f (Satellite date and time)
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
                                                        long timestamp, Location location) {
        try {
            // First, add EXIF location data to the photo file
            boolean exifSuccess = addLocationToExif(photoPath, latitude, longitude, location);

            // Create description with coordinates and timestamp
            String description = createLocationDescription(latitude, longitude, address, timestamp, location);

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
     * Overloaded method for backward compatibility
     */
    public static boolean savePhotoWithLocationMetadata(String photoPath, double latitude,
                                                        double longitude, String address,
                                                        long timestamp) {
        return savePhotoWithLocationMetadata(photoPath, latitude, longitude, address, timestamp, null);
    }

    /**
     * Adds GPS coordinates to EXIF data of the photo
     */
    private static boolean addLocationToExif(String photoPath, double latitude, double longitude, Location location) {
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

            // Use GPS time if available from location, otherwise current time
            if (location != null) {
                exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, getGPSTimeStamp(location));
                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, getGPSDateStamp(location));

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

                // Add altitude if available
                if (location.hasAltitude()) {
                    double altitude = location.getAltitude();
                    String altitudeRef = altitude >= 0 ? "0" : "1"; // 0 = above sea level, 1 = below
                    String altitudeDMS = String.format(Locale.US, "%.0f/1", Math.abs(altitude));
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, altitudeDMS);
                    exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, altitudeRef);
                }
            } else {
                exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, getCurrentTimeStamp());
                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, getCurrentDateStamp());
            }

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
                                                    String address, long timestamp, Location location) {
        StringBuilder description = new StringBuilder();

        // Add timestamp - use GPS time if available
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (location != null && location.getTime() > 0) {
            description.append("GPS Time: ").append(dateFormat.format(new Date(location.getTime()))).append("\n");
            description.append("Captured: ").append(dateFormat.format(new Date(timestamp))).append("\n");
        } else {
            description.append("Captured: ").append(dateFormat.format(new Date(timestamp))).append("\n");
        }

        // Add coordinates
        description.append("Coordinates: ").append(String.format(Locale.US, "%.6f, %.6f", latitude, longitude)).append("\n");

        // Add GPS provider and accuracy information
        if (location != null) {
            String provider = location.getProvider();
            if (provider != null) {
                description.append("Source: ").append(provider.toUpperCase()).append("\n");
            }

            if (location.hasAccuracy()) {
                description.append("Accuracy: ±").append(String.format(Locale.US, "%.1f", location.getAccuracy())).append("m\n");
            }

            if (location.hasAltitude()) {
                description.append("Altitude: ").append(String.format(Locale.US, "%.1f", location.getAltitude())).append("m\n");
            }

            if (location.hasSpeed()) {
                description.append("Speed: ").append(String.format(Locale.US, "%.1f", location.getSpeed())).append(" m/s\n");
            }

            if (location.hasBearing()) {
                description.append("Bearing: ").append(String.format(Locale.US, "%.1f", location.getBearing())).append("°\n");
            }
        }

        // Add address if available
        if (address != null && !address.isEmpty() && !address.equals("Location unavailable")) {
            description.append("Location: ").append(address).append("\n");
        }

        // Add final note
<<<<<<< HEAD
        description.append("GPS Location Data Embedded by LocationCamera");
=======
        description.append("Satellite GPS Location Data Embedded");
>>>>>>> parent of 902dc8f (Satellite date and time)

        return description.toString();
    }

    /**
     * Overloaded method for backward compatibility
     */
    private static String createLocationDescription(double latitude, double longitude,
                                                    String address, long timestamp) {
        return createLocationDescription(latitude, longitude, address, timestamp, null);
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

            // Set software tag to identify our app
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "LocationCamera Android App");

            // Save EXIF data
            exif.saveAttributes();

            Log.d(TAG, "Photo description saved successfully");
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

            // Basic file information
            String fileName = "LocationCamera_" + timestamp + ".jpg";
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, timestamp / 1000);
            contentValues.put(MediaStore.MediaColumns.DATE_MODIFIED, timestamp / 1000);

            // Add location data to MediaStore
            if (latitude != 0 && longitude != 0) {
                contentValues.put(MediaStore.Images.Media.LATITUDE, latitude);
                contentValues.put(MediaStore.Images.Media.LONGITUDE, longitude);
            }

            // Create description with location information
            String description = createLocationDescription(latitude, longitude, address, timestamp);

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

            // Read GPS timestamp
            String gpsTimestamp = exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
            String gpsDatestamp = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);

            // Read altitude
            String altitudeString = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
            String altitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);

            LocationMetadata metadata = new LocationMetadata();
            if (hasGPS) {
                metadata.latitude = latLong[0];
                metadata.longitude = latLong[1];
                metadata.hasLocation = true;

                // Parse altitude if available
                if (altitudeString != null) {
                    try {
                        // Altitude is stored as a rational number (e.g., "123/1")
                        String[] parts = altitudeString.split("/");
                        if (parts.length == 2) {
                            double altitude = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                            // Check if below sea level
                            if ("1".equals(altitudeRef)) {
                                altitude = -altitude;
                            }
                            metadata.altitude = altitude;
                            metadata.hasAltitude = true;
                        }
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Error parsing altitude from EXIF", e);
                    }
                }
            }

            metadata.description = description;
            metadata.dateTime = dateTime;
            metadata.gpsTimestamp = gpsTimestamp;
            metadata.gpsDatestamp = gpsDatestamp;

            Log.d(TAG, "Location metadata read - GPS: " + hasGPS +
                    ", Description: " + (description != null) +
                    ", Altitude: " + metadata.hasAltitude);
            return metadata;

        } catch (IOException e) {
            Log.e(TAG, "Error reading location metadata", e);
            return new LocationMetadata();
        }
    }

    /**
     * Gets current timestamp for EXIF
     */
    private static String getCurrentTimeStamp() {
<<<<<<< HEAD
=======
        // Use GPS time if available, otherwise system time
>>>>>>> parent of 902dc8f (Satellite date and time)
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(new Date(System.currentTimeMillis()));
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
     * Data class for location metadata
     */
    public static class LocationMetadata {
        public double latitude = 0;
        public double longitude = 0;
        public double altitude = 0;
        public boolean hasLocation = false;
        public boolean hasAltitude = false;
        public String description = null;
        public String dateTime = null;
        public String gpsTimestamp = null;
        public String gpsDatestamp = null;

        @Override
        public String toString() {
            return "LocationMetadata{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    ", altitude=" + altitude +
                    ", hasLocation=" + hasLocation +
                    ", hasAltitude=" + hasAltitude +
                    ", description='" + description + '\'' +
                    ", dateTime='" + dateTime + '\'' +
                    '}';
        }
    }
}