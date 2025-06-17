package com.damb.myhealthapp.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.databinding.ActivityEntrenamientoGuiadoBinding;
import com.damb.myhealthapp.models.SuggestedExcercise;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EntrenamientoGuiadoActivity extends AppCompatActivity {

    private ActivityEntrenamientoGuiadoBinding binding;
    private List<SuggestedExcercise> rutina;
    private int indiceActual;
    private int completados;
    private CountDownTimer timer;
    private long tiempoInicio;

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

        // Inicializar el tiempo de inicio
        tiempoInicio = System.currentTimeMillis();

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

        rutina = (ArrayList<SuggestedExcercise>) getIntent().getSerializableExtra("ejercicios");
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
        SuggestedExcercise ej = rutina.get(indiceActual);

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

        // Calcular la duración total del entrenamiento
        long duracionTotal = (System.currentTimeMillis() - tiempoInicio);
        
        // Calcular calorías y esperar el resultado
        calcularCaloriasQuemadas(duracionTotal, calorias -> {
            Log.d("EntrenamientoGuiado", "Calorías calculadas: " + calorias);
            Log.d("EntrenamientoGuiado", "Duración total: " + duracionTotal);
            Log.d("EntrenamientoGuiado", "Nombre del plan: " + nombrePlan);

            // Redirigir a la vista de resumen solo después de calcular las calorías
            Intent intent = new Intent(EntrenamientoGuiadoActivity.this, WorkoutSummaryActivity.class);
            intent.putExtra("workout_name", nombrePlan);
            intent.putExtra("duration", duracionTotal);
            intent.putExtra("calories", calorias);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                          Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                          Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            // Asegurarnos de que la actividad actual se cierre correctamente
            finish();
            
            // Iniciar la nueva actividad
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    @Override
    public void onBackPressed() {
        // Prevenir que el usuario regrese durante el entrenamiento
        if (indiceActual < rutina.size()) {
            Toast.makeText(this, "No puedes salir durante el entrenamiento", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    private void calcularCaloriasQuemadas(long duracionMs, OnCaloriasCalculadasListener listener) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            listener.onCaloriasCalculadas(0);
            return;
        }

        // Obtener datos del usuario de Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> onboardingData = (Map<String, Object>) documentSnapshot.get("onboardingData");
                        if (onboardingData != null) {
                            Object weightObj = onboardingData.get("weight");
                            Object birthdayObj = onboardingData.get("birthday");
                            Object genderObj = onboardingData.get("gender");
                            if (weightObj != null && birthdayObj != null && genderObj != null) {
                                double peso = Double.parseDouble(weightObj.toString());
                                long birthdayMillis = Long.parseLong(birthdayObj.toString());
                                String genero = genderObj.toString();

                                // Calcular la edad a partir del timestamp de cumpleaños
                                java.util.Calendar birthCal = java.util.Calendar.getInstance();
                                birthCal.setTimeInMillis(birthdayMillis);
                                java.util.Calendar today = java.util.Calendar.getInstance();
                                int edad = today.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR);
                                if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                                    edad--;
                                }

                                // Calcular calorías sumando cada ejercicio
                                double totalCalorias = 0;
                                for (SuggestedExcercise ejercicio : rutina) {
                                    double duracionEjercicioHoras = ejercicio.getDuracionSegundos() / 3600.0;
                                    totalCalorias += ejercicio.getMet() * peso * duracionEjercicioHoras;
                                }
                                int calorias = (int) Math.round(totalCalorias);
                                // Notificar el resultado
                                listener.onCaloriasCalculadas(calorias);
                            } else {
                                Toast.makeText(this, "Faltan datos de usuario para calcular calorías.", Toast.LENGTH_LONG).show();
                                listener.onCaloriasCalculadas(0);
                            }
                        } else {
                            Toast.makeText(this, "No se encontró información de usuario.", Toast.LENGTH_LONG).show();
                            listener.onCaloriasCalculadas(0);
                        }
                    } else {
                        Toast.makeText(this, "No se encontró el usuario en la base de datos.", Toast.LENGTH_LONG).show();
                        listener.onCaloriasCalculadas(0);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener datos del usuario.", Toast.LENGTH_LONG).show();
                    listener.onCaloriasCalculadas(0);
                });
    }

    // Interfaz para el callback de calorías
    private interface OnCaloriasCalculadasListener {
        void onCaloriasCalculadas(int calorias);
    }

    private double calcularMET(String tipoEjercicio) {
        // Valores MET aproximados para diferentes tipos de ejercicio
        switch (tipoEjercicio) {
            case "Pérdida de Peso (Quema de Grasa)":
                return 8.0; // HIIT y cardio intenso
            case "Ganancia de Masa Muscular (Hipertrofia)":
                return 6.0; // Entrenamiento de fuerza
            case "Flexibilidad y Movilidad":
                return 2.5; // Yoga y estiramientos
            case "Salud General y Bienestar":
                return 4.0; // Ejercicio moderado
            case "Resistencia y Cardio":
                return 7.0; // Cardio moderado
            case "Entrenamiento Funcional":
                return 7.5; // Ejercicio funcional intenso
            case "Plan Rápido para Tonificación":
                return 6.5; // Circuito de tonificación
            case "Plan para Principiantes":
                return 3.5; // Ejercicio suave
            default:
                return 5.0; // Valor por defecto
        }
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
