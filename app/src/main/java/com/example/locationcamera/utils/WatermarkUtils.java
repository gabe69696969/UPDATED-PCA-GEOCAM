package com.example.locationcamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.example.locationcamera.R;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WatermarkUtils {

    private static final String TAG = "WatermarkUtils";
    private static final int WATERMARK_PADDING = 20;
    private static final int LINE_SPACING = 8;
    private static final int ICON_TEXT_SPACING = 12; // Space between icon and text
    private static final float TEXT_SIZE_RATIO = 0.025f; // 2.5% of image width
    private static final int MIN_TEXT_SIZE = 24;
    private static final int MAX_TEXT_SIZE = 48;
    private static final int ICON_SIZE_RATIO = 32; // Base icon size

    // Overloaded method for backward compatibility
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
                                                 String address, long timestamp, Double altitude) {
        if (originalBitmap == null) {
            Log.e(TAG, "Original bitmap is null");
            return null;
        }

        try {
            // Create a mutable copy of the bitmap
            Bitmap watermarkedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(watermarkedBitmap);

            // Calculate text size based on image dimensions
            int textSize = calculateTextSize(watermarkedBitmap.getWidth());
            int iconSize = calculateIconSize(textSize);

            // Create paint for text
            Paint textPaint = createTextPaint(textSize);
            Paint shadowPaint = createShadowPaint(textSize);

<<<<<<< HEAD
=======
            // Prepare watermark text lines
>>>>>>> 8b82bdf2383a466e94e2594b2d321e0b14d6a7d1
            String[] watermarkLines = prepareWatermarkText(latitude, longitude, address, timestamp, altitude);

            // Calculate text dimensions
            Rect textBounds = new Rect();
            int maxTextWidth = 0;
            int totalTextHeight = 0;

            for (String line : watermarkLines) {
                textPaint.getTextBounds(line, 0, line.length(), textBounds);
                maxTextWidth = Math.max(maxTextWidth, textBounds.width());
                totalTextHeight += textBounds.height() + LINE_SPACING;
            }

            // Calculate total watermark dimensions including icon
            int totalWatermarkWidth = iconSize + ICON_TEXT_SPACING + maxTextWidth;
            int totalWatermarkHeight = Math.max(iconSize, totalTextHeight);

            // Calculate watermark position (TOP-LEFT corner)
            int watermarkX = WATERMARK_PADDING;
            int watermarkY = WATERMARK_PADDING;

            // Draw semi-transparent background
            drawWatermarkBackground(canvas, watermarkX - 10, watermarkY - 10,
                    totalWatermarkWidth + 20, totalWatermarkHeight + 20);

            // Draw camera icon if context is available
            if (context != null) {
                drawWatermarkIcon(context, canvas, watermarkX, watermarkY, iconSize);
            }

            // Calculate text starting position (next to icon)
            int textStartX = watermarkX + (context != null ? iconSize + ICON_TEXT_SPACING : 0);
            int currentY = watermarkY;

            // Draw each line of text starting from top
            for (String line : watermarkLines) {
                textPaint.getTextBounds(line, 0, line.length(), textBounds);
                currentY += textBounds.height();

                // Draw shadow first
                canvas.drawText(line, textStartX + 2, currentY + 2, shadowPaint);
                // Draw main text
                canvas.drawText(line, textStartX, currentY, textPaint);

                currentY += LINE_SPACING;
            }

            Log.d(TAG, "Watermark with icon added successfully at top-left position");
            return watermarkedBitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error adding watermark", e);
            return originalBitmap; // Return original if watermarking fails
        }
    }

    private static void drawWatermarkIcon(Context context, Canvas canvas, int x, int y, int iconSize) {
        try {
            // Load the camera icon from drawable resources
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.latestlogo);

            if (drawable != null) {
                // Set the bounds for the drawable
                drawable.setBounds(x, y, x + iconSize, y + iconSize);

                // Draw the icon on the canvas
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
        // Icon size should be proportional to text size
        return Math.max(ICON_SIZE_RATIO, (int) (textSize * 1.2f));
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
        paint.setAlpha(128); // Semi-transparent shadow
        return paint;
    }

    private static String[] prepareWatermarkText(double latitude, double longitude,
                                                 String address, long timestamp, Double altitude) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTime = dateFormat.format(new Date(timestamp));

        // Handle case where no location is available
        if (latitude == 0 && longitude == 0) {
            return new String[]{
                    "Philippine Coconut Authority",
                    dateTime,
                    "Location unavailable"
            };
        }

        String coordinates = String.format(Locale.US, "%.6f, %.6f", latitude, longitude);

        // Add altitude if available
        String coordinatesWithAltitude = coordinates;
        if (altitude != null && altitude != 0) {
            coordinatesWithAltitude = coordinates + String.format(Locale.US, " (%.0fm)", altitude);
        }

        if (address != null && !address.isEmpty() && !address.equals("Location unavailable")) {
            return new String[]{
                    dateTime,
                    coordinatesWithAltitude,
                    address
            };
        } else {
            return new String[]{
                    dateTime,
                    coordinatesWithAltitude
            };
        }
    }


    private static void drawWatermarkBackground(Canvas canvas, int x, int y, int width, int height) {
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setAlpha(128); // Semi-transparent background
        backgroundPaint.setAntiAlias(true);

        // Draw rounded rectangle background
        canvas.drawRoundRect(x, y, x + width, y + height, 8, 8, backgroundPaint);
    }

    public static Bitmap addCoordinatesWatermark(Bitmap originalBitmap, double latitude, double longitude) {
        return addWatermarkWithContext(null, originalBitmap, latitude, longitude, null, System.currentTimeMillis());
    }

