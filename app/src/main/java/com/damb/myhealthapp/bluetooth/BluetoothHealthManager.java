package com.damb.myhealthapp.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothHealthManager {
    private static final String TAG = "BluetoothHealthManager";
    
    // UUIDs específicos para Huawei Band 4
    private static final UUID HUAWEI_SERVICE_UUID = UUID.fromString("0000FEE1-0000-1000-8000-00805F9B34FB");
    private static final UUID HUAWEI_HEART_RATE_CHAR_UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB");
    private static final UUID HUAWEI_SPO2_CHAR_UUID = UUID.fromString("00002A5E-0000-1000-8000-00805F9B34FB");
    private static final UUID HUAWEI_NOTIFICATION_CHAR_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    
    // UUID para el descriptor de notificación
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    private final Context context;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice connectedDevice;

    private OnHealthDataListener healthDataListener;
    private boolean isScanning = false;

    public interface OnHealthDataListener {
        void onHeartRateUpdate(int heartRate);
        void onSpO2Update(int spo2);
        void onDeviceConnected(BluetoothDevice device);
        void onDeviceDisconnected();
        void onError(String error);
    }

    public BluetoothHealthManager(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void setHealthDataListener(OnHealthDataListener listener) {
        this.healthDataListener = listener;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void startScan() {
        if (!isBluetoothEnabled()) {
            if (healthDataListener != null) {
                healthDataListener.onError("Bluetooth no está habilitado. Por favor, actívalo en la configuración del teléfono.");
            }
            return;
        }

        // Verificar permiso de ubicación (necesario para escaneo BLE en Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (healthDataListener != null) {
                healthDataListener.onError("Se requiere permiso de Ubicación para escanear dispositivos Bluetooth. Por favor, concédelo en la configuración de la aplicación.");
            }
            return;
        }

        // Verificar si los servicios de ubicación están activados en el dispositivo
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = false;
        if (locationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLocationEnabled = locationManager.isLocationEnabled();
            } else {
                try {
                    int mode = android.provider.Settings.Secure.getInt(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_MODE);
                    isLocationEnabled = (mode != android.provider.Settings.Secure.LOCATION_MODE_OFF);
                } catch (android.provider.Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!isLocationEnabled) {
            if (healthDataListener != null) {
                healthDataListener.onError("Los servicios de Ubicación del teléfono están desactivados. Por favor, actívalos para permitir el escaneo.");
            }
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (healthDataListener != null) {
                healthDataListener.onError("Se requiere permiso de escaneo Bluetooth. Por favor, concédelo en la configuración de la aplicación.");
            }
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            if (healthDataListener != null) {
                healthDataListener.onError("El dispositivo no soporta Bluetooth LE");
            }
            return;
        }

        // Configurar escaneo sin filtros específicos para ser compatible con más dispositivos
        List<ScanFilter> filters = new ArrayList<>();
        // No añadimos filtros aquí para escanear todos los dispositivos BLE

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        isScanning = true;
        bluetoothLeScanner.startScan(filters, settings, scanCallback);
    }

    public void stopScan() {
        if (isScanning && bluetoothLeScanner != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothLeScanner.stopScan(scanCallback);
                isScanning = false;
            }
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                // Opcional: Podrías añadir alguna lógica aquí para filtrar dispositivos por nombre
                // Por ejemplo, si el nombre contiene "Mi" o "Galaxy Watch"
                Log.d(TAG, "Dispositivo encontrado: " + device.getName() + " (" + device.getAddress() + ")");
                connectToDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            if (healthDataListener != null) {
                healthDataListener.onError("Error al escanear: " + errorCode);
            }
            isScanning = false;
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (healthDataListener != null) {
                healthDataListener.onError("Se requiere permiso de conexión Bluetooth");
            }
            return;
        }

        stopScan(); // Detener escaneo una vez que intentamos conectar
        connectedDevice = device;
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    gatt.discoverServices();
                    if (healthDataListener != null) {
                        healthDataListener.onDeviceConnected(connectedDevice);
                    }
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (healthDataListener != null) {
                    healthDataListener.onDeviceDisconnected();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Buscar servicios estándar de frecuencia cardíaca y SpO2
                BluetoothGattService heartRateService = gatt.getService(UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")); // Heart Rate Service
                BluetoothGattService spo2Service = gatt.getService(UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")); // Pulse Oximeter Service (for SpO2)

                if (heartRateService != null) {
                    // Configurar notificaciones para la característica estándar de frecuencia cardíaca
                    BluetoothGattCharacteristic heartRateChar = heartRateService.getCharacteristic(UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")); // Heart Rate Measurement Characteristic
                    if (heartRateChar != null) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { return; }
                        gatt.setCharacteristicNotification(heartRateChar, true);
                        BluetoothGattDescriptor descriptor = heartRateChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }

                if (spo2Service != null) {
                    // Configurar notificaciones para la característica estándar de SpO2
                    BluetoothGattCharacteristic spo2Char = spo2Service.getCharacteristic(UUID.fromString("00002A5E-0000-1000-8000-00805f9b34fb")); // SpO2 Measurement Characteristic
                    if (spo2Char != null) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { return; }
                        gatt.setCharacteristicNotification(spo2Char, true);
                        BluetoothGattDescriptor descriptor = spo2Char.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Usar los UUIDs de característica estándar para identificar los datos
            if (UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb").equals(characteristic.getUuid())) { // Heart Rate Measurement
                int heartRate = parseHeartRate(characteristic.getValue()); // Reutilizamos el parser existente
                if (healthDataListener != null) {
                    healthDataListener.onHeartRateUpdate(heartRate);
                }
            } else if (UUID.fromString("00002A5E-0000-1000-8000-00805f9b34fb").equals(characteristic.getUuid())) { // SpO2 Measurement
                int spo2 = parseSpO2(characteristic.getValue()); // Reutilizamos el parser existente
                if (healthDataListener != null) {
                    healthDataListener.onSpO2Update(spo2);
                }
            }
        }
    };

    // Mantenemos los parsers existentes por ahora, si el formato estándar difiere,
    // necesitaremos ajustarlos o añadir lógica para diferentes formatos.
    private int parseHeartRate(byte[] value) {
        // El primer byte indica el formato de los datos (estándar BLE Heart Rate Service)
        int flag = value[0] & 0xFF;
        int format = flag & 0x01;
        int heartRate;

        if (format == 0) {
            // Formato UINT8
            heartRate = value[1] & 0xFF;
        } else {
            // Formato UINT16
            heartRate = (value[1] & 0xFF) + (value[2] << 8);
        }

        return heartRate;
    }

    private int parseSpO2(byte[] value) {
        // El valor de SpO2 suele estar en el primer byte (esto puede variar según el dispositivo)
        if (value.length >= 1) {
            return value[0] & 0xFF;
        }
        return 0;
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
            }
        }
    }
} 