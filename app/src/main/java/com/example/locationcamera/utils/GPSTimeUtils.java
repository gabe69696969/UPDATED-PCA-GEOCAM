package com.example.locationcamera.utils;

import android.location.Location;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GPSTimeUtils {

    private static final String TAG = "GPSTimeUtils";

    /**
     * Gets the most accurate timestamp available, preferring GPS time over system time
     */
    public static long getAccurateTimestamp(Location location) {
        if (location != null && location.getTime() > 0) {
            // GPS time is available and valid
            long gpsTime = location.getTime();
            long systemTime = System.currentTimeMillis();

            // Check if GPS time is reasonable (not too far from system time)
            long timeDifference = Math.abs(gpsTime - systemTime);

            // If GPS time is within 24 hours of system time, use it
            if (timeDifference < 24 * 60 * 60 * 1000) {
                Log.d(TAG, "Using GPS satellite time: " + formatTimestamp(gpsTime));
                return gpsTime;
            } else {
                Log.w(TAG, "GPS time seems invalid (difference: " + timeDifference + "ms), using system time");
                return systemTime;
            }
        } else {
            // No GPS time available, use system time
            long systemTime = System.currentTimeMillis();
            Log.d(TAG, "GPS time not available, using system time: " + formatTimestamp(systemTime));
            return systemTime;
        }
    }

    /**
     * Gets GPS time if available, otherwise returns system time
     */
    public static Date getAccurateDate(Location location) {
        return new Date(getAccurateTimestamp(location));
    }

    /**
     * Formats timestamp for display
     */
    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Formats timestamp with timezone information
     */
    public static String formatTimestampWithTimezone(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Gets GPS time formatted for EXIF timestamp
     */
    public static String getGPSTimeForExif(Location location) {
        long timestamp = getAccurateTimestamp(location);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }

    /**
     * Gets GPS date formatted for EXIF datestamp
     */
    public static String getGPSDateForExif(Location location) {
        long timestamp = getAccurateTimestamp(location);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * Gets GPS time in UTC for EXIF (satellite time is in UTC)
     */
    public static String getGPSTimeUTCForExif(Location location) {
        if (location != null && location.getTime() > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return timeFormat.format(new Date(location.getTime()));
        } else {
            // Fallback to system time in UTC
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return timeFormat.format(new Date(System.currentTimeMillis()));
        }
    }

    /**
     * Gets GPS date in UTC for EXIF (satellite time is in UTC)
     */
    public static String getGPSDateUTCForExif(Location location) {
        if (location != null && location.getTime() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(new Date(location.getTime()));
        } else {
            // Fallback to system time in UTC
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(new Date(System.currentTimeMillis()));
        }
    }

    /**
     * Checks if GPS time is available and valid
     */
    public static boolean hasValidGPSTime(Location location) {
        if (location == null || location.getTime() <= 0) {
            return false;
        }

        // Check if GPS time is reasonable (within 24 hours of system time)
        long gpsTime = location.getTime();
        long systemTime = System.currentTimeMillis();
        long timeDifference = Math.abs(gpsTime - systemTime);

        return timeDifference < 24 * 60 * 60 * 1000; // 24 hours
    }

    /**
     * Gets time source description for display
     */
    public static String getTimeSourceDescription(Location location) {
        if (hasValidGPSTime(location)) {
            return "GPS Satellite Time";
        } else {
            return "System Time";
        }
    }

    /**
     * Gets detailed time information for debugging
     */
    public static String getDetailedTimeInfo(Location location) {
        StringBuilder info = new StringBuilder();

        if (location != null && location.getTime() > 0) {
            info.append("GPS Time: ").append(formatTimestampWithTimezone(location.getTime())).append("\n");
            info.append("GPS Time UTC: ").append(formatTimestampUTC(location.getTime())).append("\n");
        }

        info.append("System Time: ").append(formatTimestampWithTimezone(System.currentTimeMillis())).append("\n");
        info.append("Using: ").append(getTimeSourceDescription(location));

        return info.toString();
    }

    /**
     * Formats timestamp in UTC
     */
    private static String formatTimestampUTC(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestamp));
    }
}