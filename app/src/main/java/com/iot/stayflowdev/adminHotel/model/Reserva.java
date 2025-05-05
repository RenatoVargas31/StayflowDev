package com.iot.stayflowdev.adminHotel.model;

public class Reserva {
    private String nombre;
    private String habitacion;
    private String detallePersonas;
    private int imagenResId;
    private String codigoReserva;
    private String fechaLlegada;
    private String fechaSalida;

    public Reserva(String nombre, String habitacion, String detallePersonas, int imagenResId,
                   String codigoReserva, String fechaLlegada, String fechaSalida) {
        this.nombre = nombre;
        this.habitacion = habitacion;
        this.detallePersonas = detallePersonas;
        this.imagenResId = imagenResId;
        this.codigoReserva = codigoReserva;
        this.fechaLlegada = fechaLlegada;
        this.fechaSalida = fechaSalida;
    }

    public String getNombre() { return nombre; }
    public String getHabitacion() { return habitacion; }
    public String getDetallePersonas() { return detallePersonas; }
    public int getImagenResId() { return imagenResId; }
    public String getCodigoReserva() { return codigoReserva; }
    public String getFechaLlegada() { return fechaLlegada; }
    public String getFechaSalida() { return fechaSalida; }
}
