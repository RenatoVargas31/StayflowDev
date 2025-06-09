package com.iot.stayflowdev.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.repositories.LugaresCercanosRepository;

import java.util.List;

public class LugaresCercanosViewModel extends ViewModel {

    private final LugaresCercanosRepository repository = new LugaresCercanosRepository();
    private LiveData<List<LugaresCercanos>> lugares;

    public void cargarLugares(String hotelId) {
        lugares = repository.getLugares(hotelId);
    }

    public LiveData<List<LugaresCercanos>> getLugares() {
        return lugares;
    }

    public void agregarLugar(String hotelId, LugaresCercanos lugar, Runnable onSuccess) {
        repository.agregarLugar(hotelId, lugar, onSuccess);
    }

    public void eliminarLugar(String hotelId, String lugarId, Runnable onSuccess) {
        repository.eliminarLugar(hotelId, lugarId, onSuccess);
    }

    public void actualizarLugar(String hotelId, LugaresCercanos lugar, Runnable onSuccess) {
        repository.actualizarLugar(hotelId, lugar, onSuccess);
    }
}
