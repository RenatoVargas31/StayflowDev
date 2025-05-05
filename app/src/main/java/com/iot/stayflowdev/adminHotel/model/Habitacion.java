package com.iot.stayflowdev.adminHotel.model;

public class Habitacion {
    private String tipo;
    private int capacidad;
    private int tamano; // en m²

    public Habitacion(String tipo, int capacidad, int tamaño) {
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tamano = tamano;
    }

    public String getTipo() {
        return tipo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getTamano() {
        return tamano;
    }
}
