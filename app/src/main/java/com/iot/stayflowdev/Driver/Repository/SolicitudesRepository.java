package com.iot.stayflowdev.Driver.Repository;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolicitudesRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public SolicitudesRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }
    // Listar todas las solicitudes de servicio existentes en la db
    public void obtenerSolicitudesPendientes(OnSuccessListener<List<SolicitudTaxi>> success, OnFailureListener failure) {
        db.collection("solicitudesTaxi")
                .whereEqualTo("esAceptada", false) // Solo solicitudes no aceptadas
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SolicitudTaxi> solicitudes = new ArrayList<>();

                    if (snapshot.isEmpty()) {
                        Log.d("SolicitudesRepo", "No hay solicitudes pendientes");
                        success.onSuccess(solicitudes);
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot) {
                        try {
                            // Obtener datos del documento
                            String origen = doc.getString("origen");
                            String origenDireccion = doc.getString("origenDireccion");
                            String destino = doc.getString("destino");
                            String destinoDireccion = doc.getString("destinoDireccion");
                            String nombrePasajero = doc.getString("nombrePasajero");
                            String telefonoPasajero = doc.getString("telefonoPasajero");
                            String tipoVehiculo = doc.getString("tipoVehiculo");
                            String notas = doc.getString("notas");
                            String estado = doc.getString("estado");

                            Long pasajeros = doc.getLong("numeroPasajeros");
                            int numeroPasajeros = pasajeros != null ? pasajeros.intValue() : 0;

                            // Crear objeto SolicitudTaxi
                            SolicitudTaxi solicitud = new SolicitudTaxi(origen, origenDireccion, destino, destinoDireccion);
                            solicitud.setSolicitudId(doc.getId());
                            solicitud.setNombrePasajero(nombrePasajero);
                            solicitud.setTelefonoPasajero(telefonoPasajero);
                            solicitud.setNumeroPasajeros(numeroPasajeros);
                            solicitud.setTipoVehiculo(tipoVehiculo);
                            solicitud.setNotas(notas);
                            solicitud.setEstado(estado);
                            solicitud.setEsAceptada(false);

                            solicitudes.add(solicitud);

                        } catch (Exception e) {
                            Log.e("SolicitudesRepo", "Error al procesar solicitud: " + doc.getId(), e);
                        }
                    }

                    Log.d("SolicitudesRepo", "Solicitudes pendientes cargadas: " + solicitudes.size());
                    success.onSuccess(solicitudes);
                })
                .addOnFailureListener(failure);
    }

    // Generar solicitudes desde reservas con quieroTaxi = true
    public void generarSolicitudesDesdeReservas(OnSuccessListener<Integer> success, OnFailureListener failure) {
        db.collection("reservas")
                .whereEqualTo("quieroTaxi", true)
                .get()
                .addOnSuccessListener(reservasSnapshot -> {
                    Log.d("SolicitudesRepo", "Total de reservas con quieroTaxi=true: " + reservasSnapshot.size());

                    if (reservasSnapshot.isEmpty()) {
                        success.onSuccess(0);
                        return;
                    }

                    int[] solicitudesCreadas = {0}; // Array para poder modificar en callbacks
                    int totalReservas = reservasSnapshot.size();

                    for (DocumentSnapshot reservaDoc : reservasSnapshot) {
                        procesarReserva(reservaDoc,
                                () -> {
                                    solicitudesCreadas[0]++;
                                    // Si ya procesamos todas las reservas
                                    if (solicitudesCreadas[0] == totalReservas) {
                                        success.onSuccess(solicitudesCreadas[0]);
                                    }
                                },
                                error -> {
                                    Log.e("SolicitudesRepo", "Error al procesar reserva: " + reservaDoc.getId(), error);
                                    solicitudesCreadas[0]++;
                                    // Continuar aunque falle una reserva
                                    if (solicitudesCreadas[0] == totalReservas) {
                                        success.onSuccess(solicitudesCreadas[0]);
                                    }
                                }
                        );
                    }
                })
                .addOnFailureListener(failure);
    }

    private void procesarReserva(DocumentSnapshot reservaDoc, Runnable onComplete, OnFailureListener onError) {
        String reservaId = reservaDoc.getId();
        String idUsuario = reservaDoc.getString("idUsuario");
        String idHotel = reservaDoc.getString("idHotel");
        Timestamp fechaSalida = reservaDoc.getTimestamp("fechaSalida");
        Map<String, Object> cantHuespedes = (Map<String, Object>) reservaDoc.get("cantHuespedes");

        // Validar datos obligatorios
        if (idUsuario == null || idHotel == null || cantHuespedes == null) {
            Log.w("SolicitudesRepo", "Datos incompletos en reserva: " + reservaId);
            onComplete.run();
            return;
        }

        // Verificar si ya existe una solicitud para esta reserva
        db.collection("solicitudesTaxi")
                .whereEqualTo("reservaId", reservaId)
                .get()
                .addOnSuccessListener(existingSolicitudes -> {
                    if (!existingSolicitudes.isEmpty()) {
                        Log.d("SolicitudesRepo", "Ya existe solicitud para reserva: " + reservaId);
                        onComplete.run();
                        return;
                    }

                    // Calcular número de pasajeros
                    int totalPasajeros = calcularTotalPasajeros(cantHuespedes);

                    // Obtener información del usuario y hotel
                    obtenerDatosCompletos(idUsuario, idHotel, reservaDoc, totalPasajeros, onComplete, onError);
                })
                .addOnFailureListener(onError);
    }

    private int calcularTotalPasajeros(Map<String, Object> cantHuespedes) {
        int adultos = 0;
        int ninos = 0;

        try {
            Object adultosObj = cantHuespedes.get("adultos");
            Object ninosObj = cantHuespedes.get("ninos");

            // Manejar tanto String como Number
            if (adultosObj instanceof String) {
                try {
                    adultos = Integer.parseInt((String) adultosObj);
                } catch (NumberFormatException e) {
                    Log.e("SolicitudesRepo", "Error al convertir adultos: " + adultosObj);
                }
            } else if (adultosObj instanceof Number) {
                adultos = ((Number) adultosObj).intValue();
            }

            if (ninosObj instanceof String) {
                try {
                    ninos = Integer.parseInt((String) ninosObj);
                } catch (NumberFormatException e) {
                    Log.e("SolicitudesRepo", "Error al convertir niños: " + ninosObj);
                }
            } else if (ninosObj instanceof Number) {
                ninos = ((Number) ninosObj).intValue();
            }

        } catch (Exception e) {
            Log.e("SolicitudesRepo", "Error al calcular pasajeros", e);
        }

        return adultos + ninos;
    }

    private void obtenerDatosCompletos(String idUsuario, String idHotel, DocumentSnapshot reservaDoc,
                                       int totalPasajeros, Runnable onComplete, OnFailureListener onError) {

        // Obtener datos del usuario
        db.collection("usuarios").document(idUsuario).get()
                .addOnSuccessListener(usuarioDoc -> {
                    if (!usuarioDoc.exists()) {
                        Log.w("SolicitudesRepo", "Usuario no encontrado: " + idUsuario);
                        onComplete.run();
                        return;
                    }

                    String nombre = usuarioDoc.getString("nombre");
                    String telefono = usuarioDoc.getString("telefono");
                    String rol = usuarioDoc.getString("rol");

                    // Verificar rol de usuario
                    if (!"usuario".equalsIgnoreCase(rol)) {
                        Log.d("SolicitudesRepo", "Rol no válido: " + rol);
                        onComplete.run();
                        return;
                    }

                    // Obtener datos del hotel
                    db.collection("hoteles").document(idHotel).get()
                            .addOnSuccessListener(hotelDoc -> {
                                if (!hotelDoc.exists()) {
                                    Log.w("SolicitudesRepo", "Hotel no encontrado: " + idHotel);
                                    onComplete.run();
                                    return;
                                }

                                String nombreHotel = hotelDoc.getString("nombre");
                                String direccionHotel = hotelDoc.getString("ubicacion");
                                if (direccionHotel == null) direccionHotel = "Dirección no disponible";

                                // Crear y guardar la solicitud
                                crearSolicitudTaxi(reservaDoc, idUsuario, idHotel, nombre, telefono,
                                        nombreHotel, direccionHotel, totalPasajeros, onComplete, onError);
                            })
                            .addOnFailureListener(onError);
                })
                .addOnFailureListener(onError);
    }

    private void crearSolicitudTaxi(DocumentSnapshot reservaDoc, String idUsuario, String idHotel,
                                    String nombre, String telefono, String nombreHotel,
                                    String direccionHotel, int totalPasajeros,
                                    Runnable onComplete, OnFailureListener onError) {

        String reservaId = reservaDoc.getId();
        Timestamp fechaSalida = reservaDoc.getTimestamp("fechaSalida");

        // Crear datos de la solicitud
        Map<String, Object> datosSolicitud = new HashMap<>();
        datosSolicitud.put("reservaId", reservaId);
        datosSolicitud.put("idCliente", "usuarios/" + idUsuario);
        datosSolicitud.put("idHotel", "hoteles/" + idHotel);
        datosSolicitud.put("origen", nombreHotel);
        datosSolicitud.put("origenDireccion", direccionHotel);
        datosSolicitud.put("destino", "Aeropuerto Internacional Jorge Chávez");
        datosSolicitud.put("destinoDireccion", "Av. Elmer Faucett s/n, Callao 07031, Lima, Perú");
        datosSolicitud.put("fechaSalida", fechaSalida);
        datosSolicitud.put("nombrePasajero", nombre);
        datosSolicitud.put("telefonoPasajero", telefono != null ? telefono : "No disponible");
        datosSolicitud.put("numeroPasajeros", totalPasajeros);
        datosSolicitud.put("tipoVehiculo", determinarTipoVehiculo(totalPasajeros));
        datosSolicitud.put("notas", "Solicitud generada automáticamente");
        datosSolicitud.put("estado", "pendiente");
        datosSolicitud.put("esAceptada", false);
        datosSolicitud.put("idTaxista", null);
        datosSolicitud.put("horaAceptacion", null);
        datosSolicitud.put("fechaCreacion", FieldValue.serverTimestamp());

        // Guardar en Firestore
        db.collection("solicitudesTaxi")
                .add(datosSolicitud)
                .addOnSuccessListener(docRef -> {
                    Log.d("SolicitudesRepo", "Solicitud creada con ID: " + docRef.getId());
                    onComplete.run();
                })
                .addOnFailureListener(onError);
    }

    private String determinarTipoVehiculo(int numeroPasajeros) {
        if (numeroPasajeros <= 4) {
            return "Estándar";
        } else if (numeroPasajeros <= 6) {
            return "Van";
        } else {
            return "Minibus";
        }
    }

    // Método para actualizar una solicitud (ejemplo: aceptarla)
    public void aceptarSolicitud(String solicitudId, String taxistaId,
                                 OnSuccessListener<Void> success, OnFailureListener failure) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("esAceptada", true);
        updates.put("idTaxista", taxistaId);
        updates.put("estado", "aceptada");
        updates.put("horaAceptacion", FieldValue.serverTimestamp());

        db.collection("solicitudesTaxi")
                .document(solicitudId)
                .update(updates)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }

    // Método para rechazar una solicitud
    public void rechazarSolicitud(String solicitudId, String motivo,
                                  OnSuccessListener<Void> success, OnFailureListener failure) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "rechazada");
        updates.put("motivoRechazo", motivo);
        updates.put("fechaRechazo", FieldValue.serverTimestamp());

        db.collection("solicitudesTaxi")
                .document(solicitudId)
                .update(updates)
                .addOnSuccessListener(success)
                .addOnFailureListener(failure);
    }


}
