package com.damb.myhealthapp;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.damb.myhealthapp.bluetooth.BluetoothHealthManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MedicionSaludActivity extends AppCompatActivity implements BluetoothHealthManager.OnHealthDataListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private BluetoothHealthManager bluetoothManager;
    private TextView heartRateTextView;
    private TextView spo2TextView;
    private TextView statusTextView;
    private Button connectButton;
    private Button saveButton;
    private String measurementType; // "heart_rate" o "spo2"
    private int currentHeartRate = 0;
    private int currentSpO2 = 0;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicion_salud);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtener el tipo de medición de los extras
        measurementType = getIntent().getStringExtra("measurement_type");
        if (measurementType == null) {
            measurementType = "heart_rate"; // Valor por defecto
        }

        // Inicializar vistas
        heartRateTextView = findViewById(R.id.heartRateValue);
        spo2TextView = findViewById(R.id.spo2Value);
        statusTextView = findViewById(R.id.statusText);
        connectButton = findViewById(R.id.connectButton);
        saveButton = findViewById(R.id.saveButton);

        // Configurar el título según el tipo de medición
        TextView titleTextView = findViewById(R.id.titleText);
        if ("heart_rate".equals(measurementType)) {
            titleTextView.setText("Medición de Frecuencia Cardíaca");
            spo2TextView.setVisibility(View.GONE);
        } else {
            titleTextView.setText("Medición de SpO2");
            heartRateTextView.setVisibility(View.GONE);
        }

        // Inicializar BluetoothManager
        bluetoothManager = new BluetoothHealthManager(this);
        bluetoothManager.setHealthDataListener(this);

        // Configurar botones
        connectButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                if (bluetoothManager.isBluetoothEnabled()) {
                    bluetoothManager.startScan();
                    statusTextView.setText("Buscando dispositivo...");
                    connectButton.setEnabled(false);
                } else {
                    Toast.makeText(this, "Por favor, activa el Bluetooth", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermissions();
            }
        });

        saveButton.setOnClickListener(v -> saveMeasurement());
        saveButton.setEnabled(false);
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
                connectButton.performClick();
            } else {
                Toast.makeText(this, "Se requieren permisos para conectar el dispositivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onHeartRateUpdate(int heartRate) {
        runOnUiThread(() -> {
            currentHeartRate = heartRate;
            heartRateTextView.setText(String.format("%d bpm", heartRate));
            saveButton.setEnabled(true);
        });
    }

    @Override
    public void onSpO2Update(int spo2) {
        runOnUiThread(() -> {
            currentSpO2 = spo2;
            spo2TextView.setText(String.format("%d%%", spo2));
            saveButton.setEnabled(true);
        });
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        runOnUiThread(() -> {
            statusTextView.setText("Conectado a: " + device.getName());
            connectButton.setText("Desconectar");
            connectButton.setEnabled(true);
            connectButton.setOnClickListener(v -> disconnectDevice());
        });
    }

    @Override
    public void onDeviceDisconnected() {
        runOnUiThread(() -> {
            statusTextView.setText("Dispositivo desconectado");
            connectButton.setText("Conectar");
            connectButton.setEnabled(true);
            connectButton.setOnClickListener(v -> connectButton.performClick());
            saveButton.setEnabled(false);
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            statusTextView.setText("Error: " + error);
            connectButton.setEnabled(true);
        });
    }

    private void disconnectDevice() {
        bluetoothManager.disconnect();
        onDeviceDisconnected();
    }

    private void saveMeasurement() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar las mediciones", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> measurement = new HashMap<>();
        measurement.put("userId", mAuth.getCurrentUser().getUid());
        measurement.put("timestamp", new Date());
        measurement.put("type", measurementType);

        if ("heart_rate".equals(measurementType)) {
            measurement.put("value", currentHeartRate);
        } else {
            measurement.put("value", currentSpO2);
        }

        db.collection("health_measurements")
                .add(measurement)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Medición guardada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar la medición: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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