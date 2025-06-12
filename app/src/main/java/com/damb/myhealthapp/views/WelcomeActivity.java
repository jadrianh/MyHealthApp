package com.damb.myhealthapp.views;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.views.LoginActivity;
import com.damb.myhealthapp.onboarding.BirthdayProfileActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ImageView welcomeImage = findViewById(R.id.welcomeImage);
        TextView welcomeTitle = findViewById(R.id.welcomeTitle);
        TextView welcomeSubtitle = findViewById(R.id.welcomeSubtitle);
        Button registerButton = findViewById(R.id.registerButton);
        Button signInButton = findViewById(R.id.signInButton);

        // Animaciones de Fade-in
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator imageFadeIn = ObjectAnimator.ofFloat(welcomeImage, "alpha", 0f, 1f);
        imageFadeIn.setDuration(800);

        ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(welcomeTitle, "alpha", 0f, 1f);
        titleFadeIn.setStartDelay(200); // Retraso para que aparezca después de la imagen
        titleFadeIn.setDuration(800);

        ObjectAnimator subtitleFadeIn = ObjectAnimator.ofFloat(welcomeSubtitle, "alpha", 0f, 1f);
        subtitleFadeIn.setStartDelay(400);
        subtitleFadeIn.setDuration(800);

        ObjectAnimator registerButtonFadeIn = ObjectAnimator.ofFloat(registerButton, "alpha", 0f, 1f);
        registerButtonFadeIn.setStartDelay(600);
        registerButtonFadeIn.setDuration(800);

        ObjectAnimator signInButtonFadeIn = ObjectAnimator.ofFloat(signInButton, "alpha", 0f, 1f);
        signInButtonFadeIn.setStartDelay(800);
        signInButtonFadeIn.setDuration(800);

        animatorSet.playTogether(imageFadeIn, titleFadeIn, subtitleFadeIn, registerButtonFadeIn, signInButtonFadeIn);
        animatorSet.start();

        // Implementa los listeners para tus botones aquí
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