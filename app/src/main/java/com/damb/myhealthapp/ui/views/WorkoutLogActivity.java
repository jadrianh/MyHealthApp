package com.damb.myhealthapp.ui.views;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;

import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import com.damb.myhealthapp.ui.adapters.RutinaLogAdapter;

public class WorkoutLogActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewRutinas;
    private RutinaLogAdapter rutinaLogAdapter;
    private ArrayList<RutinaLogAdapter.RutinaLogItem> listaRutinas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_log);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerViewRutinas = findViewById(R.id.recyclerViewRutinas);
        recyclerViewRutinas.setLayoutManager(new LinearLayoutManager(this));
        rutinaLogAdapter = new RutinaLogAdapter(listaRutinas, (rutinaId, nombreRutina) -> {
            Intent intent = new Intent(WorkoutLogActivity.this, WorkoutDetailActivity.class);
            intent.putExtra("rutina_id", rutinaId);
            intent.putExtra("nombre_rutina", nombreRutina);
            startActivity(intent);
        });
        recyclerViewRutinas.setAdapter(rutinaLogAdapter);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            cargarRegistrosEjercicio(user.getUid());
        } else {
            // textViewListaRegistros.setText("Usuario no autenticado. No se pueden cargar los registros."); // Eliminar esta línea
        }
    }

    private void cargarRegistrosEjercicio(String userId) {
        // Obtener la fecha actual en formato yyyy-MM-dd para el ID del documento diario
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .collection("registrosEjercicio").document(todayDate)
                .collection("workouts").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaRutinas.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Si no hay rutinas, podrías mostrar un mensaje especial
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String tipoRutina = document.getString("tipoRutina");
                            String rutinaId = document.getId();
                            if (tipoRutina != null) {
                                listaRutinas.add(new RutinaLogAdapter.RutinaLogItem(rutinaId, tipoRutina));
                            }
                        }
                    }
                    rutinaLogAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("WorkoutLogActivity", "Error al cargar registros de ejercicio", e);
                    // textViewListaRegistros.setText("Error al cargar registros: " + e.getMessage()); // Eliminar esta línea
                });
    }
} 