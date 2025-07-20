package com.iot.stayflowdev.Driver.Repository;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.model.TarjetaCredito;

import java.util.HashMap;
import java.util.Map;

public class TarjetaCreditoRepository {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "TarjetaCreditoRepo";
    private static final String COLLECTION_NAME = "tarjetasCredito";

    public TarjetaCreditoRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    private String obtenerUidUsuario() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    // Obtener tarjeta del usuario autenticado
    public void obtenerTarjetaUsuario(OnSuccessListener<TarjetaCredito> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection(COLLECTION_NAME)
                    .document(uid) // El ID del documento es el UID del usuario
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            TarjetaCredito tarjeta = documentSnapshot.toObject(TarjetaCredito.class);

                            if (tarjeta != null) {
                                tarjeta.setId(documentSnapshot.getId());

                                // Verificar si la tarjeta está activa
                                if (tarjeta.isActiva()) {
                                    success.onSuccess(tarjeta);
                                } else {
                                    failure.onFailure(new Exception("La tarjeta está desactivada"));
                                }
                            } else {
                                failure.onFailure(new Exception("Error al convertir datos de la tarjeta"));
                            }
                        } else {
                            failure.onFailure(new Exception("No hay tarjeta registrada"));
                        }
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Agregar nueva tarjeta
    public void agregarTarjeta(TarjetaCredito tarjeta, OnSuccessListener<String> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            // Validar que la tarjeta sea válida
            if (!tarjeta.esValida()) {
                failure.onFailure(new Exception("Los datos de la tarjeta no son válidos"));
                return;
            }

            // Verificar si ya tiene una tarjeta
            db.collection(COLLECTION_NAME)
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Ya tiene tarjeta, actualizar en lugar de crear nueva
                            actualizarTarjeta(tarjeta, success, failure);
                        } else {
                            // No tiene tarjeta, crear nueva
                            crearNuevaTarjeta(uid, tarjeta, success, failure);
                        }
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    private void crearNuevaTarjeta(String uid, TarjetaCredito tarjeta,
                                   OnSuccessListener<String> success, OnFailureListener failure) {

        tarjeta.setId(uid);
        tarjeta.setActiva(true);
        tarjeta.setFechaCreacion(System.currentTimeMillis());
        tarjeta.setFechaActualizacion(System.currentTimeMillis());

        db.collection(COLLECTION_NAME)
                .document(uid) // Usar UID como ID del documento
                .set(tarjeta)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Tarjeta creada exitosamente para usuario: " + uid);
                    success.onSuccess(uid);
                })
                .addOnFailureListener(failure);
    }

    // Actualizar tarjeta existente
    public void actualizarTarjeta(TarjetaCredito tarjeta, OnSuccessListener<String> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            if (!tarjeta.esValida()) {
                failure.onFailure(new Exception("Los datos de la tarjeta no son válidos"));
                return;
            }

            tarjeta.setFechaActualizacion(System.currentTimeMillis());

            db.collection(COLLECTION_NAME)
                    .document(uid)
                    .set(tarjeta)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tarjeta actualizada exitosamente");
                        success.onSuccess(uid);
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Desactivar tarjeta (no eliminar físicamente)
    public void desactivarTarjeta(OnSuccessListener<Void> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            Map<String, Object> updates = new HashMap<>();
            updates.put("activa", false);
            updates.put("fechaActualizacion", System.currentTimeMillis());

            db.collection(COLLECTION_NAME)
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tarjeta desactivada exitosamente");
                        success.onSuccess(aVoid);
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Reactivar tarjeta
    public void reactivarTarjeta(OnSuccessListener<Void> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            Map<String, Object> updates = new HashMap<>();
            updates.put("activa", true);
            updates.put("fechaActualizacion", System.currentTimeMillis());

            db.collection(COLLECTION_NAME)
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tarjeta reactivada exitosamente");
                        success.onSuccess(aVoid);
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Eliminar tarjeta permanentemente
    public void eliminarTarjeta(OnSuccessListener<Void> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection(COLLECTION_NAME)
                    .document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Tarjeta eliminada permanentemente");
                        success.onSuccess(aVoid);
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
            failure.onFailure(e);
        }
    }

    // Verificar si el usuario tiene tarjeta
    public void tieneTarjeta(OnSuccessListener<Boolean> success, OnFailureListener failure) {
        try {
            String uid = obtenerUidUsuario();

            db.collection(COLLECTION_NAME)
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean tieneTarjeta = documentSnapshot.exists() &&
                                documentSnapshot.getBoolean("activa") == Boolean.TRUE;
                        success.onSuccess(tieneTarjeta);
                    })
                    .addOnFailureListener(failure);
        } catch (IllegalStateException e) {
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
}
