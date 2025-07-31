# Location Camera - Native Android App

A native Android camera application built in Java that captures photos with location data and displays them in a gallery with detailed location information.

## Features

### 📸 Camera Functionality
- **Real-time Camera Preview**: Live camera feed with CameraX API
- **Photo Capture**: High-quality photo capture with location embedding
- **Camera Switching**: Toggle between front and rear cameras
- **Location Status**: Real-time location status indicator

### 📍 Location Services
- **GPS Integration**: Automatic location detection using FusedLocationProviderClient
- **Address Resolution**: Reverse geocoding to convert coordinates to readable addresses
- **Location Accuracy**: Display location accuracy and altitude information
- **Manual Refresh**: Option to manually refresh location data

### 🖼️ Photo Gallery
- **Grid Layout**: Beautiful 2-column grid display of captured photos
- **Location Indicators**: Visual indicators for photos with location data
- **Pull-to-Refresh**: Swipe down to refresh the gallery
- **Empty State**: Elegant empty state when no photos are available

### 📱 Photo Details
- **Full-Screen View**: Detailed view of individual photos
- **Location Information**: Complete location data including:
  - Full address (when available)
  - GPS coordinates
  - Location accuracy
  - Altitude information
- **Timestamp**: Photo capture date and time
- **Delete Functionality**: Remove photos with confirmation dialog

### 🗄️ Data Management
- **Local Database**: Room database for persistent photo storage
- **File Management**: Organized photo storage in app-specific directories
- **Data Integrity**: Automatic cleanup of orphaned files

## Technical Architecture

### Core Technologies
- **Language**: Java
- **UI Framework**: Android Views with Material Design
- **Camera**: CameraX API for modern camera functionality
- **Location**: Google Play Services Location API
- **Database**: Room Persistence Library
- **Image Loading**: Glide for efficient image loading and caching
- **Navigation**: Android Navigation Component

### Key Components

#### MainActivity
- Hosts the main navigation structure
- Manages bottom navigation between Camera and Gallery tabs

#### CameraFragment
- Handles camera preview and photo capture
- Manages location services and status updates
- Implements permission handling for camera and location

#### GalleryFragment
- Displays photos in a responsive grid layout
- Implements pull-to-refresh functionality
- Handles empty states and loading indicators

#### PhotoDetailActivity
- Shows detailed photo information
- Displays location data in an organized format
- Provides photo deletion functionality

#### Database Layer
- **PhotoLocation Entity**: Stores photo metadata and location data
- **PhotoLocationDao**: Data access operations
- **AppDatabase**: Room database configuration

### Permissions Required
- `CAMERA`: For photo capture functionality
- `ACCESS_FINE_LOCATION`: For precise GPS location
- `ACCESS_COARSE_LOCATION`: For network-based location
- `WRITE_EXTERNAL_STORAGE`: For photo storage
- `READ_EXTERNAL_STORAGE`: For photo access

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 (Android 7.0) or higher
- Device or emulator with camera and GPS capabilities

### Installation Steps

1. **Clone or Download** the project files to your local machine

2. **Open in Android Studio**:
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the `android-camera-app` folder
   - Click "OK" to open the project

3. **Sync Dependencies**:
   - Android Studio will automatically prompt to sync Gradle files
   - Click "Sync Now" when prompted
   - Wait for all dependencies to download

4. **Configure Device**:
   - Connect an Android device via USB with Developer Options enabled
   - OR set up an Android Virtual Device (AVD) with camera support

5. **Build and Run**:
   - Click the "Run" button (green play icon) in Android Studio
   - Select your target device
   - The app will build and install automatically

### First Launch
- Grant camera and location permissions when prompted
- Allow the app to access your device's location
- The camera will initialize and show your current location status
- Start taking photos with embedded location data!

## Usage Guide

### Taking Photos
1. Open the app to the Camera tab
2. Wait for location to be acquired (green status indicator)
3. Tap the large capture button to take a photo
4. Photos are automatically saved with location data

### Viewing Gallery
1. Switch to the Gallery tab
2. Browse your photos in the grid layout
3. Photos with location data show a location indicator
4. Pull down to refresh the gallery

### Photo Details
1. Tap any photo in the gallery
2. View the full-size image and location details
3. See complete address, coordinates, and metadata
4. Use the delete button to remove photos

## Customization Options

### Styling
- Modify colors in `res/values/colors.xml`
- Update themes in `res/values/themes.xml`
- Customize layouts in the `res/layout/` directory

### Database Schema
- Extend the `PhotoLocation` entity for additional metadata
- Add new DAO methods for custom queries
- Implement data migration for schema changes

### Camera Settings
- Adjust photo quality in `CameraFragment.java`
- Modify camera resolution and format options
- Add additional camera features (flash, zoom, etc.)

## Performance Considerations

- **Image Loading**: Glide handles efficient image caching and memory management
- **Database Operations**: Room provides optimized SQLite operations
- **Location Updates**: Efficient location handling with minimal battery impact
- **Memory Management**: Proper lifecycle management prevents memory leaks

## Security & Privacy

- **Local Storage**: All photos and data remain on the device
- **Permission Handling**: Graceful permission requests and fallbacks
- **Data Validation**: Input validation for all user interactions
- **File Security**: Photos stored in app-specific directories

This native Android application provides a complete, production-ready camera solution with location services, offering the same functionality as your React Native app but built entirely in Java for optimal Android performance and integration.