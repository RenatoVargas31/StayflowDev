package com.iot.stayflowdev.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.LugaresCercanos;
import com.iot.stayflowdev.model.Servicio;
import com.iot.stayflowdev.repositories.HotelRepository;

import java.util.List;

public class HotelViewModel extends ViewModel {

    private final HotelRepository repository = new HotelRepository();
    private LiveData<List<Hotel>> hoteles;
    private LiveData<List<Hotel>> hotelesMejorValorados;
    private LiveData<String> error;

    /**
     * Obtiene todos los hoteles disponibles
     * @return LiveData con la lista de hoteles
     */
    public LiveData<List<Hotel>> getHoteles() {
        if (hoteles == null) {
            hoteles = repository.obtenerHoteles();
        }
        return hoteles;
    }

    /**
     * Obtiene los 10 hoteles mejor valorados
     * @return LiveData con la lista de los 10 hoteles con mejor calificación
     */
    public LiveData<List<Hotel>> getHotelesMejorValorados() {
        if (hotelesMejorValorados == null) {
            hotelesMejorValorados = repository.obtenerHotelesMejorValorados();
        }
        return hotelesMejorValorados;
    }

    /**
     * Obtiene un hotel específico por su ID
     * @param hotelId ID del hotel a obtener
     * @return LiveData con el hotel solicitado
     */
    public LiveData<Hotel> getHotelPorId(String hotelId) {
        return repository.obtenerHotelPorId(hotelId);
    }

    /**
     * Obtiene errores que puedan ocurrir durante las operaciones con hoteles
     * @return LiveData con mensajes de error
     */
    public LiveData<String> getError() {
        if (error == null) {
            error = repository.getErrorLiveData();
        }
        return error;
    }

    /**
     * Refresca la lista de hoteles mejor valorados
     */
    public void refrescarHotelesMejorValorados() {
        hotelesMejorValorados = repository.obtenerHotelesMejorValorados();
    }

    /**
     * Busca hoteles por nombre
     * @param query Texto a buscar en nombre de hoteles
     * @return LiveData con la lista de hoteles que coinciden con la búsqueda
     */
    public LiveData<List<Hotel>> buscarHotelesPorNombre(String query) {
        return repository.buscarHotelesPorNombre(query);
    }

    /**
     * Obtiene los servicios de un hotel específico
     * @param hotelId ID del hotel
     * @return LiveData con la lista de servicios del hotel
     */
    public LiveData<List<Servicio>> getServiciosPorHotel(String hotelId) {
        return repository.obtenerServiciosPorHotel(hotelId);
    }

    /**
     * Obtiene los lugares históricos cercanos a un hotel específico
     * @param hotelId ID del hotel
     * @return LiveData con la lista de lugares históricos cercanos al hotel
     */
    public LiveData<List<LugaresCercanos>> getLugaresHistoricosPorHotel(String hotelId) {
        return repository.obtenerLugaresHistoricosCercanos(hotelId);
    }
    public void disminuirHabitacionesDisponibles(String hotelId, String habitacionId, int cantidad) {
        repository.disminuirHabitacionesDisponibles(hotelId, habitacionId,cantidad);
    }

    private final MutableLiveData<String> descripcionHotel = new MutableLiveData<>();
    public LiveData<String> getDescripcionHotel() {
        return descripcionHotel;
    }
}
