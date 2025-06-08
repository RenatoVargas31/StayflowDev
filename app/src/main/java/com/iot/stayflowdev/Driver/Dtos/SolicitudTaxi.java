package com.iot.stayflowdev.Driver.Dtos;

import java.io.Serializable;
import java.io.Serializable;
import java.security.Timestamp;


public class SolicitudTaxi implements Serializable {

    private String origen;
    private String origenDireccion;
    private String destino;
    private String destinoDireccion;

    private String nombrePasajero;
    private String telefonoPasajero;
    private int numeroPasajeros;
    private String tipoVehiculo;
    private String notas;

    private Timestamp fechaCreacion;
    private String estado;
    private double distanciaKm;
    private int tiempoEstimadoMin;


// ... lo mismo para los otros campos


    public SolicitudTaxi() {
    }

    // Constructor básico (puedes seguir usando este)
    public SolicitudTaxi(String origen, String origenDireccion, String destino, String destinoDireccion) {
        this.origen = origen;
        this.origenDireccion = origenDireccion;
        this.destino = destino;
        this.destinoDireccion = destinoDireccion;
    }

    // Getters
    public String getOrigen() { return origen; }
    public String getOrigenDireccion() { return origenDireccion; }
    public String getDestino() { return destino; }
    public String getDestinoDireccion() { return destinoDireccion; }
    public String getNombrePasajero() { return nombrePasajero; }
    public String getTelefonoPasajero() { return telefonoPasajero; }
    public int getNumeroPasajeros() { return numeroPasajeros; }
    public String getTipoVehiculo() { return tipoVehiculo; }
    public String getNotas() { return notas; }
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Setters
    public void setNombrePasajero(String nombrePasajero) {
        this.nombrePasajero = nombrePasajero;
    }

    public void setTelefonoPasajero(String telefonoPasajero) {
        this.telefonoPasajero = telefonoPasajero;
    }

    public void setNumeroPasajeros(int numeroPasajeros) {
        this.numeroPasajeros = numeroPasajeros;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}

