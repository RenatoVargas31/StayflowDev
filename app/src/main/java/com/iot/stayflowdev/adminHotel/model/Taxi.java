package com.iot.stayflowdev.adminHotel.model;

public class Taxi {
    private String nombre;
    private String codigoReserva;  // ← importante, nombre claro
    private String estadoTaxi;
    private String detalleViaje;
    private String ruta;
    private int imagenResId;
    private String idTaxista; // nuevo campo

    // Inclúyelo en el constructor y en los getters/setters
    public Taxi(String nombre, String codigoReserva, String estadoTaxi, String detalleViaje, String ruta, int imagenResId, String idTaxista) {
        this.nombre = nombre;
        this.codigoReserva = codigoReserva;
        this.estadoTaxi = estadoTaxi;
        this.detalleViaje = detalleViaje;
        this.ruta = ruta;
        this.imagenResId = imagenResId;
        this.idTaxista = idTaxista;
    }

    public String getIdTaxista() {
        return idTaxista;
    }

    public Taxi(String nombre, String codigoReserva, String estadoTaxi, String detalleViaje, String ruta, int imagenResId) {
        this.nombre = nombre;
        this.codigoReserva = codigoReserva;
        this.estadoTaxi = estadoTaxi;
        this.detalleViaje = detalleViaje;
        this.ruta = ruta;
        this.imagenResId = imagenResId;
    }

    public String getNombre() { return nombre; }
    public String getCodigoReserva() { return codigoReserva; }  // ← asegúrate de usar este getter
    public String getEstadoTaxi() { return estadoTaxi; }
    public String getDetalleViaje() { return detalleViaje; }
    public String getRuta() { return ruta; }
    public int getImagenResId() { return imagenResId; }
}
