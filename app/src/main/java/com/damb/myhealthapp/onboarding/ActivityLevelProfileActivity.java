package com.damb.myhealthapp.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.views.RegisterActivity;

public class ActivityLevelProfileActivity extends AppCompatActivity {

    private RadioGroup activityLevelGroup;
    private Button submitButton;
    private String selectedLevel = null;
    private long birthday;
    private String gender;
    private int height;
    private int weight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_level_profile);

        Intent GETintent = getIntent();
        birthday = GETintent.getLongExtra("birthday", 0L); // <- clave corregida
        gender = GETintent.getStringExtra("gender");
        height = GETintent.getIntExtra("height", 0);
        weight = GETintent.getIntExtra("weight", 0);

        activityLevelGroup = findViewById(R.id.activityLevelGroup);
        submitButton = findViewById(R.id.submitButton);

        activityLevelGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.veryActiveButton) {
                selectedLevel = "very_active";
                submitButton.setEnabled(true);
            } else if (checkedId == R.id.moderatelyActiveButton) {
                selectedLevel = "moderately_active";
                submitButton.setEnabled(true);
            } else if (checkedId == R.id.lightlyActiveButton) {
                selectedLevel = "lightly_active";
                submitButton.setEnabled(true);
            } else if (checkedId == R.id.sedentaryButton) {
                selectedLevel = "sedentary";
                submitButton.setEnabled(true);
            }
        });

        submitButton.setOnClickListener(v -> {
            if (selectedLevel != null) {
                Intent intent = new Intent(this, RegisterActivity.class);

                // Pasar TODOS los datos al intent
                intent.putExtra("birthday", birthday);
                intent.putExtra("gender", gender);
                intent.putExtra("height", height);
                intent.putExtra("weight", weight);
                intent.putExtra("activity_level", selectedLevel);

                startActivity(intent);
            } else {
                Toast.makeText(this, "Por favor, selecciona un nivel de actividad", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

