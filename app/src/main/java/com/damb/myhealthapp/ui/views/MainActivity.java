package com.damb.myhealthapp.ui.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.ui.components.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.damb.myhealthapp.ui.adapters.ExerciseViewPagerAdapter;
import com.damb.myhealthapp.ui.components.GoogleFitManager;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends BaseActivity implements
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
    private static ViewPager2 viewPagerEjercicios;
    private GoogleFitManager googleFitManager;
    private TextView stepsTextView;
    private TextView caloriesTextView;
    private TextView stepsGoalTextView;
    private ProgressBar stepsProgressBar;
    private LinearLayout stepsCaloriesDataLayout;
    private MaterialButton btnConnectGoogleFit;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final long DAILY_STEP_GOAL = 10000;
    private boolean isRequestingGoogleFitPermissions = false;
    private boolean isGoogleFitOAuthRequesting = false;
    private static final String TAG = "MainActivity";

    private ActivityResultLauncher<Intent> googleFitSignInLauncher;

    private TextView textCurrentWater;
    private TextView textGoalWater;
    private TextView textProgressWaterPercentage;
    private ProgressBar progressBarWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        ImageView menuIcon = findViewById(R.id.menuIcon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Username = findViewById(R.id.Username);

        View headerView = navigationView.getHeaderView(0);

        if (headerView != null) {
            textViewDrawerUsername = headerView.findViewById(R.id.textView_drawer_username);
            textViewDrawerUserEmail = headerView.findViewById(R.id.textView_drawer_user_email);
            drawerProfileImageView = headerView.findViewById(R.id.imageView_drawer_user_profile);
        }


        viewPagerEjercicios = findViewById(R.id.viewPagerEjercicios);

        // *** CAMBIO CLAVE AQUÍ: Llenar la lista con objetos EjercicioData ***
        List<ExerciseViewPagerAdapter.EjercicioData> ejerciciosData = new ArrayList<>();
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Pérdida de Peso (Quema de Grasa)", "Quema de grasa", "40 min", "560 Cal"));
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Ganancia de Masa Muscular (Hipertrofia)", "Entrenamiento de Fuerza", "60 min", "420 Cal"));
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Flexibilidad y Movilidad", "Yoga y Estiramientos", "30 min", "170 Cal"));
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Salud General y Bienestar", "Actividad Moderada", "16 min", "280 Cal"));
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Resistencia y Cardio", "Running Intervalos", "45 min", "480 Cal"));
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Entrenamiento Funcional", "Movimientos Compuestos", "25 min", "525 Cal"));
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Plan Rápido para Tonificación", "Entrenamiento Express", "20 min", "455 Cal"));
        ejerciciosData.add(new ExerciseViewPagerAdapter.EjercicioData("Plan para Principiantes", "Introducción al Fitness", "15 min", "245 Cal"));

        ExerciseViewPagerAdapter exerciseViewPagerAdapter = new ExerciseViewPagerAdapter(this, ejerciciosData);
        viewPagerEjercicios.setAdapter(exerciseViewPagerAdapter);

        // Initialize Google Fit views
        stepsTextView = findViewById(R.id.stepsTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        stepsGoalTextView = findViewById(R.id.stepsGoalTextView);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);
        stepsCaloriesDataLayout = findViewById(R.id.stepsCaloriesDataLayout);
        btnConnectGoogleFit = findViewById(R.id.btnConnectGoogleFit);

        // Initial visibility: Hide data, show connect button
        stepsCaloriesDataLayout.setVisibility(View.GONE);
        btnConnectGoogleFit.setVisibility(View.VISIBLE);

        // Handle back press to close drawer if open
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finishAffinity();
                }
            }
        });

        checkCurrentUserAndLoadData();

        // Verificar y solicitar permiso de notificaciones
        checkAndRequestNotificationPermission();

        // Inicializar el launcher para Google Fit
        googleFitSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (googleFitManager != null) {
                    googleFitManager.handleSignInResult(result.getResultCode(), result.getData());
                }
            }
        );

        // Cambiar la inicialización de GoogleFitManager para pasar el launcher
        googleFitManager = new GoogleFitManager(this, this, googleFitSignInLauncher);

        // Cambiar el click del botón para usar el launcher
        btnConnectGoogleFit.setOnClickListener(v -> {
            Log.d(TAG, "btnConnectGoogleFit clicked. Initiating Google Fit flow.");
            googleFitManager.initiateFitSignInFlow();
        });

        textCurrentWater = findViewById(R.id.textCurrentWater);
        textGoalWater = findViewById(R.id.textGoalWater);
        textProgressWaterPercentage = findViewById(R.id.textProgressWaterPercentage);
        progressBarWater = findViewById(R.id.progressBarWater);

        cargarMetaYConsumoAgua();
    }

    private void checkCurrentUserAndLoadData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            googleFitManager = new GoogleFitManager(this, this, googleFitSignInLauncher);

            // Intentar leer datos agregados de Google Fit si los permisos ya están concedidos
            if (googleFitManager.readAggregatedData()) {
                // Permisos ya concedidos y datos leídos, mostrar datos y ocultar botón
                stepsCaloriesDataLayout.setVisibility(View.VISIBLE);
                btnConnectGoogleFit.setVisibility(View.GONE);
            } else {
                // Permisos no concedidos, mostrar botón de conexión
                stepsCaloriesDataLayout.setVisibility(View.GONE);
                btnConnectGoogleFit.setVisibility(View.VISIBLE);
                btnConnectGoogleFit.setOnClickListener(v -> {
                    Log.d(TAG, "btnConnectGoogleFit clicked. Initiating Google Fit flow.");
                    googleFitManager.initiateFitSignInFlow();
                });
            }

            loadDisplayNameFromFirestore(currentUser.getUid());
            textViewDrawerUserEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "N/A");

        } else {
            Log.d(TAG, "No authenticated user found. Redirecting to LoginActivity.");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) y superiores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permiso de notificación ya concedido");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(this, "Se necesita el permiso de notificaciones para los recordatorios.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void checkAndSetupGoogleFit() {
        if (checkPermissions(FIT_PERMISSIONS)) {
            if (googleFitManager == null) {
                Log.e(TAG, "checkAndSetupGoogleFit: googleFitManager is null after onCreate. This should not happen.");
                return;
            }
            if (!isGoogleFitOAuthRequesting) {
                isGoogleFitOAuthRequesting = true;
                googleFitManager.initiateFitSignInFlow();
            }
        } else {
            if (!isRequestingGoogleFitPermissions) {
                isRequestingGoogleFitPermissions = true;
                requestAppPermissions(FIT_PERMISSIONS, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onStepsReceived(long steps) {
        runOnUiThread(() -> {
            stepsTextView.setText(String.format(Locale.getDefault(), "%d pasos", steps));
            stepsProgressBar.setMax((int) DAILY_STEP_GOAL);
            stepsProgressBar.setProgress((int) steps);
            stepsGoalTextView.setText(String.format(Locale.getDefault(), "Meta: %d pasos", DAILY_STEP_GOAL));
            // Asegurarse de que los datos estén visibles y el botón oculto
            stepsCaloriesDataLayout.setVisibility(View.VISIBLE);
            btnConnectGoogleFit.setVisibility(View.GONE);
            saveDailyMetrics(steps, -1.0f); // Guardar pasos, calorías se actualizarán por separado
        });
    }

    @Override
    public void onCaloriesReceived(float calories) {
        runOnUiThread(() -> {
            caloriesTextView.setText(String.format(Locale.getDefault(), "%d kcal", Math.round(calories)));
            // Asegurarse de que los datos estén visibles y el botón oculto
            stepsCaloriesDataLayout.setVisibility(View.VISIBLE);
            btnConnectGoogleFit.setVisibility(View.GONE);
            saveDailyMetrics(-1L, calories); // Guardar calorías, pasos se actualizarán por separado
        });
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Google Fit Error: " + error);
        runOnUiThread(() -> {
            Toast.makeText(this, "Error de Google Fit: " + error, Toast.LENGTH_LONG).show();
            stepsTextView.setText("Pasos: N/A");
            caloriesTextView.setText("Calorías: N/A");
            // Si hay un error, mostrar el botón de conexión nuevamente
            stepsCaloriesDataLayout.setVisibility(View.GONE);
            btnConnectGoogleFit.setVisibility(View.VISIBLE);
        });
    }

    private void logout() {
        Log.d(TAG, "Iniciando logout.");
        mAuth.signOut();

        // También cierra la sesión de Google si el usuario inició sesión con Google
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAccount != null) {
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "Cierre de sesión de Google completado.");
                        navigateToWelcomeActivity();
                    });
        } else {
            navigateToWelcomeActivity();
        }
    }

    private void navigateToWelcomeActivity() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void setViewPagerEnabled(boolean enabled) {
        if (viewPagerEjercicios != null) {
            viewPagerEjercicios.setUserInputEnabled(enabled);
        }
    }

    private boolean checkPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestAppPermissions(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
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
                isRequestingGoogleFitPermissions = false;
                checkAndSetupGoogleFit();
            } else {
                Toast.makeText(this, "Permisos de Google Fit denegados. La función de conteo de pasos no estará disponible.", Toast.LENGTH_LONG).show();
                // If the permissions are denied, keep the connect button visible
                stepsCaloriesDataLayout.setVisibility(View.GONE);
                btnConnectGoogleFit.setVisibility(View.VISIBLE);
            }
        } else if (requestCode == REQUEST_NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado. Es posible que los recordatorios no funcionen.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveDailyMetrics(long steps, float calories) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user to save daily metrics.");
            return;
        }

        String userId = currentUser.getUid();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> dailyMetrics = new HashMap<>();
        dailyMetrics.put("date", Calendar.getInstance().getTime()); // Guarda el timestamp completo del día

        if (steps != -1L) {
            dailyMetrics.put("steps", steps);
        }
        if (calories != -1.0f) {
            dailyMetrics.put("caloriesBurned", calories);
        }

        db.collection("users").document(userId)
                .collection("dailyMetrics").document(todayDate)
                .set(dailyMetrics, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Métricas diarias guardadas/actualizadas exitosamente para " + todayDate))
                .addOnFailureListener(e -> Log.e(TAG, "Error al guardar métricas diarias: ", e));
    }

    private void cargarMetaYConsumoAgua() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        // Escuchar cambios en la meta diaria en tiempo real
        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists()) return;
                    int metaDiaria = 2000;
                    Long valor = documentSnapshot.getLong("dailyGoal");
                    if (valor != null) metaDiaria = valor.intValue();
                    final int metaFinal = metaDiaria;
                    textGoalWater.setText(String.valueOf(metaFinal));

                    // Escuchar cambios en los registros de agua del día en tiempo real
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Date startOfDay = calendar.getTime();
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                    Date endOfDay = calendar.getTime();

                    db.collection("users")
                            .document(userId)
                            .collection("waterLogs")
                            .whereGreaterThanOrEqualTo("timestamp", new com.google.firebase.Timestamp(startOfDay))
                            .whereLessThan("timestamp", new com.google.firebase.Timestamp(endOfDay))
                            .addSnapshotListener((value, error2) -> {
                                if (error2 != null || value == null) return;
                                int totalDelDia = 0;
                                for (DocumentSnapshot doc : value.getDocuments()) {
                                    Integer amount = doc.getLong("amount") != null ? doc.getLong("amount").intValue() : 0;
                                    totalDelDia += amount;
                                }
                                textCurrentWater.setText(String.valueOf(totalDelDia));
                                int porcentaje = (int) ((totalDelDia * 100.0f) / metaFinal);
                                porcentaje = Math.min(porcentaje, 100);
                                textProgressWaterPercentage.setText(porcentaje + "%");
                                progressBarWater.setProgress(porcentaje);
                            });
                });
    }

    private void setDrawerProfileAvatar(int resId) {
        if (drawerProfileImageView == null) return;
        drawerProfileImageView.setImageResource(resId);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setSize(180, 180);
        if (resId == R.drawable.ic_face_male) {
            bg.setColor(0xFF2196F3); // Azul
        } else if (resId == R.drawable.ic_face_female) {
            bg.setColor(0xFFE91E63); // Rosado
        } else {
            bg.setColor(0xFFE0E0E0); // Gris claro
        }
        drawerProfileImageView.setBackground(bg);
    }
}
