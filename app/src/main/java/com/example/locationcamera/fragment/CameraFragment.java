package com.example.locationcamera.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.locationcamera.R;
import com.example.locationcamera.database.AppDatabase;
import com.example.locationcamera.databinding.FragmentCameraBinding;
import com.example.locationcamera.model.PhotoLocation;
import com.example.locationcamera.utils.ImageUtils;
import com.example.locationcamera.utils.WatermarkUtils;
import com.example.locationcamera.utils.PhotoMetadataUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private FragmentCameraBinding binding;
    private ImageCapture imageCapture;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private AppDatabase database;
    private boolean isFrontCamera = false;
    private ProcessCameraProvider cameraProvider;
    private boolean isCameraReady = false;
    private boolean isInitializing = false;
    private Handler mainHandler;
    private boolean isFragmentActive = false;
    private boolean isCapturing = false;
    private boolean isFlipping = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            Log.d(TAG, "CameraFragment onViewCreated");
            isFragmentActive = true;

            mainHandler = new Handler(Looper.getMainLooper());
            database = AppDatabase.getDatabase(requireContext());
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

            // Initialize UI state
            binding.progressBar.setVisibility(View.GONE);

            // Set up click listeners
            binding.captureButton.setOnClickListener(v -> takePhoto());
            binding.flipCameraButton.setOnClickListener(v -> flipCamera());
            binding.refreshLocationButton.setOnClickListener(v -> getCurrentLocation());

            // Check permissions and initialize
            if (allPermissionsGranted()) {
                Log.d(TAG, "All permissions granted, initializing camera");
                updateLocationStatus("Initializing camera...", R.color.orange);
                initializeCamera();
                getCurrentLocation();
            } else {
                Log.d(TAG, "Requesting permissions");
                updateLocationStatus("Requesting permissions...", R.color.orange);
                ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }

            // Enable buttons initially - they will handle their own permission checks
            updateButtonStates();

        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            showError("Error initializing camera");
        }
    }

    private void updateButtonStates() {
        if (binding == null || !isFragmentActive) {
            return;
        }

        mainHandler.post(() -> {
            try {
                // Capture button: enabled if has camera permission and not capturing
                boolean captureEnabled = hasCameraPermission() && !isCapturing && isCameraReady;
                binding.captureButton.setEnabled(captureEnabled);
                binding.captureButton.setAlpha(captureEnabled ? 1.0f : 0.6f);

                // Flip camera button: enabled if has camera permission and not busy
                boolean flipEnabled = hasCameraPermission() && !isFlipping && !isCapturing && isCameraReady;
                binding.flipCameraButton.setEnabled(flipEnabled);
                binding.flipCameraButton.setAlpha(flipEnabled ? 1.0f : 0.6f);

                // Location refresh button: always enabled (will show error if no permission)
                boolean locationEnabled = true;
                binding.refreshLocationButton.setEnabled(locationEnabled);
                binding.refreshLocationButton.setAlpha(locationEnabled ? 1.0f : 0.6f);

                Log.d(TAG, "Button states updated - Capture: " + captureEnabled +
                        ", Flip: " + flipEnabled + ", Location: " + locationEnabled +
                        ", Camera Ready: " + isCameraReady);

            } catch (Exception e) {
                Log.e(TAG, "Error updating button states", e);
            }
        });
    }

    private boolean hasCameraPermission() {
        return getContext() != null &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasLocationPermission() {
        return getContext() != null &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeCamera() {
        if (isInitializing) {
            Log.d(TAG, "Camera initialization already in progress");
            return;
        }

        if (!isFragmentActive || getContext() == null) {
            Log.e(TAG, "Fragment not active or context is null, cannot initialize camera");
            return;
        }

        if (!hasCameraPermission()) {
            Log.e(TAG, "Camera permission not granted");
            updateLocationStatus("Camera permission required", R.color.red);
            return;
        }

        try {
            isInitializing = true;
            isCameraReady = false;
            updateButtonStates();

            Log.d(TAG, "Starting camera initialization");
            updateLocationStatus("Starting camera...", R.color.orange);

            ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                    ProcessCameraProvider.getInstance(requireContext());

            cameraProviderFuture.addListener(() -> {
                try {
                    if (!isFragmentActive) {
                        Log.d(TAG, "Fragment no longer active, skipping camera initialization");
                        isInitializing = false;
                        return;
                    }

                    Log.d(TAG, "Camera provider future completed");
                    cameraProvider = cameraProviderFuture.get();

                    if (cameraProvider != null) {
                        Log.d(TAG, "Camera provider obtained successfully");
                        startCamera();
                    } else {
                        Log.e(TAG, "Camera provider is null");
                        handleCameraError("Camera provider failed");
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error getting camera provider", e);
                    handleCameraError("Camera initialization failed: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error in camera provider listener", e);
                    handleCameraError("Unexpected camera error: " + e.getMessage());
                }
            }, ContextCompat.getMainExecutor(requireContext()));

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeCamera", e);
            handleCameraError("Camera setup failed: " + e.getMessage());
        }
    }

    private void startCamera() {
        if (!isFragmentActive) {
            Log.d(TAG, "Fragment not active, skipping camera start");
            isInitializing = false;
            return;
        }

        mainHandler.post(() -> {
            try {
                if (cameraProvider == null) {
                    Log.e(TAG, "Camera provider is null, cannot start camera");
                    handleCameraError("Camera provider not ready");
                    return;
                }

                if (binding == null || binding.previewView == null) {
                    Log.e(TAG, "Binding or preview view is null");
                    handleCameraError("Camera view not ready");
                    return;
                }

                if (!isFragmentActive || getContext() == null) {
                    Log.e(TAG, "Fragment not active or context is null, cannot start camera");
                    handleCameraError("Fragment context lost");
                    return;
                }

                Log.d(TAG, "Starting camera with provider");
                updateLocationStatus("Configuring camera...", R.color.orange);

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll();

                // Create preview use case
                Preview preview = new Preview.Builder()
                        .build();

                // Create image capture use case with proper orientation handling
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(requireActivity().getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                // Select camera
                CameraSelector cameraSelector = isFrontCamera ?
                        CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                            this,
                            cameraSelector,
                            preview,
                            imageCapture
                    );

                    // Connect the preview to the PreviewView
                    preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                    // Camera is now ready
                    isCameraReady = true;
                    isInitializing = false;
                    isFlipping = false;

                    // Enable buttons
                    updateButtonStates();

                    Log.d(TAG, "Camera started successfully - Ready for use");
                    updateLocationStatus("Camera ready", R.color.green);

                } catch (Exception e) {
                    Log.e(TAG, "Use case binding failed", e);
                    handleCameraError("Camera binding failed: " + e.getMessage());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in startCamera", e);
                handleCameraError("Camera start failed: " + e.getMessage());
            }
        });
    }

    private void handleCameraError(String errorMessage) {
        isInitializing = false;
        isCameraReady = false;
        isFlipping = false;
        updateButtonStates();
        updateLocationStatus("Camera error", R.color.red);
        showError(errorMessage);
        Log.e(TAG, "Camera error: " + errorMessage);

        // Try to reinitialize after a delay
        mainHandler.postDelayed(() -> {
            if (isFragmentActive && hasCameraPermission() && !isCameraReady) {
                Log.d(TAG, "Attempting to reinitialize camera after error");
                initializeCamera();
            }
        }, 2000);
    }

    private void flipCamera() {
        try {
            if (!hasCameraPermission()) {
                showError("Camera permission required");
                return;
            }

            if (isCapturing || isFlipping || !isCameraReady) {
                Log.w(TAG, "Camera busy - Capturing: " + isCapturing + ", Flipping: " + isFlipping + ", Ready: " + isCameraReady);
                showError("Please wait, camera is busy");
                return;
            }

            Log.d(TAG, "Flipping camera from " + (isFrontCamera ? "front" : "back") +
                    " to " + (isFrontCamera ? "back" : "front"));

            isFlipping = true;
            isCameraReady = false;
            isFrontCamera = !isFrontCamera;

            // Update buttons during flip
            updateButtonStates();
            updateLocationStatus("Switching camera...", R.color.orange);

            // Start camera with new facing
            startCamera();

        } catch (Exception e) {
            Log.e(TAG, "Error flipping camera", e);
            isFlipping = false;
            handleCameraError("Error switching camera: " + e.getMessage());
        }
    }

    private void takePhoto() {
        try {
            if (!hasCameraPermission()) {
                showError("Camera permission required");
                return;
            }

            if (imageCapture == null || !isCameraReady) {
                showError("Camera not ready - please wait");
                return;
            }

            if (isCapturing || isFlipping) {
                Log.w(TAG, "Camera busy - Capturing: " + isCapturing + ", Flipping: " + isFlipping);
                showError("Please wait, camera is busy");
                return;
            }

            Log.d(TAG, "Taking photo");
            isCapturing = true;

            // Update buttons and show progress
            updateButtonStates();
            binding.progressBar.setVisibility(View.VISIBLE);
            updateLocationStatus("Capturing photo...", R.color.orange);

            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                    .format(System.currentTimeMillis());

            File picturesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (picturesDir == null) {
                picturesDir = new File(requireContext().getFilesDir(), "Pictures");
                if (!picturesDir.exists()) {
                    boolean created = picturesDir.mkdirs();
                    Log.d(TAG, "Pictures directory created: " + created);
                }
            }

            File photoFile = new File(picturesDir, name + ".jpg");
            Log.d(TAG, "Photo will be saved to: " + photoFile.getAbsolutePath());

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(requireContext()),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                            showError("Photo capture failed: " + exception.getMessage());
                            resetCaptureState();
                        }

                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            Log.d(TAG, "Photo captured successfully to: " + photoFile.getAbsolutePath());
                            updateLocationStatus("Processing photo...", R.color.orange);
                            processPhotoWithWatermarkAndMetadata(photoFile.getAbsolutePath());
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(TAG, "Error in takePhoto", e);
            showError("Error taking photo: " + e.getMessage());
            resetCaptureState();
        }
    }

    private void resetCaptureState() {
        if (!isFragmentActive) return;

        mainHandler.post(() -> {
            isCapturing = false;
            if (binding != null) {
                binding.progressBar.setVisibility(View.GONE);
                updateButtonStates();
                if (isCameraReady) {
                    updateLocationStatus("Camera ready", R.color.green);
                } else if (hasLocationPermission() && currentLocation != null) {
                    updateLocationStatus("Location ready", R.color.green);
                }
            }
        });
    }

    private void processPhotoWithWatermarkAndMetadata(String originalPhotoPath) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Processing photo with watermark, orientation correction, and metadata");

                // First, fix the orientation of the photo
                Bitmap originalBitmap = ImageUtils.rotateBitmapIfNeeded(originalPhotoPath);
                if (originalBitmap == null) {
                    throw new Exception("Failed to load and orient captured photo");
                }

                Log.d(TAG, "Photo loaded and oriented successfully. Size: " +
                        originalBitmap.getWidth() + "x" + originalBitmap.getHeight());

                // Add watermark with coordinates and icon
                Bitmap watermarkedBitmap;
                String address = "";
                Double altitude = null;
                if (currentLocation != null) {
                    address = getAddressFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                    if (currentLocation.hasAltitude()) {
                        altitude = currentLocation.getAltitude();
                    }
                    watermarkedBitmap = WatermarkUtils.addWatermarkWithContext(
                            requireContext(),
                            originalBitmap,
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude(),
                            address,
                            System.currentTimeMillis(),
                            altitude
                    );
                    Log.d(TAG, "Watermark with icon added with location: " +
                            currentLocation.getLatitude() + ", " + currentLocation.getLongitude() +
                            (altitude != null ? ", altitude: " + altitude + "m" : ""));
                } else {
                    // If no location, just add a timestamp watermark with icon
                    watermarkedBitmap = WatermarkUtils.addWatermarkWithContext(
                            requireContext(),
                            originalBitmap,
                            0,
                            0,
                            "Location unavailable",
                            System.currentTimeMillis(),
                            null
                    );
                    Log.d(TAG, "Watermark with icon added without location");
                }

                if (watermarkedBitmap != null) {
                    // Save the watermarked photo (overwrite original)
                    saveWatermarkedPhoto(watermarkedBitmap, originalPhotoPath);

                    // Clean up bitmaps
                    if (watermarkedBitmap != originalBitmap) {
                        originalBitmap.recycle();
                    }
                    watermarkedBitmap.recycle();

                    Log.d(TAG, "Watermarked photo with icon saved successfully");
                } else {
                    Log.w(TAG, "Watermarked bitmap is null, saving original with correct orientation");
                    WatermarkUtils.saveBitmapToFile(originalBitmap, originalPhotoPath);
                    originalBitmap.recycle();
                }

                // Add location metadata to photo EXIF and description
                if (currentLocation != null) {
                    boolean metadataSuccess = PhotoMetadataUtils.savePhotoWithLocationMetadata(
                            originalPhotoPath,
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude(),
                            address,
                            System.currentTimeMillis(),
                            currentLocation
                    );
                    Log.d(TAG, "Location metadata saved to photo: " + metadataSuccess);
                } else {
                    Log.w(TAG, "No location available for metadata");
                }

                // Save photo record to database
                savePhotoWithLocation(originalPhotoPath);

            } catch (Exception e) {
                Log.e(TAG, "Error processing photo with watermark and metadata", e);
                if (isFragmentActive) {
                    mainHandler.post(() -> {
                        showError("Error processing photo: " + e.getMessage());
                        resetCaptureState();
                    });
                }
            }
        }).start();
    }

    private void saveWatermarkedPhoto(Bitmap watermarkedBitmap, String photoPath) throws Exception {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(photoPath);
            boolean compressed = watermarkedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            if (!compressed) {
                throw new Exception("Failed to compress bitmap");
            }
            Log.d(TAG, "Watermarked photo saved successfully to: " + photoPath);
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

    private void savePhotoWithLocation(String photoPath) {
        try {
            String address = "";
            if (currentLocation != null) {
                address = getAddressFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
            }

            PhotoLocation photoLocation = new PhotoLocation(
                    photoPath,
                    currentLocation != null ? currentLocation.getLatitude() : 0,
                    currentLocation != null ? currentLocation.getLongitude() : 0,
                    currentLocation != null ? currentLocation.getAccuracy() : 0,
                    currentLocation != null ? currentLocation.getAltitude() : 0,
                    address,
                    System.currentTimeMillis()
            );

            long id = database.photoLocationDao().insertPhoto(photoLocation);
            Log.d(TAG, "Photo record saved to database with ID: " + id);

            if (isFragmentActive) {
                mainHandler.post(() -> {
                    showSuccess("Photo saved with location watermark, metadata, and icon!");
                    resetCaptureState();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving photo with location", e);
            if (isFragmentActive) {
                mainHandler.post(() -> {
                    showError("Error saving photo: " + e.getMessage());
                    resetCaptureState();
                });
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (!isFragmentActive) return;

        if (!hasLocationPermission()) {
            updateLocationStatus("Location permission required", R.color.red);
            showError("Location permission required");
            return;
        }

        try {
            updateLocationStatus("Getting location...", R.color.orange);

            // Request high-accuracy satellite-based location using new API
            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(500)
                    .setMaxUpdateDelayMillis(15000)
                    .setMaxUpdates(1)
                    .build();

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (!isFragmentActive) return;

                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        currentLocation = location;

                        // Check if location is from GPS/satellite
                        String provider = location.getProvider();
                        boolean isGPS = LocationManager.GPS_PROVIDER.equals(provider);

                        String locationText = String.format(Locale.US, "%.6f, %.6f %s",
                                location.getLatitude(), location.getLongitude(),
                                isGPS ? "(GPS)" : "(" + provider + ")");
                        updateLocationStatus(locationText, isGPS ? R.color.green : R.color.orange);

                        Log.d(TAG, "Location obtained from " + provider + ": " + locationText +
                                ", Accuracy: " + location.getAccuracy() + "m");

                        // Get address in background
                        new Thread(() -> {
                            String address = getAddressFromLocation(location.getLatitude(), location.getLongitude());
                            if (!address.isEmpty() && isFragmentActive) {
                                mainHandler.post(() -> {
                                    String statusText = address + (isGPS ? " (GPS)" : " (" + provider + ")");
                                    updateLocationStatus(statusText, isGPS ? R.color.green : R.color.orange);
                                });
                            }
                        }).start();
                    } else {
                        updateLocationStatus("Satellite location unavailable", R.color.red);
                        Log.w(TAG, "Location is null from satellite request");
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (!isFragmentActive) return;

                    if (!locationAvailability.isLocationAvailable()) {
                        updateLocationStatus("GPS satellites not available", R.color.red);
                        Log.w(TAG, "GPS satellites not available");
                    }
                }
            };

            // Request location updates with high accuracy
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                        .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (!isFragmentActive) return;
                                Log.e(TAG, "Error requesting satellite location", e);
                                updateLocationStatus("Satellite location error", R.color.red);
                            }
                        });
            } else {
                updateLocationStatus("Location permission not granted", R.color.red);
                Log.e(TAG, "Location permission not granted when requesting updates");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in getCurrentLocation", e);
            updateLocationStatus("Satellite location service error", R.color.red);
        }
    }

    private void updateLocationStatus(String text, int colorRes) {
        if (!isFragmentActive) return;

        mainHandler.post(() -> {
            if (binding != null && binding.locationStatus != null) {
                try {
                    binding.locationStatus.setText(text);
                    binding.locationStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
                } catch (Exception e) {
                    Log.e(TAG, "Error updating location status", e);
                }
            }
        });
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        try {
            if (!Geocoder.isPresent()) {
                return String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
            }

            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(address.getAddressLine(i));
                }

                return sb.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting address", e);
        }

        return String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
    }

    private boolean allPermissionsGranted() {
        if (getContext() == null) return false;

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showError(String message) {
        if (!isFragmentActive || getContext() == null) return;

        mainHandler.post(() -> {
            try {
                Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing error toast", e);
            }
        });
    }

    private void showSuccess(String message) {
        if (!isFragmentActive || getContext() == null) return;

        mainHandler.post(() -> {
            try {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing success toast", e);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Log.d(TAG, "All permissions granted, initializing camera");
                initializeCamera();
                getCurrentLocation();
            } else {
                Log.w(TAG, "Not all permissions granted");
                showError("All permissions are required for the app to work properly");
                updateLocationStatus("Permissions required", R.color.red);
            }
            // Update button states regardless of permission result
            updateButtonStates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive = true;
        Log.d(TAG, "CameraFragment onResume - Camera ready: " + isCameraReady + ", Initializing: " + isInitializing);

        // Reset states
        isCapturing = false;
        isFlipping = false;

        // Update button states
        updateButtonStates();

        // Only restart camera if we have permissions and it's not already working
        if (hasCameraPermission()) {
            if (!isCameraReady && !isInitializing) {
                if (cameraProvider != null) {
                    Log.d(TAG, "Restarting camera on resume");
                    startCamera();
                } else {
                    Log.d(TAG, "Reinitializing camera on resume");
                    initializeCamera();
                }
            }
        } else {
            Log.w(TAG, "Camera permission not granted on resume");
            updateLocationStatus("Camera permission required", R.color.red);
        }

        // Check location if we have permission
        if (hasLocationPermission() && currentLocation == null) {
            getCurrentLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive = false;
        Log.d(TAG, "CameraFragment onPause");
        // Don't unbind camera on pause to maintain state
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentActive = false;
        Log.d(TAG, "CameraFragment onDestroyView");

        // Clean up camera resources
        if (cameraProvider != null) {
            try {
                cameraProvider.unbindAll();
                Log.d(TAG, "Camera unbound successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding camera", e);
            }
            cameraProvider = null;
        }

        isCameraReady = false;
        isInitializing = false;
        isCapturing = false;
        isFlipping = false;
        imageCapture = null;
        binding = null;
        mainHandler = null;

        Log.d(TAG, "CameraFragment cleanup completed");
    }
}