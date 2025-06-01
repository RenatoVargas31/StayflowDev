package com.iot.stayflowdev.Driver.Model;

public class SolicitudModel {
    private String distance;
    private String pickupLocation;
    private String destinationLocation;
    private String tiempo;
    private String estimatedTime;

    // Campos adicionales para la información detallada
    private String passengerName;
    private String passengerPhone;
    private String passengersCount;
    private String vehicleType;
    private String notes;
    private String status;

    // Constructor básico (el que ya estabas usando)
    public SolicitudModel(String distance, String pickupLocation, String destinationLocation,
                          String tiempo, String estimatedTime) {
        this.distance = distance;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.tiempo = tiempo;
        this.estimatedTime = estimatedTime;

        // Valores por defecto para los campos adicionales
        this.passengerName = "Usuario";
        this.passengerPhone = "+51 999 888 777";
        this.passengersCount = "1";
        this.vehicleType = "Sedán estándar";
        this.notes = "";
        this.status = "Pendiente";
    }

    // Constructor completo con todos los campos
    public SolicitudModel(String distance, String pickupLocation, String destinationLocation,
                          String tiempo, String estimatedTime, String passengerName,
                          String passengerPhone, String passengersCount, String vehicleType,
                          String notes, String status) {
        this.distance = distance;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.tiempo = tiempo;
        this.estimatedTime = estimatedTime;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.passengersCount = passengersCount;
        this.vehicleType = vehicleType;
        this.notes = notes;
        this.status = status;
    }

    // Getters y setters para todos los campos
    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getPassengersCount() {
        return passengersCount;
    }

    public void setPassengersCount(String passengersCount) {
        this.passengersCount = passengersCount;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}