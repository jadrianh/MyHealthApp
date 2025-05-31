package com.damb.myhealthapp.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.damb.myhealthapp.R;

public class GenderProfileActivity extends AppCompatActivity {

    private Button maleButton;
    private Button femaleButton;
    private Button acceptButton;
    private String selectedGender = null;
    private long birthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_profile);

        Intent GETintent = getIntent();
        if (GETintent != null && GETintent.hasExtra("birthday")) {
            birthday = GETintent.getLongExtra("birthday", 0L);
        } else {
            // Manejar el caso donde no se recibió la fecha de nacimiento
            Toast.makeText(this, "No se recibió la fecha de nacimiento", Toast.LENGTH_SHORT).show();
            // Puedes decidir qué hacer aquí: finalizar la actividad, usar una fecha por defecto, etc.
        }

        maleButton = findViewById(R.id.maleButton);
        femaleButton = findViewById(R.id.femaleButton);
        acceptButton = findViewById(R.id.acceptButton);

        // Inicialmente, el botón Aceptar debe estar deshabilitado
        acceptButton.setEnabled(false);

        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGender("Masculino");
            }
        });

        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGender("Femenino");
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedGender != null) {
                    Intent intent = new Intent(GenderProfileActivity.this, HeightProfileActivity.class);
                    intent.putExtra("birthday", birthday);
                    intent.putExtra("gender", selectedGender);
                    startActivity(intent);
                } else {
                    Toast.makeText(GenderProfileActivity.this, "Por favor, selecciona un género", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void selectGender(String gender) {
        selectedGender = gender;
        acceptButton.setEnabled(true);

        // Resetea los estilos de los botones
        resetGenderButtonStyles();

        // Aplica estilos al botón seleccionado
        if ("Masculino".equals(gender)) {
            applySelectedStyle(maleButton);
            applyDisabledStyle(femaleButton);
        } else if ("Femenino".equals(gender)) {
            applySelectedStyle(femaleButton);
            applyDisabledStyle(maleButton);
        }
    }

    private void resetGenderButtonStyles() {
        maleButton.setBackgroundResource(R.drawable.gender_button_background);
        femaleButton.setBackgroundResource(R.drawable.gender_button_background);
        maleButton.setTextColor(ContextCompat.getColor(this, android.R.color.black)); // Color por defecto
        femaleButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void applySelectedStyle(Button button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void applyDisabledStyle(Button button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        button.setTextColor(ContextCompat.getColor(this, android.R.color.secondary_text_dark));
    }
}