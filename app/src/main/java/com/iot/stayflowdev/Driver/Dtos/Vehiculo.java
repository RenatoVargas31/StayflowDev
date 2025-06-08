package com.iot.stayflowdev.Driver.Dtos;

import java.security.Timestamp;
import java.util.Calendar;
import java.util.Calendar;

public class Vehiculo {
    private String id; // ID del documento de Firebase
    private String licensePlate;
    private String brand;
    private String model;
    private String color;
    private int year;
    private boolean isPrimary;
    private int photoId;
    private int driverId;

    // Constructor vacío requerido por Firebase
    public Vehiculo() {}

    // Constructor with parameters
    public Vehiculo(String licensePlate, String brand, String model, String color,
                    int year, boolean isPrimary) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.year = year;
        this.isPrimary = isPrimary;
    }

    // Constructor completo
    public Vehiculo(String id, String licensePlate, String brand, String model, String color,
                    int year, boolean isPrimary, int photoId, int driverId) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.year = year;
        this.isPrimary = isPrimary;
        this.photoId = photoId;
        this.driverId = driverId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    // Métodos adicionales para el RecyclerView
    public String getFullName() {
        return brand + " " + model + " " + year;
    }

    public String getDetails() {
        return "Sedán • " + color; // Puedes agregar más lógica aquí
    }

    public String getFormattedPlate() {
        return "Placa: " + licensePlate;
    }

    // Validation method
    public boolean isValid() {
        return licensePlate != null && !licensePlate.trim().isEmpty() &&
                brand != null && !brand.trim().isEmpty() &&
                model != null && !model.trim().isEmpty() &&
                color != null && !color.trim().isEmpty() &&
                year >= 1950 && year <= Calendar.getInstance().get(Calendar.YEAR) + 1;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Vehiculo{" +
                "id='" + id + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", isPrimary=" + isPrimary +
                '}';
    }
}