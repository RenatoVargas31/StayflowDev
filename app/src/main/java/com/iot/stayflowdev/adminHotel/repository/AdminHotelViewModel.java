package com.iot.stayflowdev.adminHotel.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.model.Reserva;
import com.iot.stayflowdev.adminHotel.model.NotificacionCheckout;
import com.iot.stayflowdev.model.User;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminHotelViewModel extends ViewModel {

    private final AdminHotelRepository repository;
    private String hotelIdCache = null;
    private final MutableLiveData<String> descripcionHotel = new MutableLiveData<>();

    // Variables para notificaciones
    private MutableLiveData<List<NotificacionCheckout>> notificacionesCheckout = new MutableLiveData<>();
    private MutableLiveData<Integer> contadorNotificaciones = new MutableLiveData<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable actualizadorNotificaciones;

    public AdminHotelViewModel() {
        repository = new AdminHotelRepository();

        // Esperar a que se obtenga el hotelId y luego iniciar todo lo necesario
        repository.getHotelId().observeForever(id -> {
            if (id != null && !id.trim().isEmpty()) {
                hotelIdCache = id;
                Log.d("AdminVM", "Hotel ID cacheado: " + id);
                cargarDescripcionHotel();
                cargarNotificacionesCheckout(); // ✅ Se llama SOLO cuando hotelId está listo
                iniciarActualizacionAutomatica(); // También automático
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
    public void cargarNotificacionesCheckout() {
        String hotelId = obtenerHotelIdActual();
        if (hotelId == null) {
            Log.e("AdminHotelViewModel", "Hotel ID no disponible");
            notificacionesCheckout.setValue(new ArrayList<>());
            contadorNotificaciones.setValue(0);
            return;
        }

        Log.d("NotificationDebug", "Buscando notificaciones para hotel: " + hotelId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .whereEqualTo("estado", "sin checkout") // Cambio: buscar solo reservas "sin checkout"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("NotificationDebug", "Reservas sin checkout encontradas: " + queryDocumentSnapshots.size());

                    if (queryDocumentSnapshots.isEmpty()) {
                        actualizarNotificaciones(new ArrayList<>());
                        return;
                    }

                    List<NotificacionCheckout> notificaciones = new ArrayList<>();
                    AtomicInteger contador = new AtomicInteger(0);
                    int totalDocumentos = queryDocumentSnapshots.size();

                    // Obtener fechas de referencia para comparar solo el día (sin horas)
                    Calendar calendarHoy = Calendar.getInstance();
                    calendarHoy.set(Calendar.HOUR_OF_DAY, 0);
                    calendarHoy.set(Calendar.MINUTE, 0);
                    calendarHoy.set(Calendar.SECOND, 0);
                    calendarHoy.set(Calendar.MILLISECOND, 0);
                    Timestamp inicioHoy = new Timestamp(calendarHoy.getTime());

                    calendarHoy.add(Calendar.DAY_OF_MONTH, 1);
                    Timestamp finHoy = new Timestamp(calendarHoy.getTime());

                    // También obtener ayer para checkouts vencidos
                    calendarHoy.add(Calendar.DAY_OF_MONTH, -2); // Volver a ayer
                    Timestamp inicioAyer = new Timestamp(calendarHoy.getTime());

                    Log.d("NotificationDebug", "Inicio hoy: " + inicioHoy.toDate());
                    Log.d("NotificationDebug", "Fin hoy: " + finHoy.toDate());
                    Log.d("NotificationDebug", "Inicio ayer: " + inicioAyer.toDate());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        reserva.setId(doc.getId());

                        Log.d("NotificationDebug", "Procesando reserva: " + reserva.getId());
                        Log.d("NotificationDebug", "Estado: " + reserva.getEstado());
                        Log.d("NotificationDebug", "Fecha fin: " + (reserva.getFechaFin() != null ? reserva.getFechaFin().toDate() : "null"));
                        Log.d("NotificationDebug", "ID Hotel reserva: " + reserva.getIdHotel());
                        Log.d("NotificationDebug", "ID Usuario: " + reserva.getIdUsuario());

                        // Obtener datos del usuario para el nombre
                        obtenerNombreUsuario(reserva.getIdUsuario(), nombreUsuario -> {
                            Timestamp fechaCheckout = reserva.getFechaFin();
                            String tipoNotificacion = "";

                            if (fechaCheckout != null) {
                                Log.d("NotificationDebug", "Comparando fechas - Checkout: " + fechaCheckout.toDate());

                                // Determinar tipo de notificación comparando solo fechas (sin horas)
                                if (fechaCheckout.compareTo(inicioHoy) < 0) {
                                    // Checkout era ayer o antes = VENCIDO
                                    tipoNotificacion = "CHECKOUT_VENCIDO";
                                    Log.d("NotificationDebug", "Tipo: CHECKOUT_VENCIDO");
                                } else if (fechaCheckout.compareTo(inicioHoy) >= 0 && fechaCheckout.compareTo(finHoy) < 0) {
                                    // Checkout es hoy = HOY
                                    tipoNotificacion = "CHECKOUT_HOY";
                                    Log.d("NotificationDebug", "Tipo: CHECKOUT_HOY");
                                } else {
                                    Log.d("NotificationDebug", "No genera notificación - checkout futuro");
                                }
                            } else {
                                Log.d("NotificationDebug", "fechaFin es null");
                            }

                            if (!tipoNotificacion.isEmpty()) {
                                // Obtener información de habitaciones
                                String tipoHabitacion = "Habitación";
                                int cantidadHabitaciones = 1;

                                if (reserva.getHabitaciones() != null && !reserva.getHabitaciones().isEmpty()) {
                                    Reserva.Habitacion primeraHabitacion = reserva.getHabitaciones().get(0);
                                    tipoHabitacion = primeraHabitacion.getTipo();
                                    cantidadHabitaciones = 0;

                                    // Sumar todas las habitaciones
                                    for (Reserva.Habitacion hab : reserva.getHabitaciones()) {
                                        cantidadHabitaciones += hab.getCantidad();
                                    }
                                }

                                NotificacionCheckout notificacion = new NotificacionCheckout(
                                        reserva.getId(),
                                        reserva.getIdUsuario(),
                                        nombreUsuario,
                                        fechaCheckout,
                                        reserva.getCostoTotal(),
                                        tipoNotificacion,
                                        "sin checkout" // Estado fijo para todas las notificaciones
                                );
                                notificacion.setId("notif_" + reserva.getId());

                                synchronized (notificaciones) {
                                    notificaciones.add(notificacion);
                                    Log.d("NotificationDebug", "Notificación agregada: " + tipoNotificacion);
                                }
                            }

                            // Verificar si hemos procesado todas las reservas
                            if (contador.incrementAndGet() == totalDocumentos) {
                                Log.d("NotificationDebug", "Total notificaciones generadas: " + notificaciones.size());
                                actualizarNotificaciones(notificaciones);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminHotelViewModel", "Error cargando notificaciones", e);
                    notificacionesCheckout.setValue(new ArrayList<>());
                    contadorNotificaciones.setValue(0);
                });
    }

    private void obtenerNombreUsuario(String idUsuario, OnNombreUsuarioListener listener) {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            Log.w("AdminHotelViewModel", "ID usuario vacío o null");
            listener.onNombreObtenido("Usuario");
            return;
        }

        Log.d("AdminHotelViewModel", "Obteniendo nombre para usuario: " + idUsuario);

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

                        // Log de todos los campos para debugging
                        Log.d("AdminHotelViewModel", "Campos del usuario:");
                        Log.d("AdminHotelViewModel", "- nombre: " + documentSnapshot.getString("nombre"));
                        Log.d("AdminHotelViewModel", "- email: " + documentSnapshot.getString("email"));
                        Log.d("AdminHotelViewModel", "- displayName: " + documentSnapshot.getString("displayName"));
                        Log.d("AdminHotelViewModel", "- firstName: " + documentSnapshot.getString("firstName"));
                        Log.d("AdminHotelViewModel", "- apellidos: " + documentSnapshot.getString("apellidos"));
                        Log.d("AdminHotelViewModel", "Nombre final usado: " + nombre);

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
                cargarNotificacionesCheckout();
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

    @Override
    protected void onCleared() {
        super.onCleared();
        detenerActualizacionAutomatica();
    }
}