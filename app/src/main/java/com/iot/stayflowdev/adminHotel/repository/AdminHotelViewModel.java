package com.iot.stayflowdev.adminHotel.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.iot.stayflowdev.adminHotel.repository.AdminHotelRepository;

public class AdminHotelViewModel extends ViewModel {

    private final AdminHotelRepository repository;

    public AdminHotelViewModel() {
        repository = new AdminHotelRepository();
    }

    public LiveData<String> getNombreAdmin() {
        return repository.getNombreAdmin();
    }

    public LiveData<String> getHotelId() {
        return repository.getHotelId();
    }

    public LiveData<String> getNombreHotel() {
        return repository.getNombreHotel();
    }
    public LiveData<String> getUbicacionHotel() {
        return repository.getUbicacionHotel();
    }
    public void actualizarUbicacion(String hotelId, String nuevaUbicacion, Runnable onSuccess, Runnable onError) {
        repository.actualizarUbicacionHotel(hotelId, nuevaUbicacion, onSuccess, onError);
    }
}
