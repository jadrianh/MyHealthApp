package com.damb.myhealthapp.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SleepLogsHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SleepSessionAdapter adapter;
    private List<SleepSession> sesiones = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_log_history);

        recyclerView = findViewById(R.id.recyclerSesiones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SleepSessionAdapter(sesiones);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cargarSesiones();
    }

    private void cargarSesiones() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("sleepLogs")
                .orderBy("start", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sesiones.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            // Mapeo seguro con verificación de campos
                            SleepSession sesion = new SleepSession();

                            // Manejo de timestamp (puede ser Timestamp o long)
                            if (doc.get("start") instanceof com.google.firebase.Timestamp) {
                                sesion.inicio = ((com.google.firebase.Timestamp) doc.get("start")).toDate().getTime();
                            } else if (doc.get("start") != null) {
                                sesion.inicio = doc.getLong("start");
                            }

                            if (doc.get("end") instanceof com.google.firebase.Timestamp) {
                                sesion.fin = ((com.google.firebase.Timestamp) doc.get("end")).toDate().getTime();
                            } else if (doc.get("end") != null) {
                                sesion.fin = doc.getLong("end");
                            }

                            sesion.duracion_min = doc.getLong("duracion_min") != null ? doc.getLong("duracion_min").intValue() : 0;
                            sesion.sueño_profundo_min = doc.getLong("sueño_profundo_min") != null ? doc.getLong("sueño_profundo_min").intValue() : 0;
                            sesion.sueño_ligero_min = doc.getLong("sueño_ligero_min") != null ? doc.getLong("sueño_ligero_min").intValue() : 0;
                            sesion.sueño_inquieto_min = doc.getLong("sueño_inquieto_min") != null ? doc.getLong("sueño_inquieto_min").intValue() : 0;

                            sesiones.add(sesion);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error al procesar documento " + doc.getId() + ": " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (sesiones.isEmpty()) {
                        Toast.makeText(this, "No se encontraron registros de sueño", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar historial: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Clase SleepSession integrada
    private static class SleepSession {
        public long inicio;
        public long fin;
        public int duracion_min;
        public int sueño_profundo_min;
        public int sueño_ligero_min;
        public int sueño_inquieto_min;

        public long getInicio() { return inicio; }
        public long getFin() { return fin; }
        public int getDuracionMin() { return duracion_min; }
        public int getSueñoProfundoMin() { return sueño_profundo_min; }
        public int getSueñoLigeroMin() { return sueño_ligero_min; }
        public int getSueñoInquietoMin() { return sueño_inquieto_min; }
    }

    // Adapter integrado
    private class SleepSessionAdapter extends RecyclerView.Adapter<SleepSessionAdapter.ViewHolder> {
        private List<SleepSession> sesiones;

        public SleepSessionAdapter(List<SleepSession> sesiones) {
            this.sesiones = sesiones;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sleep_session, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SleepSession s = sesiones.get(position);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            holder.textFecha.setText("Inicio: " + sdf.format(new Date(s.getInicio())));

            int horas = s.getDuracionMin() / 60;
            int minutos = s.getDuracionMin() % 60;
            holder.textDuracion.setText("Duración: " + horas + "h " + minutos + "m");

            int total = s.getSueñoProfundoMin() + s.getSueñoLigeroMin() + s.getSueñoInquietoMin();
            int calidad = total > 0 ? (int) ((s.getSueñoProfundoMin() + 0.5 * s.getSueñoLigeroMin()) / total * 100) : 0;
            holder.textCalidad.setText("Calidad estimada: " + calidad + "%");
        }

        @Override
        public int getItemCount() {
            return sesiones.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textFecha, textDuracion, textCalidad;

            ViewHolder(View v) {
                super(v);
                textFecha = v.findViewById(R.id.textFecha);
                textDuracion = v.findViewById(R.id.textDuracion);
                textCalidad = v.findViewById(R.id.textCalidad);
            }
        }
    }
}