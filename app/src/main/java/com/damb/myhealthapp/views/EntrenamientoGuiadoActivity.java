package com.damb.myhealthapp.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.databinding.ActivityEntrenamientoGuiadoBinding;
import com.damb.myhealthapp.models.EjercicioSugerido;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EntrenamientoGuiadoActivity extends AppCompatActivity {

    private ActivityEntrenamientoGuiadoBinding binding;
    private List<EjercicioSugerido> rutina;
    private int indiceActual;
    private int completados;
    private CountDownTimer timer;

    private static final int DURACION_EJERCICIO = 40;
    private static final long DURACION_DESCANSO_MS = 20000; // 20 segundos
    private static final String PREFS = "entrenamiento_prefs";
    private static final String KEY_INDICE = "indice_actual";
    private static final String KEY_COMPLETADOS = "completados";
    private String nombrePlan;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Launcher para esperar el resultado de DescansoActivity
    private ActivityResultLauncher<Intent> restActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntrenamientoGuiadoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- INICIALIZACIÓN DEL LAUNCHER ---
        restActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // El descanso terminó, avanzamos al siguiente ejercicio
                        indiceActual++;
                        if (indiceActual < rutina.size()) {
                            mostrarEjercicio();
                        } else {
                            finalizarEntrenamiento();
                        }
                    }
                });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rutina = (ArrayList<EjercicioSugerido>) getIntent().getSerializableExtra("ejercicios");
        nombrePlan = getIntent().getStringExtra("nombre_plan");
        if (rutina == null) rutina = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        int guardado = prefs.getInt(KEY_INDICE + nombrePlan, 0);
        completados = prefs.getInt(KEY_COMPLETADOS + nombrePlan, 0);
        indiceActual = getIntent().getIntExtra("indice_actual", guardado);

        binding.tituloPlanGuiado.setText(nombrePlan);

        if (rutina.isEmpty()) {
            Toast.makeText(this, "La rutina está vacía.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.workoutProgressBar.setMax(rutina.size());

        // Listener para el botón de cerrar
        binding.closeButton.setOnClickListener(v -> finish());

        // Listener para el botón principal
        binding.btnSiguienteEjercicio.setOnClickListener(v -> {
            completados++;
            if (timer != null) {
                timer.cancel(); // Detiene el cronómetro del ejercicio actual
            }

            if (indiceActual >= rutina.size() - 1) {
                finalizarEntrenamiento();
            } else {
                // Lanza la actividad de descanso
                Intent intent = new Intent(this, DescansoActivity.class);
                intent.putExtra("DURACION_DESCANSO", DURACION_DESCANSO_MS);
                restActivityLauncher.launch(intent);

                // Aplica la transición de "fade"
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        mostrarEjercicio();
    }

    private void mostrarEjercicio() {
        if (indiceActual >= rutina.size()) {
            finalizarEntrenamiento();
            return;
        }

        binding.workoutProgressBar.setProgress(indiceActual + 1);
        EjercicioSugerido ej = rutina.get(indiceActual);

        binding.nombreEjercicioGuiado.setText(ej.getNombre());
        binding.detalleEjercicioGuiado.setText(ej.getDetalle());
        binding.progresoSuperior.setText((indiceActual + 1) + "/" + rutina.size());
        binding.indicadorReps.setText(String.valueOf(ej.getRepeticiones()));
        binding.indicadorSeries.setText(ej.getSeries());

        binding.btnSiguienteEjercicio.setText("TERMINAR EJERCICIO");

        Glide.with(this)
                .load(ej.getImagenResId())
                .placeholder(R.drawable.exercise_placeholder)
                .into(binding.imagenEjercicioGuiado);

        iniciarCronometro(DURACION_EJERCICIO);
    }

    private void iniciarCronometro(int segundos) {
        if (timer != null) {
            timer.cancel();
        }
        long milisegundos = segundos * 1000L;

        timer = new CountDownTimer(milisegundos, 1000) {
            public void onTick(long millisUntilFinished) {
                String tiempoFormateado = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                binding.indicadorDescanso.setText(tiempoFormateado);
            }

            public void onFinish() {
                binding.indicadorDescanso.setText("00:00");
                // Simula un clic para pasar a la pantalla de descanso automáticamente
                binding.btnSiguienteEjercicio.performClick();
            }
        };
        timer.start();
    }

    private void finalizarEntrenamiento() {
        if (binding.workoutProgressBar != null) {
            binding.workoutProgressBar.setProgress(binding.workoutProgressBar.getMax());
        }

        guardarProgreso(100);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Date fechaActual = new Date();
            Map<String, Object> registroRutina = new HashMap<>();
            registroRutina.put("fecha", fechaActual);
            registroRutina.put("tipoRutina", nombrePlan);

            db.collection("users").document(userId).collection("registrosEjercicio")
                    .add(registroRutina)
                    .addOnSuccessListener(documentReference -> Toast.makeText(EntrenamientoGuiadoActivity.this, "¡Entrenamiento finalizado y guardado!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(EntrenamientoGuiadoActivity.this, "Error al guardar el registro.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(EntrenamientoGuiadoActivity.this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(EntrenamientoGuiadoActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        int porcentaje = rutina.isEmpty() ? 0 : (int) (100.0 * completados / rutina.size());
        guardarProgreso(porcentaje);
        if (timer != null) {
            timer.cancel();
        }
    }

    private void guardarProgreso(int porcentaje) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_INDICE + nombrePlan, indiceActual);
        editor.putInt(KEY_COMPLETADOS + nombrePlan, completados);
        editor.putInt("porcentaje_" + nombrePlan, porcentaje);
        editor.apply();
    }

    public static int obtenerProgreso(Context ctx, String nombrePlan) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getInt("porcentaje_" + nombrePlan, 0);
    }

    public static boolean hayProgreso(Context ctx, String nombrePlan) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getInt(KEY_INDICE + nombrePlan, 0) > 0;
    }
}
