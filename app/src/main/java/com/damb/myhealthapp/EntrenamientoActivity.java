package com.damb.myhealthapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.damb.myhealthapp.adapters.EntrenamientoAdapter;
import com.damb.myhealthapp.models.EjercicioSugerido;
import java.util.ArrayList;
import java.util.List;

public class EntrenamientoActivity extends AppCompatActivity {
    private List<EjercicioSugerido> ejercicios;
    private List<Boolean> completados;
    private EntrenamientoAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento);

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
            int hechos = 0;
            for (boolean c : completados) if (c) hechos++;
            int porcentaje = ejercicios.size() == 0 ? 0 : (int) (100.0 * hechos / ejercicios.size());
            Intent data = new Intent();
            data.putExtra("porcentaje", porcentaje);
            setResult(RESULT_OK, data);
            finish();
        });
    }
} 