package com.iot.stayflowdev.Driver.Model;

public class ReservaModel {

    private int id;
    private String nombrePasajero;
    private String origen;
    private String destino;
    private String distancia;
    private String fecha;
    private String hora;
    private int tipoIcono; // Recursos de drawable (R.drawable.ic_hotel o R.drawable.ic_aeropuerto)
    private String estado; // "en_curso", "pasado" o "cancelado"

    // Constructor
    public ReservaModel(int id, String nombrePasajero, String origen, String destino,
                   String distancia, String fecha, String hora, int tipoIcono, String estado) {
        this.id = id;
        this.nombrePasajero = nombrePasajero;
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.fecha = fecha;
        this.hora = hora;
        this.tipoIcono = tipoIcono;
        this.estado = estado;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombrePasajero() { return nombrePasajero; }
    public void setNombrePasajero(String nombrePasajero) { this.nombrePasajero = nombrePasajero; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getDistancia() { return distancia; }
    public void setDistancia(String distancia) { this.distancia = distancia; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public int getTipoIcono() { return tipoIcono; }
    public void setTipoIcono(int tipoIcono) { this.tipoIcono = tipoIcono; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Método para obtener fecha y hora concatenadas
    public String getFechaHora() {
        return fecha + " - " + hora;
    }
}
