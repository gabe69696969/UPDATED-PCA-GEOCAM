package com.example.locationcamera.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import java.io.IOException;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Rotates a bitmap based on EXIF orientation data and forces landscape orientation,
     * then turns the final image upside down (180 degrees)
     */
    public static Bitmap rotateBitmapIfNeeded(String imagePath) {
        try {
            // Load the bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from path: " + imagePath);
                return null;
            }

            // Get EXIF orientation
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            // Rotate based on EXIF orientation
            int rotationAngle = getRotationAngle(orientation);
            Bitmap rotatedBitmap = rotationAngle != 0 ? rotateBitmap(bitmap, rotationAngle) : bitmap;

            // Force landscape orientation (if still portrait)
            if (rotatedBitmap.getHeight() > rotatedBitmap.getWidth()) {
                Log.d(TAG, "Forcing landscape orientation by rotating 90 degrees");
                Bitmap landscapeBitmap = rotateBitmap(rotatedBitmap, 90);
                if (landscapeBitmap != rotatedBitmap) {
                    rotatedBitmap.recycle();
                }
                rotatedBitmap = landscapeBitmap;
            }

            // Finally rotate 180 degrees to turn the image upside down
            Bitmap upsideDownBitmap = rotateBitmap(rotatedBitmap, 180);
            if (upsideDownBitmap != rotatedBitmap) {
                rotatedBitmap.recycle();
            }

            return upsideDownBitmap;

        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF data", e);
            return BitmapFactory.decodeFile(imagePath);
        } catch (Exception e) {
            Log.e(TAG, "Error rotating bitmap", e);
            return BitmapFactory.decodeFile(imagePath);
        }
    }

    /**
     * Rotates a bitmap by the specified angle
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationAngle) {
        if (bitmap == null || rotationAngle == 0) {
            return bitmap;
        }

        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);

            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true
            );

            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }

            return rotatedBitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error creating rotated bitmap", e);
            return bitmap;
        }
    }

    public static boolean addGpsMetadataToPhoto(String imagePath, double latitude, double longitude,
                                                double altitude, String address) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);

            setGpsCoordinates(exif, latitude, longitude);

            if (altitude != 0) {
                exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, convertToRational(Math.abs(altitude)));
                exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, altitude >= 0 ? "0" : "1");
            }

            String gpsTimestamp = new java.text.SimpleDateFormat("HH:mm:ss",
                    java.util.Locale.getDefault()).format(new java.util.Date());
            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, gpsTimestamp);

            String gpsDateStamp = new java.text.SimpleDateFormat("yyyy:MM:dd",
                    java.util.Locale.getDefault()).format(new java.util.Date());
            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, gpsDateStamp);

            String description = formatLocationDescription(latitude, longitude, address);
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description);
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, description);

            String timestamp = new java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss",
                    java.util.Locale.getDefault()).format(new java.util.Date());
            exif.setAttribute(ExifInterface.TAG_DATETIME, timestamp);
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, timestamp);
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, timestamp);

            exif.setAttribute(ExifInterface.TAG_MAKE, "LocationCamera");
            exif.setAttribute(ExifInterface.TAG_MODEL, "Android Location Camera App");

            exif.saveAttributes();

            Log.d(TAG, "GPS metadata added successfully to: " + imagePath);
            Log.d(TAG, "Coordinates: " + latitude + ", " + longitude);
            Log.d(TAG, "Description: " + description);

            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error adding GPS metadata to photo", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error adding GPS metadata", e);
            return false;
        }
    }

    private static void setGpsCoordinates(ExifInterface exif, double latitude, double longitude) {
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToDMS(Math.abs(latitude)));
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude >= 0 ? "N" : "S");

        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToDMS(Math.abs(longitude)));
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude >= 0 ? "E" : "W");

        Log.d(TAG, "GPS coordinates set: " + latitude + ", " + longitude);
    }

    private static String convertToDMS(double coordinate) {
        int degrees = (int) coordinate;
        double minutesFloat = (coordinate - degrees) * 60;
        int minutes = (int) minutesFloat;
        double seconds = (minutesFloat - minutes) * 60;

        return degrees + "/1," + minutes + "/1," + Math.round(seconds * 1000) + "/1000";
    }

    private static String formatLocationDescription(double latitude, double longitude, String address) {
        StringBuilder description = new StringBuilder();

        description.append(String.format(java.util.Locale.US, "GPS: %.6f, %.6f", latitude, longitude));

        if (address != null && !address.isEmpty() && !address.equals("Location unavailable")) {
            description.append(" | ").append(address);
        }

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date());
        description.append(" | Captured: ").append(timestamp);

        return description.toString();
    }

    private static String convertToRational(double value) {
        long numerator = Math.round(value * 1000000);
        long denominator = 1000000;

        long gcd = gcd(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;

        return numerator + "/" + denominator;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private static int getRotationAngle(int orientation) {
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
            case ExifInterface.ORIENTATION_TRANSVERSE:
                return 180;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return 0;
        }
    }

    public static boolean fixImageOrientation(String imagePath) {
        try {
            Bitmap correctedBitmap = rotateBitmapIfNeeded(imagePath);
            if (correctedBitmap == null) {
                return false;
            }

            return WatermarkUtils.saveBitmapToFile(correctedBitmap, imagePath);

        } catch (Exception e) {
            Log.e(TAG, "Error fixing image orientation", e);
            return false;
        }
    }

    public static int getCameraRotation(android.content.Context context, int cameraId, boolean isFrontCamera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        android.view.WindowManager windowManager =
                (android.view.WindowManager) context.getSystemService(android.content.Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case android.view.Surface.ROTATION_0:
                degrees = 0;
                break;
            case android.view.Surface.ROTATION_90:
                degrees = 90;
                break;
            case android.view.Surface.ROTATION_180:
                degrees = 180;
                break;
            case android.view.Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (isFrontCamera) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }
}
