package com.damb.myhealthapp.ui.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem; // Importar para manejar clicks en items del menú
import android.view.View;
// import android.widget.Button; // Unused, removed
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.damb.myhealthapp.ui.adapters.ExerciseViewPagerAdapter;
// import com.google.android.material.tabs.TabLayout; // Unused, removed
// import com.google.android.material.tabs.TabLayoutMediator; // Unused, removed
import com.damb.myhealthapp.ui.components.GoogleFitManager;

import java.util.ArrayList;
// import java.util.Arrays; // Unused, removed
import java.util.List;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private ImageView menuIcon;
    private static ViewPager2 viewPagerEjercicios;
    private GoogleFitManager googleFitManager;
    private TextView stepsTextView;
    private TextView caloriesTextView;
    private TextView stepsGoalTextView;
    private ProgressBar stepsProgressBar;
    private LinearLayout stepsCaloriesDataLayout;
    private MaterialButton btnConnectGoogleFit;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView textViewDrawerUsername;
    private TextView textViewDrawerUserEmail;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final long DAILY_STEP_GOAL = 10000;
    private boolean isRequestingGoogleFitPermissions = false;
    private boolean isGoogleFitOAuthRequesting = false;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Username = findViewById(R.id.Username);
        menuIcon = findViewById(R.id.menuIcon);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        if (headerView != null) {
            textViewDrawerUsername = headerView.findViewById(R.id.textView_drawer_username);
            textViewDrawerUserEmail = headerView.findViewById(R.id.textView_drawer_user_email);
        }

        // Set up navigation listener
        navigationView.setNavigationItemSelectedListener(this);

        // Set up drawer menu icon click listener
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set up ViewPager2
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
        ExerciseViewPagerAdapter exerciseViewPagerAdapter = new ExerciseViewPagerAdapter(this, tiposEjercicio);
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
    }

    private void checkCurrentUserAndLoadData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            googleFitManager = new GoogleFitManager(this, this);

            btnConnectGoogleFit.setOnClickListener(v -> {
                if (googleFitManager != null) { // Siempre será true aquí si currentUser != null
                    Log.d(TAG, "btnConnectGoogleFit clicked. Initiating Google Fit flow.");
                    googleFitManager.initiateFitSignInFlow();
                }
            });

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

    private void loadDisplayNameFromFirestore(String userId) {
        // Asumo que tienes una colección "users" y cada documento de usuario tiene el UID como ID.
        // Y dentro de ese documento, tienes un campo llamado "displayName".
        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String firestoreDisplayName = document.getString("displayName");
                        // Asignar el nombre de usuario desde Firestore o "Usuario"
                        Username.setText(firestoreDisplayName != null && !firestoreDisplayName.trim().isEmpty() ? firestoreDisplayName : "Usuario");
                        textViewDrawerUsername.setText(firestoreDisplayName != null && !firestoreDisplayName.trim().isEmpty() ? firestoreDisplayName : "Usuario");
                        Log.d(TAG, "Display name from Firestore: " + firestoreDisplayName);
                    } else {
                        Log.d(TAG, "No such document for user ID: " + userId);
                        // Documento no existe, mostrar "Usuario"
                        Username.setText("Usuario");
                        textViewDrawerUsername.setText("Usuario");
                    }
                } else {
                    Log.e(TAG, "Failed to get user document: ", task.getException());
                    // Error al obtener el documento, mostrar "Usuario"
                    Username.setText("Usuario");
                    textViewDrawerUsername.setText("Usuario");
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_activity_record) {
            Intent recordIntent = new Intent(MainActivity.this, TrainingPlanDetailsActivity.class);
            startActivity(recordIntent);
        } else if (id == R.id.nav_profile) {
            Intent profileIntent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(profileIntent);
        } else if (id == R.id.nav_logout) {
            logout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (googleFitManager != null) {
            googleFitManager.handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStepsReceived(long steps) {
        runOnUiThread(() -> {
            stepsTextView.setText(String.format("Pasos hoy: %d", steps));
            stepsGoalTextView.setText(String.format("Meta: %d / %d pasos", steps, DAILY_STEP_GOAL));
            stepsProgressBar.setProgress((int) Math.min(steps, DAILY_STEP_GOAL));
            // Show data, hide connect button
            stepsCaloriesDataLayout.setVisibility(View.VISIBLE);
            btnConnectGoogleFit.setVisibility(View.GONE);
        });
    }

    @Override
    public void onCaloriesReceived(float calories) {
        runOnUiThread(() -> {
            caloriesTextView.setText(String.format("Calorías: %.0f", calories));
            // Show data, hide connect button
            stepsCaloriesDataLayout.setVisibility(View.VISIBLE);
            btnConnectGoogleFit.setVisibility(View.GONE);
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Google Fit Error: " + error);
            // If there's an error, keep the connect button visible
            stepsCaloriesDataLayout.setVisibility(View.GONE);
            btnConnectGoogleFit.setVisibility(View.VISIBLE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewPagerEnabled(true);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onResume: Current Firebase user email: " + currentUser.getEmail());
            
            // Initialize GoogleFitManager if it's null
            if (googleFitManager == null) {
                googleFitManager = new GoogleFitManager(this, this);
            }

            // Show connect button and hide data initially
            stepsCaloriesDataLayout.setVisibility(View.GONE);
            btnConnectGoogleFit.setVisibility(View.VISIBLE);

        } else {
            Log.d(TAG, "onResume: No hay usuario autenticado en Firebase. Redirigiendo a WelcomeActivity.");
            googleFitManager = null;
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
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

    /**
     * Habilita o deshabilita la interacción del usuario con el ViewPager2.
     * @param enabled true para habilitar, false para deshabilitar.
     */
    public void setViewPagerEnabled(boolean enabled) {
        if (viewPagerEjercicios != null) {
            viewPagerEjercicios.setUserInputEnabled(enabled);
        }
    }

    // Método para verificar los permisos de runtime
    private boolean checkPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Método para solicitar permisos de runtime
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
                // Permisos concedidos. Ahora, inicializa GoogleFitManager e inicia el flujo OAuth.
                // El método checkAndSetupGoogleFit() ya maneja esta lógica verificando el proveedor del usuario actual.
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
}
