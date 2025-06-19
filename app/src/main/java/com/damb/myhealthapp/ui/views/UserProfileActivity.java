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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.HorizontalScrollView;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST = 1;
    private TextView nameTextView, emailTextView, birthdayTextView,
            heightTextView, weightTextView;

    private LinearLayout notificationsItem;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("MyHealthAppPrefs", MODE_PRIVATE);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        birthdayTextView = findViewById(R.id.birthdayTextView);
        heightTextView = findViewById(R.id.heightTextView);
        weightTextView = findViewById(R.id.weightTextView);
        notificationsItem = findViewById(R.id.notificationsItem);

        // Cargar imagen de perfil guardada
        int savedResId = prefs.getInt(PREF_PROFILE_IMAGE, R.drawable.ic_account_circle);
        setProfileAvatar(savedResId);
        profileImageView.setOnClickListener(v -> mostrarDialogoAvatares());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        notificationsItem.setOnClickListener(v -> {
            // Crear un Intent para iniciar NotificationsActivity
            Intent intent = new Intent(UserProfileActivity.this, NotificationsActivity.class);
            startActivity(intent); // Iniciar la nueva Activity
        });
        //BOTON EDITAR LOS DATOS DEL USUARIO
        LinearLayout manageUserItem = findViewById(R.id.manageUserItem);
        manageUserItem.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        //FIN boton
        //BOTON EDITAR PASSWORD
        LinearLayout changePasswordItem = findViewById(R.id.changePasswordItem);
        changePasswordItem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        //FIN boton
        // Boton para elminar la acc
        LinearLayout DeleteAccItem = findViewById(R.id.DeleteAccItem);

        DeleteAccItem.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(UserProfileActivity.this)
                    .setTitle("Eliminar cuenta")
                    .setMessage("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        eliminarCuentaUsuario();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });


        //

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
                                String formattedDate = convertTimestampToDate(birthdayTimestamp);
                                birthdayTextView.setText(" : " + formattedDate);
                            } else {
                                birthdayTextView.setText(" : N/A");
                            }

                            Double heightValue = ((Number) onboardingData.get("height")).doubleValue();
                            if (heightValue != null) {
                                heightTextView.setText(" : " + String.format(Locale.getDefault(), "%.1f cm", heightValue));
                            } else {
                                heightTextView.setText(": N/A cm");
                            }

                            Double weightValue = ((Number) onboardingData.get("weight")).doubleValue();
                            if (weightValue != null) {
                                weightTextView.setText(" : " + String.format(Locale.getDefault(), "%.1f kg", weightValue));
                            } else {
                                weightTextView.setText(": N/A kg");
                            }
                        } else {
                            // Si no hay datos de onboarding, establecer como N/A
                            birthdayTextView.setText(" : N/A");
                            heightTextView.setText(": N/A cm");
                            weightTextView.setText(": N/A kg");
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


    private void eliminarCuentaUsuario() {
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserProfileActivity.this, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show();
                            auth.signOut();
                            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "Error al eliminar la cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
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

}