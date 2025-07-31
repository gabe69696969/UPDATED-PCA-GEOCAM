package com.example.locationcamera;

import android.app.Application;
import android.util.Log;
import androidx.multidex.MultiDexApplication;

public class LocationCameraApplication extends MultiDexApplication {

    private static final String TAG = "LocationCameraApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Log.e(TAG, "Uncaught exception: ", throwable);

                // Log the stack trace
                throwable.printStackTrace();

                // You can add crash reporting here (e.g., Firebase Crashlytics)

                // Let the system handle the crash
                System.exit(1);
            }
        });

        Log.d(TAG, "Application started successfully");
    }
}