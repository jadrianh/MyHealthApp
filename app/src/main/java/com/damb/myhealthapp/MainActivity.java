package com.damb.myhealthapp;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.damb.myhealthapp.adapters.ViewPagerEjercicioAdapter;
import com.damb.myhealthapp.models.EjercicioSugerido;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.cardview.widget.CardView;
import com.damb.myhealthapp.bluetooth.BluetoothHealthManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BluetoothHealthManager.OnHealthDataListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private TextView saludoTextView;
    private TextView consejoTextView;
    private FirebaseAuth mAuth;
    private static ViewPager2 viewPagerEjercicios;
    private ImageView btnLogout;
    private CardView cardEjercicio;
    private BluetoothHealthManager bluetoothManager;
    private TextView bluetoothStatus;
    private Button btnConnectBluetooth;
    private ImageView bluetoothIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        // Inicializar BluetoothManager
        bluetoothManager = new BluetoothHealthManager(this);
        bluetoothManager.setHealthDataListener(this);

        // Inicializar vistas de Bluetooth
        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        btnConnectBluetooth = findViewById(R.id.btnConnectBluetooth);
        bluetoothIcon = findViewById(R.id.bluetoothIcon);

        // Configurar botón de conexión Bluetooth
        btnConnectBluetooth.setOnClickListener(v -> {
            if (checkPermissions()) {
                if (bluetoothManager.isBluetoothEnabled()) {
                    if (bluetoothStatus.getText().equals("No conectado")) {
                        bluetoothManager.startScan();
                        bluetoothStatus.setText("Buscando dispositivo...");
                        btnConnectBluetooth.setEnabled(false);
                    } else {
                        bluetoothManager.disconnect();
                    }
                } else {
                    Toast.makeText(this, "Por favor, activa el Bluetooth", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermissions();
            }
        });

        saludoTextView = findViewById(R.id.saludoTextView);
        consejoTextView = findViewById(R.id.consejoTextView);
        Button btnPeso = findViewById(R.id.btnPeso);
        Button btnPresion = findViewById(R.id.btnPresion);
        Button btnAlimentacion = findViewById(R.id.btnAlimentacion);
        cardEjercicio = findViewById(R.id.cardEjercicio);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String correo = user.getEmail();
            String nombre = correo != null ? correo.split("@")[0] : "Usuario";
            saludoTextView.setText("Hola, " + nombre + " 👋");
        }

        consejoTextView.setText("Consejo: Haz al menos 30 minutos de ejercicio diario 💪");

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

        btnPeso.setOnClickListener(v -> {
            // TODO: Ir a registrar peso
        });

        btnPresion.setOnClickListener(v -> {
            // TODO: Ir a registrar altura
        });

        btnAlimentacion.setOnClickListener(v -> {
            // TODO: Ir a alimentación
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            logout();
        });

        cardEjercicio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrarEjercicioActivity.class);
            startActivity(intent);
        });

        // Configurar click listeners para las tarjetas de salud
        CardView cardCorazon = findViewById(R.id.cardCorazon);
        CardView cardSpO2 = findViewById(R.id.cardSpO2);

        cardCorazon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicionSaludActivity.class);
            intent.putExtra("measurement_type", "heart_rate");
            startActivity(intent);
        });

        cardSpO2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MedicionSaludActivity.class);
            intent.putExtra("measurement_type", "spo2");
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewPagerEnabled(true);
    }

    private void logout() {
        mAuth.signOut();

        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public static void setViewPagerEnabled(boolean enabled) {
        if (viewPagerEjercicios != null) {
            viewPagerEjercicios.setUserInputEnabled(enabled);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                btnConnectBluetooth.performClick();
            } else {
                Toast.makeText(this, "Se requieren permisos para conectar el dispositivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onHeartRateUpdate(int heartRate) {
        // No necesitamos manejar actualizaciones de frecuencia cardíaca en la pantalla principal
    }

    @Override
    public void onSpO2Update(int spo2) {
        // No necesitamos manejar actualizaciones de SpO2 en la pantalla principal
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        runOnUiThread(() -> {
            bluetoothStatus.setText("Conectado a: " + device.getName());
            btnConnectBluetooth.setText("Desconectar");
            btnConnectBluetooth.setEnabled(true);
            bluetoothIcon.setImageResource(android.R.drawable.ic_menu_share);
            bluetoothIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
        });
    }

    @Override
    public void onDeviceDisconnected() {
        runOnUiThread(() -> {
            bluetoothStatus.setText("No conectado");
            btnConnectBluetooth.setText("Conectar Dispositivo");
            btnConnectBluetooth.setEnabled(true);
            bluetoothIcon.setImageResource(android.R.drawable.ic_menu_share);
            bluetoothIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            bluetoothStatus.setText("Error: " + error);
            btnConnectBluetooth.setEnabled(true);
            bluetoothIcon.setImageResource(android.R.drawable.ic_menu_share);
            bluetoothIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothManager != null) {
            bluetoothManager.disconnect();
        }
    }
}
