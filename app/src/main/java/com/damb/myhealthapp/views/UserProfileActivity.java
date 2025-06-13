package com.damb.myhealthapp.views;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private TextView emailTextView, genderTextView, birthdayTextView,
            heightTextView, weightTextView, activityLevelTextView;

    private EditText currentPasswordInput, newPasswordInput, confirmPasswordInput;
    private Button updatePasswordButton;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_user_profile);

            // Inicializar Firebase
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            db = FirebaseFirestore.getInstance();

            // Referencias de vistas
            emailTextView = findViewById(R.id.emailTextView);
            genderTextView = findViewById(R.id.genderTextView);
            birthdayTextView = findViewById(R.id.birthdayTextView);
            heightTextView = findViewById(R.id.heightTextView);
            weightTextView = findViewById(R.id.weightTextView);
            activityLevelTextView = findViewById(R.id.activityLevelTextView);

            currentPasswordInput = findViewById(R.id.currentPasswordInput);
            newPasswordInput = findViewById(R.id.newPasswordInput);
            confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
            updatePasswordButton = findViewById(R.id.updatePasswordButton);

            updatePasswordButton.setOnClickListener(v -> actualizarPassword());

            cargarDatosUsuario();

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarDatosUsuario() {
        try {
            if (user == null) return;

            String uid = user.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (documentSnapshot.exists()) {
                                emailTextView.setText("Correo: " + documentSnapshot.getString("email"));
                                genderTextView.setText("Género: " + documentSnapshot.getString("gender"));
                                birthdayTextView.setText("Fecha de nacimiento: " + documentSnapshot.get("birthday").toString());
                                heightTextView.setText("Altura: " + documentSnapshot.get("height").toString());
                                weightTextView.setText("Peso: " + documentSnapshot.get("weight").toString());
                                activityLevelTextView.setText("Nivel de actividad: " + documentSnapshot.getString("activityLevel"));
                            } else {
                                Toast.makeText(this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Error al mostrar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al obtener datos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        } catch (Exception e) {
            Toast.makeText(this, "Excepción al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarPassword() {
        try {
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = user.getEmail();
            if (email == null) {
                Toast.makeText(this, "Usuario sin correo registrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Contraseña actualizada correctamente.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Error al actualizar contraseña: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Contraseña actual incorrecta.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Excepción al actualizar contraseña: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
