package com.example.locationcamera;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.locationcamera.R;
import com.example.locationcamera.database.AppDatabase;
import com.example.locationcamera.databinding.ActivityPhotoDetailBinding;
import com.example.locationcamera.model.PhotoLocation;
import com.example.locationcamera.utils.PhotoMetadataUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoDetailActivity extends AppCompatActivity {

    private static final String TAG = "PhotoDetailActivity";
    private ActivityPhotoDetailBinding binding;
    private AppDatabase database;
    private PhotoLocation currentPhoto;
    private boolean isSavingToGallery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityPhotoDetailBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Photo Details");
            }

            database = AppDatabase.getDatabase(this);

            int photoId = getIntent().getIntExtra("photo_id", -1);
            if (photoId != -1) {
                loadPhotoDetails(photoId);
            } else {
                Toast.makeText(this, "Invalid photo ID", Toast.LENGTH_SHORT).show();
                finish();
            }

            // Set up save to gallery button click listener
            binding.saveToGalleryButton.setOnClickListener(v -> savePhotoToGallery());

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading photo details", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadPhotoDetails(int photoId) {
        new Thread(() -> {
            try {
                currentPhoto = database.photoLocationDao().getPhotoById(photoId);

                runOnUiThread(() -> {
                    if (currentPhoto != null) {
                        displayPhotoDetails();
                    } else {
                        Toast.makeText(this, "Photo not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading photo details", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void displayPhotoDetails() {
        try {
            // Load photo with error handling
            File photoFile = new File(currentPhoto.getPhotoPath());
            if (photoFile.exists()) {
                Glide.with(this)
                        .load(photoFile)
                        .error(R.drawable.ic_image_error)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(binding.photoImageView);
            } else {
                binding.photoImageView.setImageResource(R.drawable.ic_image_error);
            }

            // Display timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            String dateText = sdf.format(new Date(currentPhoto.getTimestamp()));
            binding.dateText.setText(dateText);

            // Display location information
            if (currentPhoto.getLatitude() != 0 && currentPhoto.getLongitude() != 0) {
                binding.locationCard.setVisibility(android.view.View.VISIBLE);

                if (currentPhoto.getAddress() != null && !currentPhoto.getAddress().isEmpty()) {
                    binding.addressText.setText(currentPhoto.getAddress());
                    binding.addressText.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.addressText.setVisibility(android.view.View.GONE);
                }

                String coordinates = String.format(Locale.US, "%.6f, %.6f",
                        currentPhoto.getLatitude(), currentPhoto.getLongitude());
                binding.coordinatesText.setText(coordinates);

                if (currentPhoto.getAccuracy() > 0) {
                    String accuracy = String.format(Locale.US, "Accuracy: ±%.0fm", currentPhoto.getAccuracy());
                    binding.accuracyText.setText(accuracy);
                    binding.accuracyText.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.accuracyText.setVisibility(android.view.View.GONE);
                }

                if (currentPhoto.getAltitude() != 0) {
                    String altitude = String.format(Locale.US, "Altitude: %.0fm", currentPhoto.getAltitude());
                    binding.altitudeText.setText(altitude);
                    binding.altitudeText.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.altitudeText.setVisibility(android.view.View.GONE);
                }
            } else {
                binding.locationCard.setVisibility(android.view.View.GONE);
            }

            // Update save button state
            updateSaveButtonState();

        } catch (Exception e) {
            Log.e(TAG, "Error displaying photo details", e);
            Toast.makeText(this, "Error displaying photo details", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSaveButtonState() {
        if (isSavingToGallery) {
            binding.saveToGalleryButton.setEnabled(false);
            binding.saveToGalleryButton.setText("Saving...");
            binding.saveProgressBar.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.saveToGalleryButton.setEnabled(true);
            binding.saveToGalleryButton.setText("Save to Gallery");
            binding.saveProgressBar.setVisibility(android.view.View.GONE);
        }
    }

    private void savePhotoToGallery() {
        if (currentPhoto == null || isSavingToGallery) {
            return;
        }

        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    isSavingToGallery = true;
                    updateSaveButtonState();
                });

                File sourceFile = new File(currentPhoto.getPhotoPath());
                if (!sourceFile.exists()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Photo file not found", Toast.LENGTH_SHORT).show();
                        isSavingToGallery = false;
                        updateSaveButtonState();
                    });
                    return;
                }

                boolean success = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Use MediaStore API for Android 10+ with metadata
                    success = saveToGalleryModernWithMetadata(sourceFile);
                } else {
                    // Use legacy method for older Android versions
                    success = saveToGalleryLegacy(sourceFile);
                }

                final boolean finalSuccess = success;
                runOnUiThread(() -> {
                    if (finalSuccess) {
                        Toast.makeText(this, "Photo saved to gallery with location metadata!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to save photo to gallery", Toast.LENGTH_SHORT).show();
                    }
                    isSavingToGallery = false;
                    updateSaveButtonState();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error saving photo to gallery", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving photo to gallery", Toast.LENGTH_SHORT).show();
                    isSavingToGallery = false;
                    updateSaveButtonState();
                });
            }
        }).start();
    }

    private boolean saveToGalleryModernWithMetadata(File sourceFile) {
        try {
            // Use the PhotoMetadataUtils to save with complete metadata
            Uri savedUri = PhotoMetadataUtils.savePhotoToMediaStoreWithMetadata(
                    getContentResolver(),
                    sourceFile.getAbsolutePath(),
                    currentPhoto.getLatitude(),
                    currentPhoto.getLongitude(),
                    currentPhoto.getAddress(),
                    currentPhoto.getTimestamp()
            );

            if (savedUri != null) {
                Log.d(TAG, "Photo saved to gallery with metadata: " + savedUri);
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving to gallery with metadata", e);
        }

        return false;
    }

    private boolean saveToGalleryLegacy(File sourceFile) {
        try {
            // First, ensure the photo has metadata embedded
            if (currentPhoto.getLatitude() != 0 && currentPhoto.getLongitude() != 0) {
                PhotoMetadataUtils.savePhotoWithLocationMetadata(
                        sourceFile.getAbsolutePath(),
                        currentPhoto.getLatitude(),
                        currentPhoto.getLongitude(),
                        currentPhoto.getAddress(),
                        currentPhoto.getTimestamp()
                );
            }

            // For older Android versions, use MediaStore.Images.Media.insertImage
            String savedImagePath = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    sourceFile.getAbsolutePath(),
                    "LocationCamera_" + currentPhoto.getTimestamp(),
                    createLocationDescription()
            );

            if (savedImagePath != null) {
                // Notify the media scanner about the new file
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.parse(savedImagePath);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);

                Log.d(TAG, "Photo saved to gallery (legacy) with metadata: " + savedImagePath);
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving to gallery (legacy)", e);
        }

        return false;
    }

    private String createLocationDescription() {
        StringBuilder description = new StringBuilder();

        // Add timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        description.append("Captured: ").append(dateFormat.format(new Date(currentPhoto.getTimestamp()))).append("\n");

        // Add coordinates if available
        if (currentPhoto.getLatitude() != 0 && currentPhoto.getLongitude() != 0) {
            description.append("Coordinates: ").append(String.format(Locale.US, "%.6f, %.6f",
                    currentPhoto.getLatitude(), currentPhoto.getLongitude())).append("\n");

            // Add address if available
            if (currentPhoto.getAddress() != null && !currentPhoto.getAddress().isEmpty()) {
                description.append("Location: ").append(currentPhoto.getAddress()).append("\n");
            }

            description.append("GPS Location Data Embedded");
        } else {
            description.append("No location data available");
        }

        return description.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.photo_detail_menu, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating options menu", e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmation();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu item selection", e);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmation() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Photo")
                    .setMessage("Are you sure you want to delete this photo?")
                    .setPositiveButton("Delete", (dialog, which) -> deletePhoto())
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete dialog", e);
            Toast.makeText(this, "Error showing delete dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePhoto() {
        new Thread(() -> {
            try {
                // Delete from database
                database.photoLocationDao().deletePhoto(currentPhoto);

                // Delete physical file
                File photoFile = new File(currentPhoto.getPhotoPath());
                if (photoFile.exists()) {
                    boolean deleted = photoFile.delete();
                    if (!deleted) {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Warning: Could not delete photo file", Toast.LENGTH_SHORT).show()
                        );
                    }
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting photo", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error deleting photo", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}