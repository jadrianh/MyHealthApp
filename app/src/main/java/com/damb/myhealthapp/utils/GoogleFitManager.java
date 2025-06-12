package com.damb.myhealthapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException; // Importar ResolvableApiException
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

// import java.util.Calendar; // No se usa, eliminar
// import java.util.Date; // No se usa, eliminar
// import java.util.concurrent.TimeUnit; // No se usa, eliminar

public class GoogleFitManager {
    private static final String TAG = "GoogleFitManager";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1; // Código para solicitar permisos OAuth

    private Activity activity;
    private GoogleSignInClient googleSignInClient;
    private FitnessOptions fitnessOptions; // Declarar fitnessOptions a nivel de clase
    private OnFitDataReceivedListener fitDataListener;

    public interface OnFitDataReceivedListener {
        void onStepsReceived(long steps);
        void onCaloriesReceived(float calories);
        void onError(String error);
    }

    public interface OnStepGoalCheckListener {
        void onStepCountReceived(long steps);
        void onError(String error);
    }

    public GoogleFitManager(Activity activity, OnFitDataReceivedListener listener) {
        this.activity = activity;
        this.fitDataListener = listener;
        setupGoogleFitOptions(); // Configurar las opciones de Fitness
        checkAndRequestGoogleFitPermissions(); // Verificar y solicitar permisos
    }

