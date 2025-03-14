package com.example.notes;

import android.app.Application;

public class NotesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply theme from settings
        SettingsManager settingsManager = SettingsManager.getInstance(this);
        settingsManager.applyTheme();
        
        // Initialize the database (eager initialization)
        DatabaseHelper.getInstance(this);
    }
}
