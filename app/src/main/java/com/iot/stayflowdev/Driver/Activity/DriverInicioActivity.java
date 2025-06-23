package com.iot.stayflowdev.Driver.Activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.Driver.Adapter.SolicitudesAdapter;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;
import com.iot.stayflowdev.Driver.Model.SolicitudModel;
import com.iot.stayflowdev.LoginActivity;
import com.iot.stayflowdev.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;

public class DriverInicioActivity extends AppCompatActivity {

    private RecyclerView rvSolicitudesCercanas;
    private TextView tvNoSolicitudes;
    private MaterialSwitch statusSwitch;
    private TextView statusText;
    private SolicitudesAdapter adapter;
    private List<SolicitudTaxi> solicitudes;
    private BottomNavigationView bottomNavigation;
    private ImageView notificationIcon;

    // Variables para notificaciones
    private String channelId = "channelDefaultPri";
    private String gpsChannelId = "channelGPSAlert";
    private TextView tvSinSolicitudes;

    // Firebase
    private FirebaseFirestore db; // Si necesitas Firestore, descomentar esta línea
    private FirebaseAuth mAuth; // Si necesitas autenticación, descomentar esta línea

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_inicio);

        // Configurar márgenes para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView titleTextView = findViewById(R.id.title);

        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // ← ESTE ES EL ID ÚNICO DEL USUARIO
            Log.d("DriverInicio", "UID del usuario autenticado: " + uid);

            // Obtener información del usuario desde Firestore
            db.collection("usuarios")
                    .document(uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            DocumentSnapshot document = task.getResult();

                            String nombre = document.getString("nombre");
                            String correo = document.getString("correo");
                            String rol = document.getString("rol");

                            Log.d("DriverInicio", "Nombre: " + nombre);
                            Log.d("DriverInicio", "Correo: " + correo);
                            Log.d("DriverInicio", "Rol: " + rol);

                            // Mostrar el nombre en el título de la actividad
                            titleTextView.setText(nombre);
                        } else {
                            Log.e("DriverInicio", "Error al obtener datos del usuario o documento no existe", task.getException());
                        }
                    });
        } else {
            Log.d("DriverInicio", "No hay usuario autenticado.");
            // Puedes redirigir al login si lo deseas:
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        // Crear colección de solicitudes de taxi
        coleccionSolicitudesTaxi();

        crearDatosDeSolicitudes();

        // Crear canales de notificación primero
        crearCanalesNotificacion();

        //   INICIALIZAR TODAS LAS VISTAS PRIMERO
        inicializarVistas();

        configurarRecyclerView();
        configurarSwitch();
        configurarBottomNavigation();
        configurarNotificaciones();

        // Establecer estado inicial
        actualizarEstadoUI(statusSwitch.isChecked());

        // Verificar GPS al iniciar la app
        verificarEstadoGPS();
    }
    //   METODO SEPARADO PARA INICIALIZAR VISTAS
    private void inicializarVistas() {
        rvSolicitudesCercanas = findViewById(R.id.rvSolicitudesCercanas);
        tvNoSolicitudes = findViewById(R.id.tvNoSolicitudes);
        tvSinSolicitudes = findViewById(R.id.tvSinSolicitudes);
        statusSwitch = findViewById(R.id.statusSwitch);
        statusText = findViewById(R.id.statusText);
        notificationIcon = findViewById(R.id.notification_icon);
        bottomNavigation = findViewById(R.id.bottomNavigation); //   ESTO FALTABA!

        //   VERIFICAR QUE TODAS LAS VISTAS SE ENCONTRARON
        if (bottomNavigation == null) {
            Log.e("MainActivity", "Error: bottomNavigation es null");
        }
        if (rvSolicitudesCercanas == null) {
            Log.e("MainActivity", "Error: rvSolicitudesCercanas es null");
        }
    }
    private void crearCanalesNotificacion() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // Canal para notificaciones generales
            NotificationChannel channelDefault = new NotificationChannel(
                    channelId,
                    "Canal notificaciones default",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channelDefault.setDescription("Canal para notificaciones con prioridad default");

            // Canal para alertas de GPS (alta prioridad)
            NotificationChannel channelGPS = new NotificationChannel(
                    gpsChannelId,
                    "Alertas de GPS",
                    NotificationManager.IMPORTANCE_HIGH);
            channelGPS.setDescription("Notificaciones importantes sobre el estado del GPS");
            channelGPS.enableVibration(true);
            channelGPS.setVibrationPattern(new long[]{0, 500, 1000});

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelDefault);
            notificationManager.createNotificationChannel(channelGPS);

            askPermission();
        }
    }
    private void askPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(DriverInicioActivity.this,
                    new String[]{POST_NOTIFICATIONS},
                    101);
        }
    }
    // Metodo para verificar el estado del GPS
    private void verificarEstadoGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGpsEnabled) {
            mostrarNotificacionGPS();
        }
    }
    // Metodo para mostrar notificación de GPS
    private void mostrarNotificacionGPS() {
        // Intent para abrir configuración de ubicación
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent gpsPendingIntent = PendingIntent.getActivity(
                this,
                100,
                gpsIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Intent para cuando se toque la notificación (volver a MainActivity)
        Intent mainIntent = new Intent(this, DriverInicioActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                this,
                101,
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, gpsChannelId)
                .setSmallIcon(R.drawable.ic_location) // Asegúrate de tener este ícono
                .setContentTitle("GPS Desactivado")
                .setContentText("Activa la ubicación para recibir solicitudes de viaje cercanas")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Para poder recibir solicitudes de viaje y mostrar tu ubicación a los pasajeros, necesitas activar el GPS en la configuración de tu dispositivo."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 1000})
                .setLights(0xFFFF6B00, 300, 1000) // Luz naranja
                // Agregar botón de acción para ir directo a configuración
                .addAction(R.drawable.ic_exclamation, "Activar GPS", gpsPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(999, builder.build()); // ID único para notificación GPS
        }
    }
    private boolean verificarGPSParaServicio() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGpsEnabled) {
            mostrarNotificacionGPS();
            Toast.makeText(this, "Activa el GPS para ponerte disponible", Toast.LENGTH_SHORT).show();
            return false; // GPS no disponible
        }

        return true; // GPS disponible
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Si el switch está activado pero el GPS se desactivó
        if (statusSwitch != null && statusSwitch.isChecked()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGpsEnabled) {
                statusSwitch.setOnCheckedChangeListener(null);
                statusSwitch.setChecked(false);
                configurarSwitch();
                actualizarEstadoUI(false);
                mostrarNotificacionGPS();
            }
        }
    }
    private void coleccionSolicitudesTaxi() {
        db.collection("reservas")
                .whereEqualTo("quieroTaxi", true)
                .get()
                .addOnSuccessListener(reservasSnapshot -> {
                    Log.d("SolicitudTaxi", "Total de reservas con quieroTaxi=true: " + reservasSnapshot.size());

                    for (DocumentSnapshot reservaDoc : reservasSnapshot) {
                        String reservaId = reservaDoc.getId();
                        String idUsuario = reservaDoc.getString("idUsuario");
                        String idHotel = reservaDoc.getString("idHotel");
                        Timestamp fechaSalida = reservaDoc.getTimestamp("fechaSalida");
                        Map<String, Object> cantHuespedes = (Map<String, Object>) reservaDoc.get("cantHuespedes");

                        Log.d("SolicitudTaxi", "Reserva ID: " + reservaId);
                        Log.d("SolicitudTaxi", "idUsuario: " + idUsuario);
                        Log.d("SolicitudTaxi", "idHotel: " + idHotel);
                        Log.d("SolicitudTaxi", "cantHuespedes: " + cantHuespedes);

                        boolean datosIncompletos = false;

                        if (idUsuario == null) {
                            Log.w("SolicitudTaxi", "Falta idUsuario en reserva: " + reservaId);
                            datosIncompletos = true;
                        }
                        if (idHotel == null) {
                            Log.w("SolicitudTaxi", "Falta idHotel en reserva: " + reservaId);
                            datosIncompletos = true;
                        }
                        if (cantHuespedes == null) {
                            Log.w("SolicitudTaxi", "Falta cantHuespedes en reserva: " + reservaId);
                            datosIncompletos = true;
                        }

                        if (datosIncompletos) {
                            Log.w("SolicitudTaxi", "Omisión de reserva por datos incompletos: " + reservaId);
                            continue;
                        }

                        db.collection("solicitudesTaxi")
                                .whereEqualTo("reservaId", reservaId)
                                .get()
                                .addOnSuccessListener(existingSolicitudes -> {
                                    if (!existingSolicitudes.isEmpty()) {
                                        Log.d("SolicitudTaxi", "Ya existe solicitud para reserva: " + reservaId);
                                        return;
                                    }

                                    // Obtener número de pasajeros - CORREGIDO para manejar Strings
                                    int adultos = 0;
                                    int ninos = 0;
                                    try {
                                        Object adultosObj = cantHuespedes.get("adultos");
                                        Object ninosObj = cantHuespedes.get("ninos");

                                        Log.d("SolicitudTaxi", "Valor adultos: " + adultosObj + " (tipo: " +
                                                (adultosObj != null ? adultosObj.getClass().getSimpleName() : "null") + ")");
                                        Log.d("SolicitudTaxi", "Valor ninos: " + ninosObj + " (tipo: " +
                                                (ninosObj != null ? ninosObj.getClass().getSimpleName() : "null") + ")");

                                        // Manejar tanto String como Number
                                        if (adultosObj instanceof String) {
                                            try {
                                                adultos = Integer.parseInt((String) adultosObj);
                                            } catch (NumberFormatException e) {
                                                Log.e("SolicitudTaxi", "Error al convertir adultos String a int: " + adultosObj);
                                                adultos = 0;
                                            }
                                        } else if (adultosObj instanceof Number) {
                                            adultos = ((Number) adultosObj).intValue();
                                        }

                                        if (ninosObj instanceof String) {
                                            try {
                                                ninos = Integer.parseInt((String) ninosObj);
                                            } catch (NumberFormatException e) {
                                                Log.e("Ayudddaaaaa", "Error al convertir ninos String a int: " + ninosObj);
                                                ninos = 0;
                                            }
                                        } else if (ninosObj instanceof Number) {
                                            ninos = ((Number) ninosObj).intValue();

                                        }

                                        Log.d("Ayudddaaaaa", "Adultos parseados: " + adultos + ", Niños parseados: " + ninos);

                                    } catch (Exception e) {
                                        Log.e("Ayudddaaaaa", "Error al leer cantidad de huéspedes", e);
                                    }

                                    int totalPasajeros = adultos + ninos;
                                    Log.d("Ayudddaaaaa", "Total de pasajeros calculado: " + totalPasajeros);

                                    // Obtener datos del usuario
                                    db.collection("usuarios").document(idUsuario).get()
                                            .addOnSuccessListener(usuarioDoc -> {
                                                if (!usuarioDoc.exists()) {
                                                    Log.w("SolicitudTaxi", "Usuario no encontrado: " + idUsuario);
                                                    return;
                                                }

                                                String nombre = usuarioDoc.getString("nombre");
                                                String telefono = usuarioDoc.getString("telefono");
                                                String rol = usuarioDoc.getString("rol");

                                                if (!"usuario".equalsIgnoreCase(rol)) {
                                                    Log.d("SolicitudTaxi", "Rol no válido: " + rol);
                                                    return;
                                                }

                                                // Obtener datos del hotel
                                                db.collection("hoteles").document(idHotel).get()
                                                        .addOnSuccessListener(hotelDoc -> {
                                                            if (!hotelDoc.exists()) {
                                                                Log.w("SolicitudTaxi", "Hotel no encontrado: " + idHotel);
                                                                return;
                                                            }

                                                            String nombreHotel = hotelDoc.getString("nombre");
                                                            String direccionHotel = hotelDoc.getString("ubicacion");
                                                            if (direccionHotel == null) direccionHotel = "Dirección no disponible";

                                                            // Crear solicitud
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

                                                            db.collection("solicitudesTaxi")
                                                                    .add(datosSolicitud)
                                                                    .addOnSuccessListener(docRef ->
                                                                            Log.d("SolicitudTaxi", "Solicitud guardada con ID: " + docRef.getId()))
                                                                    .addOnFailureListener(e ->
                                                                            Log.e("SolicitudTaxi", "Error al guardar solicitud", e));
                                                        })
                                                        .addOnFailureListener(e ->
                                                                Log.e("SolicitudTaxi", "Error al obtener hotel", e));
                                            })
                                            .addOnFailureListener(e ->
                                                    Log.e("SolicitudTaxi", "Error al obtener usuario", e));
                                })
                                .addOnFailureListener(e ->
                                        Log.e("SolicitudTaxi", "Error al verificar solicitud existente", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("SolicitudTaxi", "Error al consultar reservas", e));
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
    private void crearDatosDeSolicitudes() {
        solicitudes = new ArrayList<>();

        db.collection("solicitudesTaxi")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        Log.d("Solicitudes", "No hay solicitudes en solicitudesTaxi");
                        adapter.setListaSolicitudes(solicitudes);
                        mostrarMensajeSiCorresponde();
                        return;
                    }

                    for (DocumentSnapshot doc : snapshot) {
                        Boolean esAceptada = doc.getBoolean("esAceptada");

                        // Si la solicitud ya fue aceptada, la ignoramos
                        if (esAceptada != null && esAceptada) continue;

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

                        // Crear objeto DTO
                        SolicitudTaxi solicitud = new SolicitudTaxi(origen, origenDireccion, destino, destinoDireccion);
                        solicitud.setSolicitudId(doc.getId());
                        solicitud.setNombrePasajero(nombrePasajero);
                        solicitud.setTelefonoPasajero(telefonoPasajero);
                        solicitud.setNumeroPasajeros(numeroPasajeros);
                        solicitud.setTipoVehiculo(tipoVehiculo);
                        solicitud.setNotas(notas);
                        solicitud.setEstado(estado);
                        solicitud.setEsAceptada(false); // ya sabemos que no es aceptada porque la filtramos

                        solicitudes.add(solicitud);
                    }

                    adapter.setListaSolicitudes(new ArrayList<>(solicitudes));
                    mostrarMensajeSiCorresponde();
                    Log.d("Solicitudes", "Solicitudes NO aceptadas cargadas: " + solicitudes.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("Solicitudes", "Error al obtener solicitudesTaxi", e);

                    if (statusSwitch.isChecked()) {
                        tvSinSolicitudes.setVisibility(View.VISIBLE);
                        tvSinSolicitudes.setText("Error al cargar solicitudes");
                    } else {
                        tvSinSolicitudes.setVisibility(View.GONE);
                    }
                });

    }

    private void mostrarMensajeSiCorresponde() {
        if (statusSwitch != null && statusSwitch.isChecked() && solicitudes.isEmpty()) {
            tvSinSolicitudes.setVisibility(View.VISIBLE);
        } else {
            tvSinSolicitudes.setVisibility(View.GONE);
        }
    }


    //   METODO SEPARADO PARA CONFIGURAR NOTIFICACIONES
    private void configurarNotificaciones() {
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(v -> {
                Intent intent = new Intent(DriverInicioActivity.this, DriverNotificacionActivity.class);
                startActivity(intent);
            });
        }
    }

    //   CONFIGURACIÓN DE BOTTOM NAVIGATION SIMPLIFICADA
    private void configurarBottomNavigation() {
        if (bottomNavigation == null) {
            Log.e("MainActivity", "bottomNavigation es null, no se puede configurar");
            return;
        }

        // ESTABLECER ÍTEM SELECCIONADO
        bottomNavigation.setSelectedItemId(R.id.nav_inicio);

        // CONFIGURAR LISTENER
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                return true; // Ya estamos aquí
            } else if (itemId == R.id.nav_reservas) {
                navegarSinAnimacion(DriverReservaActivity.class);
                return true;
            } else if (itemId == R.id.nav_mapa) {
                navegarSinAnimacion(DriverMapaActivity.class);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                navegarSinAnimacion(DriverPerfilActivity.class);
                return true;
            }
            return false;
        });
    }
    // Metodo para navegar sin animación
    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        // ELIMINAR ANIMACIÓN COMPLETAMENTE
        overridePendingTransition(0, 0);
        finish();
    }
    private void configurarRecyclerView() {
        if (rvSolicitudesCercanas == null) {
            Log.e("MainActivity", "rvSolicitudesCercanas es null");
            return;
        }

        rvSolicitudesCercanas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SolicitudesAdapter();
        adapter.setContext(this);
        adapter.setListaSolicitudes(new ArrayList<>()); // inicial vacío
        rvSolicitudesCercanas.setAdapter(adapter);
    }


    private void configurarSwitch() {
        if (statusSwitch == null) {
            Log.e("MainActivity", "statusSwitch es null");
            return;
        }

        statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Si intenta activar, verificar GPS primero
                if (!verificarGPSParaServicio()) {
                    // GPS no disponible - revertir el switch
                    buttonView.setOnCheckedChangeListener(null); // Quitar listener temporalmente
                    buttonView.setChecked(false); // Revertir a false
                    configurarSwitch(); // Volver a configurar listener
                    return; // No actualizar UI
                }
            }

            // Si llegamos aquí, actualizar la UI normalmente
            actualizarEstadoUI(isChecked);
        });
    }

    private void actualizarEstadoUI(boolean disponible) {
        if (statusText == null || rvSolicitudesCercanas == null || tvNoSolicitudes == null) {
            Log.e("MainActivity", "Alguna vista es null en actualizarEstadoUI");
            return;
        }

        if (disponible) {
            // Usuario disponible
            statusText.setText("Disponible");
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));

            // Mostrar RecyclerView, ocultar mensaje
            rvSolicitudesCercanas.setVisibility(View.VISIBLE);
            tvNoSolicitudes.setVisibility(View.GONE);
        } else {
            // Usuario no disponible
            statusText.setText("No disponible");
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));

            // Ocultar RecyclerView, mostrar mensaje
            rvSolicitudesCercanas.setVisibility(View.GONE);
            tvNoSolicitudes.setVisibility(View.VISIBLE);
        }
    }
}