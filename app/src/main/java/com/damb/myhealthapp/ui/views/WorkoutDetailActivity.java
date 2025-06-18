package com.damb.myhealthapp.ui.views;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.damb.myhealthapp.ui.views.CaloriasProgressView;

public class WorkoutDetailActivity extends AppCompatActivity {
    private TextView tvWorkoutName, tvDuration, tvCalories;
    private Button btnFinish;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CaloriasProgressView progressCalorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvWorkoutName = findViewById(R.id.tvWorkoutName);
        tvDuration = findViewById(R.id.tvDuration);
        tvCalories = findViewById(R.id.tvCalories);
        btnFinish = findViewById(R.id.btnFinish);
        progressCalorias = findViewById(R.id.progressCalorias);

        String rutinaId = getIntent().getStringExtra("rutina_id");
        String nombreRutina = getIntent().getStringExtra("nombre_rutina");
        tvWorkoutName.setText(nombreRutina);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && rutinaId != null) {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            db.collection("users").document(user.getUid())
                .collection("registrosEjercicio").document(todayDate)
                .collection("workouts").document(rutinaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> mostrarDetalle(documentSnapshot))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar detalle", Toast.LENGTH_SHORT).show());
        }

        btnFinish.setOnClickListener(v -> finish());
    }

    private void mostrarDetalle(DocumentSnapshot doc) {
        if (doc.exists()) {
            long duracion = doc.contains("duracion") ? doc.getLong("duracion") : 0;
            int calorias = doc.contains("calorias") ? doc.getLong("calorias").intValue() : 0;
            tvDuration.setText(formatDuration(duracion));
            tvCalories.setText(String.valueOf(calorias));
            progressCalorias.setCalorias(calorias);
        }
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
} 