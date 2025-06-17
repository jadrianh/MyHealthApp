package com.damb.myhealthapp.ui.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.ui.views.onboarding.BirthdayProfileActivity;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();

        ImageView welcomeImage = findViewById(R.id.welcomeImage);
        ImageView logoImageView = findViewById(R.id.logoImageView);
        TextView welcomeSubtitle = findViewById(R.id.welcomeSubtitle);
        LinearLayout registerLayout = findViewById(R.id.registerLayout);
        Button registerButton = findViewById(R.id.registerButton);
        Button signInButton = findViewById(R.id.signInButton);

        // Animaciones de Fade-in
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator imageFadeIn = ObjectAnimator.ofFloat(welcomeImage, "alpha", 0f, 1f);
        imageFadeIn.setDuration(800);

        ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f);
        titleFadeIn.setStartDelay(200);
        titleFadeIn.setDuration(800);

        ObjectAnimator subtitleFadeIn = ObjectAnimator.ofFloat(welcomeSubtitle, "alpha", 0f, 1f);
        subtitleFadeIn.setStartDelay(400);
        subtitleFadeIn.setDuration(800);

        ObjectAnimator registerLayoutFadeIn = ObjectAnimator.ofFloat(registerLayout, "alpha", 0f, 1f);
        registerLayoutFadeIn.setStartDelay(600);
        registerLayoutFadeIn.setDuration(800);

        ObjectAnimator signInButtonFadeIn = ObjectAnimator.ofFloat(signInButton, "alpha", 0f, 1f);
        signInButtonFadeIn.setStartDelay(800);
        signInButtonFadeIn.setDuration(800);

        animatorSet.playTogether(imageFadeIn, titleFadeIn, subtitleFadeIn, registerLayoutFadeIn, signInButtonFadeIn);
        animatorSet.start();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, BirthdayProfileActivity.class);
                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}