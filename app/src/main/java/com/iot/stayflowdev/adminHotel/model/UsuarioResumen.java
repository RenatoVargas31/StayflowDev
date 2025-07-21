package com.iot.stayflowdev.adminHotel.model;

public class UsuarioResumen {
    private String idUsuario;
    private int cantidadReservas;
    private double monto;
    private String nombre;

    public UsuarioResumen(String idUsuario, int cantidadReservas) {
        this.idUsuario = idUsuario;
        this.cantidadReservas = cantidadReservas;
    }

    public UsuarioResumen(String idUsuario, double monto) {
        this.idUsuario = idUsuario;
        this.monto = monto;
    }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public int getCantidadReservas() { return cantidadReservas; }
    public void setCantidadReservas(int cantidadReservas) { this.cantidadReservas = cantidadReservas; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
