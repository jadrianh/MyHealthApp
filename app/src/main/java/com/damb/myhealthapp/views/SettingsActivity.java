package com.damb.myhealthapp.views;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import android.util.Log;

import com.damb.myhealthapp.R;
import com.damb.myhealthapp.receivers.NotificationReceiver;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Switch switchWaterReminder;
    private EditText editTextWaterInterval;
    private Switch switchExerciseReminder;
    private Button btnExerciseTimePicker;
    private TextView textViewExerciseTime;
    private Switch switchStepGoalReminder;
    private Button btnStepGoalTimePicker;
    private TextView textViewStepGoalTime;
    private Button btnSaveSettings;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inicializar vistas
        switchWaterReminder = findViewById(R.id.switchWaterReminder);
        editTextWaterInterval = findViewById(R.id.editTextWaterInterval);
        switchExerciseReminder = findViewById(R.id.switchExerciseReminder);
        btnExerciseTimePicker = findViewById(R.id.btnExerciseTimePicker);
        textViewExerciseTime = findViewById(R.id.textViewExerciseTime);
        switchStepGoalReminder = findViewById(R.id.switchStepGoalReminder);
        btnStepGoalTimePicker = findViewById(R.id.btnStepGoalTimePicker);
        textViewStepGoalTime = findViewById(R.id.textViewStepGoalTime);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        sharedPreferences = getSharedPreferences("ReminderPrefs", MODE_PRIVATE);

        loadReminderPreferences();

        btnExerciseTimePicker.setOnClickListener(v -> showTimePicker(textViewExerciseTime, "exercise_hour", "exercise_minute"));
        btnStepGoalTimePicker.setOnClickListener(v -> showTimePicker(textViewStepGoalTime, "step_goal_hour", "step_goal_minute"));

        btnSaveSettings.setOnClickListener(v -> saveReminders());
    }

    private void showTimePicker(TextView textView, String hourKey, String minuteKey) {
        Calendar c = Calendar.getInstance();
        int hour = sharedPreferences.getInt(hourKey, c.get(Calendar.HOUR_OF_DAY));
        int minute = sharedPreferences.getInt(minuteKey, c.get(Calendar.MINUTE));

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    textView.setText(String.format("Hora seleccionada: %02d:%02d", selectedHour, selectedMinute));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(hourKey, selectedHour);
                    editor.putInt(minuteKey, selectedMinute);
                    editor.apply();
                },
                hour, minute, true);
        timePickerDialog.show();
    }

    private void loadReminderPreferences() {
        switchWaterReminder.setChecked(sharedPreferences.getBoolean("water_reminder_enabled", false));
        editTextWaterInterval.setText(String.valueOf(sharedPreferences.getInt("water_interval", 2)));

        switchExerciseReminder.setChecked(sharedPreferences.getBoolean("exercise_reminder_enabled", false));
        int exerciseHour = sharedPreferences.getInt("exercise_hour", -1);
        int exerciseMinute = sharedPreferences.getInt("exercise_minute", -1);
        if (exerciseHour != -1 && exerciseMinute != -1) {
            textViewExerciseTime.setText(String.format("Hora seleccionada: %02d:%02d", exerciseHour, exerciseMinute));
        }

        switchStepGoalReminder.setChecked(sharedPreferences.getBoolean("step_goal_reminder_enabled", false));
        int stepGoalHour = sharedPreferences.getInt("step_goal_hour", -1);
        int stepGoalMinute = sharedPreferences.getInt("step_goal_minute", -1);
        if (stepGoalHour != -1 && stepGoalMinute != -1) {
            textViewStepGoalTime.setText(String.format("Hora seleccionada: %02d:%02d", stepGoalHour, stepGoalMinute));
        }
    }

    private void saveReminders() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("water_reminder_enabled", switchWaterReminder.isChecked());
        try {
            editor.putInt("water_interval", Integer.parseInt(editTextWaterInterval.getText().toString()));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Intervalo de agua inválido, usando 2 horas por defecto", Toast.LENGTH_SHORT).show();
            editor.putInt("water_interval", 2);
        }

        editor.putBoolean("exercise_reminder_enabled", switchExerciseReminder.isChecked());
        editor.putBoolean("step_goal_reminder_enabled", switchStepGoalReminder.isChecked());
        editor.apply();

        setupReminders();

        Toast.makeText(this, "Recordatorios guardados", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "saveReminders: Recordatorios guardados y setupReminders() llamado.");
    }

    private void setupReminders() {
        Log.d(TAG, "setupReminders: Iniciando configuración de recordatorios.");

        // Recordatorio de Agua
        if (switchWaterReminder.isChecked()) {
            int interval = sharedPreferences.getInt("water_interval", 2);
            scheduleRepeatingAlarm(this, NotificationReceiver.class, "water_reminder_action", interval * 60 * 60 * 1000L, 101);
            Log.d(TAG, "setupReminders: Recordatorio de agua programado cada " + interval + " horas.");
        } else {
            cancelAlarm(this, NotificationReceiver.class, "water_reminder_action", 101);
            Log.d(TAG, "setupReminders: Recordatorio de agua cancelado.");
        }

        // Recordatorio de Ejercicio
        if (switchExerciseReminder.isChecked()) {
            int hour = sharedPreferences.getInt("exercise_hour", -1);
            int minute = sharedPreferences.getInt("exercise_minute", -1);
            if (hour != -1 && minute != -1) {
                scheduleDailyAlarm(this, NotificationReceiver.class, "exercise_reminder_action", hour, minute, 102);
                Log.d(TAG, "setupReminders: Recordatorio de ejercicio programado para " + String.format("%02d:%02d", hour, minute) + ".");
            } else {
                Toast.makeText(this, "Por favor, selecciona una hora para el recordatorio de ejercicio", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "setupReminders: No se pudo programar recordatorio de ejercicio, hora no establecida.");
            }
        } else {
            cancelAlarm(this, NotificationReceiver.class, "exercise_reminder_action", 102);
            Log.d(TAG, "setupReminders: Recordatorio de ejercicio cancelado.");
        }

        // Recordatorio de Meta de Pasos
        if (switchStepGoalReminder.isChecked()) {
            int hour = sharedPreferences.getInt("step_goal_hour", -1);
            int minute = sharedPreferences.getInt("step_goal_minute", -1);
            if (hour != -1 && minute != -1) {
                scheduleDailyAlarm(this, NotificationReceiver.class, "step_goal_reminder_action", hour, minute, 103);
                Log.d(TAG, "setupReminders: Recordatorio de meta de pasos programado para " + String.format("%02d:%02d", hour, minute) + ".");
            } else {
                Toast.makeText(this, "Por favor, selecciona una hora para el recordatorio de meta de pasos", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "setupReminders: No se pudo programar recordatorio de meta de pasos, hora no establecida.");
            }
        } else {
            cancelAlarm(this, NotificationReceiver.class, "step_goal_reminder_action", 103);
            Log.d(TAG, "setupReminders: Recordatorio de meta de pasos cancelado.");
        }
    }

    private void scheduleRepeatingAlarm(Context context, Class<?> receiverClass, String action, long intervalMillis, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, receiverClass);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            long triggerTime = System.currentTimeMillis() + intervalMillis;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalMillis, pendingIntent);
            Log.d(TAG, "scheduleRepeatingAlarm: Alarma repetida programada para acción: " + action + ", intervalo: " + intervalMillis + "ms, requestCode: " + requestCode);
        } else {
            Log.e(TAG, "scheduleRepeatingAlarm: AlarmManager es nulo.");
        }
    }

    private void scheduleDailyAlarm(Context context, Class<?> receiverClass, String action, int hour, int minute, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, receiverClass);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.d(TAG, "scheduleDailyAlarm: Alarma diaria programada para acción: " + action + ", hora: " + hour + ":" + minute + ", requestCode: " + requestCode + ", triggerTime: " + calendar.getTimeInMillis());
        } else {
            Log.e(TAG, "scheduleDailyAlarm: AlarmManager es nulo.");
        }
    }

    private void cancelAlarm(Context context, Class<?> receiverClass, String action, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, receiverClass);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "cancelAlarm: Alarma cancelada para acción: " + action + ", requestCode: " + requestCode);
        } else {
            Log.e(TAG, "cancelAlarm: AlarmManager es nulo al intentar cancelar.");
        }
    }
} 