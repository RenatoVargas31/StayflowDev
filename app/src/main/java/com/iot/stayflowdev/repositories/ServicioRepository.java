package com.iot.stayflowdev.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.model.Servicio;

import java.util.ArrayList;
import java.util.List;

public class ServicioRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Servicio>> serviciosLiveData = new MutableLiveData<>();

    public LiveData<List<Servicio>> getServicios(String hotelId) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("servicios")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) {
                        Log.e("Firebase", "Error al escuchar servicios", error);
                        serviciosLiveData.setValue(new ArrayList<>()); // o manten lo que había
                        return;
                    }

                    List<Servicio> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Servicio s = doc.toObject(Servicio.class);
                        if (s != null) {
                            s.setId(doc.getId()); // 👈 muy importante
                            lista.add(s);
                        }
                    }

                    serviciosLiveData.setValue(lista);
                });

        return serviciosLiveData;
    }


    public void agregar(String hotelId, Servicio servicio, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("servicios")
                .add(servicio)
                .addOnSuccessListener(doc -> {
                    Log.d("Firebase", "Servicio agregado");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error al agregar servicio", e));
    }

    public void actualizar(String hotelId, Servicio servicio, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("servicios")
                .document(servicio.getId())
                .set(servicio)
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(e -> Log.e("Firebase", "Error al actualizar servicio", e));
    }

    public void eliminar(String hotelId, String servicioId, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("servicios")
                .document(servicioId)
                .delete()
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(e -> Log.e("Firebase", "Error al eliminar servicio", e));
    }
}
