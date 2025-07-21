package com.iot.stayflowdev.adminHotel.model;

public class ClienteItem {
    private String id;
    private String nombre;
    private String email;
    private boolean conectado;

    public ClienteItem(String id, String nombre, String email, boolean conectado) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.conectado = conectado;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public boolean isConectado() { return conectado; }
}
