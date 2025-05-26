package com.iot.stayflowdev.adminHotel.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface ServicioDao {
    @Insert
    void insertar(ServicioEntity servicio);

    @Query("SELECT * FROM servicios")
    List<ServicioEntity> obtenerTodos();

    @Update
    void actualizar(ServicioEntity servicio);

    @Delete
    void eliminar(ServicioEntity servicio);
}
