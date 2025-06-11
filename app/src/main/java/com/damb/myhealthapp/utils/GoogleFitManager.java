package com.damb.myhealthapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GoogleFitManager {
    private static final String TAG = "GoogleFitManager";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;

    private Activity activity;
    private GoogleSignInClient googleSignInClient;
    private OnFitDataReceivedListener fitDataListener;

    public interface OnFitDataReceivedListener {
        void onStepsReceived(long steps);
        void onCaloriesReceived(float calories);
        void onError(String error);
    }

    public GoogleFitManager(Activity activity, OnFitDataReceivedListener listener) {
        this.activity = activity;
        this.fitDataListener = listener;
        setupGoogleFit();
    }

    private void setupGoogleFit() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, signInOptions);

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    activity,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(activity),
                    fitnessOptions);
        } else {
            readFitData();
        }
    }

    private void readFitData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            if (fitDataListener != null) {
                fitDataListener.onError("No se ha iniciado sesión en Google Fit");
            }
            return;
        }

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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (fitDataListener != null) {
                            fitDataListener.onError("Error al obtener datos de Google Fit: " + e.getMessage());
                        }
                        Log.e(TAG, "Error al leer datos de Google Fit", e);
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                readFitData();
            } else {
                if (fitDataListener != null) {
                    fitDataListener.onError("Error al autenticar con Google Fit");
                }
            }
        }
    }
} 