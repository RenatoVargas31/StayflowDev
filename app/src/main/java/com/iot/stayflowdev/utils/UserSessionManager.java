package com.iot.stayflowdev.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria para manejar las sesiones de usuarios en tiempo real
 */
public class UserSessionManager {

    private static final String TAG = "UserSessionManager";
    private static final String COLLECTION_USUARIOS = "usuarios";
    private static UserSessionManager instance;
    private FirebaseFirestore db;

    private UserSessionManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    /**
     * Marca a un usuario como conectado
     * @param userId ID del usuario
     * @param sessionToken Token de sesión
     */
    public void setUserConnected(String userId, String sessionToken) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("conectado", true);
        updates.put("ultimaConexion", System.currentTimeMillis());
        updates.put("tokenSesion", sessionToken);

        db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario " + userId + " marcado como conectado");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al marcar usuario como conectado: " + e.getMessage());
                });
    }

    /**
     * Marca a un usuario como desconectado
     * @param userId ID del usuario
     */
    public void setUserDisconnected(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("conectado", false);
        updates.put("ultimaConexion", System.currentTimeMillis());
        updates.put("tokenSesion", null);

        db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario " + userId + " marcado como desconectado");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al marcar usuario como desconectado: " + e.getMessage());
                });
    }

    /**
     * Limpia sesiones inactivas (usuarios que no han enviado heartbeat en los últimos 5 minutos)
     */
    public void cleanupInactiveSessions() {
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000); // 5 minutos

        db.collection(COLLECTION_USUARIOS)
                .whereEqualTo("conectado", true)
                .whereLessThan("ultimaConexion", fiveMinutesAgo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var document : queryDocumentSnapshots) {
                        setUserDisconnected(document.getId());
                    }
                    Log.d(TAG, "Limpieza de sesiones inactivas completada");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error en limpieza de sesiones: " + e.getMessage());
                });
    }

    /**
     * Envía un heartbeat para mantener la sesión activa
     * @param userId ID del usuario
     */
    public void sendHeartbeat(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("ultimaConexion", System.currentTimeMillis());

        db.collection(COLLECTION_USUARIOS)
                .document(userId)
                .update(updates)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error en heartbeat para usuario " + userId + ": " + e.getMessage());
                });
    }
}
