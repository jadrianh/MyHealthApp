package com.damb.myhealthapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.damb.myhealthapp.R;

import java.util.Calendar;

public class CustomDatePickerDialog extends Dialog {

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }

    public CustomDatePickerDialog(@NonNull Context context, OnDateSelectedListener listener) {
        super(context);
        setContentView(R.layout.dialog_date_picker);

        NumberPicker dayPicker = findViewById(R.id.dayPicker);
        NumberPicker monthPicker = findViewById(R.id.monthPicker);
        NumberPicker yearPicker = findViewById(R.id.yearPicker);
        Button okButton = findViewById(R.id.okButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        Calendar calendar = Calendar.getInstance();

        int currentYear = calendar.get(Calendar.YEAR);
        String[] months = {"ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sep.", "oct.", "nov.", "dic."};

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);

        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(2000);

        okButton.setOnClickListener(v -> {
            int day = dayPicker.getValue();
            int month = monthPicker.getValue();
            int year = yearPicker.getValue();

            listener.onDateSelected(year, month, day);
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }
}

