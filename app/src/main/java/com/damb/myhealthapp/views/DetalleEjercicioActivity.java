package com.damb.myhealthapp.views;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.adapters.EjerciciosSugeridosAdapter;
import com.damb.myhealthapp.models.EjercicioSugerido;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;

public class DetalleEjercicioActivity extends AppCompatActivity {
    private static final int REQ_ENTRENAMIENTO = 1001;
    private List<EjercicioSugerido> rutina;
    private TextView titulo;
    private String nombrePlan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_ejercicio);



        titulo = findViewById(R.id.tituloDetalle);
        TextView info = findViewById(R.id.infoDetalle);
        RecyclerView recyclerEjercicios = findViewById(R.id.recyclerEjercicios);
        Button btnComenzar = findViewById(R.id.btnComenzar);

        ImageView btnRegresar = findViewById(R.id.btnBack);
        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetalleEjercicioActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



        String nombre = getIntent().getStringExtra("nombre_ejercicio");
        if (nombre == null) nombre = "";
        nombrePlan = nombre;
        String detalle = "";
        rutina = new ArrayList<>();
        int ic_run = R.drawable.ic_launcher_foreground;
        int ic_fitness = R.drawable.ic_launcher_background;
        int ic_strength = R.drawable.ic_launcher_foreground;
        int ic_timer = R.drawable.ic_launcher_background;
        int ic_list = R.drawable.ic_launcher_foreground;
        int ic_bike = R.drawable.ic_launcher_background;
        int ic_walk = R.drawable.ic_launcher_foreground;
        switch (nombre) {
            case "Pérdida de Peso (Quema de Grasa)":
                detalle = "Pérdida de Peso (Quema de Grasa)\n" +
                        "Frecuencia: 5-6 días/semana\n" +
                        "Ejercicios Típicos: HIIT, cardio, circuitos full body, entrenamiento de resistencia.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Jumping Jacks", "3 x 30", ic_run));
                rutina.add(new EjercicioSugerido("Burpees", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Mountain Climbers", "3 x 20", ic_run));
                rutina.add(new EjercicioSugerido("Sentadillas con salto", "3 x 15", ic_strength));
                rutina.add(new EjercicioSugerido("Plancha", "3 x 40 seg", ic_timer));
                rutina.add(new EjercicioSugerido("HIIT 30/30", "4 rondas", ic_list));
                rutina.add(new EjercicioSugerido("Cuerda o trote", "5 min", ic_run));
                rutina.add(new EjercicioSugerido("Crunch abdominal", "3 x 20", ic_fitness));
                break;
            case "Ganancia de Masa Muscular (Hipertrofia)":
                detalle = "Ganancia de Masa Muscular (Hipertrofia)\n" +
                        "Frecuencia: 4-5 días/semana\n" +
                        "Ejercicios Típicos: Rutinas divididas por grupo muscular (push-pull-legs, torso-pierna).\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Press de banca", "4 x 10", ic_strength));
                rutina.add(new EjercicioSugerido("Sentadilla", "4 x 12", ic_strength));
                rutina.add(new EjercicioSugerido("Peso muerto", "4 x 10", ic_strength));
                rutina.add(new EjercicioSugerido("Remo con barra", "4 x 12", ic_strength));
                rutina.add(new EjercicioSugerido("Press militar", "4 x 10", ic_strength));
                rutina.add(new EjercicioSugerido("Curl de bíceps", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Fondos de tríceps", "3 x 12", ic_fitness));
                rutina.add(new EjercicioSugerido("Elevaciones laterales", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Crunch abdominal", "3 x 20", ic_fitness));
                break;
            case "Flexibilidad y Movilidad":
                detalle = "Flexibilidad y Movilidada\n" +
                        "Frecuencia: 3-4 días/semana\n" +
                        "Ejercicios Típicos: Estiramientos, yoga, movilidad articular.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Estiramiento de cuello", "2 x 30 seg", ic_walk));
                rutina.add(new EjercicioSugerido("Estiramiento de hombros", "2 x 30 seg", ic_walk));
                rutina.add(new EjercicioSugerido("Estiramiento de espalda", "2 x 30 seg", ic_walk));
                rutina.add(new EjercicioSugerido("Estiramiento de piernas", "2 x 30 seg", ic_walk));
                rutina.add(new EjercicioSugerido("Movilidad articular", "2 x 10 rep", ic_list));
                rutina.add(new EjercicioSugerido("Postura del niño (yoga)", "2 x 30 seg", ic_fitness));
                rutina.add(new EjercicioSugerido("Gato-camello (yoga)", "2 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Rotaciones de tobillo", "2 x 20", ic_list));
                break;
            case "Salud General y Bienestar":
                detalle = "Salud General y Bienestar\n" +
                        "Frecuencia: 3-5 días/semana\n" +
                        "Ejercicios Típicos: Caminata, estiramientos, ejercicios básicos funcionales.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Caminata", "20 min", ic_walk));
                rutina.add(new EjercicioSugerido("Sentadillas", "3 x 15", ic_strength));
                rutina.add(new EjercicioSugerido("Flexiones de pared", "3 x 12", ic_fitness));
                rutina.add(new EjercicioSugerido("Elevación de talones", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Estiramiento de brazos", "2 x 30 seg", ic_walk));
                rutina.add(new EjercicioSugerido("Marcha en el sitio", "3 x 1 min", ic_run));
                rutina.add(new EjercicioSugerido("Plancha", "3 x 20 seg", ic_timer));
                break;
            case "Resistencia y Cardio":
                detalle = "Resistencia y Cardio\n" +
                        "Frecuencia: 4-6 días/semana\n" +
                        "Ejercicios Típicos: HIIT, Tabata, trote, saltos, escaleras.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Trote", "15 min", ic_run));
                rutina.add(new EjercicioSugerido("Saltos de tijera", "3 x 30", ic_run));
                rutina.add(new EjercicioSugerido("Burpees", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Escalera", "3 x 1 min", ic_bike));
                rutina.add(new EjercicioSugerido("Mountain Climbers", "3 x 20", ic_run));
                rutina.add(new EjercicioSugerido("Tabata (4 min)", "8 x 20/10 seg", ic_timer));
                rutina.add(new EjercicioSugerido("Plancha", "3 x 40 seg", ic_timer));
                rutina.add(new EjercicioSugerido("Cuerda", "5 min", ic_run));
                break;
            case "Entrenamiento Funcional":
                detalle = "Entrenamiento Funcional\n" +
                        "Frecuencia: 4-5 días/semana\n" +
                        "Ejercicios Típicos: Burpees, sentadillas, planchas, saltos, movimientos compuestos.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Burpees", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Sentadillas", "4 x 12", ic_strength));
                rutina.add(new EjercicioSugerido("Plancha lateral", "3 x 30 seg", ic_timer));
                rutina.add(new EjercicioSugerido("Zancadas", "3 x 12", ic_strength));
                rutina.add(new EjercicioSugerido("Flexiones", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Saltos laterales", "3 x 20", ic_run));
                rutina.add(new EjercicioSugerido("Remo invertido", "3 x 12", ic_strength));
                rutina.add(new EjercicioSugerido("Crunch abdominal", "3 x 20", ic_fitness));
                break;
            case "Plan Rápido para Tonificación":
                detalle = "Plan Rápido para Tonificación\n" +
                        "Frecuencia: 5-6 días/semana\n" +
                        "Ejercicios Típicos: Series intensas de core, piernas, glúteos, brazos.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Sentadillas", "4 x 15", ic_strength));
                rutina.add(new EjercicioSugerido("Zancadas", "3 x 12", ic_strength));
                rutina.add(new EjercicioSugerido("Puente de glúteos", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Flexiones", "3 x 12", ic_fitness));
                rutina.add(new EjercicioSugerido("Plancha", "3 x 30 seg", ic_timer));
                rutina.add(new EjercicioSugerido("Crunch abdominal", "3 x 20", ic_fitness));
                rutina.add(new EjercicioSugerido("Elevaciones laterales", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Saltos de tijera", "3 x 30", ic_run));
                break;
            case "Plan para Principiantes":
                detalle = "Plan para Principiantes\n" +
                        "Frecuencia: 3 días/semana\n" +
                        "Ejercicios Típicos: Sentadillas, flexiones modificadas, abdominales básicos.\n" +
                        "🔥 Calorías: 1880 kca ⏱️ Duración: 540 min";
                rutina.add(new EjercicioSugerido("Sentadillas", "3 x 12", ic_strength));
                rutina.add(new EjercicioSugerido("Flexiones de rodillas", "3 x 10", ic_fitness));
                rutina.add(new EjercicioSugerido("Puente de glúteos", "3 x 12", ic_fitness));
                rutina.add(new EjercicioSugerido("Marcha en el sitio", "3 x 1 min", ic_run));
                rutina.add(new EjercicioSugerido("Plancha", "3 x 20 seg", ic_timer));
                rutina.add(new EjercicioSugerido("Elevación de talones", "3 x 15", ic_fitness));
                rutina.add(new EjercicioSugerido("Crunch abdominal", "3 x 15", ic_fitness));
                break;
            default:
                detalle = "No hay información disponible para este tipo de ejercicio.";
        }
        info.setText(detalle);

        recyclerEjercicios.setLayoutManager(new LinearLayoutManager(this));
        EjerciciosSugeridosAdapter adapter = new EjerciciosSugeridosAdapter(rutina);
        recyclerEjercicios.setAdapter(adapter);

        btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EntrenamientoGuiadoActivity.class);
            intent.putExtra("ejercicios", new ArrayList<>(rutina));
            intent.putExtra("nombre_plan", nombrePlan);
            intent.putExtra("indice_actual", 0);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ENTRENAMIENTO) {
            // Eliminada la llamada a MainActivity.setViewPagerEnabled(true);
            // MainActivity habilitará el ViewPager en su onResume()
        }
    }
} 