package com.iot.stayflowdev.Driver.Dtos;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class SolicitudReserva {

    // Campos de Firestore
    private boolean calculadoConGoogleMaps;
    private String calificacionHotel;
    private String destino;
    private GeoPoint destinoCoordenadas;
    private String destinoDireccion;
    private double distanciaKm;
    private String distanciaTexto;
    private boolean esAceptada;
    private String estado;
    private Date fechaCalculoDistancia;
    private Date fechaCreacion;
    private Date fechaSalida;
    private String horaAceptacion;
    private String idCliente;
    private String idHotel;
    private String idTaxista;
    private String nombrePasajero;
    private String notas;
    private int numeroPasajeros;
    private String origen;
    private GeoPoint origenCoordenadas;
    private String origenDireccion;
    private String reservaId;
    private String telefonoPasajero;
    private int tiempoEstimadoMin;
    private String tiempoTexto;
    private String tipoVehiculo;

    // Constructor vacío requerido por Firestore
    public SolicitudReserva() {}

    // Métodos de utilidad
    public String getFechaHora() {
        if (fechaSalida != null) {
            java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm", java.util.Locale.getDefault());
            return formato.format(fechaSalida);
        }
        return "Fecha no disponible";
    }

    public String getDistancia() {
        return distanciaTexto != null ? distanciaTexto : String.format("%.1f km", distanciaKm);
    }

    // Getters y Setters
    public boolean isCalculadoConGoogleMaps() {
        return calculadoConGoogleMaps;
    }

    public void setCalculadoConGoogleMaps(boolean calculadoConGoogleMaps) {
        this.calculadoConGoogleMaps = calculadoConGoogleMaps;
    }

    public String getCalificacionHotel() {
        return calificacionHotel;
    }

    public void setCalificacionHotel(String calificacionHotel) {
        this.calificacionHotel = calificacionHotel;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public GeoPoint getDestinoCoordenadas() {
        return destinoCoordenadas;
    }

    public void setDestinoCoordenadas(GeoPoint destinoCoordenadas) {
        this.destinoCoordenadas = destinoCoordenadas;
    }

    public String getDestinoDireccion() {
        return destinoDireccion;
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

    public String getDistanciaTexto() {
        return distanciaTexto;
    }

    public void setDistanciaTexto(String distanciaTexto) {
        this.distanciaTexto = distanciaTexto;
    }

    public boolean isEsAceptada() {
        return esAceptada;
    }

    public void setEsAceptada(boolean esAceptada) {
        this.esAceptada = esAceptada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaCalculoDistancia() {
        return fechaCalculoDistancia;
    }

    public void setFechaCalculoDistancia(Date fechaCalculoDistancia) {
        this.fechaCalculoDistancia = fechaCalculoDistancia;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(Date fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getHoraAceptacion() {
        return horaAceptacion;
    }

    public void setHoraAceptacion(String horaAceptacion) {
        this.horaAceptacion = horaAceptacion;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(String idHotel) {
        this.idHotel = idHotel;
    }

    public String getIdTaxista() {
        return idTaxista;
    }

    public void setIdTaxista(String idTaxista) {
        this.idTaxista = idTaxista;
    }

    public String getNombrePasajero() {
        return nombrePasajero;
    }

    public void setNombrePasajero(String nombrePasajero) {
        this.nombrePasajero = nombrePasajero;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public int getNumeroPasajeros() {
        return numeroPasajeros;
    }

    public void setNumeroPasajeros(int numeroPasajeros) {
        this.numeroPasajeros = numeroPasajeros;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public GeoPoint getOrigenCoordenadas() {
        return origenCoordenadas;
    }

    public void setOrigenCoordenadas(GeoPoint origenCoordenadas) {
        this.origenCoordenadas = origenCoordenadas;
    }

    public String getOrigenDireccion() {
        return origenDireccion;
    }

    public void setOrigenDireccion(String origenDireccion) {
        this.origenDireccion = origenDireccion;
    }

    public String getReservaId() {
        return reservaId;
    }

    public void setReservaId(String reservaId) {
        this.reservaId = reservaId;
    }

    public String getTelefonoPasajero() {
        return telefonoPasajero;
    }

    public void setTelefonoPasajero(String telefonoPasajero) {
        this.telefonoPasajero = telefonoPasajero;
    }

    public int getTiempoEstimadoMin() {
        return tiempoEstimadoMin;
    }

    public void setTiempoEstimadoMin(int tiempoEstimadoMin) {
        this.tiempoEstimadoMin = tiempoEstimadoMin;
    }

    public String getTiempoTexto() {
        return tiempoTexto;
    }

    public void setTiempoTexto(String tiempoTexto) {
        this.tiempoTexto = tiempoTexto;
    }

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }
}
