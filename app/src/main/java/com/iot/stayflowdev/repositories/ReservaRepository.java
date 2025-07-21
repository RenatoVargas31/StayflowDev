package com.iot.stayflowdev.repositories;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.iot.stayflowdev.model.Reserva;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio para manejar las operaciones relacionadas con las reservas en Firestore
 */
public class ReservaRepository {
    private static final String TAG = "ReservaRepository";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public ReservaRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Obtiene las reservas del usuario actual filtradas por fecha de creación
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return CompletableFuture con la lista de reservas
     */
    public CompletableFuture<List<Reserva>> getReservasByFechaCreacion(Date fechaInicio, Date fechaFin) {
        CompletableFuture<List<Reserva>> future = new CompletableFuture<>();

        // Verificar que el usuario esté autenticado
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado");
            future.completeExceptionally(new IllegalStateException("Usuario no autenticado"));
            return future;
        }

        // Convertir las fechas a Timestamp para la consulta
        Timestamp timestampInicio = new Timestamp(fechaInicio);
        Timestamp timestampFin = new Timestamp(fechaFin);

        // Consultar primero todas las reservas del usuario y luego filtrar por fecha en la aplicación
        // Esto evita la necesidad de un índice compuesto en Firestore
        db.collection("reservas")
            .whereEqualTo("idUsuario", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> todasLasReservas = new ArrayList<>();

                // Convertir los documentos a objetos Reserva
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Reserva reserva = document.toObject(Reserva.class);
                    if (reserva != null) {
                        // Asegurarse de que el ID esté asignado correctamente
                        reserva.setId(document.getId());

                        // Filtrar por fecha de creación en la aplicación
                        Timestamp fechaCreacion = reserva.getFechaCreacion();
                        if (fechaCreacion != null &&
                            (fechaCreacion.compareTo(timestampInicio) >= 0) &&
                            (fechaCreacion.compareTo(timestampFin) <= 0)) {
                            todasLasReservas.add(reserva);
                        }
                    }
                }

                // Ordenar las reservas por fecha de creación (más recientes primero)
                todasLasReservas.sort((r1, r2) -> {
                    if (r1.getFechaCreacion() == null || r2.getFechaCreacion() == null) {
                        return 0;
                    }
                    return r2.getFechaCreacion().compareTo(r1.getFechaCreacion());
                });

                Log.d(TAG, "Reservas filtradas: " + todasLasReservas.size());
                future.complete(todasLasReservas);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener reservas", e);
                future.completeExceptionally(e);
            });

        return future;
    }

    /**
     * Procesa las reservas obtenidas de Firestore
     */
    private List<Reserva> processReservas(QuerySnapshot queryDocumentSnapshots) {
        List<Reserva> reservas = new ArrayList<>();

        // Convertir los documentos a objetos Reserva
        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
            Reserva reserva = document.toObject(Reserva.class);
            if (reserva != null) {
                // Asegurarse de que el ID esté asignado correctamente
                reserva.setId(document.getId());
                reservas.add(reserva);
            }
        }

        Log.d(TAG, "Reservas obtenidas: " + reservas.size());
        return reservas;
    }

    /**
     * Obtiene todas las reservas del usuario actual
     * @return CompletableFuture con la lista de reservas
     */
    public CompletableFuture<List<Reserva>> getAllReservas() {
        CompletableFuture<List<Reserva>> future = new CompletableFuture<>();

        // Verificar que el usuario esté autenticado
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado");
            future.completeExceptionally(new IllegalStateException("Usuario no autenticado"));
            return future;
        }

        // Consultar todas las reservas del usuario
        db.collection("reservas")
            .whereEqualTo("idUsuario", userId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Reserva> reservas = processReservas(queryDocumentSnapshots);
                future.complete(reservas);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener todas las reservas", e);
                future.completeExceptionally(e);
            });

        return future;
    }
}
