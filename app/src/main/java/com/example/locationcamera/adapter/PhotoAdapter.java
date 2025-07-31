package com.example.locationcamera.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.locationcamera.R;
import com.example.locationcamera.model.PhotoLocation;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "PhotoAdapter";
    private List<PhotoLocation> photoList;
    private OnPhotoClickListener listener;

    private boolean isGrouped = false;
    private Map<String, List<PhotoLocation>> groupedPhotos;
    private Map<String, String> folderNames;
    private List<String> folderOrder = new ArrayList<>();
    private RenameFolderListener renameFolderListener;

    private boolean isSelectionMode = false; // Track if selection mode is active
    private Set<PhotoLocation> selectedPhotos = new HashSet<>(); // Track selected photos

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoLocation photo);
    }

    public interface RenameFolderListener {
        void onRenameFolder(String folderPath);
    }

    public PhotoAdapter(List<PhotoLocation> photoList, OnPhotoClickListener listener) {
        this.photoList = photoList;
        this.listener = listener;
        Log.d(TAG, "PhotoAdapter created with " + (photoList != null ? photoList.size() : 0) + " photos");
    }

    public void setGroupedData(Map<String, List<PhotoLocation>> groupedPhotos, Map<String, String> folderNames, RenameFolderListener renameFolderListener) {
        this.isGrouped = true;
        this.groupedPhotos = groupedPhotos;
        this.folderNames = folderNames;
        this.folderOrder.clear();
        this.folderOrder.addAll(groupedPhotos.keySet());
        this.renameFolderListener = renameFolderListener;
        notifyDataSetChanged();
    }

    public void setUngroupedData(List<PhotoLocation> photoList) {
        this.isGrouped = false;
        this.photoList = photoList;
        notifyDataSetChanged();
    }

    public void enableSelectionMode(boolean enable) {
        isSelectionMode = enable;
        selectedPhotos.clear();
        notifyDataSetChanged();
    }

    public Set<PhotoLocation> getSelectedPhotos() {
        return selectedPhotos;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error creating view holder", e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            if (isGrouped && groupedPhotos != null) {
                String folderPath = folderOrder.get(position);
                List<PhotoLocation> photos = groupedPhotos.get(folderPath);
                PhotoLocation firstPhoto = photos.get(0);

                GroupedPhotoViewHolder groupedHolder = (GroupedPhotoViewHolder) holder;
                String photoPath = firstPhoto.getPhotoPath();
                File photoFile = new File(photoPath);
                Glide.with(holder.itemView.getContext())
                        .load(photoFile)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(groupedHolder.imageView);

                String albumName = folderNames != null && folderNames.containsKey(folderPath)
                        ? folderNames.get(folderPath)
                        : folderPath;
                groupedHolder.albumName.setText(albumName);
                groupedHolder.albumName.setVisibility(View.VISIBLE); // Show album name when grouped

                groupedHolder.renameButton.setOnClickListener(v -> {
                    if (renameFolderListener != null) {
                        renameFolderListener.onRenameFolder(folderPath);
                    }
                });

            } else {
                if (photoList == null || position >= photoList.size()) {
                    Log.w(TAG, "Invalid position or null photo list");
                    return;
                }

                PhotoLocation photo = photoList.get(position);
                if (photo == null) {
                    Log.w(TAG, "Photo at position " + position + " is null");
                    return;
                }

                PhotoViewHolder photoHolder = (PhotoViewHolder) holder;
                String photoPath = photo.getPhotoPath();
                if (photoPath != null && !photoPath.isEmpty()) {
                    File photoFile = new File(photoPath);
                    if (photoFile.exists()) {
                        Glide.with(holder.itemView.getContext())
                                .load(photoFile)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_error)
                                .into(photoHolder.imageView);
                    } else {
                        photoHolder.imageView.setImageResource(R.drawable.ic_image_error);
                    }
                } else {
                    photoHolder.imageView.setImageResource(R.drawable.ic_image_error);
                }

                // Handle selection mode
                if (isSelectionMode) {
                    photoHolder.checkBox.setVisibility(View.VISIBLE);
                    photoHolder.checkBox.setChecked(selectedPhotos.contains(photo));
                    photoHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            selectedPhotos.add(photo);
                        } else {
                            selectedPhotos.remove(photo);
                        }
                    });
                } else {
                    photoHolder.checkBox.setVisibility(View.GONE);
                }

                photoHolder.itemView.setOnClickListener(v -> {
                    if (isSelectionMode) {
                        photoHolder.checkBox.setChecked(!photoHolder.checkBox.isChecked());
                    } else if (listener != null) {
                        listener.onPhotoClick(photo);
                    }
                });

                // Hide album name and rename button when not grouped
                if (holder instanceof GroupedPhotoViewHolder) {
                    GroupedPhotoViewHolder groupedHolder = (GroupedPhotoViewHolder) holder;
                    groupedHolder.albumName.setVisibility(View.GONE);
                    groupedHolder.renameButton.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        if (isGrouped && groupedPhotos != null) {
            return folderOrder.size();
        }
        return photoList != null ? photoList.size() : 0;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView locationIndicator;
        CheckBox checkBox; // Add for selection

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                imageView = itemView.findViewById(R.id.photo_image);
                locationIndicator = itemView.findViewById(R.id.location_indicator);
                checkBox = itemView.findViewById(R.id.photo_checkbox); // Add reference to checkbox

                if (imageView == null) {
                    Log.e(TAG, "Photo ImageView not found in layout");
                }
                if (locationIndicator == null) {
                    Log.e(TAG, "Location indicator ImageView not found in layout");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing view holder", e);
            }
        }
    }

    static class GroupedPhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView albumName;
        ImageView renameButton;

        GroupedPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_image);
            albumName = itemView.findViewById(R.id.album_name);
            renameButton = itemView.findViewById(R.id.rename_album_button);
        }
    }
}