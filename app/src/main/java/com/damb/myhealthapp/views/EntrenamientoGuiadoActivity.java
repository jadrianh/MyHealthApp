package com.damb.myhealthapp.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.models.EjercicioSugerido;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntrenamientoGuiadoActivity extends AppCompatActivity {
    private List<EjercicioSugerido> rutina;
    private int indiceActual;
    private int completados;
    private TextView nombre, detalleTextView, contador, titulo;
    private ImageView imagen;
    private Button btnSiguiente;
    private CountDownTimer timer;
    private boolean enDescanso = false;
    private static final int DURACION_EJERCICIO = 40; // segundos
    private static final int DURACION_DESCANSO = 20; // segundos
    private static final String PREFS = "entrenamiento_prefs";
    private static final String KEY_INDICE = "indice_actual";
    private static final String KEY_COMPLETADOS = "completados";
    private static final String KEY_PLAN = "plan_actual";
    private String nombrePlan;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento_guiado);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nombre = findViewById(R.id.nombreEjercicioGuiado);
        detalleTextView = findViewById(R.id.detalleEjercicioGuiado);
        contador = findViewById(R.id.cronometroSuperior);
        imagen = findViewById(R.id.imagenEjercicioGuiado);
        btnSiguiente = findViewById(R.id.btnSiguienteEjercicio);
        titulo = findViewById(R.id.tituloPlanGuiado);

        rutina = (ArrayList<EjercicioSugerido>) getIntent().getSerializableExtra("ejercicios");
        nombrePlan = getIntent().getStringExtra("nombre_plan");
        if (rutina == null) rutina = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        int guardado = prefs.getInt(KEY_INDICE + nombrePlan, 0);
        completados = prefs.getInt(KEY_COMPLETADOS + nombrePlan, 0);
        indiceActual = getIntent().getIntExtra("indice_actual", guardado);
        titulo.setText(nombrePlan);

        mostrarEjercicio();

        btnSiguiente.setOnClickListener(v -> {
            if (!enDescanso) {
                completados++;
                iniciarDescanso();
            } else {
                indiceActual++;
                if (indiceActual < rutina.size()) {
                    mostrarEjercicio();
                } else {
                    guardarProgreso(100);

                    // Guardar el tipo de rutina completada en Firebase
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        Date fechaActual = new Date();

                        Map<String, Object> registroRutina = new HashMap<>();
                        registroRutina.put("fecha", fechaActual);
                        registroRutina.put("tipoRutina", nombrePlan); // Guardar el nombre del plan/rutina

                        db.collection("users").document(userId).collection("registrosEjercicio")
                                .add(registroRutina)
                                .addOnSuccessListener(documentReference -> {
                                    // Opcional: Log de éxito
                                    // Log.d("EntrenamientoGuiado", "Registro de rutina guardado con éxito");
                                })
                                .addOnFailureListener(e -> {
                                    // Opcional: Log de error
                                    // Log.e("EntrenamientoGuiado", "Error al guardar registro de rutina", e);
                                });

                        Toast.makeText(EntrenamientoGuiadoActivity.this, "haz finalizado el ejercicio", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(EntrenamientoGuiadoActivity.this, "Usuario no autenticado. No se pudo guardar el registro.", Toast.LENGTH_SHORT).show();
                    }

                    // Regresar al Home (MainActivity) limpiando la pila de actividades
                    Intent intent = new Intent(EntrenamientoGuiadoActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Finalizar esta actividad después de iniciar MainActivity
                }
            }
        });
    }
    private void mostrarEjercicio() {
        if (indiceActual >= rutina.size()) {
            guardarProgreso(100);
            Toast.makeText(this, "La rutina está vacía.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        EjercicioSugerido ej = rutina.get(indiceActual);
        nombre.setText(ej.getNombre());
        detalleTextView.setText(ej.getSeriesReps());
        imagen.setImageResource(ej.getImagenResId());
        btnSiguiente.setText("Terminar ejercicio");
        enDescanso = false;
        iniciarCronometro(DURACION_EJERCICIO, "Ejercicio");
    }
    private void iniciarDescanso() {
        btnSiguiente.setText("Siguiente ejercicio");
        enDescanso = true;
        iniciarCronometro(DURACION_DESCANSO, "Descanso");
    }
    private void iniciarCronometro(int segundos, String tipo) {
        if (timer != null) timer.cancel();
        contador.setText(tipo + ": " + segundos + "s");
        timer = new CountDownTimer(segundos * 1000, 1000) {
            int restante = segundos;
            public void onTick(long millisUntilFinished) {
                restante--;
                contador.setText(tipo + ": " + restante + "s");
            }
            public void onFinish() {
                contador.setText(tipo + ": 0s");
                if (tipo.equals("Ejercicio")) btnSiguiente.setText("Siguiente ejercicio");
                else btnSiguiente.setText("Continuar");
            }
        };
        timer.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        int porcentaje = rutina.size() == 0 ? 0 : (int) (100.0 * completados / rutina.size());
        guardarProgreso(porcentaje);
        if (timer != null) timer.cancel();
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