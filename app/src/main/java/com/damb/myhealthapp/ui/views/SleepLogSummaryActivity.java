package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SleepLogSummaryActivity extends AppCompatActivity {

    private BarChart barChart;
    private TextView textLegendLight, textLegendDeep, textLegendRestless;
    private TextView textViewTotalSleep, textViewLightSleep, textViewDeepSleep, textViewRestlessSleep, textViewSessionDate;
    private Button btnNext;
    private LinearLayout percentageBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_log_summary);

        barChart = findViewById(R.id.barChart);
        textLegendLight = findViewById(R.id.textLegendLight);
        textLegendDeep = findViewById(R.id.textLegendDeep);
        textLegendRestless = findViewById(R.id.textLegendRestless);
        percentageBarContainer = findViewById(R.id.percentageBarContainer);
        textViewTotalSleep = findViewById(R.id.textViewTotalSleep);
        textViewLightSleep = findViewById(R.id.textViewLightSleep);
        textViewDeepSleep = findViewById(R.id.textViewDeepSleep);
        textViewRestlessSleep = findViewById(R.id.textViewRestlessSleep);
        textViewSessionDate = findViewById(R.id.textViewSessionDate);
        btnNext = findViewById(R.id.btnNext);


        Intent intent = getIntent();
        ArrayList<Integer> fases = intent.getIntegerArrayListExtra("fases_sueno");
        long startTimeMillis = intent.getLongExtra("inicio", 0);

        btnNext.setOnClickListener(v -> {
            finish();
        });

        if (fases != null && !fases.isEmpty() && startTimeMillis > 0) {
            Date sessionDate = new Date(startTimeMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            String formattedDate = sdf.format(sessionDate);
            String finalTitle = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1) + " - Sesión 1";
            textViewSessionDate.setText(finalTitle);

            procesarYMostrarDatos(fases, startTimeMillis);

        } else {
            Toast.makeText(this, "No hay datos de sueño disponibles", Toast.LENGTH_SHORT).show();
            textViewSessionDate.setText("Resumen de sueño"); // Texto por defecto si no hay datos
        }
    }

    private void procesarYMostrarDatos(List<Integer> fases, long startTimeMillis) {
        int deepSleepMinutes = 0;
        int lightSleepMinutes = 0;
        int restlessSleepMinutes = 0;

        for (Integer fase : fases) {
            switch (fase) {
                case 0: // Profundo
                    deepSleepMinutes++;
                    break;
                case 1: // Ligero
                    lightSleepMinutes++;
                    break;
                case 2: // Inquieto
                    restlessSleepMinutes++;
                    break;
            }
        }

        int totalSleepMinutes = deepSleepMinutes + lightSleepMinutes + restlessSleepMinutes;
        if (totalSleepMinutes == 0) return; // Evitar división por cero

        textViewTotalSleep.setText(formatMinutes(totalSleepMinutes));
        textViewDeepSleep.setText(formatMinutes(deepSleepMinutes));
        textViewLightSleep.setText(formatMinutes(lightSleepMinutes));
        textViewRestlessSleep.setText(formatMinutes(restlessSleepMinutes));

        int deepPercent = (deepSleepMinutes * 100) / totalSleepMinutes;
        int lightPercent = (lightSleepMinutes * 100) / totalSleepMinutes;
        int restlessPercent = 100 - deepPercent - lightPercent; // Para asegurar que sume 100

        String deepText = String.format("● <font color='%s'>Profundo:</font> %d%%", "#4588fb", deepPercent);
        String lightText = String.format("● <font color='%s'>Ligero:</font> %d%%", "#ffeb6e", lightPercent);
        String restlessText = String.format("● <font color='%s'>Inquieto:</font> %d%%", "#85fbff", restlessPercent);
        textLegendDeep.setText(Html.fromHtml(deepText));
        textLegendLight.setText(Html.fromHtml(lightText));
        textLegendRestless.setText(Html.fromHtml(restlessText));

        crearBarraPorcentajes(lightPercent, deepPercent, restlessPercent);

        mostrarGraficoFases(fases, startTimeMillis);
    }

    private void crearBarraPorcentajes(int lightPercent, int deepPercent, int restlessPercent) {
        percentageBarContainer.removeAllViews(); // Limpiar por si acaso

        View lightView = new View(this);
        lightView.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
        LinearLayout.LayoutParams lightParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, lightPercent);
        percentageBarContainer.addView(lightView, lightParams);

        View deepView = new View(this);
        deepView.setBackgroundColor(ContextCompat.getColor(this, R.color.blueDark));
        LinearLayout.LayoutParams deepParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, deepPercent);
        percentageBarContainer.addView(deepView, deepParams);

        View restlessView = new View(this);
        restlessView.setBackgroundColor(ContextCompat.getColor(this, R.color.skyBlue));
        LinearLayout.LayoutParams restlessParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, restlessPercent);
        percentageBarContainer.addView(restlessView, restlessParams);
    }

    private String formatMinutes(int minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d h %d min", hours, mins);
        } else {
            return String.format(Locale.getDefault(), "%d min", mins);
        }
    }

    private void mostrarGraficoFases(List<Integer> fases, long startTimeMillis) {
        List<BarEntry> entries = new ArrayList<>();
        long intervalo = 60 * 1000;

        for (int i = 0; i < fases.size(); i++) {
            entries.add(new BarEntry(i, fases.get(i) + 1));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Fase de Sueño");
        dataSet.setDrawValues(false);

        dataSet.setColors(
                ContextCompat.getColor(this, R.color.blueDark),
                ContextCompat.getColor(this, R.color.skyBlue),
                ContextCompat.getColor(this, R.color.yellow)
        );

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setDrawGridLines(false);
        // Formatter para las horas
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                long tiempo = startTimeMillis + ((long)value * intervalo);
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(tiempo));
            }
        });
        xAxis.setLabelCount(6, true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawLabels(true);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(4f);
        leftAxis.setGranularity(1f);
        leftAxis.setLabelCount(4, true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 1f) return "Profundo";
                if (value == 2f) return "Ligero";
                if (value == 3f) return "Inquieto";
                return "";
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.invalidate();
    }
}