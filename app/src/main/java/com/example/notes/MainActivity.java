package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabAddNote;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
