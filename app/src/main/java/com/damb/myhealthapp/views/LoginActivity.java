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
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.FacebookAuthProvider;
import java.util.Arrays;
import com.google.firebase.auth.OAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Google Sign In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    // Facebook Sign In
    private CallbackManager mCallbackManager;

    // Microsoft Sign In
    private OAuthProvider.Builder microsoftProvider;

    // Yahoo Sign In
    private OAuthProvider.Builder yahooProvider;

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

        // Check if user is signed in (non-null) and update UI accordingly.
        // Si hay un usuario logueado al iniciar la actividad, verificarlo en Firestore
        if (mAuth.getCurrentUser() != null) {
            checkUserInFirestoreAndNavigate(mAuth.getCurrentUser());
        } else {
            // Si no hay usuario logueado, asegurarse de que la pantalla de login esté visible
            // No es necesario hacer nada aquí explícitamente, ya que setContentView() ya lo hace.
        }

        emailEditText = findViewById(R.id.emailInput);
        passwordEditText = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerLink);
        Button googleSignInButton = findViewById(R.id.buttonGoogle);
        Button facebookSignInButton = findViewById(R.id.buttonFacebook);
        Button buttonMicrosoft = findViewById(R.id.buttonMicrosoft);
        Button buttonYahoo = findViewById(R.id.buttonYahoo);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Initialize Facebook CallbackManager
        mCallbackManager = CallbackManager.Factory.create();

        // Set up Facebook Login
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                firebaseAuthWithFacebook(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Inicio de sesión con Facebook cancelado.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error en el inicio de sesión con Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        facebookSignInButton.setOnClickListener(v -> LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile")));

        // Configure Microsoft Sign In
        microsoftProvider = OAuthProvider.newBuilder("microsoft.com");
        microsoftProvider.addCustomParameter("prompt", "consent");

        // Configure Yahoo Sign In
        yahooProvider = OAuthProvider.newBuilder("yahoo.com");

        buttonMicrosoft.setOnClickListener(v -> signInWithMicrosoft());
        buttonYahoo.setOnClickListener(v -> signInWithYahoo());
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
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                            // Después de la autenticación con email/password, verificar en Firestore
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserInFirestoreAndNavigate(user);
                            } else {
                                Toast.makeText(LoginActivity.this, "Error: Usuario de Firebase nulo después del inicio de sesión.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Error de inicio de sesión: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
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

    private void firebaseAuthWithFacebook(String accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Después de la autenticación con Facebook, verificar en Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserInFirestoreAndNavigate(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: Usuario de Firebase nulo después de la autenticación con Facebook.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Autenticación con Facebook fallida: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithMicrosoft() {
        mAuth.startActivityForSignInWithProvider(this, microsoftProvider.build())
                .addOnSuccessListener(authResult -> {
                    // Sign-in successful
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Verificar si el usuario existe en Firestore
                        checkUserInFirestoreAndNavigate(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle sign-in failure
                    Toast.makeText(LoginActivity.this, "Error al iniciar sesión con Microsoft: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MicrosoftAuth", "Error signing in with Microsoft", e);
                });
    }

    private void signInWithYahoo() {
        mAuth.startActivityForSignInWithProvider(this, yahooProvider.build())
                .addOnSuccessListener(authResult -> {
                    // Sign-in successful
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Verificar si el usuario existe en Firestore
                        checkUserInFirestoreAndNavigate(user);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle sign-in failure
                    Toast.makeText(LoginActivity.this, "Error al iniciar sesión con Yahoo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("YahooAuth", "Error signing in with Yahoo", e);
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
