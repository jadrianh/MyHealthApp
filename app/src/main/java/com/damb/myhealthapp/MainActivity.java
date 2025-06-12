package com.damb.myhealthapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.damb.myhealthapp.adapters.ViewPagerEjercicioAdapter;
import com.damb.myhealthapp.models.EjercicioSugerido;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.cardview.widget.CardView;
import com.damb.myhealthapp.utils.GoogleFitManager;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleFitManager.OnFitDataReceivedListener {
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2;
    private static final int REQUEST_NOTIFICATION_PERMISSION_CODE = 3;
    private static final String[] FIT_PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FIT_PERMISSIONS = new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.BODY_SENSORS
            };
        } else {
            FIT_PERMISSIONS = new String[]{
                    Manifest.permission.BODY_SENSORS
            };
        }
    }

    private TextView saludoTextView;
    private TextView consejoTextView;
    private FirebaseAuth mAuth;
    private static ViewPager2 viewPagerEjercicios;
    private ImageView btnLogout;
    private CardView cardEjercicio;
    private GoogleFitManager googleFitManager;
    private TextView stepsTextView;
    private TextView caloriesTextView;
    private TextView stepsGoalTextView;
    private ProgressBar stepsProgressBar;
    private final long DAILY_STEP_GOAL = 10000; // Puedes cambiar esta meta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // Guardar la meta de pasos en SharedPreferences al iniciar la aplicación
        SharedPreferences sharedPreferences = getSharedPreferences("ReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("daily_step_goal", DAILY_STEP_GOAL);
        editor.apply();

        // Solicitar permiso de notificaciones si es necesario
        checkAndRequestNotificationPermission();

        saludoTextView = findViewById(R.id.saludoTextView);
        consejoTextView = findViewById(R.id.consejoTextView);
        Button btnPeso = findViewById(R.id.btnPeso);
        Button btnPresion = findViewById(R.id.btnPresion);
        Button btnAlimentacion = findViewById(R.id.btnAlimentacion);
        cardEjercicio = findViewById(R.id.cardEjercicio);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String correo = user.getEmail();
            String nombre = correo != null ? correo.split("@")[0] : "Usuario";
            saludoTextView.setText("Hola, " + nombre + " 👋");
        }

        consejoTextView.setText("Consejo: Haz al menos 30 minutos de ejercicio diario 💪");

        viewPagerEjercicios = findViewById(R.id.viewPagerEjercicios);
        List<String> tiposEjercicio = new ArrayList<>();
        tiposEjercicio.add("Pérdida de Peso (Quema de Grasa)");
        tiposEjercicio.add("Ganancia de Masa Muscular (Hipertrofia)");
        tiposEjercicio.add("Flexibilidad y Movilidad");
        tiposEjercicio.add("Salud General y Bienestar");
        tiposEjercicio.add("Resistencia y Cardio");
        tiposEjercicio.add("Entrenamiento Funcional");
        tiposEjercicio.add("Plan Rápido para Tonificación");
        tiposEjercicio.add("Plan para Principiantes");

        ViewPagerEjercicioAdapter viewPagerEjercicioAdapter = new ViewPagerEjercicioAdapter(this, tiposEjercicio);
        viewPagerEjercicios.setAdapter(viewPagerEjercicioAdapter);

        btnPeso.setOnClickListener(v -> {
            // TODO: Ir a registrar peso
        });

        btnPresion.setOnClickListener(v -> {
            // TODO: Ir a registrar altura
        });

        btnAlimentacion.setOnClickListener(v -> {
            // TODO: Ir a alimentación
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            logout();
        });

        cardEjercicio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrarEjercicioActivity.class);
            startActivity(intent);
        });

        // Configurar el botón de recordatorios
        Button btnRecordatorios = findViewById(R.id.btnRecordatorios);
        btnRecordatorios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            startActivity(intent);
        });

        // Inicializar Google Fit si los permisos ya están concedidos
        stepsTextView = findViewById(R.id.stepsTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        stepsGoalTextView = findViewById(R.id.stepsGoalTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);
        checkAndSetupGoogleFit();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) y superiores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permiso ya concedido
                Log.d("MainActivity", "Permiso de notificación ya concedido");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Explicar por qué se necesita el permiso (opcional, para una mejor UX)
                Toast.makeText(this, "Se necesita el permiso de notificaciones para los recordatorios.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION_CODE);
            } else {
                // Solicitar el permiso directamente
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void checkAndSetupGoogleFit() {
        if (checkPermissions(FIT_PERMISSIONS)) {
            googleFitManager = new GoogleFitManager(this, this);
        } else {
            requestAppPermissions(FIT_PERMISSIONS, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (googleFitManager != null) {
            googleFitManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStepsReceived(long steps) {
        runOnUiThread(() -> {
            stepsTextView.setText(String.format("Pasos hoy: %d", steps));
            stepsGoalTextView.setText(String.format("Meta: %d / %d pasos", steps, DAILY_STEP_GOAL));
            stepsProgressBar.setProgress((int) Math.min(steps, DAILY_STEP_GOAL)); // Actualiza el progreso de la barra
        });
    }

    @Override
    public void onCaloriesReceived(float calories) {
        runOnUiThread(() -> {
            caloriesTextView.setText(String.format("Calorías: %.0f", calories));
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewPagerEnabled(true);
    }

    private void logout() {
        mAuth.signOut();

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public static void setViewPagerEnabled(boolean enabled) {
        if (viewPagerEjercicios != null) {
            viewPagerEjercicios.setUserInputEnabled(enabled);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean checkPermissions(String[] permissionsToCheck) {
        for (String permission : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestAppPermissions(String[] permissionsToRequest, int requestCode) {
        ActivityCompat.requestPermissions(this, permissionsToRequest, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            boolean allFitPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allFitPermissionsGranted = false;
                    break;
                }
            }
            if (allFitPermissionsGranted) {
                checkAndSetupGoogleFit();
            } else {
                Toast.makeText(this, "Permisos de Google Fit denegados. La función de conteo de pasos no estará disponible.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado. Es posible que los recordatorios no funcionen.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
