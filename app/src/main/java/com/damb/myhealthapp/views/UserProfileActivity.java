package com.damb.myhealthapp.views;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userEmailText;
    private EditText userNameEdit;
    private Button saveButton, deletePasswordButton, logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userEmailText = findViewById(R.id.userEmail);
        userNameEdit = findViewById(R.id.userName);
        saveButton = findViewById(R.id.saveButton);
        deletePasswordButton = findViewById(R.id.deletePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No hay usuario activo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userEmailText.setText(currentUser.getEmail());

        saveButton.setOnClickListener(v -> {
            String name = userNameEdit.getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                // Aquí podrías guardar el nombre en Firestore o Realtime DB
                Toast.makeText(this, "Nombre actualizado localmente: " + name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ingresa un nombre válido.", Toast.LENGTH_SHORT).show();
            }
        });

        deletePasswordButton.setOnClickListener(v -> confirmDeletePassword());

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void confirmDeletePassword() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar contraseña")
                .setMessage("¿Estás seguro de que deseas eliminar tu contraseña? Esto deshabilitará el inicio de sesión por email.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    currentUser.unlink("password").addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Contraseña eliminada.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
