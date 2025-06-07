// HabitacionRepository.java
package com.iot.stayflowdev.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.iot.stayflowdev.model.Habitacion;

import java.util.ArrayList;
import java.util.List;

public class HabitacionRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Habitacion>> habitacionesLiveData = new MutableLiveData<>();

    public LiveData<List<Habitacion>> getHabitaciones(String hotelId) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("habitaciones")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Habitacion> habitaciones = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Habitacion h = doc.toObject(Habitacion.class);
                        if (h != null) {
                            h.setId(doc.getId());
                            habitaciones.add(h);
                        }
                    }
                    habitacionesLiveData.setValue(habitaciones);
                });

        return habitacionesLiveData;
    }

    public void agregarHabitacion(String hotelId, Habitacion habitacion, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("habitaciones")
                .add(habitacion)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firebase", "Habitación agregada");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error al agregar", e));
    }

    public void actualizarHabitacion(String hotelId, Habitacion habitacion, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("habitaciones")
                .document(habitacion.getId())
                .set(habitacion)
                .addOnSuccessListener(unused -> {
                    Log.d("Firebase", "Habitación actualizada");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error al actualizar", e));
    }


    public void eliminarHabitacion(String hotelId, String habitacionId, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("habitaciones")
                .document(habitacionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Habitación eliminada");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error al eliminar", e));
    }

}
