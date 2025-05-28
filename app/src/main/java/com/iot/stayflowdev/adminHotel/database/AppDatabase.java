package com.iot.stayflowdev.adminHotel.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HabitacionEntity.class, ServicioEntity.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HabitacionDao habitacionDao();
    public abstract ServicioDao servicioDao();
}
