package com.example.notes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabAddNote;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Make sure theme is applied before setting content view
        SettingsManager.getInstance(this).applyTheme();

        setContentView(R.layout.activity_main);

        try {
            // Initialize UI components
            initializeUI();

            // Set up listeners
            setupListeners();

            // Set up ViewPager and Tabs
            setupViewPager();

            // Initialize database
            DatabaseHelper.getInstance(this).getWritableDatabase();

            // Check for notification permission
            checkNotificationPermission();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeUI() {
        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation view
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Floating action button
        fabAddNote = findViewById(R.id.fab_add_note);

        // ViewPager and TabLayout
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
    }

    private void setupListeners() {
        // FAB click listener
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open note editor activity
                Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                // No need to pass noteId as we're creating a new note
                startActivity(intent);
            }
        });
    }

    private void setupViewPager() {
        try {
            // Create a list of fragments for the tabs
            List<Fragment> fragments = new ArrayList<>();
            fragments.add(new NotesListFragment()); // All notes
            fragments.add(new NotesListFragment()); // Recent notes

            // Create adapter
            ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragments);
            viewPager.setAdapter(adapter);

            // Connect TabLayout with ViewPager
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("All Notes");
                        break;
                    case 1:
                        tab.setText("Recent");
                        break;
                }
            }).attach();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle gracefully to prevent app crash
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                // Show permission explanation dialog if needed
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Notification Permission")
                            .setMessage("This app needs notification permission to alert you when " +
                                    "you're tagged in notes.")
                            .setPositiveButton("OK", (dialog, which) -> requestNotificationPermission())
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                } else {
                    requestNotificationPermission();
                }
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.POST_NOTIFICATIONS },
                    NOTIFICATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Close any open search
        NotesListFragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.closeSearch();
        }

        if (id == R.id.nav_all_notes) {
            currentFragment.filterByCategory(null);
            setTitle(R.string.app_name);
        } else if (id == R.id.nav_favorites) {
            currentFragment.filterFavorites();
            setTitle(R.string.nav_favorites);
        } else if (id == R.id.nav_personal) {
            currentFragment.filterByCategory("Personal");
            setTitle(R.string.category_personal);
        } else if (id == R.id.nav_work) {
            currentFragment.filterByCategory("Work");
            setTitle(R.string.category_work);
        } else if (id == R.id.nav_study) {
            currentFragment.filterByCategory("Study");
            setTitle(R.string.category_study);
        } else if (id == R.id.nav_misc) {
            currentFragment.filterByCategory("Miscellaneous");
            setTitle(R.string.category_misc);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_trash) {
            Intent intent = new Intent(this, TrashActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sign_out) {
            try {
                // Clear database before signing out
                DatabaseHelper.getInstance(this).clearDatabase();
                // Sign out from Firebase
                mAuth.signOut();
                startActivity(new Intent(this, SignInActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error signing out", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        // Keep selected item highlighted
        navigationView.setCheckedItem(id);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private NotesListFragment getCurrentFragment() {
        if (viewPager == null || viewPager.getAdapter() == null) {
            return null;
        }

        try {
            return (NotesListFragment) ((ViewPagerAdapter) viewPager.getAdapter())
                    .getFragments().get(viewPager.getCurrentItem());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
