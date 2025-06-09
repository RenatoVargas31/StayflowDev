package com.iot.stayflowdev.repositories;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.model.LugaresCercanos;

import java.util.ArrayList;
import java.util.List;

public class LugaresCercanosRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<LugaresCercanos>> lugaresLiveData = new MutableLiveData<>();

    public LiveData<List<LugaresCercanos>> getLugares(String hotelId) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("lugaresCercanos")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<LugaresCercanos> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        LugaresCercanos lugar = doc.toObject(LugaresCercanos.class);
                        if (lugar != null) {
                            lugar.setId(doc.getId());
                            lista.add(lugar);
                        }
                    }
                    lugaresLiveData.setValue(lista);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al cargar lugares", e));

        return lugaresLiveData;
    }

    public void agregarLugar(String hotelId, LugaresCercanos lugar, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("lugaresCercanos")
                .add(lugar)
                .addOnSuccessListener(docRef -> onSuccess.run())
                .addOnFailureListener(e -> Log.e("Firestore", "Error al agregar lugar", e));
    }

    public void eliminarLugar(String hotelId, String lugarId, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("lugaresCercanos")
                .document(lugarId)
                .delete()
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(e -> Log.e("Firestore", "Error al eliminar lugar", e));
    }

    public void actualizarLugar(String hotelId, LugaresCercanos lugar, Runnable onSuccess) {
        db.collection("hoteles")
                .document(hotelId)
                .collection("lugaresCercanos")
                .document(lugar.getId())
                .set(lugar)
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar lugar", e));
    }

}
