package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.widget.HorizontalScrollView;
import android.widget.EditText;
import com.damb.myhealthapp.R;
import com.damb.myhealthapp.ui.components.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.GoogleAuthProvider;

public class UserProfileActivity extends BaseActivity {
    private static final int EDIT_PROFILE_REQUEST = 1;
    private TextView nameTextView, emailTextView, ageTextView,
            heightTextView, weightTextView;

    private RelativeLayout notificationsItem;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private static final String PREF_PROFILE_IMAGE = "profile_image_res";
    private int[] avatarResIds = {
        R.drawable.ic_face_male,
        R.drawable.ic_face_female,
        R.drawable.ic_account_circle,
        R.drawable.ic_launcher_foreground
    };
    private de.hdodenhof.circleimageview.CircleImageView profileImageView;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithDrawer(R.layout.activity_user_profile);

        ImageView menuIcon = findViewById(R.id.menuIcon);
        if (menuIcon != null) {
            menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("MyHealthAppPrefs", MODE_PRIVATE);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        heightTextView = findViewById(R.id.heightTextView);
        weightTextView = findViewById(R.id.weightTextView);
        ageTextView = findViewById(R.id.ageTextView);
        notificationsItem = findViewById(R.id.notificationsItem);

        // Cargar imagen de perfil guardada
        int savedResId = prefs.getInt(PREF_PROFILE_IMAGE, R.drawable.ic_account_circle);
        setProfileAvatar(savedResId);
        profileImageView.setOnClickListener(v -> mostrarDialogoAvatares());

        notificationsItem.setOnClickListener(v -> {
            // Crear un Intent para iniciar NotificationsActivity
            Intent intent = new Intent(UserProfileActivity.this, NotificationsActivity.class);
            startActivity(intent); // Iniciar la nueva Activity
        });
        //BOTON EDITAR LOS DATOS DEL USUARIO
        findViewById(R.id.manageUserItem).setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        //FIN boton
        //BOTON EDITAR PASSWORD
        findViewById(R.id.changePasswordItem).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        //FIN boton
        // Boton para elminar la acc
        findViewById(R.id.deleteAccountButton).setOnClickListener(v -> {
            iniciarDialogoDeEliminacion();
        });

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        try {
                            com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            com.google.android.gms.auth.api.signin.GoogleSignInAccount account = task.getResult(com.google.android.gms.common.api.ApiException.class);
                            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                            reautenticarYEliminar(credential);
                        } catch (com.google.android.gms.common.api.ApiException e) {
                            Toast.makeText(this, "Falló la re-autenticación con Google.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No se pudo re-autenticar. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
                    }
                });

