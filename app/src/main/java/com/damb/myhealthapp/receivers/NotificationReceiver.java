package com.damb.myhealthapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.SharedPreferences;
import com.damb.myhealthapp.utils.GoogleFitManager;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "MyHealthApp_Reminders";
    private static final String CHANNEL_NAME = "Recordatorios de Salud";
    private static final String CHANNEL_DESCRIPTION = "Notificaciones para recordatorios de agua, ejercicio y meta de pasos.";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive: Acción recibida: " + action);

        createNotificationChannel(context);

        if ("water_reminder_action".equals(action)) {
            showNotification(context, "¡Hora de beber agua!", "Mantente hidratado, toma un vaso de agua.", 1);
        } else if ("exercise_reminder_action".equals(action)) {
            showNotification(context, "¡Hora de moverte!", "Es momento de hacer algo de ejercicio.", 2);
        } else if ("step_goal_reminder_action".equals(action)) {
            checkStepGoalAndNotify(context);
        }
    }

    private void checkStepGoalAndNotify(Context context) {
        Log.d(TAG, "checkStepGoalAndNotify: Verificando meta de pasos.");
        SharedPreferences sharedPreferences = context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE);
        long dailyStepGoal = sharedPreferences.getLong("daily_step_goal", 10000);

        GoogleFitManager.getDailyStepsForNotification(context, new GoogleFitManager.OnStepGoalCheckListener() {
            @Override
            public void onStepCountReceived(long steps) {
                Log.d(TAG, "onStepCountReceived: Pasos recibidos: " + steps + ", Meta: " + dailyStepGoal);
                String title = "¡Meta de Pasos!";
                String message;
                if (steps >= dailyStepGoal) {
                    message = String.format("¡Felicidades! Has superado tu meta diaria de %d pasos. Pasos actuales: %d.", dailyStepGoal, steps);
                } else {
                    message = String.format("Ánimo, aún puedes alcanzar tu meta diaria de %d pasos. Pasos actuales: %d.", dailyStepGoal, steps);
                }
                showNotification(context, title, message, 3);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: Error al obtener pasos de Google Fit: " + error);
                showNotification(context, "¡Meta de Pasos!", "No pudimos verificar tus pasos. ¡Sigue adelante!", 3);
            }
        });
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "createNotificationChannel: Canal de notificación creado.");
            }
        }
    }

    private void showNotification(Context context, String title, String message, int notificationId) {
        Log.d(TAG, "showNotification: Intentando mostrar notificación - Título: " + title + ", Mensaje: " + message + ", ID: " + notificationId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "showNotification: Notificación lanzada.");
    }
} 