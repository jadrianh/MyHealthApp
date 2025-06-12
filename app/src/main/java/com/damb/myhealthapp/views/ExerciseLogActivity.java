package com.damb.myhealthapp.views;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;

public class ExerciseLogActivity extends AppCompatActivity {

    private TextView textViewListaRegistros;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_log);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textViewListaRegistros = findViewById(R.id.textViewListaRegistros);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            cargarRegistrosEjercicio(user.getUid());
        } else {
            textViewListaRegistros.setText("Usuario no autenticado. No se pueden cargar los registros.");
        }
    }

    private void cargarRegistrosEjercicio(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date inicioDelDia = calendar.getTime();

        db.collection("users").document(userId).collection("registrosEjercicio")
                .whereGreaterThanOrEqualTo("fecha", inicioDelDia)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder registrosTexto = new StringBuilder("Rutinas completadas hoy:\n");
                    if (queryDocumentSnapshots.isEmpty()) {
                        registrosTexto.append("Ninguna aún.");
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String tipoRutina = document.getString("tipoRutina");
                            if (tipoRutina != null) {
                                registrosTexto.append("- ").append(tipoRutina).append("\n");
                            }
                        }
                    }
                    textViewListaRegistros.setText(registrosTexto.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("RegistrarEjercicioActivity", "Error al cargar registros de ejercicio", e);
                    textViewListaRegistros.setText("Error al cargar registros: " + e.getMessage());
                });
    }
} 