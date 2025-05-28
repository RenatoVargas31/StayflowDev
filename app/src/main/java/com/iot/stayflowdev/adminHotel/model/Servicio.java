package com.iot.stayflowdev.adminHotel.model;

public class Servicio {
    private int id;
    private String nombre;
    private String descripcion;
    private String precio;
    private boolean esGratis;

    public Servicio(int id, String nombre, String descripcion, String precio, boolean esGratis) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.esGratis = esGratis;
    }

    public Servicio(String nombre, String descripcion, String precio, boolean esGratis) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.esGratis = esGratis;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getPrecio() { return precio; }
    public boolean isEsGratis() { return esGratis; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(String precio) { this.precio = precio; }
    public void setEsGratis(boolean esGratis) { this.esGratis = esGratis; }
}
