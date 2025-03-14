package com.example.notes;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class SettingsManager {

    // Preference keys (must match the keys in preferences.xml)
    public static final String KEY_THEME = "theme";
    public static final String KEY_SHOW_DATE = "show_date";
    public static final String KEY_DEFAULT_CATEGORY = "default_category";
    public static final String KEY_CONFIRM_DELETE = "confirm_delete";
    public static final String KEY_TRASH_RETENTION = "trash_retention";

    // Theme values
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    private static SettingsManager instance;
    private final SharedPreferences preferences;

    private SettingsManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    public void applyTheme() {
        String themeValue = preferences.getString(KEY_THEME, THEME_SYSTEM);
        switch (themeValue) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public boolean shouldShowDate() {
        return preferences.getBoolean(KEY_SHOW_DATE, true);
    }

    public String getDefaultCategory() {
        String value = preferences.getString(KEY_DEFAULT_CATEGORY, "personal");
        switch (value) {
            case "personal":
                return "Personal";
            case "work":
                return "Work";
            case "study":
                return "Study";
            case "misc":
                return "Miscellaneous";
            default:
                return "Personal";
        }
    }

    public boolean shouldConfirmDelete() {
        return preferences.getBoolean(KEY_CONFIRM_DELETE, true);
    }

    public int getTrashRetentionDays() {
        return Integer.parseInt(preferences.getString(KEY_TRASH_RETENTION, "30"));
    }

    // Register a listener for preference changes
    public void registerPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    // Unregister a listener
    public void unregisterPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
