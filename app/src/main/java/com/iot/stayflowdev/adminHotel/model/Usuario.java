package com.iot.stayflowdev.adminHotel.model;

public class Usuario {
    private String nombre;
    private int porcentaje;
    private String monto;

    public Usuario(String nombre, int porcentaje, String monto) {
        this.nombre = nombre;
        this.porcentaje = porcentaje;
        this.monto = monto;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public String getMonto() {
        return monto;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPorcentaje(int porcentaje) {
        this.porcentaje = porcentaje;
    }

    public void setMonto(String monto) {
        this.monto = monto;
    }
}
