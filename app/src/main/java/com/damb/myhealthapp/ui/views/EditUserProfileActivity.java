package com.damb.myhealthapp.ui.views;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton; // Importación necesaria para ImageButton
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.damb.myhealthapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class EditUserProfileActivity extends AppCompatActivity {
    private EditText editName, editHeight, editWeight, editBirthday;
    private Button saveButton;

    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        editName = findViewById(R.id.editName);
        editHeight = findViewById(R.id.editHeight);
        editWeight = findViewById(R.id.editWeight);
        editBirthday = findViewById(R.id.editBirthday);
        saveButton = findViewById(R.id.saveButton);
        ImageButton calendarButton = findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(v -> showDatePicker());


        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            editName.setText(document.getString("displayName"));
                            
                            // Leer datos de onboarding del mapa anidado
                            Map<String, Object> onboardingData = (Map<String, Object>) document.get("onboardingData");
                            if (onboardingData != null) {
                                Double heightValue = ((Number) onboardingData.get("height")).doubleValue();
                                if (heightValue != null) {
                                    editHeight.setText(String.valueOf(heightValue));
                                }

                                Double weightValue = ((Number) onboardingData.get("weight")).doubleValue();
                                if (weightValue != null) {
                                    editWeight.setText(String.valueOf(weightValue));
                                }

                                Long birthday = (Long) onboardingData.get("birthday");
                                if (birthday != null) {
                                    editBirthday.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(birthday)));
                                }
                            }
                        }
                    });
        }

        saveButton.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String name = editName.getText().toString();
        double height = Double.parseDouble(editHeight.getText().toString());
        height = Math.round(height * 100.0) / 100.0;

        double weight = Double.parseDouble(editWeight.getText().toString());
        String birthdayStr = editBirthday.getText().toString();

        try {
            long birthday = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(birthdayStr).getTime();

            db.collection("users").document(user.getUid())
                    .update("displayName", name,
                            "onboardingData.height", height,
                            "onboardingData.weight", weight,
                            "onboardingData.birthday", birthday)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // Volver al perfil
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

        } catch (Exception e) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
        }
    }
    //INCIO METODO PARA CXALENDARIO
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    editBirthday.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }


}
