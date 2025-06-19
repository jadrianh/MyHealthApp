package com.damb.myhealthapp.ui.views;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.FloatRange;
import androidx.appcompat.app.AppCompatActivity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.damb.myhealthapp.R;
import com.google.android.material.slider.Slider;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;

public class SleepLogActivity extends AppCompatActivity {
    private Button btnStart;
    private Slider sliderStop;
    private TextView textSleepStatus, textClock;

    private boolean isSleeping = false;
    private long startTimeMillis = 0;
    private long endTimeMillis = 0;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorListener;

    private List<Long> movimientos = new ArrayList<>();
    private static final float UMBRAL_MOVIMIENTO = 1.5f;

    private Handler clockHandler = new Handler(Looper.getMainLooper());
    private Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_log);

        btnStart = findViewById(R.id.btnStart);
        sliderStop = findViewById(R.id.sliderStop);
        textSleepStatus = findViewById(R.id.textSleepStatus);
        textClock = findViewById(R.id.textClock);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Configurar Listeners
        btnStart.setOnClickListener(view -> {
            if (!isSleeping) {
                iniciarSesion();
            }
        });

        sliderStop.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && value == slider.getValueTo()) {
                detenerSesion();
            }
        });

        // Listener para el historial no cambia
        findViewById(R.id.btnHistorial).setOnClickListener(v -> {
            Intent i = new Intent(SleepLogActivity.this, SleepLogsHistoryActivity.class);
            startActivity(i);
        });

        // Iniciar el reloj en tiempo real
        iniciarReloj();
    }

    private void iniciarReloj() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                // Formato para mostrar la hora actual
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                textClock.setText(sdf.format(new Date()));
                clockHandler.postDelayed(this, 1000 * 60); // Actualiza cada minuto
            }
        };
        clockHandler.post(clockRunnable);
    }

    private void iniciarSesion() {
        isSleeping = true;
        startTimeMillis = System.currentTimeMillis();

        // Actualizar UI para el estado "Durmiendo"
        btnStart.setVisibility(View.GONE);
        sliderStop.setVisibility(View.VISIBLE);
        sliderStop.setValue(0.0f);

        textSleepStatus.setText("Registrando sueño...");

        movimientos.clear();
        iniciarSensor();
    }

    private void detenerSesion() {
        if (startTimeMillis == 0) return;

        isSleeping = false;
        endTimeMillis = System.currentTimeMillis();

        detenerSensor();

        // Validación básica
        if (endTimeMillis <= startTimeMillis) {
            Toast.makeText(this, "Error: Tiempo final menor que inicio", Toast.LENGTH_SHORT).show();
            resetearControles();
            return;
        }

        guardarSesionEnFirebase(startTimeMillis, endTimeMillis);

        ArrayList<Integer> fasesGraficas = generarFasesParaGrafico(movimientos, startTimeMillis, endTimeMillis);
        Intent intent = new Intent(SleepLogActivity.this, SleepLogSummaryActivity.class);
        intent.putIntegerArrayListExtra("fases_sueno", fasesGraficas);
        intent.putExtra("inicio", startTimeMillis);
        startActivity(intent);

        // Resetear la UI al estado inicial después de un breve momento
        new Handler(Looper.getMainLooper()).postDelayed(this::resetearControles, 2000);
    }

    private void resetearControles() {
        isSleeping = false;
        startTimeMillis = 0;
        endTimeMillis = 0;

        // Restaurar la visibilidad de los controles
        btnStart.setVisibility(View.VISIBLE);
        sliderStop.setVisibility(View.GONE);
        sliderStop.setValue(0.0f);

        textSleepStatus.setText("Presiona para comenzar");
    }

    private ArrayList<Integer> generarFasesParaGrafico(List<Long> movimientos, long inicio, long fin) {
        ArrayList<Integer> fases = new ArrayList<>();

        long intervalo = 60 * 1000; // 1 minuto
        long actual = inicio;

        while (actual < fin) {
            long siguiente = actual + intervalo;
            int movimientosEnMinuto = 0;

            for (Long t : movimientos) {
                if (t >= actual && t < siguiente) {
                    movimientosEnMinuto++;
                }
            }

            if (movimientosEnMinuto == 0) {
                fases.add(0); // Profundo
            } else if (movimientosEnMinuto <= 2) {
                fases.add(1); // Ligero
            } else {
                fases.add(2); // Inquieto
            }

            actual = siguiente;
        }

        return fases;
    }

    private String formatHora(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private void guardarSesionEnFirebase(long inicio, long fin) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fin <= inicio) {
            Toast.makeText(this, "Error: El tiempo final debe ser después del inicio", Toast.LENGTH_LONG).show();
            return;
        }

        long duracionMinutos = TimeUnit.MILLISECONDS.toMinutes(fin - inicio);

        Map<String, Object> data = new HashMap<>();
        data.put("start", new Timestamp(new Date(inicio)));
        data.put("end", new Timestamp(new Date(fin)));
        data.put("timezone", TimeZone.getDefault().getID());
        data.put("duracion_min", duracionMinutos);
        data.put("movimientos_detectados", movimientos.size());
        data.put("timestamps_movimientos", movimientos);

        Map<String, Integer> clasificacion = clasificarSueño(movimientos, inicio, fin);
        data.put("sueño_profundo_min", clasificacion.get("sueño_profundo"));
        data.put("sueño_ligero_min", clasificacion.get("sueño_ligero"));
        data.put("sueño_inquieto_min", clasificacion.get("sueño_inquieto"));

        db.collection("users")
                .document(user.getUid())
                .collection("sleepLogs")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Sesión de sueño guardada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void iniciarSensor() {
        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float aceleracion = (float) Math.sqrt(x * x + y * y + z * z);
                float delta = Math.abs(aceleracion - SensorManager.GRAVITY_EARTH);

                if (delta > UMBRAL_MOVIMIENTO) {
                    movimientos.add(System.currentTimeMillis());
                    Toast.makeText(SleepLogActivity.this, "Movimiento detectado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        if (accelerometer != null) {
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Sensor de acelerómetro activado", Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerSensor() {
        if (sensorManager != null && sensorListener != null) {
            sensorManager.unregisterListener(sensorListener);
            Toast.makeText(this, "Sensor de acelerómetro detenido", Toast.LENGTH_SHORT).show();
        }
    }

    private Map<String, Integer> clasificarSueño(List<Long> movimientos, long inicio, long fin) {
        Map<String, Integer> resumen = new HashMap<>();
        resumen.put("sueño_profundo", 0);
        resumen.put("sueño_ligero", 0);
        resumen.put("sueño_inquieto", 0);

        long intervaloMin = 60 * 1000; // 1 minuto en milisegundos
        long tiempoActual = inicio;

        while (tiempoActual < fin) {
            long siguiente = tiempoActual + intervaloMin;
            int movimientosEnIntervalo = 0;

            for (Long t : movimientos) {
                if (t >= tiempoActual && t < siguiente) {
                    movimientosEnIntervalo++;
                }
            }

            if (movimientosEnIntervalo == 0) {
                resumen.put("sueño_profundo", resumen.get("sueño_profundo") + 1);
            } else if (movimientosEnIntervalo <= 2) {
                resumen.put("sueño_ligero", resumen.get("sueño_ligero") + 1);
            } else {
                resumen.put("sueño_inquieto", resumen.get("sueño_inquieto") + 1);
            }

            tiempoActual = siguiente;
        }

        return resumen;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerSensor();
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
    }

}