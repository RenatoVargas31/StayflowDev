package com.iot.stayflowdev.Driver.Repository;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.Driver.Activity.DistanceCalculator;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.firebase.firestore.*;

public class SolicitudesRepository {
    private String TAG = "SolicitudesRepository";
    private String LatitudDestino = "-12.0219";
    private String LongitudDestino = "-77.1143";
    private String DESTINO_NOMBRE = "Aeropuerto Internacional Jorge Chávez";
    private String DESTINO_DIRECCION = "Av. Elmer Faucett s/n, Callao 07031, Lima, Perú";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;
    private final DistanceCalculator distanceCalculator;

    // CONSTRUCTOR CON CONTEXT para cálculo de distancia
    public SolicitudesRepository(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.distanceCalculator = new DistanceCalculator(context);
    }

    // CONSTRUCTOR SIN CONTEXT para compatibilidad
    public SolicitudesRepository() {
        this.context = null;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.distanceCalculator = null;
    }

    // Método para obtener el UID del usuario autenticado
    private String obtenerUidUsuario() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    // Método para verificar si un nombre es inválido
    private boolean esNombreInvalido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return true;
        }

        String nombreLimpio = nombre.trim().toLowerCase();

        // Lista de nombres/valores inválidos
        String[] nombresInvalidos = {
                "sin nombre", "usuario", "user", "admin", "test", "prueba",
                "null", "undefined", "none", "n/a", "na", "no disponible",
                "sin datos", "vacio", "empty", "-", "_", "...", "xxx",
                "anonimo", "anonymous", "guest", "invitado", "temporal",
                "123", "000", "111", "999"
        };

        for (String invalido : nombresInvalidos) {
            if (nombreLimpio.equals(invalido) || nombreLimpio.contains(invalido)) {
                return true;
            }
        }

        // Verificar si es solo números
        if (nombreLimpio.matches("^[0-9]+$")) {
            return true;
        }

        // Verificar si es solo caracteres especiales
        if (nombreLimpio.matches("^[^a-záéíóúüñA-ZÁÉÍÓÚÜÑ]+$")) {
            return true;
        }

        // Muy corto (menos de 2 caracteres)
        if (nombreLimpio.length() < 2) {
            return true;
        }

        return false;
    }

    // Método auxiliar para calcular distancia aproximada entre dos puntos (Fórmula de Haversine)
    private double calcularDistanciaAproximada(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en kilómetros

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }
    public String getDestinoNombre() {
        return DESTINO_NOMBRE;
    }

    public String getDestinoDireccion() {
        return DESTINO_DIRECCION;
    }

    public double getDestinoLatitud() {
        return Double.parseDouble(LatitudDestino);
    }

    public double getDestinoLongitud() {
        return Double.parseDouble(LongitudDestino);
    }

    public GeoPoint getCoordenadasDestino() {
        return new GeoPoint(Double.parseDouble(LatitudDestino), Double.parseDouble(LongitudDestino));
    }
    public void obtenerSolicitudesPendientes(OnSuccessListener<List<SolicitudTaxi>> success, OnFailureListener failure) {
        db.collection("solicitudesTaxi")
                .whereEqualTo("esAceptada", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SolicitudTaxi> solicitudes = new ArrayList<>();

                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "No hay solicitudes pendientes");
                        success.onSuccess(solicitudes);
                        return;
                    }

                    // Crear lista de solicitudes básicas
                    for (DocumentSnapshot doc : snapshot) {
                        try {
                            SolicitudTaxi solicitud = crearSolicitudBasica(doc);
                            solicitudes.add(solicitud);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar solicitud: " + doc.getId(), e);
                        }
                    }

                    Log.d(TAG, "Solicitudes pendientes cargadas: " + solicitudes.size());

                    // CALCULAR DISTANCIAS si tenemos DistanceCalculator
                    if (distanceCalculator != null) {
                        calcularDistanciasYDevolver(solicitudes, success);
                    } else {
                        // Sin cálculo de distancia
                        success.onSuccess(solicitudes);
                    }
                })
                .addOnFailureListener(failure);
    }

    private void calcularDistanciasYDevolver(List<SolicitudTaxi> solicitudes, OnSuccessListener<List<SolicitudTaxi>> success) {
        Log.d(TAG, "🗺️ Iniciando cálculo de distancias para " + solicitudes.size() + " solicitudes");

        AtomicInteger procesadas = new AtomicInteger(0);
        int total = solicitudes.size();

        // Si no hay solicitudes, devolver inmediatamente
        if (total == 0) {
            success.onSuccess(solicitudes);
            return;
        }

        for (SolicitudTaxi solicitud : solicitudes) {
            if (solicitud.tieneCoordenadasValidas()) {
                Log.d(TAG, "📍 Calculando distancia para: " + solicitud.getOrigen());

                distanceCalculator.calcularDistancia(
                        solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud(),
                        solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud(),
                        new DistanceCalculator.DistanceCallback() {
                            @Override
                            public void onSuccess(double distanceKm, int timeMinutes, String distanceText, String timeText) {
                                Log.d(TAG, "✅ Distancia calculada para " + solicitud.getOrigen() + ": " + distanceText + " - " + timeText);

                                // Configurar en el objeto
                                solicitud.setDistanciaKm(distanceKm);
                                solicitud.setTiempoEstimadoMin(timeMinutes);

                                // Guardar en Firebase
                                actualizarDistanciaEnFirestore(solicitud, distanceKm, timeMinutes, distanceText, timeText, true);

                                // Verificar si terminamos
                                verificarCompletado(procesadas, total, solicitudes, success);
                            }

                            @Override
                            public void onError(String error) {
                                Log.w(TAG, "⚠️ Error calculando distancia para " + solicitud.getOrigen() + ": " + error);

                                // Usar Haversine como backup
                                double distanciaAprox = calcularDistanciaHaversine(
                                        solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud(),
                                        solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud()
                                );
                                int tiempoAprox = (int)(distanciaAprox * 1.5); // Estimación básica

                                solicitud.setDistanciaKm(distanciaAprox);
                                solicitud.setTiempoEstimadoMin(tiempoAprox);

                                // Guardar en Firebase (marcado como aproximado)
                                actualizarDistanciaEnFirestore(solicitud, distanciaAprox, tiempoAprox,
                                        String.format("~%.1f km", distanciaAprox), tiempoAprox + " min", false);

                                verificarCompletado(procesadas, total, solicitudes, success);
                            }
                        }
                );
            } else {
                Log.w(TAG, "⚠️ Solicitud sin coordenadas válidas: " + solicitud.getOrigen());

                // Sin coordenadas válidas
                solicitud.setDistanciaKm(0);
                solicitud.setTiempoEstimadoMin(0);

                verificarCompletado(procesadas, total, solicitudes, success);
            }
        }
    }

    private void verificarCompletado(AtomicInteger procesadas, int total, List<SolicitudTaxi> solicitudes, OnSuccessListener<List<SolicitudTaxi>> success) {
        int completadas = procesadas.incrementAndGet();
        Log.d(TAG, "📊 Progreso: " + completadas + "/" + total + " solicitudes procesadas");

        if (completadas == total) {
            Log.d(TAG, "🎉 Todas las distancias calculadas. Devolviendo " + solicitudes.size() + " solicitudes");
            success.onSuccess(solicitudes);
        }
    }

    private void actualizarDistanciaEnFirestore(SolicitudTaxi solicitud, double distanceKm, int timeMinutes,
                                                String distanceText, String timeText, boolean esGoogleMaps) {

        if (solicitud.getSolicitudId() == null || solicitud.getSolicitudId().isEmpty()) {
            Log.e(TAG, "❌ ID de solicitud es null o vacío, no se puede actualizar");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("distanciaKm", distanceKm);
        updates.put("tiempoEstimadoMin", timeMinutes);
        updates.put("distanciaTexto", distanceText);
        updates.put("tiempoTexto", timeText);
        updates.put("fechaCalculoDistancia", FieldValue.serverTimestamp());
        updates.put("calculadoConGoogleMaps", esGoogleMaps);

        Log.d(TAG, "💾 Actualizando Firebase para solicitud: " + solicitud.getSolicitudId());
        Log.d(TAG, "   📏 " + distanceText + " | ⏱️ " + timeText + " | 🗺️ " + (esGoogleMaps ? "Google Maps" : "Haversine"));

        db.collection("solicitudesTaxi")
                .document(solicitud.getSolicitudId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Firebase actualizado exitosamente para: " + solicitud.getOrigen());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error actualizando Firebase para: " + solicitud.getOrigen(), e);
                });
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en kilómetros
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private SolicitudTaxi crearSolicitudBasica(DocumentSnapshot doc) {
        String origen = doc.getString("origen");
        String origenDireccion = doc.getString("origenDireccion");
        String destino = doc.getString("destino");
        String destinoDireccion = doc.getString("destinoDireccion");
        String nombrePasajero = doc.getString("nombrePasajero");
        String telefonoPasajero = doc.getString("telefonoPasajero");
        String tipoVehiculo = doc.getString("tipoVehiculo");
        String notas = doc.getString("notas");
        String estado = doc.getString("estado");

        GeoPoint origenCoordenadas = doc.getGeoPoint("origenCoordenadas");
        GeoPoint destinoCoordenadas = doc.getGeoPoint("destinoCoordenadas");

        Long pasajeros = doc.getLong("numeroPasajeros");
        int numeroPasajeros = pasajeros != null ? pasajeros.intValue() : 0;

        // OBTENER DISTANCIA Y TIEMPO YA CALCULADOS (si existen)
        Double distanciaKm = doc.getDouble("distanciaKm");
        Long tiempoMin = doc.getLong("tiempoEstimadoMin");

        SolicitudTaxi solicitud = new SolicitudTaxi(origen, origenDireccion, destino, destinoDireccion);
        solicitud.setSolicitudId(doc.getId());
        solicitud.setReservaId(doc.getString("reservaId"));
        solicitud.setIdCliente(doc.getString("idCliente"));
        solicitud.setIdHotel(doc.getString("idHotel"));
        solicitud.setIdTaxista(doc.getString("idTaxista"));
        solicitud.setNombrePasajero(nombrePasajero);
        solicitud.setTelefonoPasajero(telefonoPasajero);
        solicitud.setNumeroPasajeros(numeroPasajeros);
        solicitud.setTipoVehiculo(tipoVehiculo);
        solicitud.setNotas(notas);
        solicitud.setEstado(estado);
        solicitud.setEsAceptada(false);

        // Establecer fechas
        solicitud.setFechaCreacion(doc.getTimestamp("fechaCreacion"));
        solicitud.setFechaSalida(doc.getTimestamp("fechaSalida"));
        solicitud.setHoraAceptacion(doc.getTimestamp("horaAceptacion"));

        // Coordenadas
        if (origenCoordenadas != null) {
            solicitud.setOrigenLatitud(origenCoordenadas.getLatitude());
            solicitud.setOrigenLongitud(origenCoordenadas.getLongitude());
        }
        if (destinoCoordenadas != null) {
            solicitud.setDestinoLatitud(destinoCoordenadas.getLatitude());
            solicitud.setDestinoLongitud(destinoCoordenadas.getLongitude());
        }

        // CONFIGURAR DISTANCIA Y TIEMPO YA CALCULADOS (si existen)
        if (distanciaKm != null && distanciaKm > 0) {
            solicitud.setDistanciaKm(distanciaKm);
            Log.d(TAG, "📏 Distancia precalculada encontrada: " + distanciaKm + " km");
        }
        if (tiempoMin != null && tiempoMin > 0) {
            solicitud.setTiempoEstimadoMin(tiempoMin.intValue());
            Log.d(TAG, "⏱️ Tiempo precalculado encontrado: " + tiempoMin + " min");
        }

        return solicitud;
    }

    public void generarSolicitudesDesdeReservas(OnSuccessListener<Integer> success, OnFailureListener failure) {
        Log.d(TAG, "🔄 Iniciando generación de solicitudes desde reservas...");

        db.collection("reservas")
                .whereEqualTo("quieroTaxi", true)
                .get()
                .addOnSuccessListener(reservasSnapshot -> {
                    Log.d(TAG, "📋 Total de reservas con quieroTaxi=true: " + reservasSnapshot.size());

                    if (reservasSnapshot.isEmpty()) {
                        Log.d(TAG, "✅ No hay reservas para procesar");
                        success.onSuccess(0);
                        return;
                    }

                    verificarYProcesarReservas(reservasSnapshot, success, failure);
                })
                .addOnFailureListener(failure);
    }

    private void verificarYProcesarReservas(QuerySnapshot reservasSnapshot, OnSuccessListener<Integer> success, OnFailureListener failure) {
        Log.d(TAG, "🔍 Verificando solicitudes existentes para evitar duplicados...");

        // VERIFICACIÓN MÁS ESPECÍFICA - Solo buscar por reservaId
        db.collection("solicitudesTaxi")
                .whereIn("reservaId", extraerIdsReservas(reservasSnapshot))
                .get()
                .addOnSuccessListener(solicitudesSnapshot -> {
                    Set<String> reservasConSolicitud = new HashSet<>();

                    Log.d(TAG, "🔎 Solicitudes encontradas que coinciden con reservas: " + solicitudesSnapshot.size());

                    for (DocumentSnapshot solicitudDoc : solicitudesSnapshot) {
                        String reservaId = solicitudDoc.getString("reservaId");
                        if (reservaId != null && !reservaId.trim().isEmpty()) {
                            reservasConSolicitud.add(reservaId);
                            Log.d(TAG, "📌 Reserva YA tiene solicitud: " + reservaId +
                                    " (Solicitud: " + solicitudDoc.getId() + ")");
                        }
                    }

                    Log.d(TAG, "📊 Total de reservas con solicitudes existentes: " + reservasConSolicitud.size());

                    // Filtrar reservas que NO tienen solicitud
                    List<DocumentSnapshot> reservasSinSolicitud = new ArrayList<>();
                    for (DocumentSnapshot reservaDoc : reservasSnapshot) {
                        String reservaId = reservaDoc.getId();
                        if (!reservasConSolicitud.contains(reservaId)) {
                            reservasSinSolicitud.add(reservaDoc);
                            Log.d(TAG, "✨ Reserva NUEVA para procesar: " + reservaId);
                        } else {
                            Log.w(TAG, "⚠️ SALTANDO reserva duplicada: " + reservaId);
                        }
                    }

                    Log.d(TAG, "🎯 RESUMEN:");
                    Log.d(TAG, "   📋 Total reservas con quieroTaxi: " + reservasSnapshot.size());
                    Log.d(TAG, "   ✅ Ya tienen solicitud: " + reservasConSolicitud.size());
                    Log.d(TAG, "   🆕 Nuevas a procesar: " + reservasSinSolicitud.size());

                    if (reservasSinSolicitud.isEmpty()) {
                        Log.d(TAG, "✅ Todas las reservas ya tienen solicitudes - No hay nada que hacer");
                        success.onSuccess(0);
                        return;
                    }

                    procesarReservasNuevas(reservasSinSolicitud, success, failure);
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "❌ Error verificando duplicados", error);
                    failure.onFailure(error);
                });
    }
    private void procesarReservasNuevas(List<DocumentSnapshot> reservasSinSolicitud, OnSuccessListener<Integer> success, OnFailureListener failure) {
        AtomicInteger solicitudesCreadas = new AtomicInteger(0);
        int totalReservas = reservasSinSolicitud.size();

        Log.d(TAG, "🚀 Procesando " + totalReservas + " reservas nuevas...");

        for (DocumentSnapshot reservaDoc : reservasSinSolicitud) {
            procesarReserva(reservaDoc,
                    () -> {
                        int completadas = solicitudesCreadas.incrementAndGet();
                        Log.d(TAG, "✅ Solicitud creada. Progreso: " + completadas + "/" + totalReservas);

                        if (completadas == totalReservas) {
                            Log.d(TAG, "🎉 PROCESO COMPLETADO: " + completadas + " solicitudes creadas");
                            success.onSuccess(completadas);
                        }
                    },
                    error -> {
                        int completadas = solicitudesCreadas.incrementAndGet();
                        Log.e(TAG, "❌ Error en reserva: " + reservaDoc.getId(), error);

                        if (completadas == totalReservas) {
                            Log.d(TAG, "⚠️ Proceso terminado con errores. Solicitudes creadas: " + (completadas - 1));
                            success.onSuccess(completadas - 1);
                        }
                    }
            );
        }
    }
    private List<String> extraerIdsReservas(QuerySnapshot reservasSnapshot) {
        List<String> ids = new ArrayList<>();
        for (DocumentSnapshot doc : reservasSnapshot) {
            ids.add(doc.getId());
        }

        // Firestore tiene límite de 10 elementos en whereIn, dividir si es necesario
        if (ids.size() > 10) {
            Log.w(TAG, "⚠️ Más de 10 reservas, usando consulta general por compatibilidad");
            return new ArrayList<>(); // Forzar consulta general
        }

        Log.d(TAG, "🔍 Buscando solicitudes para reservas: " + ids);
        return ids;
    }



    private void procesarReserva(DocumentSnapshot reservaDoc, Runnable onComplete, OnFailureListener onError) {
        String reservaId = reservaDoc.getId();
        String idUsuario = reservaDoc.getString("idUsuario");
        String idHotel = reservaDoc.getString("idHotel");

        Timestamp fechaCreacion = reservaDoc.getTimestamp("fechaCreacion");
        Timestamp fechaFin = reservaDoc.getTimestamp("fechaFin");
        Map<String, Object> cantHuespedes = (Map<String, Object>) reservaDoc.get("cantHuespedes");

        Log.d(TAG, "📋 Procesando reserva: " + reservaId);
        Log.d(TAG, "   👤 Usuario: " + idUsuario + " | 🏨 Hotel: " + idHotel);
        Log.d(TAG, "   📅 Fecha fin: " + fechaFin + " | 👥 Huéspedes: " + cantHuespedes);

        // Validar datos obligatorios
        if (idUsuario == null || idHotel == null || cantHuespedes == null) {
            Log.w(TAG, "❌ Datos incompletos en reserva: " + reservaId);
            onComplete.run();
            return;
        }

        int totalPasajeros = calcularTotalPasajeros(cantHuespedes);

        // Obtener información del usuario y hotel
        obtenerDatosCompletos(idUsuario, idHotel, reservaDoc, totalPasajeros,
                fechaCreacion, fechaFin, onComplete, onError);
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
                    Log.e(TAG, "Error al convertir adultos: " + adultosObj);
                }
            } else if (adultosObj instanceof Number) {
                adultos = ((Number) adultosObj).intValue();
            }

            if (ninosObj instanceof String) {
                try {
                    ninos = Integer.parseInt((String) ninosObj);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al convertir niños: " + ninosObj);
                }
            } else if (ninosObj instanceof Number) {
                ninos = ((Number) ninosObj).intValue();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al calcular pasajeros", e);
        }

        return adultos + ninos;
    }

    private void obtenerDatosCompletos(String idUsuario, String idHotel, DocumentSnapshot reservaDoc,
                                       int totalPasajeros, Timestamp fechaCreacionReserva,
                                       Timestamp fechaFinReserva, Runnable onComplete, OnFailureListener onError) {

        // Obtener datos del usuario
        db.collection("usuarios").document(idUsuario).get()
                .addOnSuccessListener(usuarioDoc -> {
                    if (!usuarioDoc.exists()) {
                        Log.w(TAG, "❌ Usuario no encontrado: " + idUsuario);
                        onComplete.run();
                        return;
                    }

                    String nombres = usuarioDoc.getString("nombres");
                    String apellidos = usuarioDoc.getString("apellidos");
                    String nombreCompleto = construirNombreCompleto(nombres, apellidos);
                    String telefono = usuarioDoc.getString("telefono");
                    String rol = usuarioDoc.getString("rol");

                    Log.d(TAG, "👤 Usuario: " + nombreCompleto + " | Tel: " + telefono + " | Rol: " + rol);

                    if (!"usuario".equalsIgnoreCase(rol)) {
                        Log.w(TAG, "❌ Rol no válido: " + rol);
                        onComplete.run();
                        return;
                    }

                    // Obtener datos del hotel
                    db.collection("hoteles").document(idHotel).get()
                            .addOnSuccessListener(hotelDoc -> {
                                if (!hotelDoc.exists()) {
                                    Log.w(TAG, "❌ Hotel no encontrado: " + idHotel);
                                    onComplete.run();
                                    return;
                                }

                                String nombreHotel = hotelDoc.getString("nombre");
                                String ubicacionHotel = hotelDoc.getString("ubicacion");
                                GeoPoint geoposicion = hotelDoc.getGeoPoint("geoposicion");
                                String calificacion = hotelDoc.getString("calificacion");

                                Log.d(TAG, "🏨 Hotel: " + nombreHotel + " | 📍 " + ubicacionHotel);
                                if (geoposicion != null) {
                                    Log.d(TAG, "🗺️ Coordenadas: " + geoposicion.getLatitude() + ", " + geoposicion.getLongitude());
                                } else {
                                    Log.w(TAG, "⚠️ Hotel sin coordenadas");
                                }

                                if (ubicacionHotel == null) ubicacionHotel = "Dirección no disponible";

                                // Crear solicitud
                                crearSolicitudTaxi(reservaDoc, idUsuario, idHotel, nombreCompleto, telefono,
                                        nombreHotel, ubicacionHotel, geoposicion, calificacion, totalPasajeros,
                                        fechaCreacionReserva, fechaFinReserva, onComplete, onError);
                            })
                            .addOnFailureListener(onError);
                })
                .addOnFailureListener(onError);
    }

    private void crearSolicitudTaxi(DocumentSnapshot reservaDoc, String idUsuario, String idHotel,
                                    String nombreCompleto, String telefono, String nombreHotel,
                                    String ubicacionHotel, GeoPoint geoposicionHotel, String calificacionHotel,
                                    int totalPasajeros, Timestamp fechaCreacionReserva, Timestamp fechaFinReserva,
                                    Runnable onComplete, OnFailureListener onError) {

        String reservaId = reservaDoc.getId();
        GeoPoint coordenadasDestino = new GeoPoint(Double.parseDouble(LatitudDestino), Double.parseDouble(LongitudDestino));

        Map<String, Object> datosSolicitud = new HashMap<>();

        // IDs y referencias
        datosSolicitud.put("reservaId", reservaId);
        datosSolicitud.put("idCliente", "usuarios/" + idUsuario);
        datosSolicitud.put("idHotel", "hoteles/" + idHotel);

        // Datos del viaje
        datosSolicitud.put("origen", nombreHotel);
        datosSolicitud.put("origenDireccion", ubicacionHotel);
        datosSolicitud.put("destino", DESTINO_NOMBRE);
        datosSolicitud.put("destinoDireccion", DESTINO_DIRECCION);

        // Coordenadas (si están disponibles)
        if (geoposicionHotel != null) {
            datosSolicitud.put("origenCoordenadas", geoposicionHotel);
        }
        datosSolicitud.put("destinoCoordenadas", coordenadasDestino);

        // Datos del pasajero
        datosSolicitud.put("nombrePasajero", nombreCompleto);
        datosSolicitud.put("telefonoPasajero", telefono != null ? telefono : "No disponible");
        datosSolicitud.put("numeroPasajeros", totalPasajeros);
        datosSolicitud.put("tipoVehiculo", determinarTipoVehiculo(totalPasajeros));

        // Fechas
        datosSolicitud.put("fechaCreacion", FieldValue.serverTimestamp());
        datosSolicitud.put("fechaSalida", fechaFinReserva);

        // Estado y control
        datosSolicitud.put("estado", "pendiente");
        datosSolicitud.put("esAceptada", false);
        datosSolicitud.put("notas", "Solicitud generada automáticamente");

        // Campos iniciales null
        datosSolicitud.put("idTaxista", null);
        datosSolicitud.put("horaAceptacion", null);

        // Datos adicionales
        datosSolicitud.put("calificacionHotel", calificacionHotel);

        Log.d(TAG, "💾 Creando solicitud:");
        Log.d(TAG, "   🎯 " + nombreCompleto + " | " + nombreHotel + " → " + DESTINO_NOMBRE);
        Log.d(TAG, "   👥 " + totalPasajeros + " pasajeros | 🚗 " + determinarTipoVehiculo(totalPasajeros));
        Log.d(TAG, "   📅 Salida: " + (fechaFinReserva != null ? fechaFinReserva.toDate() : "null"));

        // Guardar en Firestore
        db.collection("solicitudesTaxi")
                .add(datosSolicitud)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "✅ Solicitud creada exitosamente:");
                    Log.d(TAG, "   🆔 ID: " + docRef.getId());
                    Log.d(TAG, "   🎫 Reserva: " + reservaId);
                    Log.d(TAG, "   👤 " + nombreCompleto + " | 🏨 " + nombreHotel);
                    onComplete.run();
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "❌ Error al crear solicitud para reserva: " + reservaId, error);
                    onError.onFailure(error);
                });
    }

    private String construirNombreCompleto(String nombres, String apellidos) {
        if (nombres != null && apellidos != null) {
            return nombres + " " + apellidos;
        } else if (nombres != null) {
            return nombres;
        } else if (apellidos != null) {
            return apellidos;
        } else {
            return "Usuario sin nombre";
        }
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

    // Método para listar todas las reservas (útil para debugging)
    public void listarTodasLasReservas() {
        db.collection("reservas")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG, "=== LISTADO DE TODAS LAS RESERVAS ===");
                    Log.d(TAG, "Total de reservas encontradas: " + querySnapshot.size());

                    if (querySnapshot.isEmpty()) {
                        Log.d(TAG, "No hay reservas en la base de datos");
                        return;
                    }

                    int contador = 0;
                    for (DocumentSnapshot doc : querySnapshot) {
                        contador++;
                        Log.d(TAG, "\n--- RESERVA " + contador + " ---");
                        Log.d(TAG, "ID: " + doc.getId());
                        Log.d(TAG, "idUsuario: " + doc.getString("idUsuario"));
                        Log.d(TAG, "idHotel: " + doc.getString("idHotel"));
                        Log.d(TAG, "quieroTaxi: " + doc.getBoolean("quieroTaxi"));
                        Log.d(TAG, "fechaCreacion: " + doc.getTimestamp("fechaCreacion"));
                        Log.d(TAG, "fechaFin: " + doc.getTimestamp("fechaFin"));
                        Log.d(TAG, "cantHuespedes: " + doc.get("cantHuespedes"));
                    }
                    Log.d(TAG, "=== FIN LISTADO DE RESERVAS ===");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener reservas", e);
                });
    }
}