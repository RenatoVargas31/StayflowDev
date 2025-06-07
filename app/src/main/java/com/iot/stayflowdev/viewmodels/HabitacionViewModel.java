package com.iot.stayflowdev.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.iot.stayflowdev.model.Habitacion;
import com.iot.stayflowdev.repositories.HabitacionRepository;

import java.util.List;

public class HabitacionViewModel extends ViewModel {

    private final HabitacionRepository repository = new HabitacionRepository();
    private LiveData<List<Habitacion>> habitaciones;

    public void cargarHabitaciones(String hotelId) {
        habitaciones = repository.getHabitaciones(hotelId);
    }

    public LiveData<List<Habitacion>> getHabitaciones() {
        return habitaciones;
    }

    public void agregar(String hotelId, Habitacion habitacion) {
        repository.agregarHabitacion(hotelId, habitacion, () -> cargarHabitaciones(hotelId));
    }

    public void actualizar(String hotelId, Habitacion habitacion, Runnable onSuccess) {
        repository.actualizarHabitacion(hotelId, habitacion, () -> {
            cargarHabitaciones(hotelId);
            onSuccess.run();
        });
    }

    public void eliminar(String hotelId, String habitacionId, Runnable onSuccess) {
        repository.eliminarHabitacion(hotelId, habitacionId, () -> {
            cargarHabitaciones(hotelId);
            onSuccess.run();
        });
    }
}
