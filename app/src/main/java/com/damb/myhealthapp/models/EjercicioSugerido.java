package com.damb.myhealthapp.models;

import java.io.Serializable;

public class EjercicioSugerido implements Serializable {
    private String nombre;
    private int repeticiones;
    private String series;
    private String detalle;
    private int imagenResId; // Usamos un ID de recurso local

    public EjercicioSugerido(String nombre, int repeticiones, String series, String detalle, int imagenResId) {
        this.nombre = nombre;
        this.repeticiones = repeticiones;
        this.series = series;
        this.detalle = detalle;
        this.imagenResId = imagenResId;
    }

    // Getters
    public String getNombre() { return nombre; }
    public int getRepeticiones() { return repeticiones; }
    public String getSeries() { return series; }
    public String getDetalle() { return detalle; }
    public int getImagenResId() { return imagenResId; }
}