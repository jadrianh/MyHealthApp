package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final int RC_SIGN_IN = 9001;
    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private MaterialButton btnSignUp, buttonGoogle;
    private TextView loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        btnSignUp = findViewById(R.id.btnSignUp);
        loginButton = findViewById(R.id.loginLink);
        buttonGoogle = findViewById(R.id.btnGoogleSignUp);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Google Sign In Launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(RegisterActivity.this, 
                                "Error en login de Google: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Set up social media buttons
        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // Get onboarding data
        long birthdayMillis = getIntent().getLongExtra("birthday", -1L);
        String gender = getIntent().getStringExtra("gender");
        int height = getIntent().getIntExtra("height", 0);
        int weight = getIntent().getIntExtra("weight", 0);
        String activityLevel = getIntent().getStringExtra("activity_level");

        // Set up confirmation button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Enviar correo de verificación
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verificationTask -> {
                                                if (verificationTask.isSuccessful()) {
                                                    // Guardar datos del usuario en Firestore
                                                    Map<String, Object> userData = new HashMap<>();
                                                    userData.put("email", email);
                                                    userData.put("displayName", name);
                                                    userData.put("provider", "email");

                                                    // Crear un mapa para los datos del onboarding
                                                    Map<String, Object> onboardingData = new HashMap<>();
                                                    long birthdayMillis = getIntent().getLongExtra("birthday", -1L);
                                                    String gender = getIntent().getStringExtra("gender");
                                                    int height = getIntent().getIntExtra("height", 0);
                                                    int weight = getIntent().getIntExtra("weight", 0);
                                                    String activityLevel = getIntent().getStringExtra("activity_level");

                                                    if (birthdayMillis != -1L) {
                                                        onboardingData.put("birthday", birthdayMillis);
                                                        Log.d(TAG, "Guardando fecha de cumpleaños: " + birthdayMillis);
                                                    }
                                                    if (gender != null) onboardingData.put("gender", gender);
                                                    if (height != 0) onboardingData.put("height", height);
                                                    if (weight != 0) onboardingData.put("weight", weight);
                                                    if (activityLevel != null) onboardingData.put("activity_level", activityLevel);

                                                    // Añadir el mapa de onboarding al mapa principal del usuario
                                                    userData.put("onboardingData", onboardingData);

                                                    db.collection("users").document(user.getUid())
                                                            .set(userData)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d(TAG, "User data saved successfully");
                                                                Toast.makeText(RegisterActivity.this,
                                                                        "Se ha enviado un correo de verificación. Por favor, verifica tu correo antes de iniciar sesión.",
                                                                        Toast.LENGTH_LONG).show();
                                                                // Cerrar sesión y redirigir al login
                                                                mAuth.signOut();
                                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.w(TAG, "Error saving user data", e);
                                                                Toast.makeText(RegisterActivity.this,
                                                                        "Error al guardar datos: " + e.getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error en el registro: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void signInWithGoogle() {
        Log.d(TAG, "Iniciando signInWithGoogle");
        
        // Cerrar sesión de Google primero para forzar el selector de cuentas
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d(TAG, "Sesión de Google cerrada, iniciando nuevo flujo de inicio de sesión");
            // Configurar las opciones de Google Sign-In para forzar el selector de cuentas
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            
            // Crear un nuevo cliente con las opciones actualizadas
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            
            // Lanzar el intent de inicio de sesión
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            Log.d(TAG, "Intent de Google Sign-In creado: " + signInIntent);
            googleSignInLauncher.launch(signInIntent);
            Log.d(TAG, "googleSignInLauncher.launch llamado");
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Iniciando firebaseAuthWithGoogle");
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Autenticación con Google exitosa");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Verificar si el usuario ya existe en Firestore
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(documentTask -> {
                                        if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                            // Usuario ya existe, redirigir directamente al home
                                            Log.d(TAG, "Usuario existente, redirigiendo al home");
                                            startMainActivity();
                                        } else {
                                            // Usuario no existe, guardar datos y continuar
                                            Log.d(TAG, "Usuario nuevo, guardando datos");
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("email", user.getEmail());
                                            userData.put("displayName", user.getDisplayName());
                                            userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                                            userData.put("provider", "google");

                                            // Crear un mapa para los datos del onboarding
                                            Map<String, Object> onboardingData = new HashMap<>();
                                            long birthdayMillis = getIntent().getLongExtra("birthday", -1L);
                                            String gender = getIntent().getStringExtra("gender");
                                            int height = getIntent().getIntExtra("height", 0);
                                            int weight = getIntent().getIntExtra("weight", 0);
                                            String activityLevel = getIntent().getStringExtra("activity_level");

                                            if (birthdayMillis != -1L) {
                                                onboardingData.put("birthday", birthdayMillis);
                                                Log.d(TAG, "Guardando fecha de cumpleaños: " + birthdayMillis);
                                            }
                                            if (gender != null) onboardingData.put("gender", gender);
                                            if (height != 0) onboardingData.put("height", height);
                                            if (weight != 0) onboardingData.put("weight", weight);
                                            if (activityLevel != null) onboardingData.put("activity_level", activityLevel);

                                            // Añadir el mapa de onboarding al mapa principal del usuario
                                            userData.put("onboardingData", onboardingData);

                                            db.collection("users").document(user.getUid())
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Datos de usuario guardados exitosamente");
                                                        startMainActivity();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.w(TAG, "Error al guardar datos del usuario", e);
                                                        Toast.makeText(RegisterActivity.this,
                                                                "Error al guardar datos: " + e.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    });
                        }
                    } else {
                        Log.w(TAG, "Error en la autenticación con Google", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error en la autenticación con Google: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateInputs() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("El correo electrónico es requerido");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Ingresa un correo electrónico válido");
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("La contraseña es requerida");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Confirma tu contraseña");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }
}