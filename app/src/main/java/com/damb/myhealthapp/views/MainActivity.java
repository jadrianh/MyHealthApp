package com.damb.myhealthapp.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem; // Importar para manejar clicks en items del menú
import android.view.View;
// import android.widget.Button; // Unused, removed
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.damb.myhealthapp.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.damb.myhealthapp.adapters.ViewPagerEjercicioAdapter;
// import com.google.android.material.tabs.TabLayout; // Unused, removed
// import com.google.android.material.tabs.TabLayoutMediator; // Unused, removed
import com.damb.myhealthapp.utils.GoogleFitManager;
import android.content.SharedPreferences;

import java.util.ArrayList;
// import java.util.Arrays; // Unused, removed
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleFitManager.OnFitDataReceivedListener,
        NavigationView.OnNavigationItemSelectedListener {

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

    // Declaraciones de UI que permanecen
    private TextView Username;
    private TextView titleTextView;
    private ImageView menuIcon;
    private static ViewPager2 viewPagerEjercicios;
    private GoogleFitManager googleFitManager;
    private TextView stepsTextView;
    private TextView caloriesTextView;
    private TextView stepsGoalTextView;
    private ProgressBar stepsProgressBar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView textViewDrawerUsername;
    private TextView textViewDrawerUserEmail;

    private FirebaseAuth mAuth;
    private final long DAILY_STEP_GOAL = 10000; // cambiar esta meta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    moveTaskToBack(true);
                }
            }
        });

        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // Guardar la meta de pasos en SharedPreferences al iniciar la aplicación
        SharedPreferences sharedPreferences = getSharedPreferences("ReminderPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("daily_step_goal", DAILY_STEP_GOAL);
        editor.apply();

        // Solicitar permiso de notificaciones si es necesario
        checkAndRequestNotificationPermission();

        // Inicialización de componentes UI que permanecen
        Username = findViewById(R.id.Username);
        titleTextView = findViewById(R.id.title);
        menuIcon = findViewById(R.id.menuIcon);

        // Inicialización de componentes del DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar listener para el NavigationView
        navigationView.setNavigationItemSelectedListener(this);

        // Configurar el encabezado del NavigationView
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) { // Añadir una comprobación de nulidad para mayor seguridad
            textViewDrawerUsername = headerView.findViewById(R.id.textView_drawer_username);
            textViewDrawerUserEmail = headerView.findViewById(R.id.textView_drawer_user_email);
        }

        // Lógica para mostrar el nombre de usuario y correo (en el UI principal y en el drawer)
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String correo = user.getEmail();
            String nombre = correo != null ? correo.split("@")[0] : "Usuario";
            Username.setText(nombre);
            if (textViewDrawerUsername != null) {
                textViewDrawerUsername.setText(nombre);
            }
            if (textViewDrawerUserEmail != null && correo != null) {
                textViewDrawerUserEmail.setText(correo);
            }
        }

        // Configuración del ViewPager2 para los planes de entrenamiento
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

        // Listener para el icono de menú: ahora abre el Drawer
        menuIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Inicializar Google Fit si los permisos ya están concedidos
        stepsTextView = findViewById(R.id.stepsTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        stepsGoalTextView = findViewById(R.id.stepsGoalTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);
        checkAndSetupGoogleFit();
    }

    /**
     * Maneja la selección de ítems en el Navigation Drawer.
     * @param item El ítem del menú seleccionado.
     * @return true si el ítem fue manejado, false en caso contrario.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            // Acción para "Inicio"
            Toast.makeText(this, "Navegar a Inicio", Toast.LENGTH_SHORT).show();
            // Implementa aquí la lógica para ir a la pantalla de inicio o refrescar la actual
        } else if (id == R.id.nav_activity_record) {
            // Acción para "Registro de actividad"
            intent = new Intent(MainActivity.this, ExerciseLogActivity.class);
        } else if (id == R.id.nav_button3) {
            // Acción para "Boton 3"
            Toast.makeText(this, "Navegar a Botón 3", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_button4) {
            // Acción para "Boton 4"
            Toast.makeText(this, "Navegar a Botón 4", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            // Acción para "Boton 4"
            intent = new Intent(MainActivity.this, SettingsActivity.class);
        } else if (id == R.id.nav_logout) {
            // Acción para "Cerrar sesión"
            logout();
        }

        if (intent != null) {
            startActivity(intent);
        }

        drawerLayout.closeDrawer(GravityCompat.START); // Cerrar el drawer después de seleccionar un ítem
        return true;
    }


    /**
     * Verifica y solicita el permiso de notificaciones para Android 13+
     */
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

    /**
     * Verifica los permisos de Google Fit y lo inicializa.
     */
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
        // Asegura que el ViewPager2 esté habilitado para la interacción del usuario
        setViewPagerEnabled(true);
    }

    /**
     * Cierra la sesión del usuario de Firebase y redirige a la pantalla de bienvenida.
     */
    private void logout() {
        mAuth.signOut();

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Habilita o deshabilita la interacción del usuario con el ViewPager2.
     * @param enabled true para habilitar, false para deshabilitar.
     */
    public static void setViewPagerEnabled(boolean enabled) {
        if (viewPagerEjercicios != null) {
            viewPagerEjercicios.setUserInputEnabled(enabled);
        }
    }

    // Este método está duplicado por el onBackPressedCallback. Se puede eliminar o dejar por compatibilidad
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Comprueba si un conjunto de permisos han sido concedidos.
     * @param permissionsToCheck Arreglo de permisos a verificar.
     * @return true si todos los permisos están concedidos, false en caso contrario.
     */
    private boolean checkPermissions(String[] permissionsToCheck) {
        for (String permission : permissionsToCheck) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Solicita un conjunto de permisos a la aplicación.
     * @param permissionsToRequest Arreglo de permisos a solicitar.
     * @param requestCode Código de solicitud para identificar la respuesta.
     */
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
