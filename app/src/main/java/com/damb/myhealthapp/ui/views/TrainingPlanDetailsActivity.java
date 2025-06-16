package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.ui.adapters.SuggestedExercisesAdapter;
import com.damb.myhealthapp.databinding.ActivityTrainingPlanDetailsBinding;
import com.damb.myhealthapp.models.SuggestedExcercise;
import java.util.ArrayList;
import java.util.List;

public class TrainingPlanDetailsActivity extends AppCompatActivity {

    private ActivityTrainingPlanDetailsBinding binding;
    private List<SuggestedExcercise> rutina;
    private String nombrePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrainingPlanDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> finish());

        nombrePlan = getIntent().getStringExtra("nombre_ejercicio");
        if (nombrePlan == null) nombrePlan = "";

        rutina = new ArrayList<>();
        configurarPlan(nombrePlan);

        binding.recyclerEjercicios.setLayoutManager(new LinearLayoutManager(this));
        SuggestedExercisesAdapter adapter = new SuggestedExercisesAdapter(rutina);
        binding.recyclerEjercicios.setAdapter(adapter);

        binding.btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrenamientoGuiadoActivity.class);
            intent.putExtra("ejercicios", new ArrayList<>(rutina));
            intent.putExtra("nombre_plan", nombrePlan);
            intent.putExtra("indice_actual", 0);
            startActivity(intent);
        });
    }

    private void configurarPlan(String nombre) {
        String detalle = "";
        // NOTA: Debes añadir tus imágenes (ej: jumping_jacks.gif) a la carpeta res/drawable
        int placeholder = R.drawable.exercise_placeholder;

        switch (nombre) {
            case "Pérdida de Peso (Quema de Grasa)":
                detalle = "Pérdida de Peso (Quema de Grasa)\n" +
                        "Frecuencia: 5-6 días/semana\n" +
                        "Ejercicios Típicos: HIIT, cardio, circuitos full body, entrenamiento de resistencia.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                // Formato: (nombre, reps, series, detalle, R.drawable.tu_imagen)
                rutina.add(new SuggestedExcercise("Jumping Jacks", 30, "3", "segundos", R.drawable.squats));
                rutina.add(new SuggestedExcercise("Burpees", 15, "3", "", R.drawable.burpees));
                rutina.add(new SuggestedExcercise("Mountain Climbers", 20, "3", "por pierna", R.drawable.squats));
                rutina.add(new SuggestedExcercise("Sentadillas con salto", 15, "3", "", R.drawable.burpees));
                rutina.add(new SuggestedExcercise("Plancha", 40, "3", "segundos", R.drawable.squats));
                rutina.add(new SuggestedExcercise("HIIT 30/30", 4, "4", "rondas", R.drawable.burpees));
                rutina.add(new SuggestedExcercise("Cuerda o trote", 5, "1", "minutos", R.drawable.squats));
                rutina.add(new SuggestedExcercise("Crunch abdominal", 20, "3", "", R.drawable.burpees));
                break;

            case "Ganancia de Masa Muscular (Hipertrofia)":
                detalle = "Ganancia de Masa Muscular (Hipertrofia)\n" +
                        "Frecuencia: 4-5 días/semana\n" +
                        "Ejercicios Típicos: Rutinas divididas por grupo muscular (push-pull-legs, torso-pierna).\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new SuggestedExcercise("Press de banca", 10, "4", "Peso: 50kg", placeholder));
                rutina.add(new SuggestedExcercise("Sentadilla con barra", 12, "4", "Peso: 60kg", placeholder));
                rutina.add(new SuggestedExcercise("Peso muerto", 10, "4", "Peso: 80kg", placeholder));
                rutina.add(new SuggestedExcercise("Remo con barra", 12, "4", "Peso: 40kg", placeholder));
                rutina.add(new SuggestedExcercise("Press militar", 10, "4", "Peso: 30kg", placeholder));
                rutina.add(new SuggestedExcercise("Curl de bíceps", 15, "3", "Peso: 10kg", placeholder));
                rutina.add(new SuggestedExcercise("Fondos de tríceps", 12, "3", "Peso corporal", placeholder));
                rutina.add(new SuggestedExcercise("Elevaciones laterales", 15, "3", "Peso: 5kg", placeholder));
                break;

            case "Flexibilidad y Movilidad":
                detalle = "Flexibilidad y Movilidada\n" +
                        "Frecuencia: 3-4 días/semana\n" +
                        "Ejercicios Típicos: Estiramientos, yoga, movilidad articular.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new SuggestedExcercise("Estiramiento de cuello", 30, "2", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Estiramiento de hombros", 30, "2", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Estiramiento de espalda", 30, "2", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Estiramiento de piernas", 30, "2", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Movilidad articular", 10, "2", "repeticiones", placeholder));
                rutina.add(new SuggestedExcercise("Postura del niño (yoga)", 30, "2", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Gato-camello (yoga)", 15, "2", "", placeholder));
                rutina.add(new SuggestedExcercise("Rotaciones de tobillo", 20, "2", "", placeholder));
                break;

            case "Salud General y Bienestar":
                detalle = "Salud General y Bienestar\n" +
                        "Frecuencia: 3-5 días/semana\n" +
                        "Ejercicios Típicos: Caminata, estiramientos, ejercicios básicos funcionales.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new SuggestedExcercise("Caminata", 20, "1", "minutos", placeholder));
                rutina.add(new SuggestedExcercise("Sentadillas", 15, "3", "Peso corporal", placeholder));
                rutina.add(new SuggestedExcercise("Flexiones de pared", 12, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Elevación de talones", 15, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Estiramiento de brazos", 30, "2", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Marcha en el sitio", 1, "3", "minuto", placeholder));
                rutina.add(new SuggestedExcercise("Plancha", 20, "3", "segundos", placeholder));
                break;

            case "Resistencia y Cardio":
                detalle = "Resistencia y Cardio\n" +
                        "Frecuencia: 4-6 días/semana\n" +
                        "Ejercicios Típicos: HIIT, Tabata, trote, saltos, escaleras.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new SuggestedExcercise("Trote", 15, "1", "minutos", placeholder));
                rutina.add(new SuggestedExcercise("Saltos de tijera", 30, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Burpees", 15, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Escalera", 1, "3", "minuto", placeholder));
                rutina.add(new SuggestedExcercise("Mountain Climbers", 20, "3", "por pierna", placeholder));
                rutina.add(new SuggestedExcercise("Tabata (4 min)", 8, "1", "rondas 20/10 seg", placeholder));
                rutina.add(new SuggestedExcercise("Plancha", 40, "3", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Cuerda", 5, "1", "minutos", placeholder));
                break;

            case "Entrenamiento Funcional":
                detalle = "Entrenamiento Funcional\n" +
                        "Frecuencia: 4-5 días/semana\n" +
                        "Ejercicios Típicos: Burpees, sentadillas, planchas, saltos, movimientos compuestos.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new SuggestedExcercise("Burpees", 15, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Sentadillas", 12, "4", "Peso corporal", placeholder));
                rutina.add(new SuggestedExcercise("Plancha lateral", 30, "3", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Zancadas", 12, "3", "por pierna", placeholder));
                rutina.add(new SuggestedExcercise("Flexiones", 15, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Saltos laterales", 20, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Remo invertido", 12, "3", "Peso corporal", placeholder));
                rutina.add(new SuggestedExcercise("Crunch abdominal", 20, "3", "", placeholder));
                break;

            case "Plan Rápido para Tonificación":
                detalle = "Plan Rápido para Tonificación\n" +
                        "Frecuencia: 5-6 días/semana\n" +
                        "Ejercicios Típicos: Series intensas de core, piernas, glúteos, brazos.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new SuggestedExcercise("Sentadillas", 15, "4", "", placeholder));
                rutina.add(new SuggestedExcercise("Zancadas", 12, "3", "por pierna", placeholder));
                rutina.add(new SuggestedExcercise("Puente de glúteos", 15, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Flexiones", 12, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Plancha", 30, "3", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Crunch abdominal", 20, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Elevaciones laterales", 15, "3", "Peso: 5kg", placeholder));
                rutina.add(new SuggestedExcercise("Saltos de tijera", 30, "3", "", placeholder));
                break;

            case "Plan para Principiantes":
                detalle = "Plan para Principiantes\n" +
                        "Frecuencia: 3 días/semana\n" +
                        "Ejercicios Típicos: Sentadillas, flexiones modificadas, abdominales básicos.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new SuggestedExcercise("Sentadillas", 12, "3", "Peso corporal", placeholder));
                rutina.add(new SuggestedExcercise("Flexiones de rodillas", 10, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Puente de glúteos", 12, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Marcha en el sitio", 1, "3", "minuto", placeholder));
                rutina.add(new SuggestedExcercise("Plancha", 20, "3", "segundos", placeholder));
                rutina.add(new SuggestedExcercise("Elevación de talones", 15, "3", "", placeholder));
                rutina.add(new SuggestedExcercise("Crunch abdominal", 15, "3", "", placeholder));
                break;

            default:
                detalle = "No hay información disponible para este tipo de ejercicio.";
                break;
        }

        binding.infoDetalle.setText(detalle);
    }
}