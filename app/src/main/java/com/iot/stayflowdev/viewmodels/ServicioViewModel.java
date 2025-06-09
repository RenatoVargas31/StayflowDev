package com.iot.stayflowdev.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.iot.stayflowdev.model.Servicio;
import com.iot.stayflowdev.repositories.ServicioRepository;

import java.util.List;

public class ServicioViewModel extends ViewModel {

    private final ServicioRepository repository = new ServicioRepository();

    public LiveData<List<Servicio>> getServicios(String hotelId) {
        return repository.getServicios(hotelId);
    }

    public void agregar(String hotelId, Servicio servicio, Runnable onSuccess) {
        repository.agregar(hotelId, servicio, onSuccess);
    }

    public void actualizar(String hotelId, Servicio servicio, Runnable onSuccess) {
        repository.actualizar(hotelId, servicio, onSuccess);
    }

    public void eliminar(String hotelId, String servicioId, Runnable onSuccess) {
        repository.eliminar(hotelId, servicioId, onSuccess);
    }
}
