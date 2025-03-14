package com.example.notes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.nav_settings);

        // Load settings fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat 
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        
        private SettingsManager settingsManager;
        
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            settingsManager = SettingsManager.getInstance(requireContext());
        }
        
        @Override
        public void onResume() {
            super.onResume();
            // Register the listener
            settingsManager.registerPreferenceChangeListener(this);
        }
        
        @Override
        public void onPause() {
            super.onPause();
            // Unregister the listener
            settingsManager.unregisterPreferenceChangeListener(this);
        }
        
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(SettingsManager.KEY_THEME)) {
                // Apply theme change immediately
                settingsManager.applyTheme();
                
                // Recreate the activity to apply the theme
                requireActivity().recreate();
            }
        }
    }
}
