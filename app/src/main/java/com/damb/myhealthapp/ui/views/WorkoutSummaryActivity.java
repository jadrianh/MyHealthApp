package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorkoutSummaryActivity extends AppCompatActivity {
    private static final String TAG = "WorkoutSummaryActivity";
    private TextView tvWorkoutName, tvDuration, tvCalories;
    private Button btnFinish;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando WorkoutSummaryActivity");
        
        // Asegurarse de que la actividad se muestre por encima de otras
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | 
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        
        setContentView(R.layout.activity_workout_summary);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        tvWorkoutName = findViewById(R.id.tvWorkoutName);
        tvDuration = findViewById(R.id.tvDuration);
        tvCalories = findViewById(R.id.tvCalories);
        btnFinish = findViewById(R.id.btnFinish);

        // Obtener datos del intent
        Intent intent = getIntent();
        if (intent != null) {
            String workoutName = intent.getStringExtra("workout_name");
            long duration = intent.getLongExtra("duration", 0);
            int calories = intent.getIntExtra("calories", 0);

            Log.d(TAG, "Datos recibidos - Nombre: " + workoutName + 
                      ", Duración: " + duration + 
                      ", Calorías: " + calories);

            // Mostrar datos
            if (workoutName != null) {
                tvWorkoutName.setText(workoutName);
            }
            tvDuration.setText(formatDuration(duration));
            tvCalories.setText(String.valueOf(calories));

            // Guardar el registro en Firestore
            saveWorkoutRecord(workoutName, duration, calories);
        } else {
            Log.e(TAG, "Intent es null");
            Toast.makeText(this, "Error al cargar los datos del entrenamiento", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar botón de finalizar
        btnFinish.setOnClickListener(v -> {
            Log.d(TAG, "Botón finalizar presionado");
            Intent mainIntent = new Intent(WorkoutSummaryActivity.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Actividad iniciada");
        // Asegurarse de que la actividad esté en primer plano
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Actividad resumida");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Actividad pausada");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Actividad detenida");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Actividad destruida");
    }

    private String formatDuration(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    private void saveWorkoutRecord(String workoutName, long duration, int calories) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            Map<String, Object> workoutRecord = new HashMap<>();
            workoutRecord.put("fecha", new Date());
            workoutRecord.put("tipoRutina", workoutName);
            workoutRecord.put("duracion", duration);
            workoutRecord.put("calorias", calories);

            db.collection("users").document(userId)
                    .collection("registrosEjercicio").document(todayDate)
                    .collection("workouts").add(workoutRecord)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Registro guardado exitosamente");
                        // Marcar el día con hasWorkout: true
                        db.collection("users").document(userId)
                                .collection("registrosEjercicio").document(todayDate)
                                .set(new HashMap<String, Object>() {{ put("hasWorkout", true); }}, com.google.firebase.firestore.SetOptions.merge());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al guardar el registro: " + e.getMessage());
                    });
        }
    }
} 