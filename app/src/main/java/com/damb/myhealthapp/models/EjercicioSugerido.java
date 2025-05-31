package com.damb.myhealthapp.models;

import java.io.Serializable;

public class EjercicioSugerido implements Serializable {
    private String nombre;
    private String seriesReps;
    private int imagenResId;

    public EjercicioSugerido(String nombre, String seriesReps, int imagenResId) {
        this.nombre = nombre;
        this.seriesReps = seriesReps;
        this.imagenResId = imagenResId;
    }

    public String getNombre() { return nombre; }
    public String getSeriesReps() { return seriesReps; }
    public int getImagenResId() { return imagenResId; }
} 