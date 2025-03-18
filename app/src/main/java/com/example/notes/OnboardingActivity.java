package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button skipButton;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.nextButton);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new OnboardingAdapter(this));  // Pass 'this' as the FragmentActivity

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // Just the indicator dots
                }).attach();

        skipButton.setOnClickListener(v -> finishOnboarding());

        nextButton.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == 2) {
                finishOnboarding();
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    nextButton.setText(R.string.get_started);
                } else {
                    nextButton.setText(R.string.next);
                }
                skipButton.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void finishOnboarding() {
        SettingsManager.getInstance(this).setOnboardingCompleted(true);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
