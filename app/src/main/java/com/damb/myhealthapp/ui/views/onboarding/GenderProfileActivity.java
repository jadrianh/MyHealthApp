package com.damb.myhealthapp.ui.views.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.damb.myhealthapp.R;

public class GenderProfileActivity extends AppCompatActivity {

    private AppCompatButton maleButton;
    private AppCompatButton femaleButton;
    private Button acceptButton;
    private String selectedGender = null;
    private long birthday;
    private AppCompatButton currentlySelectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_profile);

        maleButton = findViewById(R.id.maleButton);
        femaleButton = findViewById(R.id.femaleButton);
        acceptButton = findViewById(R.id.acceptButton);


        Intent GETintent = getIntent();
        if (GETintent != null && GETintent.hasExtra("birthday")) {
            birthday = GETintent.getLongExtra("birthday", 0L);
        } else {
            Toast.makeText(this, "No se recibió la fecha de nacimiento", Toast.LENGTH_SHORT).show();
        }

        if (acceptButton != null) {
            acceptButton.setEnabled(false);
        }


        if (maleButton != null) {
            maleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectGenderButton(maleButton, "male");
                }
            });
        }

        if (femaleButton != null) {
            femaleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectGenderButton(femaleButton, "female");
                }
            });
        }

        if (acceptButton != null) {
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedGender != null) {
                        Intent intent = new Intent(GenderProfileActivity.this, HeightProfileActivity.class);
                        intent.putExtra("birthday", birthday);
                        intent.putExtra("gender", selectedGender);
                        startActivity(intent);
                    } else {
                        // This toast might be redundant if the button is disabled,
                        // but good as a fallback.
                        Toast.makeText(GenderProfileActivity.this, "Por favor, selecciona un género", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void selectGenderButton(AppCompatButton selectedButtonView, String genderValue) {
        if (currentlySelectedButton != null && currentlySelectedButton != selectedButtonView) {
            currentlySelectedButton.setSelected(false);
            currentlySelectedButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_unselected_background));
        }

        selectedButtonView.setSelected(true);
        selectedButtonView.setBackground(ContextCompat.getDrawable(this, R.drawable.button_selected_background));

        currentlySelectedButton = selectedButtonView;
        selectedGender = genderValue;

        if (acceptButton != null) {
            acceptButton.setEnabled(true);
        }
    }
}