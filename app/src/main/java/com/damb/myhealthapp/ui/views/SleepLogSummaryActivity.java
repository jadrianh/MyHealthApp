package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SleepLogSummaryActivity extends AppCompatActivity {

    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_log_summary);

        barChart = findViewById(R.id.barChart);

        Intent intent = getIntent();
        ArrayList<Integer> fases = intent.getIntegerArrayListExtra("fases_sueno");

        if (fases != null && !fases.isEmpty()) {
            mostrarGraficoFases(fases);
        } else {
            Toast.makeText(this, "No hay datos de sueño disponibles", Toast.LENGTH_SHORT).show();
        }

        mostrarGraficoFases(fases);
    }

    private void mostrarGraficoFases(List<Integer> fases) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        long startTimeMillis = getIntent().getLongExtra("inicio", 0);
        long intervalo = 60 * 1000; // 1 minuto por barra

        // Llenar entradas
        for (int i = 0; i < fases.size(); i++) {
            entries.add(new BarEntry(i, fases.get(i)));

            // Generar etiqueta de hora
            long tiempo = startTimeMillis + (i * intervalo);
            labels.add(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(tiempo)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Fase de Sueño");
        dataSet.setDrawValues(false);

        // Colores según fase
        List<Integer> colores = new ArrayList<>();
        for (BarEntry e : entries) {
            switch ((int) e.getY()) {
                case 0: colores.add(Color.parseColor("#4588fb")); break;
                case 1: colores.add(Color.parseColor("#ffeb6e")); break;
                default: colores.add(Color.parseColor("#85fbff")); break;
            }
        }
        dataSet.setColors(colores);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.setFitBars(true);

        // Eje X con horas reales
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        // Mostrar menos etiquetas si hay muchas
        if (fases.size() > 30) {
            xAxis.setLabelCount(8, true); // ajustable
        }

        // Ocultar eje Y
        YAxis left = barChart.getAxisLeft();
        YAxis right = barChart.getAxisRight();
        left.setEnabled(false);
        right.setEnabled(false);

        // Desactivar leyenda automática y descripción
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        barChart.invalidate();
    }

}