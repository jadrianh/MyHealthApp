package com.damb.myhealthapp.ui.views;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R; // Asegúrate de que esta importación exista
import com.damb.myhealthapp.databinding.ActivityDescansoBinding;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DescansoActivity extends AppCompatActivity {

    private ActivityDescansoBinding binding;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDescansoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long duracionDescanso = getIntent().getLongExtra("DURACION_DESCANSO", 20000);

        // El listener del botón llama a finalizarDescanso()
        binding.btnOmitirDescanso.setOnClickListener(v -> finalizarDescanso());

        iniciarCronometro(duracionDescanso);
    }

    private void iniciarCronometro(long milisegundos) {
        // Configuración de la barra de progreso circular
        int duracionTotalSegundos = (int) (milisegundos / 1000);
        binding.circularProgressBar.setMax(duracionTotalSegundos);

        timer = new CountDownTimer(milisegundos, 50) { // Intervalo más corto para una animación suave
            @Override
            public void onTick(long millisUntilFinished) {
                // Actualiza el texto del temporizador
                String tiempoFormateado = String.format(Locale.getDefault(), "0:%02d",
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                binding.timerDescanso.setText(tiempoFormateado);

                // Actualiza el anillo de progreso
                binding.circularProgressBar.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                finalizarDescanso();
            }
        };
        timer.start();
    }

    private void finalizarDescanso() {
        if (timer != null) {
            timer.cancel();
        }
        setResult(Activity.RESULT_OK);
        finish();

        // --- ¡AQUÍ ESTÁ LA SOLUCIÓN! ---
        // Se añade la misma transición de "fade" al salir de esta actividad.
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
