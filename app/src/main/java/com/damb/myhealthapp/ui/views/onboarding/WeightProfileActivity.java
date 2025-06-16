package com.damb.myhealthapp.ui.views.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;

public class WeightProfileActivity extends AppCompatActivity {

    private NumberPicker weightPicker;
    private TextView selectedWeightText;
    private Button submitButton;
    int selectedWeight = 70;
    private long birthday;
    private String gender;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_profile);

        Intent GETintent = getIntent();
        birthday = GETintent.getLongExtra("birthday", 0L); // <- corregida la key
        gender = GETintent.getStringExtra("gender");
        height = GETintent.getIntExtra("height", 172);

        weightPicker = findViewById(R.id.weightPicker);
        selectedWeightText = findViewById(R.id.selectedWeightText);
        submitButton = findViewById(R.id.submitButton);

        weightPicker.setMinValue(30);
        weightPicker.setMaxValue(200);
        weightPicker.setValue(70);

        selectedWeightText.setText(weightPicker.getValue() + " kg");

        weightPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedWeight = newVal;
            selectedWeightText.setText(newVal + " kg");
            submitButton.setEnabled(true);
        });

        submitButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityLevelProfileActivity.class);

            // pasar datos a la siguiente pantalla
            intent.putExtra("birthday", birthday);
            intent.putExtra("gender", gender);
            intent.putExtra("height", height);
            intent.putExtra("weight", selectedWeight);

            startActivity(intent);
        });
    }
}
