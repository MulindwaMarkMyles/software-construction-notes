package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button skipButton;
    private Button nextButton;
    private ProgressBar progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only check if onboarding is completed
        if (SettingsManager.getInstance(this).isOnboardingCompleted()) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.nextButton);
        progressIndicator = findViewById(R.id.progressIndicator);
        progressIndicator.setMax(100);
        progressIndicator.setProgress(33); // Initial progress

        viewPager.setAdapter(new OnboardingAdapter(this)); // Pass 'this' as the FragmentActivity

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
                progressIndicator.setProgress((position + 1) * 33); // Update progress

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
