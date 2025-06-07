package com.iot.stayflowdev.model;

import com.google.firebase.firestore.DocumentId;

public class Habitacion {

    @DocumentId
    private String id;
    private String precio;
    private String tamano;
    private String tipo;
    private String foto;
    private String cantidad;
    private Boolean disponible;
    private Capacidad capacidad;

    // Constructor vacío
    public Habitacion() {}

    // Constructor completo
    public Habitacion(String id, String precio, String tamano, String tipo, String foto,
                      String cantidad, Boolean disponible, Capacidad capacidad) {
        this.id = id;
        this.precio = precio;
        this.tamano = tamano;
        this.tipo = tipo;
        this.foto = foto;
        this.cantidad = cantidad;
        this.disponible = disponible;
        this.capacidad = capacidad;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPrecio() { return precio; }
    public void setPrecio(String precio) { this.precio = precio; }

    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getCantidad() { return cantidad; }
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }

    public Boolean getDisponible() {
        return disponible != null ? disponible : true;
    }    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public Capacidad getCapacidad() { return capacidad; }
    public void setCapacidad(Capacidad capacidad) { this.capacidad = capacidad; }

    // Clase interna Capacidad
    public static class Capacidad {
        private String adultos;
        private String ninos;

        public Capacidad() {}

        public Capacidad(String adultos, String ninos) {
            this.adultos = adultos;
            this.ninos = ninos;
        }

        public String getAdultos() { return adultos; }
        public void setAdultos(String adultos) { this.adultos = adultos; }

        public String getNinos() { return ninos; }
        public void setNinos(String ninos) { this.ninos = ninos; }
    }
}
