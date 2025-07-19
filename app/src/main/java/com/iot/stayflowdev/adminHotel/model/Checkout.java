package com.iot.stayflowdev.adminHotel.model;

import com.iot.stayflowdev.model.Reserva;

public class Checkout {
    private String nombre;
    private String codigoReserva;
    private String montoHospedaje;
    private String tarjeta;
    private String danios;
    private String total;
    private int imagenResId;
    private Reserva reservaCompleta; // Agregamos la reserva completa

    public Checkout(String nombre, String codigoReserva, String montoHospedaje,
                    String tarjeta, String danios, String total, int imagenResId, Reserva reservaCompleta) {
        this.nombre = nombre;
        this.codigoReserva = codigoReserva;
        this.montoHospedaje = montoHospedaje;
        this.tarjeta = tarjeta;
        this.danios = danios;
        this.total = total;
        this.imagenResId = imagenResId;
        this.reservaCompleta = reservaCompleta;
    }

    // Constructor original para mantener compatibilidad
    public Checkout(String nombre, String codigoReserva, String montoHospedaje,
                    String tarjeta, String danios, String total, int imagenResId) {
        this(nombre, codigoReserva, montoHospedaje, tarjeta, danios, total, imagenResId, null);
    }

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoReserva() { return codigoReserva; }
    public void setCodigoReserva(String codigoReserva) { this.codigoReserva = codigoReserva; }

    public String getMontoHospedaje() { return montoHospedaje; }
    public void setMontoHospedaje(String montoHospedaje) { this.montoHospedaje = montoHospedaje; }

    public String getTarjeta() { return tarjeta; }
    public void setTarjeta(String tarjeta) { this.tarjeta = tarjeta; }

    public String getDanios() { return danios; }
    public void setDanios(String danios) { this.danios = danios; }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }

    public int getImagenResId() { return imagenResId; }
    public void setImagenResId(int imagenResId) { this.imagenResId = imagenResId; }

    public Reserva getReservaCompleta() { return reservaCompleta; }
    public void setReservaCompleta(Reserva reservaCompleta) { this.reservaCompleta = reservaCompleta; }
}