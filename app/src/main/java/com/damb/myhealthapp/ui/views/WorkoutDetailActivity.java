package com.damb.myhealthapp.ui.views;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkoutDetailActivity extends AppCompatActivity {
    // Vistas para la lógica original
    private TextView tvWorkoutName, tvDuration, tvCalories;
    private Button btnFinish;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Vistas para la animación
    private TextView tvCongratulations, tvWorkoutCompleted;
    private View cardSummary;
    private LottieAnimationView lottieConfetti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        tvWorkoutName = findViewById(R.id.tvWorkoutName);
        tvDuration = findViewById(R.id.tvDuration);
        tvCalories = findViewById(R.id.tvCalories);
        btnFinish = findViewById(R.id.btnFinish);
        tvCongratulations = findViewById(R.id.tvCongratulations);
        tvWorkoutCompleted = findViewById(R.id.tvWorkoutCompleted);
        cardSummary = findViewById(R.id.cardSummary);
        lottieConfetti = findViewById(R.id.lottieConfetti);

        // Lógica original de esta actividad (leer datos)
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
                    .addOnSuccessListener(this::mostrarDetalle)
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar detalle", Toast.LENGTH_SHORT).show());
        }

        // Lógica original del botón
        btnFinish.setOnClickListener(v -> finish());

        // Iniciar animaciones
        startAnimations();
    }

    private void mostrarDetalle(DocumentSnapshot doc) {
        if (doc.exists()) {
            long duracion = doc.contains("duracion") ? doc.getLong("duracion") : 0;
            int calorias = doc.contains("calorias") ? doc.getLong("calorias").intValue() : 0;
            tvDuration.setText(formatDuration(duracion));
            tvCalories.setText(String.valueOf(calorias));
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

    // Funciones de animación
    private void startAnimations() {
        animateView(tvCongratulations, 1500);
        animateView(tvWorkoutCompleted, 1700);
        animateView(cardSummary, 1900);
        animateView(btnFinish, 2100);
        new Handler(Looper.getMainLooper()).postDelayed(this::showConfetti, 1000);
    }

    private void animateView(View view, long delay) {
        view.setAlpha(1f);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        animation.setStartOffset(delay);
        view.startAnimation(animation);
    }

    private void showConfetti() {
        lottieConfetti.playAnimation();
    }
}