package com.iot.stayflowdev.adminHotel.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitacionDao {
    @Insert
    void insertar(HabitacionEntity habitacion);

    @Query("SELECT * FROM habitaciones")
    List<HabitacionEntity> obtenerTodas();

    @Update
    void actualizar(HabitacionEntity habitacion);

    @Delete
    void eliminar(HabitacionEntity habitacion);

    @Query("SELECT COUNT(*) FROM habitaciones")
    int contarHabitaciones();
}
