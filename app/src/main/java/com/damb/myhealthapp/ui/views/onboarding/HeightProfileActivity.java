package com.damb.myhealthapp.ui.views.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;

public class HeightProfileActivity extends AppCompatActivity {

    private NumberPicker heightPicker;
    private TextView selectedHeightText;
    private Button submitButton;
    private int selectedHeight = 172;
    private long birthday;
    private String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_profile);

        Intent GETintent = getIntent();
        birthday = GETintent.getLongExtra("birthday", 0L);
        gender = GETintent.getStringExtra("gender");

        heightPicker = findViewById(R.id.heightPicker);
        selectedHeightText = findViewById(R.id.selectedHeightText);
        submitButton = findViewById(R.id.submitButton);

        // Configurar NumberPicker
        heightPicker.setMinValue(140);
        heightPicker.setMaxValue(210);
        heightPicker.setValue(selectedHeight);
        heightPicker.setWrapSelectorWheel(false);

        // Actualizar el texto grande cuando cambia
        heightPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedHeight = newVal;
            selectedHeightText.setText(newVal + " cm");
            submitButton.setEnabled(true);
        });

        // Acción del botón
        submitButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, WeightProfileActivity.class);

            // Aquí puedes pasar height y gender a la siguiente pantalla
            intent.putExtra("birthday", birthday);
            intent.putExtra("gender", gender);
            intent.putExtra("height", selectedHeight);

            startActivity(intent);
        });
    }
}
