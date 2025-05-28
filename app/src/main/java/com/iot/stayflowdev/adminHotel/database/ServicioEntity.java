package com.iot.stayflowdev.adminHotel.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "servicios")
public class ServicioEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull public String nombre;
    public String descripcion;
    public String precio;
    public boolean esGratis;

    public ServicioEntity(@NonNull String nombre, String descripcion, String precio, boolean esGratis) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.esGratis = esGratis;
    }
}
