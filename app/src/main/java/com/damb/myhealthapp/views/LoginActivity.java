package com.damb.myhealthapp.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.onboarding.BirthdayProfileActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView appName;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Hemos eliminado la lógica de redirección automática desde onCreate.
        // Esto asegura que la pantalla de inicio de sesión siempre sea visible al lanzar LoginActivity.
        // La verificación de la sesión actual ahora se maneja en MainActivity al iniciar.

        emailEditText = findViewById(R.id.emailInput);
        passwordEditText = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.btnSignUp);
        registerButton = findViewById(R.id.registerLink);
        Button googleSignInButton = findViewById(R.id.btnGoogleSignUp);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, BirthdayProfileActivity.class);
            startActivity(intent);
        });

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa correo y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    // El correo está verificado, proceder con el inicio de sesión
                                    // Cerrar cualquier sesión de Google existente para evitar conflictos
                                    if (mGoogleSignInClient != null) {
                                        mGoogleSignInClient.signOut()
                                                .addOnCompleteListener(signOutTask -> {
                                                    Log.d(TAG, "Cierre de sesión de Google completado después de iniciar sesión con correo/contraseña.");
                                                    checkUserInFirestoreAndNavigate(user);
                                                });
                                    } else {
                                        checkUserInFirestoreAndNavigate(user);
                                    }
                                } else {
                                    // El correo no está verificado
                                    mAuth.signOut();
                                    Toast.makeText(LoginActivity.this, 
                                        "Por favor, verifica tu correo electrónico antes de iniciar sesión.", 
                                        Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, 
                                "Error de inicio de sesión: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
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
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Verificar si el usuario existe en Firestore (centralizado en nuevo método)
                            checkUserInFirestoreAndNavigate(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: Usuario de Firebase nulo después de la autenticación.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Autenticación con Google fallida: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Nuevo método para verificar el usuario en Firestore y navegar
    private void checkUserInFirestoreAndNavigate(FirebaseUser user) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // El usuario existe en Firestore, permitir el acceso
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    } else {
                        // El usuario no existe en Firestore, cerrar sesión y mostrar mensaje
                        mAuth.signOut();
                        Toast.makeText(LoginActivity.this, "Cuenta no registrada. Por favor, regístrate.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al consultar Firestore
                    mAuth.signOut(); // Asegurarse de cerrar sesión en Firebase si hay un error en Firestore
                    Toast.makeText(LoginActivity.this, "Error al verificar el usuario en Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close LoginActivity
    }
}
