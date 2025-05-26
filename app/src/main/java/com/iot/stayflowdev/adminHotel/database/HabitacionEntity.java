// HabitacionEntity.java
package com.iot.stayflowdev.adminHotel.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habitaciones")
public class HabitacionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String tipo;
    public int capacidad;
    public int tamano;

    public HabitacionEntity(@NonNull String tipo, int capacidad, int tamano) {
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tamano = tamano;
    }
}
