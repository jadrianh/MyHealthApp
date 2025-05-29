package com.damb.myhealthapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView saludoTextView;
    private TextView consejoTextView;
    private FirebaseAuth mAuth;

    private int indiceActual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        saludoTextView = findViewById(R.id.saludoTextView);
        consejoTextView = findViewById(R.id.consejoTextView);
        Button btnPeso = findViewById(R.id.btnPeso);
        Button btnPresion = findViewById(R.id.btnPresion);
        Button btnAlimentacion = findViewById(R.id.btnAlimentacion);

        // Saludo
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String correo = user.getEmail();
            String nombre = correo != null ? correo.split("@")[0] : "Usuario";
            saludoTextView.setText("Hola, " + nombre + " 👋");
        }

        consejoTextView.setText("Consejo: Haz al menos 30 minutos de ejercicio diario 💪");

        // Botones accesos rápidos
        btnPeso.setOnClickListener(v -> {
            // TODO: Ir a registrar peso
        });

        btnPresion.setOnClickListener(v -> {
            // TODO: Ir a registrar altura
        });

        btnAlimentacion.setOnClickListener(v -> {
            // TODO: Ir a alimentación
        });
    }
}
