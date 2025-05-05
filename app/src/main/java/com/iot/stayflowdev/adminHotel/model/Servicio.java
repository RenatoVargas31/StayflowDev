package com.iot.stayflowdev.adminHotel.model;

public class Servicio {
    private String nombre;
    private int porcentaje;
    private String monto;

    // Campos nuevos
    private String descripcion;
    private String precio;

    public Servicio(String nombre, int porcentaje, String monto) {
        this.nombre = nombre;
        this.porcentaje = porcentaje;
        this.monto = monto;
    }

    // Constructor extra para administración
    public Servicio(String nombre, String descripcion, String precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getNombre() { return nombre; }
    public int getPorcentaje() { return porcentaje; }
    public String getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public String getPrecio() { return precio; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPorcentaje(int porcentaje) { this.porcentaje = porcentaje; }
    public void setMonto(String monto) { this.monto = monto; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(String precio) { this.precio = precio; }
}