<<<<<<< HEAD
=======
    // Overloaded method for backward compatibility
>>>>>>> 8b82bdf2383a466e94e2594b2d321e0b14d6a7d1
    public static Bitmap addSimpleCoordinatesWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                                  double latitude, double longitude) {
        return addSimpleCoordinatesWatermarkWithContext(context, originalBitmap, latitude, longitude, null);
    }

    public static Bitmap addSimpleCoordinatesWatermark(Bitmap originalBitmap, double latitude, double longitude) {
        return addSimpleCoordinatesWatermarkWithContext(null, originalBitmap, latitude, longitude, null);
    }

    public static Bitmap addSimpleCoordinatesWatermarkWithContext(Context context, Bitmap originalBitmap,
                                                                  double latitude, double longitude, Double altitude) {
        if (originalBitmap == null) {
            Log.e(TAG, "Original bitmap is null");
            return null;
        }

        try {
            // Create a mutable copy of the bitmap
            Bitmap watermarkedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(watermarkedBitmap);

            // Calculate text size based on image dimensions
            int textSize = calculateTextSize(watermarkedBitmap.getWidth());
            int iconSize = calculateIconSize(textSize);

            // Create paint for text
            Paint textPaint = createTextPaint(textSize);
            Paint shadowPaint = createShadowPaint(textSize);

            // Format coordinates
            String coordinates = String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
            if (altitude != null && altitude != 0) {
                coordinates += String.format(Locale.US, " (%.0fm)", altitude);
            }

            // Calculate text dimensions
            Rect textBounds = new Rect();
            textPaint.getTextBounds(coordinates, 0, coordinates.length(), textBounds);

            // Calculate total watermark dimensions
            int totalWidth = (context != null ? iconSize + ICON_TEXT_SPACING : 0) + textBounds.width();
            int totalHeight = Math.max(iconSize, textBounds.height());

            // Calculate watermark position (TOP-LEFT corner)
            int watermarkX = WATERMARK_PADDING;
            int watermarkY = WATERMARK_PADDING + textBounds.height();

            // Draw semi-transparent background
            drawWatermarkBackground(canvas, watermarkX - 10, watermarkY - textBounds.height() - 10,
                    totalWidth + 20, totalHeight + 20);

            // Draw camera icon if context is available
            if (context != null) {
                drawWatermarkIcon(context, canvas, watermarkX, watermarkY - textBounds.height(), iconSize);
            }

            // Calculate text position
            int textX = watermarkX + (context != null ? iconSize + ICON_TEXT_SPACING : 0);

            // Draw shadow first
            canvas.drawText(coordinates, textX + 2, watermarkY + 2, shadowPaint);
            // Draw main text
            canvas.drawText(coordinates, textX, watermarkY, textPaint);

            Log.d(TAG, "Simple coordinates watermark with icon added successfully at top-left position");
            return watermarkedBitmap;

        } catch (Exception e) {
            Log.e(TAG, "Error adding simple coordinates watermark", e);
            return originalBitmap; // Return original if watermarking fails
        }
    }

    /**
     * Saves a bitmap to a file
     */
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