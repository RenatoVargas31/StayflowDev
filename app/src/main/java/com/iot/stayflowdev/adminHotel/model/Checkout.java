package com.iot.stayflowdev.adminHotel.model;

public class Checkout {
    private String nombre;
    private String codigoReserva;
    private String montoHospedaje;
    private String tarjeta;
    private String danios;
    private String total;
    private int imagenResId;

    public Checkout(String nombre, String codigoReserva, String montoHospedaje,
                    String tarjeta, String danios, String total, int imagenResId) {
        this.nombre = nombre;
        this.codigoReserva = codigoReserva;
        this.montoHospedaje = montoHospedaje;
        this.tarjeta = tarjeta;
        this.danios = danios;
        this.total = total;
        this.imagenResId = imagenResId;
    }

    public String getNombre() { return nombre; }
    public String getCodigoReserva() { return codigoReserva; }
    public String getMontoHospedaje() { return montoHospedaje; }
    public String getTarjeta() { return tarjeta; }
    public String getDanios() { return danios; }
    public String getTotal() { return total; }
    public int getImagenResId() { return imagenResId; }
}
