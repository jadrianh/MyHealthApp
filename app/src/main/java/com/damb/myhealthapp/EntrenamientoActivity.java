package com.damb.myhealthapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.adapters.EntrenamientoAdapter;
import com.damb.myhealthapp.models.EjercicioSugerido;
import com.damb.myhealthapp.models.RegistroEjercicio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntrenamientoActivity extends AppCompatActivity {
    private List<EjercicioSugerido> ejercicios;
    private List<Boolean> completados;
    private EntrenamientoAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        RecyclerView recycler = findViewById(R.id.recyclerEntrenamiento);
        Button btnFinalizar = findViewById(R.id.btnFinalizarEntrenamiento);

        ejercicios = (ArrayList<EjercicioSugerido>) getIntent().getSerializableExtra("ejercicios");
        if (ejercicios == null) ejercicios = new ArrayList<>();
        completados = new ArrayList<>();
        for (int i = 0; i < ejercicios.size(); i++) completados.add(false);

        adapter = new EntrenamientoAdapter(ejercicios, completados);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        btnFinalizar.setOnClickListener(v -> {
            Log.d("EntrenamientoActivity", "Clic en Finalizar Entrenamiento");
            int hechos = 0;
            for (boolean c : completados) if (c) hechos++;
            int porcentaje = ejercicios.size() == 0 ? 0 : (int) (100.0 * hechos / ejercicios.size());

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Log.d("EntrenamientoActivity", "Usuario autenticado: " + currentUser.getUid());
                String userId = currentUser.getUid();
                Date fechaActual = new Date();

                for (int i = 0; i < ejercicios.size(); i++) {
                    if (completados.get(i)) {
                        Log.d("EntrenamientoActivity", "Guardando ejercicio: " + ejercicios.get(i).getNombre());
                        Map<String, Object> registro = new HashMap<>();
                        registro.put("fecha", fechaActual);
                        registro.put("tipoEjercicio", ejercicios.get(i).getNombre());

                        db.collection("users").document(userId).collection("registrosEjercicio")
                                .add(registro)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("EntrenamientoActivity", "Ejercicio guardado con éxito");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EntrenamientoActivity", "Error al guardar ejercicio", e);
                                });
                    }
                }
                 Toast.makeText(EntrenamientoActivity.this, "haz finalizado el ejercicio", Toast.LENGTH_SHORT).show();
                 Log.d("EntrenamientoActivity", "Mostrando Toast de finalización");

            } else {
                 Toast.makeText(EntrenamientoActivity.this, "Usuario no autenticado. No se pudo guardar el registro.", Toast.LENGTH_SHORT).show();
                 Log.d("EntrenamientoActivity", "Usuario no autenticado, no se guarda registro");
            }

            Intent data = new Intent();
            data.putExtra("porcentaje", porcentaje);
            setResult(RESULT_OK, data);
            finish();
            Log.d("EntrenamientoActivity", "Llamando a finish()");
        });
    }
} 