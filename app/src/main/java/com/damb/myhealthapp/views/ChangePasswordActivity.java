package com.damb.myhealthapp.views;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton; // Importación necesaria para ImageButton
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class ChangePasswordActivity extends AppCompatActivity{
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button changePasswordButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_activity);

        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        auth = FirebaseAuth.getInstance();

        changePasswordButton.setOnClickListener(v -> mostrarDialogoConfirmacion());
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente deseas cambiar tu contraseña?")
                .setPositiveButton("Sí, cambiar contraseña", (dialog, which) -> cambiarPassword())
                .setNegativeButton("No cambiar contraseña", null)
                .show();
    }

    private void cambiarPassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        String currentPass = currentPasswordEditText.getText().toString().trim();
        String newPass = newPasswordEditText.getText().toString().trim();
        String confirmPass = confirmPasswordEditText.getText().toString().trim();

        // Validaciones
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPass);

        user.reauthenticate(credential).addOnSuccessListener(unused -> {
            user.updatePassword(newPass).addOnSuccessListener(unused1 -> {
                Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Error al cambiar contraseña: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }).addOnFailureListener(e ->
                Toast.makeText(this, "La contraseña actual no es válida", Toast.LENGTH_SHORT).show()
        );
    }
}
