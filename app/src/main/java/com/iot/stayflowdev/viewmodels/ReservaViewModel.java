package com.iot.stayflowdev.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.iot.stayflowdev.model.Hotel;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.repositories.HotelRepository;
import com.iot.stayflowdev.repositories.ReservaRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ReservaViewModel extends ViewModel {
    private static final String TAG = "ReservaViewModel";

    private final ReservaRepository reservaRepository;
    private final HotelRepository hotelRepository;

    // LiveData para la UI
    private final MutableLiveData<List<Reserva>> reservasLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Hotel>> hotelesCache = new MutableLiveData<>(new HashMap<>());

    // Fechas de filtrado
    private Date fechaInicio;
    private Date fechaFin;

    public ReservaViewModel() {
        this.reservaRepository = new ReservaRepository();
        this.hotelRepository = new HotelRepository();

        // Inicializar fechas por defecto (últimos 30 días)
        Calendar calendar = Calendar.getInstance();
        fechaFin = calendar.getTime(); // Hoy

        calendar.add(Calendar.DAY_OF_MONTH, -30);
        fechaInicio = calendar.getTime(); // Hace 30 días
    }

    /**
     * Obtiene las reservas filtradas por la fecha de creación
     */
    public void buscarReservasPorFecha() {
        isLoadingLiveData.setValue(true);

        Log.d(TAG, "Buscando reservas desde " + formatDate(fechaInicio) + " hasta " + formatDate(fechaFin));

        reservaRepository.getReservasByFechaCreacion(fechaInicio, fechaFin)
            .thenAccept(reservas -> {
                reservasLiveData.postValue(reservas);
                isLoadingLiveData.postValue(false);
                Log.d(TAG, "Reservas encontradas: " + reservas.size());
            })
            .exceptionally(e -> {
                errorLiveData.postValue("Error al buscar reservas: " + e.getMessage());
                isLoadingLiveData.postValue(false);
                Log.e(TAG, "Error al buscar reservas", e);
                return null;
            });
    }

    /**
     * Obtiene todas las reservas del usuario actual
     */
    public void obtenerTodasLasReservas() {
        isLoadingLiveData.setValue(true);

        reservaRepository.getAllReservas()
            .thenAccept(reservas -> {
                reservasLiveData.postValue(reservas);
                isLoadingLiveData.postValue(false);
            })
            .exceptionally(e -> {
                errorLiveData.postValue("Error al obtener reservas: " + e.getMessage());
                isLoadingLiveData.postValue(false);
                return null;
            });
    }

    /**
     * Obtiene la información del hotel para una reserva específica
     * @param reserva La reserva para la que se quiere obtener el hotel
     * @return CompletableFuture con el hotel asociado a la reserva
     */
    public CompletableFuture<Hotel> obtenerHotelParaReserva(Reserva reserva) {
        CompletableFuture<Hotel> future = new CompletableFuture<>();

        // Verificar si el hotel ya está en caché
        Map<String, Hotel> cache = hotelesCache.getValue();
        if (cache != null && cache.containsKey(reserva.getIdHotel())) {
            future.complete(cache.get(reserva.getIdHotel()));
            return future;
        }

        // Si no está en caché, obtenerlo de Firestore
        hotelRepository.obtenerHotelPorId(reserva.getIdHotel())
            .observeForever(hotel -> {
                if (hotel != null) {
                    // Guardar en caché
                    Map<String, Hotel> updatedCache = hotelesCache.getValue();
                    if (updatedCache != null) {
                        updatedCache.put(reserva.getIdHotel(), hotel);
                        hotelesCache.postValue(updatedCache);
                    }
                    future.complete(hotel);
                } else {
                    Hotel hotelDefault = new Hotel();
                    hotelDefault.setNombre("Hotel no encontrado");
                    future.complete(hotelDefault);
                }
            });

        return future;
    }

    /**
     * Establece la fecha de inicio para el filtrado
     * @param fecha Nueva fecha de inicio
     */
    public void setFechaInicio(Date fecha) {
        this.fechaInicio = fecha;
    }

    /**
     * Establece la fecha de fin para el filtrado
     * @param fecha Nueva fecha de fin
     */
    public void setFechaFin(Date fecha) {
        this.fechaFin = fecha;
    }

    /**
     * Formatea una fecha en formato dd/MM/yyyy
     * @param date Fecha a formatear
     * @return String con la fecha formateada
     */
    public String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // Getters para los LiveData

    public LiveData<List<Reserva>> getReservas() {
        return reservasLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }
}
