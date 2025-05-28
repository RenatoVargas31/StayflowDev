package com.iot.stayflowdev.adminHotel.model;

public class Mensaje {
    private String nombre;
    private String ultimoMensaje;
    private int imagenResId;

    public Mensaje(String nombre, String ultimoMensaje, int imagenResId) {
        this.nombre = nombre;
        this.ultimoMensaje = ultimoMensaje;
        this.imagenResId = imagenResId;
    }

    public String getNombre() { return nombre; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public int getImagenResId() { return imagenResId; }
}
