package com.iot.stayflowdev.adminHotel.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AdminHotelRepository {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    private final MutableLiveData<String> nombreAdmin = new MutableLiveData<>();
    private final MutableLiveData<String> hotelId = new MutableLiveData<>();
    private final MutableLiveData<String> nombreHotel = new MutableLiveData<>();

    public AdminHotelRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cargarDatosAdministrador();
    }

    private void cargarDatosAdministrador() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombres = documentSnapshot.getString("nombres");
                        String apellidos = documentSnapshot.getString("apellidos");
                        nombreAdmin.setValue(nombres + " " + apellidos);

                        String idHotel = documentSnapshot.getString("hotelAsignado");
                        hotelId.setValue(idHotel);

                        if (idHotel != null && !idHotel.isEmpty()) {
                            db.collection("hoteles").document(idHotel).get()
                                    .addOnSuccessListener(hotelDoc -> {
                                        if (hotelDoc.exists()) {
                                            String nombre = hotelDoc.getString("nombre");
                                            nombreHotel.setValue(nombre);
                                        }
                                    });
                        } else {
                            nombreHotel.setValue("Hotel no asignado");
                        }
                    }
                });
    }

    public MutableLiveData<String> getNombreAdmin() {
        return nombreAdmin;
    }

    public MutableLiveData<String> getHotelId() {
        return hotelId;
    }

    public MutableLiveData<String> getNombreHotel() {
        return nombreHotel;
    }
    public MutableLiveData<String> getUbicacionHotel() {
        MutableLiveData<String> ubicacionHotel = new MutableLiveData<>();
        String idHotel = hotelId.getValue();
        if (idHotel != null && !idHotel.isEmpty()) {
            db.collection("hoteles").document(idHotel).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String ubicacion = documentSnapshot.getString("ubicacion");
                            ubicacionHotel.setValue(ubicacion);
                        } else {
                            ubicacionHotel.setValue("Ubicación no disponible");
                        }
                    });
        } else {
            ubicacionHotel.setValue("Ubicación no disponible");
        }
        return ubicacionHotel;
    }

    public void actualizarUbicacionHotel(String hotelId, String nuevaUbicacion, Runnable onSuccess, Runnable onError) {
        db.collection("hoteles")
                .document(hotelId)
                .update("ubicacion", nuevaUbicacion)
                .addOnSuccessListener(unused -> onSuccess.run())
                .addOnFailureListener(e -> onError.run());
    }
}
