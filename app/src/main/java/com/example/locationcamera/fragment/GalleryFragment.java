package com.example.locationcamera.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.locationcamera.PhotoDetailActivity;
import com.example.locationcamera.adapter.PhotoAdapter;
import com.example.locationcamera.database.AppDatabase;
import com.example.locationcamera.databinding.FragmentGalleryBinding;
import com.example.locationcamera.model.PhotoLocation;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GalleryFragment extends Fragment implements PhotoAdapter.OnPhotoClickListener {

    private static final String TAG = "GalleryFragment";
    private FragmentGalleryBinding binding;
    private PhotoAdapter photoAdapter;
    private AppDatabase database;
    private List<PhotoLocation> photoList = new ArrayList<>();

    private boolean isGrouped = false;
    private Map<String, List<PhotoLocation>> groupedPhotos = new HashMap<>();
    private Map<String, String> folderNames = new HashMap<>();

    private boolean isSelectionMode = false; // Track if selection mode is active

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            Log.d(TAG, "Creating GalleryFragment view");
            binding = FragmentGalleryBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error creating view", e);
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            Log.d(TAG, "Setting up GalleryFragment");

            if (getContext() == null) {
                Log.e(TAG, "Context is null");
                return;
            }

            database = AppDatabase.getDatabase(requireContext());

            setupRecyclerView();
            loadPhotos();

            if (binding.swipeRefreshLayout != null) {
                binding.swipeRefreshLayout.setOnRefreshListener(this::loadPhotos);
            }

            if (binding.groupButton != null) {
                binding.groupButton.setOnClickListener(v -> {
                    isGrouped = !isGrouped;
                    if (isGrouped) {
                        groupPhotosByFolder();
                        photoAdapter.setGroupedData(groupedPhotos, folderNames, this::showRenameFolderDialog);
                        binding.groupButton.setText("Ungroup");
                    } else {
                        photoAdapter.setUngroupedData(photoList);
                        binding.groupButton.setText("Group by Folder");
                    }
                });
            }

            // Add delete button functionality
            if (binding.deleteButton != null) {
                binding.deleteButton.setOnClickListener(v -> {
                    if (!isSelectionMode) {
                        enableSelectionMode(true);
                    } else {
                        deleteSelectedPhotos();
                    }
                });
            }

            if (binding.cancelButton != null) {
                binding.cancelButton.setOnClickListener(v -> {
                    enableSelectionMode(false);
                });
            }

            Log.d(TAG, "GalleryFragment setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupRecyclerView() {
        try {
            if (binding == null || binding.recyclerView == null) {
                Log.e(TAG, "Binding or RecyclerView is null");
                return;
            }

            photoAdapter = new PhotoAdapter(photoList, this);
            binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            binding.recyclerView.setAdapter(photoAdapter);

            Log.d(TAG, "RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void loadPhotos() {
        try {
            Log.d(TAG, "Loading photos from database");

            new Thread(() -> {
                try {
                    if (database == null) {
                        Log.e(TAG, "Database is null");
                        return;
                    }

                    List<PhotoLocation> photos = database.photoLocationDao().getAllPhotos();

                    if (getActivity() == null) {
                        Log.w(TAG, "Activity is null, cannot update UI");
                        return;
                    }

                    getActivity().runOnUiThread(() -> {
                        try {
                            if (binding == null) {
                                Log.w(TAG, "Binding is null, fragment may be destroyed");
                                return;
                            }

                            photoList.clear();
                            if (photos != null) {
                                photoList.addAll(photos);
                            }

                            if (photoAdapter != null) {
                                if (isGrouped) {
                                    groupPhotosByFolder();
                                    photoAdapter.setGroupedData(groupedPhotos, folderNames, this::showRenameFolderDialog);
                                } else {
                                    photoAdapter.setUngroupedData(photoList);
                                }
                                photoAdapter.notifyDataSetChanged();
                            }

                            if (binding.swipeRefreshLayout != null) {
                                binding.swipeRefreshLayout.setRefreshing(false);
                            }

                            updateEmptyState();
                            updatePhotoCount();

                            Log.d(TAG, "Photos loaded successfully: " + photoList.size());
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating UI with photos", e);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading photos from database", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (binding != null && binding.swipeRefreshLayout != null) {
                                binding.swipeRefreshLayout.setRefreshing(false);
                            }
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Error loading photos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Error in loadPhotos", e);
        }
    }

    private void groupPhotosByFolder() {
        groupedPhotos.clear();
        for (PhotoLocation photo : photoList) {
            String folderPath = getFolderPath(photo.getPhotoPath());
            if (!groupedPhotos.containsKey(folderPath)) {
                groupedPhotos.put(folderPath, new ArrayList<>());
            }
            groupedPhotos.get(folderPath).add(photo);
        }
    }

    private String getFolderPath(String photoPath) {
        if (photoPath == null) return "Unknown";
        int lastSlash = photoPath.lastIndexOf(File.separator);
        if (lastSlash > 0) {
            return photoPath.substring(0, lastSlash);
        }
        return "Unknown";
    }

    private void showRenameFolderDialog(String folderPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rename Album");

        final EditText input = new EditText(requireContext());
        input.setText(folderNames.getOrDefault(folderPath, folderPath));
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            folderNames.put(folderPath, input.getText().toString());
            photoAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateEmptyState() {
        try {
            if (binding == null) return;

            if (photoList.isEmpty()) {
                if (binding.emptyView != null) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                }
                if (binding.recyclerView != null) {
                    binding.recyclerView.setVisibility(View.GONE);
                }
            } else {
                if (binding.emptyView != null) {
                    binding.emptyView.setVisibility(View.GONE);
                }
                if (binding.recyclerView != null) {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty state", e);
        }
    }

    private void updatePhotoCount() {
        try {
            if (binding != null && binding.photoCount != null) {
                String countText = photoList.size() + " photo" + (photoList.size() != 1 ? "s" : "");
                binding.photoCount.setText(countText);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating photo count", e);
        }
    }

    private void enableSelectionMode(boolean enable) {
        isSelectionMode = enable;
        photoAdapter.enableSelectionMode(enable);
        if (enable) {
            binding.deleteButton.setText("Delete");
            binding.cancelButton.setVisibility(View.VISIBLE); // Show the cancel button
        } else {
            binding.deleteButton.setText("Select to Delete");
            binding.cancelButton.setVisibility(View.GONE); // Hide the cancel button
        }
    }

    private void deleteSelectedPhotos() {
        Set<PhotoLocation> selectedPhotos = photoAdapter.getSelectedPhotos();
        if (selectedPhotos.isEmpty()) {
            Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirm deletion
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Photos")
                .setMessage("Are you sure you want to delete the selected photos?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            for (PhotoLocation photo : selectedPhotos) {
                                database.photoLocationDao().deletePhoto(photo);
                                File file = new File(photo.getPhotoPath());
                                if (file.exists()) {
                                    boolean deleted = file.delete();
                                    if (!deleted) {
                                        Log.w(TAG, "Could not delete file: " + file.getAbsolutePath());
                                    }
                                }
                            }

                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Photos deleted", Toast.LENGTH_SHORT).show();
                                enableSelectionMode(false);
                                loadPhotos(); // Refresh the gallery
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting photos", e);
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Error deleting photos", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onPhotoClick(PhotoLocation photo) {
        try {
            if (photo == null) {
                Log.w(TAG, "Photo is null");
                return;
            }

            Log.d(TAG, "Photo clicked: " + photo.getId());

            Intent intent = new Intent(requireContext(), PhotoDetailActivity.class);
            intent.putExtra("photo_id", photo.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error handling photo click", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error opening photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "GalleryFragment resumed");
        loadPhotos(); // Refresh when returning from detail view
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "GalleryFragment view destroyed");
        binding = null;
        photoAdapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "GalleryFragment destroyed");
    }
}
