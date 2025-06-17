package com.damb.myhealthapp.ui.views;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton; // Importación necesaria para ImageButton
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;

public class DeleteAcc extends AppCompatActivity {
    Button btnEliminarCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_acc);

        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);

        btnEliminarCuenta.setOnClickListener(v -> mostrarPrimerDialogo());
    }

    private void mostrarPrimerDialogo() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro que quieres eliminar la cuenta?")
                .setPositiveButton("SÍ", (dialog, which) -> {
                    dialog.dismiss();
                    mostrarSegundoDialogo();
                })
                .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void mostrarSegundoDialogo() {
        final EditText input = new EditText(this);
        input.setHint("Escribe CONFIRMO");

        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Escribe CONFIRMO para eliminar tu cuenta")
                .setView(input)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String textoIngresado = input.getText().toString().trim();
                    if ("CONFIRMO".equals(textoIngresado)) {
                        eliminarCuenta();
                    } else {
                        Toast.makeText(this, "Texto incorrecto. La cuenta no fue eliminada.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void eliminarCuenta() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user != null) {
            String uid = user.getUid(); // Este UID es el ID del documento en "users"

            // 1. Eliminar el documento del usuario en la colección "users"
            db.collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // 2. Después de borrar Firestore, eliminamos la cuenta de Firebase Auth
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Cuenta eliminada con éxito", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(this, WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Error al eliminar cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al eliminar datos del usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}
