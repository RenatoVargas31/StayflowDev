package com.iot.stayflowdev.Driver.Dtos;

import java.security.Timestamp;
import java.util.Calendar;
import java.util.Calendar;
public class Vehiculo {
    private String id; // ID del documento de Firebase
    private String placa;
    private String modelo;
    private String driverId;
    private String fotoVehiculo;
    private boolean activo;

    // Constructor vacío requerido por Firebase
    public Vehiculo() {}

    // Constructor básico
    public Vehiculo(String placa, String modelo, String driverId) {
        this.placa = placa;
        this.modelo = modelo;
        this.driverId = driverId;
        this.activo = true; // Por defecto activo
    }

    // Constructor completo
    public Vehiculo(String id, String placa, String modelo, String driverId,
                    String fotoVehiculo, boolean activo) {
        this.id = id;
        this.placa = placa;
        this.modelo = modelo;
        this.driverId = driverId;
        this.fotoVehiculo = fotoVehiculo;
        this.activo = activo;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getFotoVehiculo() {
        return fotoVehiculo;
    }

    public void setFotoVehiculo(String fotoVehiculo) {
        this.fotoVehiculo = fotoVehiculo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Métodos de utilidad
    public boolean tieneFoto() {
        return fotoVehiculo != null && !fotoVehiculo.isEmpty();
    }

    public String getMarcaYModelo() {
        if (modelo != null && modelo.contains(" ")) {
            return modelo; // "Toyota Yaris"
        }
        return modelo != null ? modelo : "Vehículo";
    }

    public String getMarca() {
        if (modelo != null && modelo.contains(" ")) {
            return modelo.split(" ")[0]; // "Toyota"
        }
        return "Marca";
    }

    public String getModeloSolo() {
        if (modelo != null && modelo.contains(" ")) {
            String[] partes = modelo.split(" ");
            if (partes.length > 1) {
                return partes[1]; // "Yaris"
            }
        }
        return modelo != null ? modelo : "Modelo";
    }

    public String getPlacaFormateada() {
        return placa != null ? placa : "XXX-000";
    }

    // Validation method
    public boolean isValid() {
        return placa != null && !placa.trim().isEmpty() &&
                modelo != null && !modelo.trim().isEmpty() &&
                driverId != null && !driverId.trim().isEmpty();
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Vehiculo{" +
                "id='" + id + '\'' +
                ", placa='" + placa + '\'' +
                ", modelo='" + modelo + '\'' +
                ", driverId='" + driverId + '\'' +
                ", fotoVehiculo='" + fotoVehiculo + '\'' +
                ", activo=" + activo +
                '}';
    }
}