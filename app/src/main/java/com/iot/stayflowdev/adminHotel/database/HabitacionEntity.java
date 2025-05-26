package com.iot.stayflowdev.adminHotel.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habitaciones")
public class HabitacionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull public String tipo;
    public int capacidad;
    public int tamano;
    public double precio;
    public boolean enUso;

    public HabitacionEntity(@NonNull String tipo, int capacidad, int tamano, double precio, boolean enUso) {
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tamano = tamano;
        this.precio = precio;
        this.enUso = enUso;
    }
}

