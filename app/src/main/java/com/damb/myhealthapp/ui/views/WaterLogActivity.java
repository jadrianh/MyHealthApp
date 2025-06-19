package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.damb.myhealthapp.models.WaterLog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import com.damb.myhealthapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class WaterLogActivity extends AppCompatActivity {
    private ProgressBar progressCircle;
    private TextView tvCantidadActual, tvMetaProgreso, tvPercentage, textViewPromedioHora;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private BarChart barChart;
    private int cantidadActual = 0;
    private int metaDiaria = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_log);

        progressCircle = findViewById(R.id.progressCircle);
        tvCantidadActual = findViewById(R.id.tvCantidadActual);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvMetaProgreso = findViewById(R.id.tvMetaProgreso);
        textViewPromedioHora = findViewById(R.id.textViewPromedioHora);
        barChart = findViewById(R.id.barChart);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupBotones();
        escucharConsumoDiario();
        cargarMetaDiaria();
    }

    private void setupBotones() {
        asignarBoton(R.id.btn150, 150);
        asignarBoton(R.id.btn250, 250);
        asignarBoton(R.id.btn350, 350);
        asignarBoton(R.id.btn500, 500);
        asignarBoton(R.id.btn750, 750);

        Button buttonEditTarget = findViewById(R.id.buttonEditTarget);
        buttonEditTarget.setOnClickListener(v -> mostrarDialogoEditarMeta());
    }

    private void asignarBoton(int botonId, int cantidad) {
        Button btn = findViewById(botonId);
        btn.setOnClickListener(v -> agregarAgua(cantidad));
    }

    private void agregarAgua(int cantidad) {
        cantidadActual += cantidad;
        actualizarUI();

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "sin_usuario";
        WaterLog log = new WaterLog(cantidad, Timestamp.now(), userId);

        db.collection("users")
                .document(userId)
                .collection("waterLogs")
                .add(log)
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private Timestamp getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTime());
    }

    private Timestamp getEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTime());
    }

    private double calcularPromedioPorHora(int total, Timestamp primerRegistro) {
        long ahora = System.currentTimeMillis();
        long inicio = primerRegistro.toDate().getTime();

        long milisegundosTranscurridos = ahora - inicio;
        double horas = milisegundosTranscurridos / (1000.0 * 60 * 60);

        // Asegurarse que al menos haya pasado 1 minuto para evitar división por cero
        if (horas < 0.0167) { // 1 minuto en milisegundos
            return total;
        }

        return total / horas;
    }


    private void escucharConsumoDiario() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "usuario_prueba";

        db.collection("users")
                .document(userId)
                .collection("waterLogs")
                .whereGreaterThanOrEqualTo("timestamp", getStartOfDay())
                .whereLessThan("timestamp", getEndOfDay())
                .orderBy("timestamp") // Orden para obtener el primero
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    int totalDelDia = 0;
                    Timestamp primerRegistro = null;

                    for (int i = 0; i < value.size(); i++) {
                        DocumentSnapshot doc = value.getDocuments().get(i);
                        Integer amount = doc.getLong("amount") != null ? doc.getLong("amount").intValue() : 0;
                        totalDelDia += amount;

                        if (i == 0) {
                            primerRegistro = doc.getTimestamp("timestamp");
                        }
                    }

                    cantidadActual = totalDelDia;
                    actualizarUI();

                    // Cálculo del promedio
                    if (primerRegistro != null) {
                        double promedio = calcularPromedioPorHora(totalDelDia, primerRegistro);
                        textViewPromedioHora.setText(String.format(Locale.getDefault(), "%.1f mL/h", promedio));
                    } else {
                        textViewPromedioHora.setText("0 mL/h");
                    }
                });
    }

    private String formatFechaCorta(Date fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault()); // Ej: Mon, Tue
        return sdf.format(fecha);
    }

    private void cargarDatosGrafico() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "usuario_prueba";

        // Rango de los últimos 7 días
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        List<Timestamp> dias = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar dia = (Calendar) calendar.clone();
            dia.add(Calendar.DAY_OF_YEAR, -i);
            dias.add(new Timestamp(dia.getTime()));
        }

        List<BarEntry> entradas = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();

        AtomicInteger contador = new AtomicInteger(0);

        for (int i = 0; i < dias.size(); i++) {
            Timestamp start = dias.get(i);

            Calendar c = Calendar.getInstance();
            c.setTime(start.toDate());
            c.add(Calendar.DAY_OF_YEAR, 1);
            Timestamp end = new Timestamp(c.getTime());

            int finalI = i;

            db.collection("users")
                    .document(userId)
                    .collection("waterLogs")
                    .whereGreaterThanOrEqualTo("timestamp", start)
                    .whereLessThan("timestamp", end)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int total = 0;
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            total += doc.getLong("amount") != null ? doc.getLong("amount").intValue() : 0;
                        }

                        entradas.add(new BarEntry(finalI, total));
                        etiquetas.add(formatFechaCorta(start.toDate()));

                        if (contador.incrementAndGet() == dias.size()) {
                            mostrarGrafico(entradas, etiquetas);
                        }
                    });
        }
    }

    private void mostrarGrafico(List<BarEntry> entradas, List<String> etiquetas) {
        BarDataSet dataSet = new BarDataSet(entradas, "mL por día");
        dataSet.setColor(ContextCompat.getColor(this, R.color.blue));
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        xAxis.setDrawGridLines(false);

        YAxis left = barChart.getAxisLeft();
        YAxis right = barChart.getAxisRight();
        right.setEnabled(false);
        left.setAxisMinimum(0f);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.invalidate(); // Refresh
    }

    private void guardarMetaDiaria(int nuevaMeta) {
        if (nuevaMeta <= 0) {
            Toast.makeText(this, "La meta debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        } else {
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "usuario_prueba";

            db.collection("users").document(userId)
                    .update("dailyGoal", nuevaMeta)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Meta diaria actualizada", Toast.LENGTH_SHORT).show();
                        metaDiaria = nuevaMeta;
                        actualizarUI(); // Para refrescar la barra
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar meta", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void cargarMetaDiaria() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "usuario_prueba";

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long valor = documentSnapshot.getLong("dailyGoal");
                        metaDiaria = valor != null ? valor.intValue() : 2000; // Valor por defecto
                        actualizarUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar meta", Toast.LENGTH_SHORT).show();
                    metaDiaria = 2000; // Valor por defecto si falla el valor por defecto xd
                    actualizarUI();
                });
    }

    private void mostrarDialogoEditarMeta() {
        final int[] opciones = {1500, 2000, 2500, 3000};
        String[] opcionesTexto = {"1500 mL", "2000 mL", "2500 mL", "3000 mL"};
        int seleccionActual = 1; // Por defecto 2000
        for (int i = 0; i < opciones.length; i++) {
            if (metaDiaria == opciones[i]) seleccionActual = i;
        }
        new AlertDialog.Builder(this)
                .setTitle("Selecciona tu meta diaria de agua")
                .setSingleChoiceItems(opcionesTexto, seleccionActual, (dialog, which) -> {
                    int nuevaMeta = opciones[which];
                    guardarMetaDiaria(nuevaMeta);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void actualizarUI() {
        if (metaDiaria <= 0) metaDiaria = 1;
        int cantidadLimitada = Math.min(cantidadActual, metaDiaria);

        int porcentaje = (int) ((cantidadLimitada * 100.0f) / metaDiaria);
        porcentaje = Math.min(porcentaje, 100); // Asegurar no más del 100%

        tvCantidadActual.setText(String.format(Locale.getDefault(), "%d mL", cantidadActual));

        if (tvMetaProgreso != null) {
            tvMetaProgreso.setText(String.format(Locale.getDefault(), "%d mL", cantidadLimitada));
        }

        tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", porcentaje));
        progressCircle.setProgress(porcentaje);

        cargarDatosGrafico();
    }
}
