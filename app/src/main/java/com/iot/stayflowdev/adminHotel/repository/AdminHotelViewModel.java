package com.iot.stayflowdev.adminHotel.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.adminHotel.model.UsuarioResumen;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.adminHotel.model.NotificacionCheckout;
import com.iot.stayflowdev.model.User;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AdminHotelViewModel extends ViewModel {

    private final AdminHotelRepository repository;
    private String hotelIdCache = null;
    private final MutableLiveData<String> descripcionHotel = new MutableLiveData<>();

    // Variables para notificaciones
    private MutableLiveData<List<NotificacionCheckout>> notificacionesCheckout = new MutableLiveData<>();
    private MutableLiveData<Integer> contadorNotificaciones = new MutableLiveData<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable actualizadorNotificaciones;
    private ListenerRegistration listenerNotificaciones;


    public AdminHotelViewModel() {
        repository = new AdminHotelRepository();

        // Esperar a que se obtenga el hotelId y luego iniciar todo lo necesario
        repository.getHotelId().observeForever(id -> {
            if (id != null && !id.trim().isEmpty()) {
                hotelIdCache = id;
                Log.d("AdminVM", "Hotel ID cacheado: " + id);
                cargarDescripcionHotel();
                cargarNotificacionesCheckoutTiempoReal();
                iniciarActualizacionAutomatica();
            } else {
                Log.e("AdminVM", "Hotel ID no obtenido aún.");
            }
        });
    }

    public LiveData<String> getNombreAdmin() {
        return repository.getNombreAdmin();
    }

    public LiveData<String> getHotelId() {
        return repository.getHotelId();
    }

    public LiveData<String> getNombreHotel() {
        return repository.getNombreHotel();
    }

    public LiveData<String> getUbicacionHotel() {
        return repository.getUbicacionHotel();
    }

    public void actualizarUbicacion(String hotelId, String nuevaUbicacion, Runnable onSuccess, Runnable onError) {
        repository.actualizarUbicacionHotel(hotelId, nuevaUbicacion, onSuccess, onError);
    }

    public LiveData<String> getDescripcionHotel() {
        return descripcionHotel;
    }

    public void actualizarDescripcionHotel(String descripcion) {
        if (hotelIdCache == null) {
            Log.e("AdminVM", "Hotel ID aún no está disponible");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelIdCache)
                .update("descripcion", descripcion)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminVM", "Descripción actualizada");
                    descripcionHotel.setValue(descripcion);
                })
                .addOnFailureListener(e -> Log.e("AdminVM", "Error al actualizar descripción", e));
    }

    private void cargarDescripcionHotel() {
        if (hotelIdCache == null) return;

        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelIdCache)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String descripcion = document.getString("descripcion");
                        descripcionHotel.setValue(descripcion);
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminVM", "Error al cargar descripción", e));
    }

    // Getters para LiveData de notificaciones
    public LiveData<List<NotificacionCheckout>> getNotificacionesCheckout() {
        return notificacionesCheckout;
    }

    public LiveData<Integer> getContadorNotificaciones() {
        return contadorNotificaciones;
    }

    // Método para cargar notificaciones de checkout
    public void cargarNotificacionesCheckoutTiempoReal() {
        String hotelId = obtenerHotelIdActual();
        if (hotelId == null) {
            Log.e("AdminHotelViewModel", "Hotel ID no disponible");
            notificacionesCheckout.setValue(new ArrayList<>());
            contadorNotificaciones.setValue(0);
            return;
        }

        if (listenerNotificaciones != null) {
            listenerNotificaciones.remove(); // Detener listener anterior
        }

        listenerNotificaciones = FirebaseFirestore.getInstance()
                .collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .whereEqualTo("estado", "sin checkout")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("AdminHotelViewModel", "Error en snapshot de notificaciones", error);
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        actualizarNotificaciones(new ArrayList<>());
                        return;
                    }

                    List<NotificacionCheckout> notificaciones = new ArrayList<>();
                    AtomicInteger contador = new AtomicInteger(0);
                    int totalDocumentos = queryDocumentSnapshots.size();

                    Calendar calendarHoy = Calendar.getInstance();
                    calendarHoy.set(Calendar.HOUR_OF_DAY, 0);
                    calendarHoy.set(Calendar.MINUTE, 0);
                    calendarHoy.set(Calendar.SECOND, 0);
                    calendarHoy.set(Calendar.MILLISECOND, 0);
                    Timestamp inicioHoy = new Timestamp(calendarHoy.getTime());

                    calendarHoy.add(Calendar.DAY_OF_MONTH, 1);
                    Timestamp finHoy = new Timestamp(calendarHoy.getTime());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        reserva.setId(doc.getId());

                        obtenerNombreUsuario(reserva.getIdUsuario(), nombreUsuario -> {
                            Timestamp fechaCheckout = reserva.getFechaFin();
                            String tipoNotificacion = "";

                            if (fechaCheckout != null) {
                                if (fechaCheckout.compareTo(inicioHoy) < 0) {
                                    tipoNotificacion = "CHECKOUT_VENCIDO";
                                } else if (fechaCheckout.compareTo(inicioHoy) >= 0 && fechaCheckout.compareTo(finHoy) < 0) {
                                    tipoNotificacion = "CHECKOUT_HOY";
                                }
                            }

                            if (!tipoNotificacion.isEmpty()) {
                                NotificacionCheckout notificacion = new NotificacionCheckout(
                                        reserva.getId(),
                                        reserva.getIdUsuario(),
                                        nombreUsuario,
                                        fechaCheckout,
                                        reserva.getCostoTotal(),
                                        tipoNotificacion,
                                        "sin checkout"
                                );
                                notificacion.setId("notif_" + reserva.getId());
                                synchronized (notificaciones) {
                                    notificaciones.add(notificacion);
                                }
                            }

                            if (contador.incrementAndGet() == totalDocumentos) {
                                actualizarNotificaciones(notificaciones);
                            }
                        });
                    }
                });
    }

    private void obtenerNombreUsuario(String idUsuario, OnNombreUsuarioListener listener) {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            Log.w("AdminHotelViewModel", "ID usuario vacío o null");
            listener.onNombreObtenido("Usuario");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(idUsuario)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User usuario = documentSnapshot.toObject(User.class);
                        String nombre = "Usuario";

                        if (usuario != null) {
                            if (usuario.getNombres() != null && !usuario.getNombres().trim().isEmpty()) {
                                nombre = usuario.getNombres();
                                if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
                                    nombre += " " + usuario.getApellidos();
                                }
                            } else if (usuario.getApellidos() != null && !usuario.getApellidos().trim().isEmpty()) {
                                nombre = usuario.getApellidos();
                            } else if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                                nombre = usuario.getEmail().split("@")[0];
                            }
                        }

                        listener.onNombreObtenido(nombre);
                    } else {
                        Log.w("AdminHotelViewModel", "Usuario no existe en Firestore: " + idUsuario);
                        listener.onNombreObtenido("Usuario");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminHotelViewModel", "Error obteniendo nombre usuario: " + idUsuario, e);
                    listener.onNombreObtenido("Usuario");
                });
    }

    private interface OnNombreUsuarioListener {
        void onNombreObtenido(String nombre);
    }

    private void actualizarNotificaciones(List<NotificacionCheckout> notificaciones) {
        // Ordenar por prioridad y fecha
        notificaciones.sort((n1, n2) -> {
            // Primero por prioridad
            int prioridad1 = getPrioridadNotificacion(n1.getTipoNotificacion());
            int prioridad2 = getPrioridadNotificacion(n2.getTipoNotificacion());

            if (prioridad1 != prioridad2) {
                return Integer.compare(prioridad2, prioridad1); // Descendente
            }

            // Luego por fecha
            return n1.getFechaCheckout().compareTo(n2.getFechaCheckout());
        });

        notificacionesCheckout.setValue(notificaciones);
        contadorNotificaciones.setValue(notificaciones.size());
    }

    private int getPrioridadNotificacion(String tipo) {
        switch (tipo) {
            case "CHECKOUT_VENCIDO": return 3;
            case "CHECKOUT_HOY": return 2;
            default: return 0;
        }
    }

    public void marcarNotificacionComoLeida(String notificacionId) {
        List<NotificacionCheckout> notificacionesActuales = notificacionesCheckout.getValue();
        if (notificacionesActuales != null) {
            for (NotificacionCheckout notif : notificacionesActuales) {
                if (notif.getId().equals(notificacionId)) {
                    notif.setLeida(true);
                    break;
                }
            }
            notificacionesCheckout.setValue(notificacionesActuales);
        }
    }

    // Método para actualizar automáticamente cada 5 minutos
    public void iniciarActualizacionAutomatica() {
        detenerActualizacionAutomatica(); // Detener si ya existe

        actualizadorNotificaciones = new Runnable() {
            @Override
            public void run() {
                cargarNotificacionesCheckoutTiempoReal();
                handler.postDelayed(this, 5 * 60 * 1000); // Cada 5 minutos
            }
        };
        handler.post(actualizadorNotificaciones);
    }

    public void detenerActualizacionAutomatica() {
        if (actualizadorNotificaciones != null) {
            handler.removeCallbacks(actualizadorNotificaciones);
        }
    }

    public void setHotelIdManual(String hotelId) {
        if (hotelId != null && !hotelId.trim().isEmpty()) {
            Log.d("AdminVM", "Hotel ID establecido manualmente: " + hotelId);
            hotelIdCache = hotelId;
        }
    }

    // Método helper para obtener el ID del hotel actual
    private String obtenerHotelIdActual() {
        // Usar el cache si está disponible
        if (hotelIdCache != null && !hotelIdCache.trim().isEmpty()) {
            Log.d("AdminHotelViewModel", "Usando hotel ID desde cache: " + hotelIdCache);
            return hotelIdCache;
        }

        // Como fallback, usar el UID del usuario actual
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            Log.w("AdminHotelViewModel", "Cache vacío, usando UID como fallback: " + uid);
            return uid;
        }

        Log.e("AdminHotelViewModel", "No se pudo obtener hotel ID - cache vacío y sin usuario autenticado");
        return null;
    }

    public void actualizarUbicacionConCoordenadas(String hotelId, String direccion, LatLng coordenadas,
                                                  Runnable onSuccess, Runnable onFailure) {
        FirebaseFirestore.getInstance().collection("hoteles")
                .document(hotelId)
                .update(
                        "ubicacion", direccion,
                        "geoposicion", new GeoPoint(coordenadas.latitude, coordenadas.longitude)
                )
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.run());
    }

    public LiveData<String> obtenerHotelIdAdministrador() {
        MutableLiveData<String> hotelIdLiveData = new MutableLiveData<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            hotelIdLiveData.setValue(null);
            return hotelIdLiveData;
        }

        db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && "adminhotel".equalsIgnoreCase(snapshot.getString("rol"))) {
                        String hotelId = snapshot.getString("hotelAsignado");
                        hotelIdLiveData.setValue(hotelId);
                    } else {
                        hotelIdLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> hotelIdLiveData.setValue(null));

        return hotelIdLiveData;
    }

    public void obtenerNombresUsuarios(List<UsuarioResumen> listaResumen, Consumer<List<UsuarioResumen>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<UsuarioResumen> listaConNombres = new ArrayList<>();
        AtomicInteger pendientes = new AtomicInteger(listaResumen.size());

        for (UsuarioResumen resumen : listaResumen) {
            db.collection("usuarios")
                    .document(resumen.getIdUsuario())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String nombres = snapshot.getString("nombres");
                        String apellidos = snapshot.getString("apellidos");
                        String nombreCompleto = (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
                        resumen.setNombre(nombreCompleto.trim().isEmpty() ? "(sin nombre)" : nombreCompleto.trim());

                        listaConNombres.add(resumen);

                        if (pendientes.decrementAndGet() == 0) {
                            callback.accept(listaConNombres);
                        }
                    })
                    .addOnFailureListener(e -> {
                        resumen.setNombre("(error)");
                        listaConNombres.add(resumen);

                        if (pendientes.decrementAndGet() == 0) {
                            callback.accept(listaConNombres);
                        }
                    });
        }

        // En caso de lista vacía
        if (listaResumen.isEmpty()) {
            callback.accept(listaConNombres);
        }
    }

    // ===== MÉTODOS DE REPORTES SIN FILTROS =====

    // MÉTODO PARA VENTAS SIN FILTROS
    public LiveData<Map<String, Double>> obtenerVentasTotalesPorUsuario(String hotelId) {
        MutableLiveData<Map<String, Double>> liveData = new MutableLiveData<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("VENTAS_DEBUG", "Obteniendo ventas para hotel: " + hotelId);

        db.collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("VENTAS_DEBUG", "Documentos encontrados: " + querySnapshot.size());

                    Map<String, Double> ventasPorUsuario = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            Reserva reserva = doc.toObject(Reserva.class);
                            if (reserva != null) {
                                Log.d("VENTAS_DEBUG", "Procesando reserva: " + doc.getId() +
                                        " | Usuario: " + reserva.getIdUsuario() +
                                        " | Estado: " + reserva.getEstado() +
                                        " | Costo: " + reserva.getCostoTotal());

                                // Verificar estado válido
                                if (reserva.getIdUsuario() != null &&
                                        reserva.getEstado() != null &&
                                        (reserva.getEstado().equalsIgnoreCase("completada") ||
                                                reserva.getEstado().equalsIgnoreCase("confirmada"))) {

                                    String idUsuario = reserva.getIdUsuario();
                                    double monto = 0;

                                    try {
                                        // Manejar costoTotal como String o Double
                                        if (reserva.getCostoTotal() != null) {
                                            String costoStr = reserva.getCostoTotal().toString().replaceAll("[^0-9.]", "");
                                            if (!costoStr.isEmpty()) {
                                                monto = Double.parseDouble(costoStr);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.w("VENTAS_DEBUG", "Error parseando costo: " + reserva.getCostoTotal());
                                        monto = 0;
                                    }

                                    // Acumular ventas por usuario
                                    ventasPorUsuario.put(idUsuario,
                                            ventasPorUsuario.getOrDefault(idUsuario, 0.0) + monto);

                                    Log.d("VENTAS_DEBUG", "Agregado - Usuario: " + idUsuario +
                                            " | Monto: " + monto +
                                            " | Total acumulado: " + ventasPorUsuario.get(idUsuario));
                                } else {
                                    Log.d("VENTAS_DEBUG", "Reserva ignorada - Estado inválido o datos faltantes");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("VENTAS_DEBUG", "Error procesando documento: " + e.getMessage());
                        }
                    }

                    Log.d("VENTAS_DEBUG", "Resultado final - Usuarios con ventas: " + ventasPorUsuario.size());
                    for (Map.Entry<String, Double> entry : ventasPorUsuario.entrySet()) {
                        Log.d("VENTAS_DEBUG", "Usuario: " + entry.getKey() + " = S/. " + entry.getValue());
                    }

                    liveData.setValue(ventasPorUsuario);
                })
                .addOnFailureListener(e -> {
                    Log.e("VENTAS_DEBUG", "Error obteniendo ventas: " + e.getMessage());
                    liveData.setValue(new HashMap<>());
                });

        return liveData;
    }

    // MÉTODO PARA RESERVAS SIN FILTROS
    public LiveData<Map<String, Integer>> obtenerReservasPorUsuario(String hotelId) {
        MutableLiveData<Map<String, Integer>> liveData = new MutableLiveData<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("RESERVAS_DEBUG", "Obteniendo reservas para hotel: " + hotelId);

        db.collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("RESERVAS_DEBUG", "Documentos encontrados: " + querySnapshot.size());

                    Map<String, Integer> conteoPorUsuario = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            Reserva reserva = doc.toObject(Reserva.class);
                            if (reserva != null) {
                                Log.d("RESERVAS_DEBUG", "Procesando reserva: " + doc.getId() +
                                        " | Usuario: " + reserva.getIdUsuario() +
                                        " | Estado: " + reserva.getEstado());

                                // Verificar estado válido
                                if (reserva.getIdUsuario() != null &&
                                        reserva.getEstado() != null &&
                                        (reserva.getEstado().equalsIgnoreCase("completada") ||
                                                reserva.getEstado().equalsIgnoreCase("confirmada"))) {

                                    String idUsuario = reserva.getIdUsuario();
                                    conteoPorUsuario.put(idUsuario,
                                            conteoPorUsuario.getOrDefault(idUsuario, 0) + 1);

                                    Log.d("RESERVAS_DEBUG", "Agregado - Usuario: " + idUsuario +
                                            " | Total reservas: " + conteoPorUsuario.get(idUsuario));
                                } else {
                                    Log.d("RESERVAS_DEBUG", "Reserva ignorada - Estado inválido o datos faltantes");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("RESERVAS_DEBUG", "Error procesando documento: " + e.getMessage());
                        }
                    }

                    Log.d("RESERVAS_DEBUG", "Resultado final - Usuarios con reservas: " + conteoPorUsuario.size());
                    for (Map.Entry<String, Integer> entry : conteoPorUsuario.entrySet()) {
                        Log.d("RESERVAS_DEBUG", "Usuario: " + entry.getKey() + " = " + entry.getValue() + " reservas");
                    }

                    liveData.setValue(conteoPorUsuario);
                })
                .addOnFailureListener(e -> {
                    Log.e("RESERVAS_DEBUG", "Error obteniendo reservas: " + e.getMessage());
                    liveData.setValue(new HashMap<>());
                });

        return liveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerNotificaciones != null) {
            listenerNotificaciones.remove();
        }
        detenerActualizacionAutomatica();
    }
}