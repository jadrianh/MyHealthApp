package com.damb.myhealthapp.models;

import java.util.Date;

public class WorkoutLog {
    private Date fecha;
    private String tipoEjercicio;
    // Podemos añadir más campos aquí según sea necesario, como duracion, calorias, etc.

    public WorkoutLog() {
        // Constructor vacío requerido para Firebase u otras operaciones de deserialización
    }

    public WorkoutLog(Date fecha, String tipoEjercicio) {
        this.fecha = fecha;
        this.tipoEjercicio = tipoEjercicio;
    }

    // Getters
    public Date getFecha() {
        return fecha;
    }

    public String getTipoEjercicio() {
        return tipoEjercicio;
    }

    // Setters (opcional, dependiendo de si necesitas modificar los objetos después de crearlos)
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setTipoEjercicio(String tipoEjercicio) {
        this.tipoEjercicio = tipoEjercicio;
    }
} 