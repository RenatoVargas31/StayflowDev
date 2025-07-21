package com.iot.stayflowdev.Driver.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iot.stayflowdev.Driver.Dtos.SolicitudReserva;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaxistaReservaRepository {
    private static final String TAG = "TaxistaReservaRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public TaxistaReservaRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Interfaces para callbacks
    public interface OnSolicitudesLoadedListener {
        void onSuccess(List<SolicitudReserva> solicitudes);
        void onError(String error);
    }

    public interface OnSolicitudActionListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnSolicitudDetailListener {
        void onSuccess(SolicitudReserva solicitud);
        void onError(String error);
    }

    // Obtener el UID del usuario autenticado
    private String obtenerUidUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    // Listar SOLO las solicitudes aceptadas (esAceptada = true Y estado = "aceptada")
    public void listarSolicitudesAceptadas(OnSolicitudesLoadedListener listener) {
        String uidTaxista = obtenerUidUsuario();

        db.collection("solicitudesTaxi")
                .whereEqualTo("esAceptada", true)
                .whereEqualTo("estado", "aceptada")
                .whereEqualTo("idTaxista", uidTaxista)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SolicitudReserva> solicitudes = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            SolicitudReserva solicitud = doc.toObject(SolicitudReserva.class);
                            if (solicitud != null) {
                                solicitudes.add(solicitud);
                                android.util.Log.d(TAG, "Solicitud encontrada: " + solicitud.getNombrePasajero() +
                                        " - Estado: " + solicitud.getEstado() +
                                        " - Aceptada: " + solicitud.isEsAceptada());
                            }
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error procesando documento " + doc.getId() + ": " + e.getMessage());
                        }
                    }
                    android.util.Log.d(TAG, "Total solicitudes aceptadas encontradas: " + solicitudes.size());
                    listener.onSuccess(solicitudes);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Error al obtener solicitudes aceptadas: " + e.getMessage());
                    listener.onError("Error al cargar solicitudes: " + e.getMessage());
                });
    }

    // Obtener una solicitud específica por reservaId
    public void obtenerSolicitudPorReservaId(String reservaId, OnSolicitudDetailListener listener) {
        db.collection("solicitudesTaxi")
                .whereEqualTo("reservaId", reservaId)
                .whereEqualTo("esAceptada", true)
                .whereEqualTo("estado", "aceptada")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        try {
                            SolicitudReserva solicitud = doc.toObject(SolicitudReserva.class);
                            if (solicitud != null) {
                                android.util.Log.d(TAG, "Solicitud encontrada: " + solicitud.getReservaId());
                                listener.onSuccess(solicitud);
                            } else {
                                listener.onError("Error al procesar los datos de la solicitud");
                            }
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error al convertir documento: " + e.getMessage());
                            listener.onError("Error al procesar los datos: " + e.getMessage());
                        }
                    } else {
                        android.util.Log.w(TAG, "Solicitud no encontrada con reservaId: " + reservaId);
                        listener.onError("Solicitud no encontrada");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Error al obtener solicitud: " + e.getMessage());
                    listener.onError("Error al cargar la solicitud: " + e.getMessage());
                });
    }

    // Actualizar el estado de una solicitud aceptada
    public void actualizarEstadoSolicitud(String reservaId, String nuevoEstado, OnSolicitudActionListener listener) {
        String uidTaxista = obtenerUidUsuario();

        db.collection("solicitudesTaxi")
                .whereEqualTo("reservaId", reservaId)
                .whereEqualTo("esAceptada", true)
                .whereEqualTo("idTaxista", uidTaxista)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("estado", nuevoEstado);

                        doc.getReference().update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    android.util.Log.d(TAG, "Estado actualizado a: " + nuevoEstado);
                                    listener.onSuccess("Estado actualizado a: " + nuevoEstado);
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e(TAG, "Error al actualizar estado: " + e.getMessage());
                                    listener.onError("Error al actualizar el estado: " + e.getMessage());
                                });
                    } else {
                        listener.onError("Solicitud no encontrada o no tienes permisos para modificarla");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Error al buscar solicitud: " + e.getMessage());
                    listener.onError("Error al buscar la solicitud: " + e.getMessage());
                });
    }

    // Listener en tiempo real para solicitudes aceptadas SOLAMENTE
    public ListenerRegistration escucharSolicitudesAceptadas(OnSolicitudesLoadedListener listener) {
        String uidTaxista = obtenerUidUsuario();

        return db.collection("solicitudesTaxi")
                .whereEqualTo("esAceptada", true)
                .whereEqualTo("estado", "aceptada")
                .whereEqualTo("idTaxista", uidTaxista)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        android.util.Log.e(TAG, "Error en listener: " + error.getMessage());
                        listener.onError("Error al escuchar cambios: " + error.getMessage());
                        return;
                    }

                    if (querySnapshot != null) {
                        List<SolicitudReserva> solicitudes = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            try {
                                SolicitudReserva solicitud = doc.toObject(SolicitudReserva.class);
                                if (solicitud != null) {
                                    solicitudes.add(solicitud);
                                }
                            } catch (Exception e) {
                                android.util.Log.e(TAG, "Error procesando documento en listener: " + e.getMessage());
                            }
                        }
                        android.util.Log.d(TAG, "Listener - Total solicitudes: " + solicitudes.size());
                        listener.onSuccess(solicitudes);
                    }
                });
    }

    // Filtrar solicitudes aceptadas de hoy SOLAMENTE
    public void listarSolicitudesAceptadasDeHoy(OnSolicitudesLoadedListener listener) {
        String uidTaxista = obtenerUidUsuario();

        // Calcular el inicio y fin del día actual
        java.util.Calendar hoy = java.util.Calendar.getInstance();
        hoy.set(java.util.Calendar.HOUR_OF_DAY, 0);
        hoy.set(java.util.Calendar.MINUTE, 0);
        hoy.set(java.util.Calendar.SECOND, 0);
        hoy.set(java.util.Calendar.MILLISECOND, 0);
        Date inicioHoy = hoy.getTime();

        hoy.add(java.util.Calendar.DAY_OF_MONTH, 1);
        Date inicioMañana = hoy.getTime();

        db.collection("solicitudesTaxi")
                .whereEqualTo("esAceptada", true)
                .whereEqualTo("estado", "aceptada")
                .whereEqualTo("idTaxista", uidTaxista)
                .whereGreaterThanOrEqualTo("fechaSalida", inicioHoy)
                .whereLessThan("fechaSalida", inicioMañana)
                .orderBy("fechaSalida", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SolicitudReserva> solicitudes = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            SolicitudReserva solicitud = doc.toObject(SolicitudReserva.class);
                            if (solicitud != null) {
                                solicitudes.add(solicitud);
                            }
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error procesando documento de hoy: " + e.getMessage());
                        }
                    }
                    android.util.Log.d(TAG, "Solicitudes aceptadas de hoy: " + solicitudes.size());
                    listener.onSuccess(solicitudes);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Error al obtener solicitudes de hoy: " + e.getMessage());
                    listener.onError("Error al cargar solicitudes de hoy: " + e.getMessage());
                });
    }

    // Métodos específicos para cambiar estado (solo para solicitudes ya aceptadas)
    public void marcarEnCamino(String reservaId, OnSolicitudActionListener listener) {
        actualizarEstadoSolicitud(reservaId, "en_camino", listener);
    }

    public void marcarEnCurso(String reservaId, OnSolicitudActionListener listener) {
        actualizarEstadoSolicitud(reservaId, "en_curso", listener);
    }

    public void completarSolicitud(String reservaId, OnSolicitudActionListener listener) {
        actualizarEstadoSolicitud(reservaId, "completada", listener);
    }

    public void cancelarSolicitud(String reservaId, OnSolicitudActionListener listener) {
        actualizarEstadoSolicitud(reservaId, "cancelada", listener);
    }
}
