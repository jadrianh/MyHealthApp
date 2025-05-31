package com.damb.myhealthapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.models.EjercicioSugerido;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento_guiado);
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
                    finish();
                }
            }
        });
    }
    private void mostrarEjercicio() {
        if (indiceActual >= rutina.size()) {
            guardarProgreso(100);
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