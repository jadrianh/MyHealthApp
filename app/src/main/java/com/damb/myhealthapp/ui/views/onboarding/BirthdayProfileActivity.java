package com.damb.myhealthapp.ui.views.onboarding;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.ui.components.CustomDatePickerDialog;

import java.util.Calendar;
import java.util.Locale;

public class BirthdayProfileActivity extends AppCompatActivity {
    private Button openDatePickerButton, acceptButton;
    private Calendar selectedDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthdate_profile);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        openDatePickerButton = findViewById(R.id.openDatePickerButton);
        acceptButton = findViewById(R.id.acceptButton);

        openDatePickerButton.setOnClickListener(v -> {
            CustomDatePickerDialog dialog = new CustomDatePickerDialog(this, (year, month, day) -> {
                Calendar now = Calendar.getInstance();
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, day);

                if (!chosen.after(now)) {
                    selectedDate = chosen;
                    acceptButton.setEnabled(true);
                    openDatePickerButton.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
                } else {
                    Toast.makeText(this, "La fecha no puede ser futura", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });

        acceptButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                Intent intent = new Intent(BirthdayProfileActivity.this, GenderProfileActivity.class);
                long birthdayMillis = selectedDate.getTimeInMillis();
                intent.putExtra("birthday", birthdayMillis);
                Log.d(TAG, "Enviando 'birthday' a GenderProfileActivity: " + birthdayMillis); // LOG 2
                startActivity(intent);
                // finish(); // Opcional, si no quieres volver a esta pantalla
            } else {
                Toast.makeText(this, "Por favor, selecciona una fecha primero", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Botón Aceptar presionado sin fecha seleccionada."); // LOG advertencia
            }
        });
    }
}