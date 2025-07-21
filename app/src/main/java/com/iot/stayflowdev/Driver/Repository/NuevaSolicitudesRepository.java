package com.iot.stayflowdev.Driver.Repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NuevaSolicitudesRepository {
/*
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public NuevaSolicitudesRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    // Obtener el UID del usuario autenticado
    private String obtenerUidUsuario() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    // Listar las solicitudes de los Clientes que deseen servicio de taxi
    public void listarSolicitudes() {
        String uid = obtenerUidUsuario();
        db.collection("usuarios")
                .document(uid)
                .collection("solicitudes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Manejar los documentos obtenidos
                    queryDocumentSnapshots.forEach(document -> {
                        // Procesar cada solicitud
                        System.out.println(document.getData());
                    });
                })
                .addOnFailureListener(e -> {
                    // Manejar el error
                    System.err.println("Error al obtener solicitudes: " + e.getMessage());
                });
    }

    // Confirmar una solicitud de servicio



    // Rechazar una solicitud de servicio


    // Validar una solicitud de servicio
    public void validarSolicitud(String solicitudId, boolean isValid) {
        String uid = obtenerUidUsuario();
        db.collection("usuarios")
                .document(uid)
                .collection("solicitudes")
                .document(solicitudId)
                .update("validado", isValid)
                .addOnSuccessListener(aVoid -> {
                    // Manejar el éxito de la validación
                    System.out.println("Solicitud validada correctamente");
                })
                .addOnFailureListener(e -> {
                    // Manejar el error de validación
                    System.err.println("Error al validar solicitud: " + e.getMessage());
                });
    }
*/
}
