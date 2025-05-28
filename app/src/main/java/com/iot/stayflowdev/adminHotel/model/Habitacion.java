package com.iot.stayflowdev.adminHotel.model;

public class Habitacion {
    private int id;
    private String tipo;
    private int capacidad;
    private int tamano; // en m²
    private double precio;
    private boolean enUso;

    public Habitacion(int id, String tipo, int capacidad, int tamano, double precio, boolean enUso) {
        this.id = id;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tamano = tamano;
        this.precio = precio;
        this.enUso = enUso;
    }

    public Habitacion(String tipo, int capacidad, int tamano, double precio, boolean enUso) {
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tamano = tamano;
        this.precio = precio;
        this.enUso = enUso;
    }

    // Getters y setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getPrecio(){
        return precio;
    }

    public boolean isEnUso() {
        return enUso;
    }

    public void setEnUso(boolean enUso) {
        this.enUso = enUso;
    }
}
