package com.damb.myhealthapp.models;

import java.io.Serializable;

public class SuggestedExcercise implements Serializable {
    private String nombre;
    private int repeticiones;
    private String series;
    private String detalle;
    private int imagenResId; // Usamos un ID de recurso local
    private double met; // Valor MET para el ejercicio
    private int duracionSegundos; // Duración del ejercicio en segundos

    public SuggestedExcercise(String nombre, int repeticiones, String series, String detalle, int imagenResId, double met, int duracionSegundos) {
        this.nombre = nombre;
        this.repeticiones = repeticiones;
        this.series = series;
        this.detalle = detalle;
        this.imagenResId = imagenResId;
        this.met = met;
        this.duracionSegundos = duracionSegundos;
    }

    // Constructor con MET pero sin duración (por compatibilidad)
    public SuggestedExcercise(String nombre, int repeticiones, String series, String detalle, int imagenResId, double met) {
        this(nombre, repeticiones, series, detalle, imagenResId, met, 40); // 40 segundos por defecto
    }

    // Constructor antiguo (por compatibilidad)
    public SuggestedExcercise(String nombre, int repeticiones, String series, String detalle, int imagenResId) {
        this(nombre, repeticiones, series, detalle, imagenResId, 1.0, 40);
    }

    // Getters
    public String getNombre() { return nombre; }
    public int getRepeticiones() { return repeticiones; }
    public String getSeries() { return series; }
    public String getDetalle() { return detalle; }
    public int getImagenResId() { return imagenResId; }
    public double getMet() { return met; }
    public void setMet(double met) { this.met = met; }
    public int getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(int duracionSegundos) { this.duracionSegundos = duracionSegundos; }
}