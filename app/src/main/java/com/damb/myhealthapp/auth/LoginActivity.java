package com.damb.myhealthapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damb.myhealthapp.MainActivity;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.onboarding.BirthdayProfileActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_MICROSOFT_SIGN_IN = 9002; // Código no usado por startActivityForSignInWithProvider
    private static final int RC_YAHOO_SIGN_IN = 9003; // Código no usado por startActivityForSignInWithProvider

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private TextView forgotPasswordLink;
    private Button buttonGoogle;
    private Button buttonFacebook;
    private Button buttonMicrosoft;
    private Button buttonYahoo;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private OAuthProvider.Builder microsoftProvider;
    private OAuthProvider.Builder yahooProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        // Initialize social media buttons
        buttonGoogle = findViewById(R.id.buttonGoogle);
        buttonFacebook = findViewById(R.id.buttonFacebook);
        buttonMicrosoft = findViewById(R.id.buttonMicrosoft);
        buttonYahoo = findViewById(R.id.buttonYahoo);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Forzar cierre de sesión de Google para mostrar el selector de cuentas
        mGoogleSignInClient.signOut();

        // Configure Facebook Sign In
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Facebook login cancelado",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this, "Error en login de Facebook: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Configure Microsoft Sign In
        microsoftProvider = OAuthProvider.newBuilder("microsoft.com");
        // Optional: Add custom parameters or scopes if needed for Microsoft
        // microsoftProvider.addCustomParameter("prompt", "consent");
        // microsoftProvider.setScopes(Arrays.asList("mail.read"));

        // Configure Yahoo Sign In
        yahooProvider = OAuthProvider.newBuilder("yahoo.com");

        // Set up social media buttons click listeners
        buttonGoogle.setOnClickListener(v -> signInWithGoogle());
        buttonFacebook.setOnClickListener(v -> signInWithFacebook());
        buttonMicrosoft.setOnClickListener(v -> signInWithMicrosoft());
        buttonYahoo.setOnClickListener(v -> signInWithYahoo());

        // Configurar el botón de login
        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                
                // Iniciar sesión con Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Verificar si el correo está verificado
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Inicio de sesión exitoso
                                Log.d(TAG, "signInWithEmail:success");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Correo no verificado
                                mAuth.signOut(); // Cerrar sesión
                                Toast.makeText(LoginActivity.this,
                                    "Por favor, verifica tu correo electrónico antes de iniciar sesión",
                                    Toast.LENGTH_LONG).show();
                                
                                // Reenviar correo de verificación si el usuario lo solicita
                                if (user != null) {
                                    user.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this,
                                                    "Se ha enviado un nuevo correo de verificación",
                                                    Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this,
                                                    "Error al enviar correo de verificación: " + verificationTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                }
                            }
                        } else {
                            // Error en el inicio de sesión
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this,
                                "Error en el inicio de sesión: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        // Configurar el enlace de registro
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, BirthdayProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Configurar el enlace de olvidé mi contraseña
        forgotPasswordLink.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Ingresa tu correo electrónico para restablecer la contraseña");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Ingresa un correo electrónico válido");
                return;
            }

            // Enviar correo de restablecimiento
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                            "Se ha enviado un correo para restablecer tu contraseña",
                            Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                            "Error al enviar el correo: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    // Métodos para inicio de sesión social

    private void signInWithGoogle() {
        Log.d(TAG, "Iniciando signInWithGoogle");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.d(TAG, "Intent de Google Sign-In creado: " + signInIntent);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.d(TAG, "startActivityForResult llamado con RC_SIGN_IN: " + RC_SIGN_IN);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(LoginActivity.this, "Error en login de Google: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión con Google exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Verificar si el usuario existe en Firestore
                            db.collection("users").document(user.getUid())
                                .get()
                                .addOnCompleteListener(documentTask -> {
                                    if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                        // Usuario existe en Firestore, permitir inicio de sesión
                                        startMainActivity();
                                    } else {
                                        // Usuario no existe en Firestore, cerrar sesión y mostrar mensaje
                                        mAuth.signOut();
                                        mGoogleSignInClient.signOut();
                                        Toast.makeText(LoginActivity.this,
                                            "Esta cuenta no está registrada. Por favor, regístrate primero.",
                                            Toast.LENGTH_LONG).show();
                                    }
                                });
                        }
                    } else {
                        // Si la autenticación con credencial falla
                        Log.w(TAG, "signInWithCredential[Google]:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Error en autenticación con Google: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para iniciar sesión con Facebook
    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    // Método para manejar el AccessToken de Facebook
    private void handleFacebookAccessToken(AccessToken token) {
        mAuth.signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión con Facebook exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Verificar si el usuario existe en Firestore
                            db.collection("users").document(user.getUid())
                                .get()
                                .addOnCompleteListener(documentTask -> {
                                    if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                        // Usuario existe en Firestore, permitir inicio de sesión
                                        startMainActivity();
                                    } else {
                                        // Usuario no existe en Firestore, cerrar sesión y mostrar mensaje
                                        mAuth.signOut();
                                        LoginManager.getInstance().logOut();
                                        Toast.makeText(LoginActivity.this,
                                            "Esta cuenta no está registrada. Por favor, regístrate primero.",
                                            Toast.LENGTH_LONG).show();
                                    }
                                });
                        }
                    } else {
                        // Si la autenticación con credencial falla
                        Log.w(TAG, "signInWithCredential[Facebook]:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Error en autenticación con Facebook: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para iniciar sesión con Microsoft
    private void signInWithMicrosoft() {
        mAuth.startActivityForSignInWithProvider(this, microsoftProvider.build())
                .addOnSuccessListener(authResult -> {
                    // Inicio de sesión con Microsoft exitoso
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Verificar si el usuario existe en Firestore
                        db.collection("users").document(user.getUid())
                            .get()
                            .addOnCompleteListener(documentTask -> {
                                if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                    // Usuario existe en Firestore, permitir inicio de sesión
                                    startMainActivity();
                                } else {
                                    // Usuario no existe en Firestore, cerrar sesión y mostrar mensaje
                                    mAuth.signOut();
                                    Toast.makeText(LoginActivity.this,
                                        "Esta cuenta no está registrada. Por favor, regístrate primero.",
                                        Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejar error de inicio de sesión con Microsoft
                    Log.w(TAG, "Microsoft sign in failed", e);
                    Toast.makeText(LoginActivity.this, "Error en login de Microsoft: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Método para iniciar sesión con Yahoo
    private void signInWithYahoo() {
        mAuth.startActivityForSignInWithProvider(this, yahooProvider.build())
                .addOnSuccessListener(authResult -> {
                    // Inicio de sesión con Yahoo exitoso
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Verificar si el usuario existe en Firestore
                        db.collection("users").document(user.getUid())
                            .get()
                            .addOnCompleteListener(documentTask -> {
                                if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                    // Usuario existe en Firestore, permitir inicio de sesión
                                    startMainActivity();
                                } else {
                                    // Usuario no existe en Firestore, cerrar sesión y mostrar mensaje
                                    mAuth.signOut();
                                    Toast.makeText(LoginActivity.this,
                                        "Esta cuenta no está registrada. Por favor, regístrate primero.",
                                        Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejar error de inicio de sesión con Yahoo
                    Log.w(TAG, "Yahoo sign in failed", e);
                    Toast.makeText(LoginActivity.this, "Error en login de Yahoo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        // Note: Microsoft and Yahoo login are handled by startActivityForSignInWithProvider and don't use onActivityResult
    }

    private boolean validateInputs() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("El correo electrónico es requerido");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Ingresa un correo electrónico válido");
            return false;
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("La contraseña es requerida");
            return false;
        }

        return true;
    }
}
