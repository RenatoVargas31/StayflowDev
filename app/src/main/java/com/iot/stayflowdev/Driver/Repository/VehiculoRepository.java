package com.iot.stayflowdev.Driver.Repository;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iot.stayflowdev.Driver.Dtos.Vehiculo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehiculoRepository {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private static final String TAG = "VehiculoRepository";

    public VehiculoRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    private String obtenerUidUsuario() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    // Obtener vehículo del taxista usando la placa desde el usuario
    public void obtenerVehiculoTaxista(String placaUsuario, OnSuccessListener<Vehiculo> success, OnFailureListener failure) {
        if (placaUsuario == null || placaUsuario.isEmpty()) {
            failure.onFailure(new Exception("No hay placa configurada en el usuario"));
            return;
        }

        db.collection("vehiculo")
                .document(placaUsuario) // El ID del documento es la placa
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Vehiculo vehiculo = documentSnapshot.toObject(Vehiculo.class);

                        if (vehiculo != null) {
                            vehiculo.setId(documentSnapshot.getId()); // La placa como ID
                            success.onSuccess(vehiculo);
                        } else {
                            failure.onFailure(new Exception("Error al convertir datos del vehículo"));
                        }
                    } else {
                        failure.onFailure(new Exception("No se encontró vehículo con placa: " + placaUsuario));
                    }
                })
                .addOnFailureListener(failure);
    }

    // Obtener vehículo por placa (ID del documento)
    public void obtenerVehiculoPorPlaca(String placa, OnSuccessListener<Vehiculo> success, OnFailureListener failure) {
        db.collection("vehiculo")
                .document(placa) // La placa ES el ID del documento
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Vehiculo vehiculo = documentSnapshot.toObject(Vehiculo.class);

                        if (vehiculo != null) {
                            vehiculo.setId(documentSnapshot.getId());
                            success.onSuccess(vehiculo);
                        } else {
                            failure.onFailure(new Exception("Error al convertir datos del vehículo"));
                        }
                    } else {
                        failure.onFailure(new Exception("Vehículo no encontrado"));
                    }
                })
                .addOnFailureListener(failure);
    }

    // Obtener todos los vehículos del taxista autenticado
    public void obtenerTodosVehiculosTaxista(OnSuccessListener<List<Vehiculo>> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection("vehiculo")
                    .whereEqualTo("driverId", uid)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Vehiculo> vehiculos = new ArrayList<>();

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Vehiculo vehiculo = document.toObject(Vehiculo.class);
                            if (vehiculo != null) {
                                vehiculo.setId(document.getId());
                                vehiculos.add(vehiculo);
                            }
                        }

                        success.onSuccess(vehiculos);
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Crear nuevo vehículo usando la placa como ID del documento
    public void crearVehiculo(Vehiculo vehiculo, OnSuccessListener<String> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();
            vehiculo.setDriverId(uid);
            vehiculo.setActivo(true);

            if (vehiculo.getPlaca() == null || vehiculo.getPlaca().isEmpty()) {
                failure.onFailure(new Exception("La placa es requerida"));
                return;
            }

            // Usar la placa como ID del documento
            db.collection("vehiculo")
                    .document(vehiculo.getPlaca())
                    .set(vehiculo)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Vehículo creado con placa: " + vehiculo.getPlaca());
                        success.onSuccess(vehiculo.getPlaca());
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Actualizar información del vehículo usando la placa como ID
    public void actualizarVehiculo(String placa, Map<String, Object> datos,
                                   OnSuccessListener<Void> success, OnFailureListener failure) {
        db.collection("vehiculo")
                .document(placa)
                .update(datos)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }

    // Desactivar vehículo (en lugar de eliminarlo)
    public void desactivarVehiculo(String placa, OnSuccessListener<Void> success, OnFailureListener failure) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("activo", false);

        actualizarVehiculo(placa, datos, success, failure);
    }

    // Activar vehículo
    public void activarVehiculo(String placa, OnSuccessListener<Void> success, OnFailureListener failure) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("activo", true);

        actualizarVehiculo(placa, datos, success, failure);
    }

    // Subir foto del vehículo
    public void subirFotoVehiculo(String placa, Uri imagenUri,
                                  OnSuccessListener<String> success, OnFailureListener failure,
                                  OnProgressListener<UploadTask.TaskSnapshot> progress) {
        try {
            // Crear referencia con la placa en el nombre
            String nombreArchivo = placa + ".jpg";
            StorageReference imageRef = storage.getReference()
                    .child("fotos_vehiculo")
                    .child(nombreArchivo);

            // Subir imagen
            UploadTask uploadTask = imageRef.putFile(imagenUri);

            // Progreso de subida
            if (progress != null) {
                uploadTask.addOnProgressListener(progress);
            }

            // Al completar la subida
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String downloadUrl = task.getResult().toString();

                    // Actualizar la URL en Firestore
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("fotoVehiculo", downloadUrl);

                    actualizarVehiculo(placa, datos,
                            aVoid -> success.onSuccess(downloadUrl),
                            failure
                    );
                } else {
                    failure.onFailure(task.getException());
                }
            });

        } catch (Exception e) {
            failure.onFailure(e);
        }
    }

    // Verificar si el usuario está autenticado
    public boolean usuarioEstaAutenticado() {
        return mAuth.getCurrentUser() != null;
    }

    // Obtener UID del usuario actual
    public String obtenerUidActual() {
        return obtenerUidUsuario();
    }

    // Clase helper para devolver vehículo con información adicional del taxista
    public static class VehiculoConTaxista {
        private Vehiculo vehiculo;
        private String nombreTaxista;

        public VehiculoConTaxista(Vehiculo vehiculo, String nombreTaxista) {
            this.vehiculo = vehiculo;
            this.nombreTaxista = nombreTaxista;
        }

        public Vehiculo getVehiculo() {
            return vehiculo;
        }

        public String getNombreTaxista() {
            return nombreTaxista;
        }
    }
}