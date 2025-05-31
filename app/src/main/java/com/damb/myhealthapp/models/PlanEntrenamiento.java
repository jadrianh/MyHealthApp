package com.damb.myhealthapp.models;

public class PlanEntrenamiento {
    private String titulo;
    private String duracion;
    private String modalidad;
    private String frecuencia;
    private String ejercicios;
    private String idealPara;

    public PlanEntrenamiento(String titulo, String duracion, String modalidad, String frecuencia, String ejercicios, String idealPara) {
        this.titulo = titulo;
        this.duracion = duracion;
        this.modalidad = modalidad;
        this.frecuencia = frecuencia;
        this.ejercicios = ejercicios;
        this.idealPara = idealPara;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getModalidad() {
        return modalidad;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public String getEjercicios() {
        return ejercicios;
    }

    public String getIdealPara() {
        return idealPara;
    }
} 