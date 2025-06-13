package com.damb.myhealthapp.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.views.LoginActivity;
import com.google.android.material.button.MaterialButton;
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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_MICROSOFT_SIGN_IN = 9002;
    
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private MaterialButton confirmButton;
    private MaterialButton buttonGoogle;
    private MaterialButton buttonFacebook;
    private MaterialButton buttonMicrosoft;
    private MaterialButton buttonYahoo;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private OAuthProvider.Builder microsoftProvider;
    private OAuthProvider.Builder yahooProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        confirmButton = findViewById(R.id.confirmButton);
        buttonGoogle = findViewById(R.id.buttonGoogle);
        buttonFacebook = findViewById(R.id.buttonFacebook);
        buttonMicrosoft = findViewById(R.id.buttonMicrosoft);
        buttonYahoo = findViewById(R.id.buttonYahoo);

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
                        Toast.makeText(RegisterActivity.this, "Facebook login cancelado",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(RegisterActivity.this, "Error en login de Facebook: " + error.getMessage(),
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

        // Set up social media buttons
        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botón de Google clickeado");
                signInWithGoogle();
            }
        });
        buttonFacebook.setOnClickListener(v -> signInWithFacebook());
        buttonMicrosoft.setOnClickListener(v -> signInWithMicrosoft());
        buttonYahoo.setOnClickListener(v -> signInWithYahoo());

        // Get onboarding data
        long birthdayMillis = getIntent().getLongExtra("birthday", -1L);
        String gender = getIntent().getStringExtra("gender");
        int height = getIntent().getIntExtra("height", 0);
        int weight = getIntent().getIntExtra("weight", 0);
        String activityLevel = getIntent().getStringExtra("activity_level");

        // Set up confirmation button
        confirmButton.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                // Create user in Firebase Auth
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // User created successfully
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Send verification email
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                // Create map with user data
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("email", email);
                                                userData.put("birthday", birthdayMillis);
                                                userData.put("gender", gender);
                                                userData.put("height", height);
                                                userData.put("weight", weight);
                                                userData.put("activityLevel", activityLevel);
                                                userData.put("emailVerified", false);

                                                // Save data to Firestore
                                                db.collection("users").document(user.getUid())
                                                        .set(userData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "User data saved successfully");
                                                            Toast.makeText(RegisterActivity.this,
                                                                    "Se ha enviado un correo de verificación. Por favor, verifica tu correo antes de iniciar sesión.",
                                                                    Toast.LENGTH_LONG).show();
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
                                            } else {
                                                Log.w(TAG, "Error sending verification email", verificationTask.getException());
                                                Toast.makeText(RegisterActivity.this,
                                                        "Error al enviar correo de verificación: " + verificationTask.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Registration error
                                Log.w(TAG, "Registration error", task.getException());
                                Toast.makeText(RegisterActivity.this,
                                        "Error en el registro: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void signInWithGoogle() {
        Log.d(TAG, "Iniciando signInWithGoogle");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.d(TAG, "Intent de Google Sign-In creado: " + signInIntent);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.d(TAG, "startActivityForResult llamado con RC_SIGN_IN: " + RC_SIGN_IN);
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void signInWithMicrosoft() {
        mAuth.startActivityForSignInWithProvider(this, microsoftProvider.build())
                .addOnSuccessListener(authResult -> {
                    // User is signed in. Get the User object
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Verificar si el usuario ya existe en Firestore
                        db.collection("users").document(user.getUid())
                            .get()
                            .addOnCompleteListener(documentTask -> {
                                if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                    // Usuario ya existe, mostrar mensaje y redirigir a login
                                    mAuth.signOut();
                                    Toast.makeText(RegisterActivity.this,
                                        "Esta cuenta ya está registrada. Por favor, inicia sesión.",
                                        Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Usuario no existe, guardar datos y continuar
                                    saveUserData(user);
                                    startMainActivity();
                                }
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.w(TAG, "Microsoft sign in failed", e);
                    Toast.makeText(RegisterActivity.this, "Error en login de Microsoft: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void signInWithYahoo() {
        mAuth.startActivityForSignInWithProvider(this, yahooProvider.build())
                .addOnSuccessListener(authResult -> {
                    // Sign-in successful
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Verificar si el usuario ya existe en Firestore
                        db.collection("users").document(user.getUid())
                            .get()
                            .addOnCompleteListener(documentTask -> {
                                if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                    // Usuario ya existe, mostrar mensaje y redirigir a login
                                    mAuth.signOut();
                                    Toast.makeText(RegisterActivity.this,
                                        "Esta cuenta ya está registrada. Por favor, inicia sesión.",
                                        Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Usuario no existe, guardar datos y continuar
                                    saveUserData(user);
                                    startMainActivity();
                                }
                            });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle sign-in failure
                    Toast.makeText(RegisterActivity.this, "Error al iniciar sesión con Yahoo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("YahooAuth", "Error signing in with Yahoo", e);
                });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(RegisterActivity.this, "Error en login de Google: " + e.getMessage(),
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
                            // Verificar si el usuario ya existe en Firestore
                            db.collection("users").document(user.getUid())
                                .get()
                                .addOnCompleteListener(documentTask -> {
                                    if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                        // Usuario ya existe, mostrar mensaje y redirigir a login
                                        mAuth.signOut();
                                        mGoogleSignInClient.signOut();
                                        Toast.makeText(RegisterActivity.this,
                                            "Esta cuenta ya está registrada. Por favor, inicia sesión.",
                                            Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Usuario no existe, guardar datos y continuar
                                        saveUserData(user);
                                        startMainActivity();
                                    }
                                });
                        }
                    } else {
                        // Si la autenticación con credencial falla
                        Log.w(TAG, "signInWithCredential[Google]:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error en autenticación con Google: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        mAuth.signInWithCredential(FacebookAuthProvider.getCredential(token.getToken()))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Verificar si el usuario ya existe en Firestore
                            db.collection("users").document(user.getUid())
                                .get()
                                .addOnCompleteListener(documentTask -> {
                                    if (documentTask.isSuccessful() && documentTask.getResult().exists()) {
                                        // Usuario ya existe, mostrar mensaje y redirigir a login
                                        mAuth.signOut();
                                        LoginManager.getInstance().logOut();
                                        Toast.makeText(RegisterActivity.this,
                                            "Esta cuenta ya está registrada. Por favor, inicia sesión.",
                                            Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Usuario no existe, guardar datos y continuar
                                        saveUserData(user);
                                        startMainActivity();
                                    }
                                });
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error en autenticación: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(FirebaseUser user) {
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("displayName", user.getDisplayName());
            userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
            userData.put("provider", user.getProviderData().get(0).getProviderId());

            db.collection("users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User data saved successfully"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error saving user data", e));
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
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
        // Note: Microsoft login is handled by startActivityForSignInWithProvider and doesn't use onActivityResult
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