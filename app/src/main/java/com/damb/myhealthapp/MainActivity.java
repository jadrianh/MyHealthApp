package com.damb.myhealthapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.damb.myhealthapp.adapters.ViewPagerEjercicioAdapter;
import com.damb.myhealthapp.models.EjercicioSugerido;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView saludoTextView;
    private TextView consejoTextView;
    private FirebaseAuth mAuth;
    private static ViewPager2 viewPagerEjercicios;
    private ImageView btnLogout;

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

        // Configurar ViewPager2 para el carrusel de ejercicios
        viewPagerEjercicios = findViewById(R.id.viewPagerEjercicios);
        List<String> tiposEjercicio = new ArrayList<>();
        tiposEjercicio.add("Pérdida de Peso (Quema de Grasa)");
        tiposEjercicio.add("Ganancia de Masa Muscular (Hipertrofia)");
        tiposEjercicio.add("Flexibilidad y Movilidad");
        tiposEjercicio.add("Salud General y Bienestar");
        tiposEjercicio.add("Resistencia y Cardio");
        tiposEjercicio.add("Entrenamiento Funcional");
        tiposEjercicio.add("Plan Rápido para Tonificación");
        tiposEjercicio.add("Plan para Principiantes");

        ViewPagerEjercicioAdapter viewPagerEjercicioAdapter = new ViewPagerEjercicioAdapter(this, tiposEjercicio);
        viewPagerEjercicios.setAdapter(viewPagerEjercicioAdapter);

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

        // Configurar botón de logout
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void logout() {
        // Cerrar sesión de Firebase
        mAuth.signOut();

        // Redirigir a la pantalla de login
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Cierra MainActivity
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegurarse de que el ViewPager2 esté habilitado al volver a la actividad principal
        setViewPagerEnabled(true);
    }

    public static void setViewPagerEnabled(boolean enabled) {
        if (viewPagerEjercicios != null) {
            viewPagerEjercicios.setUserInputEnabled(enabled);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Mover la tarea al fondo en lugar de finalizar la actividad
            moveTaskToBack(true);
            return true; // Consumir el evento del botón de retroceso
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
