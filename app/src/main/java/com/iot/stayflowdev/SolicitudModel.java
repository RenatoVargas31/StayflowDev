package com.iot.stayflowdev;

public class SolicitudModel {

    private String distance;
    private String pickupLocation;
    private String destinationLocation;
    private String tiempo;
    private String estimatedTime;

    public SolicitudModel(String distance, String pickupLocation,
                          String destinationLocation, String tiempo,
                          String estimatedTime) {
        this.distance = distance;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.tiempo = tiempo;
        this.estimatedTime = estimatedTime;
    }

    public String getDistance() { return distance; }
    public String getPickupLocation() { return pickupLocation; }
    public String getDestinationLocation() { return destinationLocation; }
    public String getTiempo() { return tiempo; }
    public String getEstimatedTime() { return estimatedTime; }
}