    // Configura las opciones de Fitness (qué tipos de datos se necesitan)
    private void setupGoogleFitOptions() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();
    }

    // Verifica si ya se tienen los permisos de Google Fit o los solicita
    private void checkAndRequestGoogleFitPermissions() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);

        if (account == null) {
            // No hay cuenta de Google logueada, intentar iniciar sesión pasivamente
            googleSignInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN);
            googleSignInClient.silentSignIn()
                    .addOnSuccessListener(signedInAccount -> {
                        // Sesión iniciada pasivamente, ahora verificar permisos de Fit
                        if (!GoogleSignIn.hasPermissions(signedInAccount, fitnessOptions)) {
                            requestFitnessPermissions(signedInAccount);
                        } else {
                            readFitData(); // Permisos concedidos, leer datos
                        }
                    })
                    .addOnFailureListener(e -> {
                        // No se pudo iniciar sesión pasivamente, pedir inicio de sesión interactivo
                        Log.w(TAG, "No se pudo iniciar sesión pasivamente: " + e.getMessage());
                        if (fitDataListener != null) {
                            fitDataListener.onError("Inicia sesión en Google para usar My Health App.");
                        }
                        // Opcional: Podrías aquí lanzar una actividad de inicio de sesión de Google
                        // para que el usuario pueda seleccionar una cuenta si no hay ninguna.
                        // Sin embargo, por simplicidad y UX, a menudo se espera que MainActivity
                        // maneje el flujo inicial de Google Sign-In.
                    });
        } else {
            // Hay una cuenta de Google logueada, verificar si tiene permisos de Fit
            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                requestFitnessPermissions(account);
            } else {
                readFitData(); // Permisos concedidos, leer datos
            }
        }
    }

    // Solicita los permisos de Google Fit al usuario
    private void requestFitnessPermissions(GoogleSignInAccount account) {
        if (account == null) {
            Log.e(TAG, "Intentando solicitar permisos sin una cuenta de Google activa.");
            if (fitDataListener != null) {
                fitDataListener.onError("Error de autenticación, por favor inicia sesión de nuevo.");
            }
            return;
        }

        GoogleSignIn.requestPermissions(
                activity,
                REQUEST_OAUTH_REQUEST_CODE,
                account,
                fitnessOptions);
        Log.d(TAG, "Solicitando permisos de Google Fit.");
    }

    // Lee los datos de pasos y calorías de Google Fit
    private void readFitData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            // Esto no debería ocurrir si el flujo es correcto, pero es una salvaguarda
            if (fitDataListener != null) {
                fitDataListener.onError("No hay sesión activa o permisos de Google Fit denegados. " +
                        "Por favor, asegúrate de haber iniciado sesión y concedido los permisos.");
            }
            return;
        }

        Log.d(TAG, "Leyendo datos de Google Fit para la cuenta: " + account.getEmail());

        // Leer pasos
        Task<DataSet> stepsTask = Fitness.getHistoryClient(activity, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA);

        // Leer calorías
        Task<DataSet> caloriesTask = Fitness.getHistoryClient(activity, account)
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED);

        Tasks.whenAll(stepsTask, caloriesTask)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        long totalSteps = 0;
                        DataSet stepsDataSet = stepsTask.getResult();
                        if (stepsDataSet != null) {
                            for (DataPoint dp : stepsDataSet.getDataPoints()) {
                                for (Field field : dp.getDataType().getFields()) {
                                    totalSteps += dp.getValue(field).asInt();
                                }
                            }
                        }
                        if (fitDataListener != null) {
                            fitDataListener.onStepsReceived(totalSteps);
                        }

                        float totalCalories = 0f;
                        DataSet caloriesDataSet = caloriesTask.getResult();
                        if (caloriesDataSet != null) {
                            for (DataPoint dp : caloriesDataSet.getDataPoints()) {
                                for (Field field : dp.getDataType().getFields()) {
                                    totalCalories += dp.getValue(field).asFloat();
                                }
                            }
                        }
                        if (fitDataListener != null) {
                            fitDataListener.onCaloriesReceived(totalCalories);
                        }
                        Log.d(TAG, "Datos de Google Fit leídos exitosamente.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = "Error al obtener datos de Google Fit.";
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                                errorMessage += " Se requiere acción para resolver el problema. " +
                                        "Por favor, intenta de nuevo y sigue las instrucciones.";
                                // Verificar si es una ResolvableApiException antes de intentar resolver
                                if (apiException instanceof ResolvableApiException) {
                                    try {
                                        ResolvableApiException resolvable = (ResolvableApiException) apiException;
                                        resolvable.startResolutionForResult(activity, REQUEST_OAUTH_REQUEST_CODE);
                                    } catch (IntentSender.SendIntentException sendIntentException) {
                                        Log.e(TAG, "Error al intentar resolver la API de Google Fit", sendIntentException);
                                        errorMessage += " Error interno al iniciar resolución.";
                                    }
                                } else {
                                    // Esto puede ocurrir si el statusCode es RESOLUTION_REQUIRED pero la excepción no es ResolvableApiException
                                    Log.e(TAG, "ApiException con RESOLUTION_REQUIRED no es ResolvableApiException. Detalles: " + e.getMessage());
                                    errorMessage += " (No se pudo iniciar la resolución automáticamente).";
                                }
                            } else if (statusCode == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
                                errorMessage += " La aplicación necesita permisos adicionales de Google Fit. " +
                                        "Por favor, verifica la conexión y concede los permisos.";
                            } else {
                                errorMessage += " Código de error: " + statusCode + ". " + e.getMessage();
                            }
                        } else {
                            errorMessage += " Detalles: " + e.getMessage();
                        }

                        if (fitDataListener != null) {
                            fitDataListener.onError(errorMessage);
                        }
                        Log.e(TAG, "Error al leer datos de Google Fit: " + errorMessage, e);
                    }
                });
    }

    // Método estático para leer pasos diarios desde NotificationReceiver
    public static void getDailyStepsForNotification(Context context, OnStepGoalCheckListener listener) {
        FitnessOptions notificationFitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        if (account == null || !GoogleSignIn.hasPermissions(account, notificationFitnessOptions)) {
            Log.w(TAG, "No hay sesión o permisos para la notificación de pasos.");
            if (listener != null) {
                listener.onError("No se ha iniciado sesión en Google Fit o no se tienen permisos para las notificaciones.");
            }
            return;
        }

        Fitness.getHistoryClient(context, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        long totalSteps = 0;
                        if (dataSet != null) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                for (Field field : dp.getDataType().getFields()) {
                                    totalSteps += dp.getValue(field).asInt();
                                }
                            }
                        }
                        if (listener != null) {
                            listener.onStepCountReceived(totalSteps);
                        }
                        Log.d(TAG, "Pasos para notificación leídos: " + totalSteps);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = "Error al obtener datos de Google Fit para la notificación.";
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            errorMessage += " Código de error: " + statusCode;
                            if (statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
                                errorMessage += ". Se requiere acción en la app principal.";
                            }
                        }
                        if (listener != null) {
                            listener.onError(errorMessage);
                        }
                        Log.e(TAG, "Error al leer datos de Google Fit para la notificación: " + errorMessage, e);
                    }
                });
    }

    // Maneja el resultado de la solicitud de permisos de Google Fit
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Permisos de Google Fit concedidos. Releyendo datos.");
                readFitData(); // Si los permisos se concedieron, intentar leer datos de nuevo
            } else {
                String errorMessage = "Autenticación con Google Fit cancelada o fallida.";
                if (data != null) {
                    // Puedes extraer más detalles del error si es necesario
                    // Status status = GoogleSignInApi.getSignInResultFromIntent(data).getStatus();
                    // if (status != null) errorMessage += " (Código: " + status.getStatusCode() + ")";
                }
                if (fitDataListener != null) {
                    fitDataListener.onError(errorMessage);
                }
                Log.w(TAG, "Error al autenticar con Google Fit: " + errorMessage);
            }
        }
    }
}
