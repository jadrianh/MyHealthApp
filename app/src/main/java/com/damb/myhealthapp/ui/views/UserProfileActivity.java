package com.damb.myhealthapp.ui.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, birthdayTextView,
            heightTextView, weightTextView;

    private LinearLayout notificationsItem;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        birthdayTextView = findViewById(R.id.birthdayTextView);
        heightTextView = findViewById(R.id.heightTextView);
        weightTextView = findViewById(R.id.weightTextView);

        notificationsItem = findViewById(R.id.notificationsItem);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        notificationsItem.setOnClickListener(v -> {
            // Crear un Intent para iniciar NotificationsActivity
            Intent intent = new Intent(UserProfileActivity.this, NotificationsActivity.class);
            startActivity(intent); // Iniciar la nueva Activity
        });
        //BOTON EDITAR LOS DATOS DEL USUARIO
        LinearLayout manageUserItem = findViewById(R.id.manageUserItem);
        manageUserItem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, EditUserProfileActivity.class);
                startActivity(intent);
            }
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

}