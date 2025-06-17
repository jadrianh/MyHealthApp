package com.damb.myhealthapp.models;

import com.google.firebase.Timestamp;

public class WaterLog {

    private int amount;
    private Timestamp timestamp;
    private String userId;

    // Constructor vacío (obligatorio para Firestore)
    public WaterLog() {}

    public WaterLog(int amount, Timestamp timestamp, String userId) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public int getAmount() {
        return amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }
}
