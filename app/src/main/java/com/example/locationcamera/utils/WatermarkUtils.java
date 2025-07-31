package com.example.locationcamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;
<<<<<<< HEAD
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

=======
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
import com.example.locationcamera.R;
import com.example.locationcamera.utils.GPSTimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WatermarkUtils {

    private static final String TAG = "WatermarkUtils";
    private static final int WATERMARK_PADDING = 20;
    private static final int LINE_SPACING = 8;
    private static final int ICON_TEXT_SPACING = 12;
<<<<<<< HEAD
    private static final float TEXT_SIZE_RATIO = 0.025f;
    private static final int MIN_TEXT_SIZE = 24;
    private static final int MAX_TEXT_SIZE = 48;
    private static final int ICON_SIZE_RATIO = 48; // Updated from 32 to 36
=======
    private static final float TEXT_SIZE_RATIO = 0.045f; // Increased by 50%
    private static final int MIN_TEXT_SIZE = 75; // Increased by 50%
    private static final int MAX_TEXT_SIZE = 75; // Increased by 50%
    private static final int ICON_SIZE_RATIO = 48; // Increased by 50%
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d

    public static Bitmap addWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                 double latitude, double longitude,
                                                 String address, long timestamp) {
        return addWatermarkWithContext(context, originalBitmap, latitude, longitude, address, timestamp, null);
    }

    public static Bitmap addWatermark(Bitmap originalBitmap, double latitude, double longitude,
                                      String address, long timestamp) {
        return addWatermarkWithContext(null, originalBitmap, latitude, longitude, address, timestamp, null);
    }

    public static Bitmap addWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                 double latitude, double longitude,
                                                 String address, long timestamp, Double altitude,
                                                 android.location.Location location) {
        if (originalBitmap == null) {
            Log.e(TAG, "Original bitmap is null");
            return null;
        }

        try {
            Bitmap watermarkedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(watermarkedBitmap);

            int textSize = calculateTextSize(watermarkedBitmap.getWidth());
            int iconSize = calculateIconSize(textSize);

            Paint textPaint = createTextPaint(textSize);
            Paint shadowPaint = createShadowPaint(textSize);

<<<<<<< HEAD
            String[] watermarkLines = prepareWatermarkText(latitude, longitude, address, timestamp, altitude, location);
=======
            String[] watermarkLines = prepareWatermarkText(latitude, longitude, address, timestamp, altitude);
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d

            Rect textBounds = new Rect();
            int maxTextWidth = 0;
            int totalTextHeight = 0;

            for (String line : watermarkLines) {
                textPaint.getTextBounds(line, 0, line.length(), textBounds);
                maxTextWidth = Math.max(maxTextWidth, textBounds.width());
                totalTextHeight += textBounds.height() + LINE_SPACING;
            }

            int totalWatermarkWidth = iconSize + ICON_TEXT_SPACING + maxTextWidth;
            int totalWatermarkHeight = Math.max(iconSize, totalTextHeight);

            int watermarkX = WATERMARK_PADDING;
            int watermarkY = WATERMARK_PADDING;

            drawWatermarkBackground(canvas, watermarkX - 10, watermarkY - 10,
                    totalWatermarkWidth + 20, totalWatermarkHeight + 20);

            if (context != null) {
                drawWatermarkIcon(context, canvas, watermarkX, watermarkY, iconSize);
            }

            int textStartX = watermarkX + (context != null ? iconSize + ICON_TEXT_SPACING : 0);
            int currentY = watermarkY;

            for (String line : watermarkLines) {
                textPaint.getTextBounds(line, 0, line.length(), textBounds);
                currentY += textBounds.height();
<<<<<<< HEAD

=======
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
                canvas.drawText(line, textStartX + 2, currentY + 2, shadowPaint);
                canvas.drawText(line, textStartX, currentY, textPaint);
                currentY += LINE_SPACING;
            }

            Log.d(TAG, "Watermark with icon added successfully at top-left position");
            return watermarkedBitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error adding watermark", e);
            return originalBitmap;
        }
    }

    public static Bitmap addWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                 double latitude, double longitude,
                                                 String address, long timestamp, Double altitude) {
        return addWatermarkWithContext(context, originalBitmap, latitude, longitude, address, timestamp, altitude, null);
    }

    private static void drawWatermarkIcon(Context context, Canvas canvas, int x, int y, int iconSize) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.latestlogo);

            if (drawable != null) {
                drawable.setBounds(x, y, x + iconSize, y + iconSize);
                drawable.draw(canvas);
                Log.d(TAG, "Camera icon drawn successfully");
            } else {
                Log.w(TAG, "Camera icon drawable not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error drawing watermark icon", e);
        }
    }

    private static int calculateTextSize(int imageWidth) {
        int textSize = (int) (imageWidth * TEXT_SIZE_RATIO);
        return Math.max(MIN_TEXT_SIZE, Math.min(MAX_TEXT_SIZE, textSize));
    }

    private static int calculateIconSize(int textSize) {
<<<<<<< HEAD
        return Math.max(ICON_SIZE_RATIO, (int) (textSize * 1.2f));
=======
        return Math.max(ICON_SIZE_RATIO, (int) (textSize * 2f));
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
    }

    private static Paint createTextPaint(int textSize) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        return paint;
    }

    private static Paint createShadowPaint(int textSize) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setAlpha(128);
        return paint;
    }

    private static String[] prepareWatermarkText(double latitude, double longitude,
                                                 String address, long timestamp, Double altitude,
                                                 android.location.Location location) {
        long accurateTimestamp = location != null ? GPSTimeUtils.getAccurateTimestamp(location) : timestamp;
        String timeSource = location != null ? GPSTimeUtils.getTimeSourceDescription(location) : "System Time";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTime = dateFormat.format(new Date(accurateTimestamp));

        if (location != null && GPSTimeUtils.hasValidGPSTime(location)) {
            dateTime += " (GPS)";
        }

        if (latitude == 0 && longitude == 0) {
            return new String[]{
                    dateTime,
                    "Location unavailable"
            };
        }

        String coordinates = String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
        String altitudeText = (altitude != null) ? String.format(Locale.US, "Alt: %.1f m", altitude) : null;

        if (address != null && !address.isEmpty() && !address.equals("Location unavailable")) {
            return new String[]{
                    dateTime,
                    coordinates,
                    altitudeText,
                    address
            };
        } else {
            return new String[]{
                    dateTime,
                    coordinates,
                    altitudeText
            };
        }
    }

    private static String[] prepareWatermarkText(double latitude, double longitude,
                                                 String address, long timestamp, Double altitude) {
        return prepareWatermarkText(latitude, longitude, address, timestamp, altitude, null);
    }

    private static void drawWatermarkBackground(Canvas canvas, int x, int y, int width, int height) {
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(128);
        backgroundPaint.setAntiAlias(true);
        canvas.drawRoundRect(x, y, x + width, y + height, 8, 8, backgroundPaint);
    }

    public static Bitmap addCoordinatesWatermark(Bitmap originalBitmap, double latitude, double longitude) {
        return addWatermarkWithContext(null, originalBitmap, latitude, longitude, null, System.currentTimeMillis(), null, null);
    }

    public static Bitmap addSimpleCoordinatesWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                                  double latitude, double longitude) {
        return addSimpleCoordinatesWatermarkWithContext(context, originalBitmap, latitude, longitude, null);
    }

    public static Bitmap addSimpleCoordinatesWatermark(Bitmap originalBitmap, double latitude, double longitude) {
        return addSimpleCoordinatesWatermarkWithContext(null, originalBitmap, latitude, longitude, null, null);
    }

    public static Bitmap addSimpleCoordinatesWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                                  double latitude, double longitude, Double altitude,
                                                                  android.location.Location location) {
        if (originalBitmap == null) {
            Log.e(TAG, "Original bitmap is null");
            return null;
        }

        try {
            Bitmap watermarkedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(watermarkedBitmap);

            int textSize = calculateTextSize(watermarkedBitmap.getWidth());
            int iconSize = calculateIconSize(textSize);

            Paint textPaint = createTextPaint(textSize);
            Paint shadowPaint = createShadowPaint(textSize);

            String coordinates = String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
            if (altitude != null && altitude != 0) {
                coordinates += String.format(Locale.US, " (%.0fm)", altitude);
            }

            if (location != null && GPSTimeUtils.hasValidGPSTime(location)) {
                coordinates += " GPS";
            }

            Rect textBounds = new Rect();
            textPaint.getTextBounds(coordinates, 0, coordinates.length(), textBounds);

            int totalWidth = (context != null ? iconSize + ICON_TEXT_SPACING : 0) + textBounds.width();
            int totalHeight = Math.max(iconSize, textBounds.height());

            int watermarkX = WATERMARK_PADDING;
            int watermarkY = WATERMARK_PADDING + textBounds.height();

            drawWatermarkBackground(canvas, watermarkX - 10, watermarkY - textBounds.height() - 10,
                    totalWidth + 20, totalHeight + 20);

            if (context != null) {
                drawWatermarkIcon(context, canvas, watermarkX, watermarkY - textBounds.height(), iconSize);
            }

            int textX = watermarkX + (context != null ? iconSize + ICON_TEXT_SPACING : 0);

            canvas.drawText(coordinates, textX + 2, watermarkY + 2, shadowPaint);
            canvas.drawText(coordinates, textX, watermarkY, textPaint);

            Log.d(TAG, "Simple coordinates watermark with icon added successfully at top-left position");
            return watermarkedBitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error adding simple coordinates watermark", e);
            return originalBitmap;
        }
    }

<<<<<<< HEAD
    public static Bitmap addSimpleCoordinatesWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                                  double latitude, double longitude, Double altitude) {
        return addSimpleCoordinatesWatermarkWithContext(context, originalBitmap, latitude, longitude, altitude, null);
    }

=======
>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
    public static boolean saveBitmapToFile(Bitmap bitmap, String filePath) {
        if (bitmap == null || filePath == null) {
            Log.e(TAG, "Bitmap or file path is null");
            return false;
        }

        FileOutputStream out = null;
        try {
            File file = new File(filePath);
            out = new FileOutputStream(file);
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            if (!compressed) {
                Log.e(TAG, "Failed to compress bitmap");
                return false;
            }
            Log.d(TAG, "Bitmap saved successfully to: " + filePath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving bitmap to file", e);
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file output stream", e);
                }
            }
        }
    }
}
<<<<<<< HEAD
=======

>>>>>>> bcc18ad8a9271bec8a217c3c0da9bdf1ef8c140d
