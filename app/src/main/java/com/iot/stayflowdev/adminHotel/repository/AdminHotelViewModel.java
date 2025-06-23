package com.iot.stayflowdev.adminHotel.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHotelViewModel extends ViewModel {

    private final AdminHotelRepository repository;
    private String hotelIdCache = null;
    private final MutableLiveData<String> descripcionHotel = new MutableLiveData<>();

    public AdminHotelViewModel() {
        repository = new AdminHotelRepository();

        // Cachear el hotelId cuando esté disponible
        repository.getHotelId().observeForever(id -> {
            hotelIdCache = id;
            cargarDescripcionHotel();
        });
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

    public LiveData<String> getDescripcionHotel() {
        return descripcionHotel;
    }

    public void actualizarDescripcionHotel(String descripcion) {
        if (hotelIdCache == null) {
            Log.e("AdminVM", "Hotel ID aún no está disponible");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelIdCache)
                .update("descripcion", descripcion)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminVM", "Descripción actualizada");
                    descripcionHotel.setValue(descripcion);
                })
                .addOnFailureListener(e -> Log.e("AdminVM", "Error al actualizar descripción", e));
    }

    private void cargarDescripcionHotel() {
        if (hotelIdCache == null) return;

        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelIdCache)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String descripcion = document.getString("descripcion");
                        descripcionHotel.setValue(descripcion);
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminVM", "Error al cargar descripción", e));
    }
}
