package com.iot.stayflowdev.Driver.Dtos;

import java.io.Serializable;
import java.io.Serializable;
import java.security.Timestamp;


public class SolicitudTaxi implements Serializable {

    // Campos de Viaje
    private String origen;
    private String origenDireccion;
    private String destino;
    private String destinoDireccion;
    // Campos de Pasajero
    private String nombrePasajero;
    private String telefonoPasajero;
    private int numeroPasajeros;
    private String tipoVehiculo;
    private String notas;
    // Campos de Solicitud

    private Timestamp fechaCreacion;
    private String estado; // Estado de la solicitud (pendiente, aceptada, rechazada, etc.)
    // Campos de Información Adicional
    private double distanciaKm;
    private int tiempoEstimadoMin;
    private String horaSolicitud; // Hora en que se realizó la solicitud
    private String horaAceptacion; // Hora en que se aceptó la solicitud
    private boolean esAceptada; // Indica si la solicitud ha sido aceptada por un conductor y cambia el estado a "aceptada"
    private String solicitudId;

    public String getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(String solicitudId) {
        this.solicitudId = solicitudId;
    }
    private String idTaxista;

    public String getIdTaxista() {
        return idTaxista;
    }

    public void setIdTaxista(String idTaxista) {
        this.idTaxista = idTaxista;
    }



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

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setOrigenDireccion(String origenDireccion) {
        this.origenDireccion = origenDireccion;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public void setDestinoDireccion(String destinoDireccion) {
        this.destinoDireccion = destinoDireccion;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public int getTiempoEstimadoMin() {
        return tiempoEstimadoMin;
    }

    public void setTiempoEstimadoMin(int tiempoEstimadoMin) {
        this.tiempoEstimadoMin = tiempoEstimadoMin;
    }

    public String getHoraSolicitud() {
        return horaSolicitud;
    }

    public void setHoraSolicitud(String horaSolicitud) {
        this.horaSolicitud = horaSolicitud;
    }

    public String getHoraAceptacion() {
        return horaAceptacion;
    }

    public void setHoraAceptacion(String horaAceptacion) {
        this.horaAceptacion = horaAceptacion;
    }

    public boolean isEsAceptada() {
        return esAceptada;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public void setEsAceptada(boolean b) {
    }
}