        cargarDatosUsuario();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            cargarDatosUsuario();
            Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
        }
    }
    private void cargarDatosUsuario() {
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nameTextView.setText(documentSnapshot.getString("displayName"));
                        emailTextView.setText(documentSnapshot.getString("email"));

                        // Obtener datos de onboarding del mapa anidado
                        Map<String, Object> onboardingData = (Map<String, Object>) documentSnapshot.get("onboardingData");
                        if (onboardingData != null) {
                            Long birthdayTimestamp = (Long) onboardingData.get("birthday");
                            if (birthdayTimestamp != null) {
                                int age = calculateAge(birthdayTimestamp);
                                ageTextView.setText(age + " años");
                            } else {
                                ageTextView.setText("N/A");
                            }

                            // Usar Number para evitar problemas de casteo entre Long y Double
                            Object heightObj = onboardingData.get("height");
                            if (heightObj instanceof Number) {
                                double heightValue = ((Number) heightObj).doubleValue();
                                heightTextView.setText(String.format(Locale.getDefault(), "%.1f cm", heightValue));
                            } else {
                                heightTextView.setText("N/A cm");
                            }

                            Object weightObj = onboardingData.get("weight");
                            if (weightObj instanceof Number) {
                                double weightValue = ((Number) weightObj).doubleValue();
                                weightTextView.setText(String.format(Locale.getDefault(), "%.1f kg", weightValue));
                            } else {
                                weightTextView.setText("N/A kg");
                            }
                        } else {
                            // Si no hay datos de onboarding, establecer como N/A
                            ageTextView.setText("N/A");
                            heightTextView.setText("N/A cm");
                            weightTextView.setText("N/A kg");
                        }
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserProfileActivity", "Error al cargar datos del usuario", e);
                    Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String convertTimestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private int calculateAge(long timestamp) {
        long now = System.currentTimeMillis();
        long ageInMillis = now - timestamp;
        return (int) (ageInMillis / (1000L * 60 * 60 * 24 * 365));
    }

    private void iniciarDialogoDeEliminacion() {
        if (user == null) return;
        String providerId = user.getProviderData().get(user.getProviderData().size() - 1).getProviderId();

        if (GoogleAuthProvider.PROVIDER_ID.equals(providerId)) {
            mostrarDialogoConfirmarParaGoogle();
        } else if (EmailAuthProvider.PROVIDER_ID.equals(providerId)) {
            mostrarDialogoPasswordParaEliminar();
        }
    }

    private void reautenticarConGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void mostrarDialogoConfirmarParaGoogle() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmación para eliminar cuenta");
        builder.setMessage("Para continuar, escribe 'confirmar' en el campo de texto.");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(48, 0, 48, 0);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            if (input.getText().toString().trim().equalsIgnoreCase("confirmar")) {
                eliminarCuentaUsuario();
            } else {
                Toast.makeText(this, "Debes escribir 'confirmar' para eliminar la cuenta.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoPasswordParaEliminar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Re-autenticación requerida");
        builder.setMessage("Por tu seguridad, por favor ingresa tu contraseña para eliminar la cuenta.");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(48, 0, 48, 0);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String password = input.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, "La contraseña no puede estar vacía.", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            reautenticarYEliminar(credential);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void reautenticarYEliminar(AuthCredential credential) {
        if (user == null) {
            Toast.makeText(this, "No se pudo obtener la información del usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                user.delete().addOnCompleteListener(deleteTask -> {
                    if (deleteTask.isSuccessful()) {
                        Toast.makeText(UserProfileActivity.this, "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "Error al eliminar la cuenta: " + deleteTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(UserProfileActivity.this, "La re-autenticación falló. Inténtalo de nuevo.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDialogoAvatares() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona tu avatar");
        ImageView[] imageViews = new ImageView[avatarResIds.length];
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(32, 32, 32, 32);
        for (int i = 0; i < avatarResIds.length; i++) {
            final int resId = avatarResIds[i];
            imageViews[i] = new ImageView(this);
            imageViews[i].setImageResource(resId);
            imageViews[i].setPadding(24, 0, 24, 0);
            imageViews[i].setLayoutParams(new LinearLayout.LayoutParams(180, 180));

            // Fondo circular de color según el avatar
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
            imageViews[i].setBackground(bg);
        }
        // Hacer el layout desplazable
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.addView(layout);
        builder.setView(scrollView);
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        // Listener para seleccionar y cerrar
        for (int i = 0; i < avatarResIds.length; i++) {
            final int resId = avatarResIds[i];
            imageViews[i].setOnClickListener(v -> {
                setProfileAvatar(resId);
                prefs.edit().putInt(PREF_PROFILE_IMAGE, resId).apply();
                dialog.dismiss();
            });
            layout.addView(imageViews[i]);
        }
        dialog.show();
    }

    private void setProfileAvatar(int resId) {
        profileImageView.setImageResource(resId);
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
        profileImageView.setBackground(bg);
    }

    private void eliminarCuentaUsuario() {
        if (user == null) return;

        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UserProfileActivity.this, "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                    iniciarDialogoDeEliminacion();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Error al eliminar la cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}