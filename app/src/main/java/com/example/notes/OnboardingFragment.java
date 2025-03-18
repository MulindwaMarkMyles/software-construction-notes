package com.example.notes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingFragment extends Fragment {
    private static final String ARG_POSITION = "position";

    public static OnboardingFragment newInstance(int position) {
        OnboardingFragment fragment = new OnboardingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int position = getArguments() != null ? getArguments().getInt(ARG_POSITION) : 0;
        
        ImageView imageView = view.findViewById(R.id.onboardingImage);
        TextView titleText = view.findViewById(R.id.titleText);
        TextView descText = view.findViewById(R.id.descriptionText);

        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.onboarding_1);
                titleText.setText(R.string.onboarding_title_1);
                descText.setText(R.string.onboarding_desc_1);
                break;
            case 1:
                imageView.setImageResource(R.drawable.onboarding_2);
                titleText.setText(R.string.onboarding_title_2);
                descText.setText(R.string.onboarding_desc_2);
                break;
            case 2:
                imageView.setImageResource(R.drawable.onboarding_3);
                titleText.setText(R.string.onboarding_title_3);
                descText.setText(R.string.onboarding_desc_3);
                break;
        }
    }
}
