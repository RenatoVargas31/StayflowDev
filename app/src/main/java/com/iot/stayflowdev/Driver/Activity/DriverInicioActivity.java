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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.iot.stayflowdev.Driver.Adapter.SolicitudesAdapter;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;
import com.iot.stayflowdev.Driver.Model.SolicitudModel;
import com.iot.stayflowdev.LoginActivity;
import com.iot.stayflowdev.R;

import java.util.ArrayList;
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
        // Crear canales de notificación primero
        crearCanalesNotificacion();

        //   INICIALIZAR TODAS LAS VISTAS PRIMERO
        inicializarVistas();

        crearDatosDeSolicitudes();
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

    // Metodo para verificar GPS cuando el usuario cambia el estado del switch
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


    private void crearDatosDeSolicitudes() {
        solicitudes = new ArrayList<>();
        db.collection("reservas")
                .whereEqualTo("quieroTaxi", true)
                .get()
                .addOnSuccessListener(reservaSnapshots -> {
                    if (reservaSnapshots.isEmpty()) {
                        Log.d("Solicitudes", "No hay reservas con taxi = true");
                        adapter.setListaSolicitudes(solicitudes); // vacío
                        return;
                    }

                    for (DocumentSnapshot reservaDoc : reservaSnapshots) {
                        Map<String, Object> servicioTaxi = (Map<String, Object>) reservaDoc.get("servicioTaxi");
                        Map<String, Object> cantHuespedes = (Map<String, Object>) reservaDoc.get("cantHuespedes");
                        String idUsuario = reservaDoc.getString("idUsuario");

                        if (servicioTaxi == null || cantHuespedes == null || idUsuario == null) continue;

                        String origen = (String) servicioTaxi.get("Origen");
                        String origenDireccion = (String) servicioTaxi.get("OrigenDireccion");
                        String destino = (String) servicioTaxi.get("Destino");
                        String destinoDireccion = (String) servicioTaxi.get("DestinoDireccion");

                        String adultosStr = (String) cantHuespedes.get("adultos");
                        String ninosStr = (String) cantHuespedes.get("ninos");

                        int adultos = adultosStr != null ? Integer.parseInt(adultosStr) : 0;
                        int ninos = ninosStr != null ? Integer.parseInt(ninosStr) : 0;
                        int totalPasajeros = adultos + ninos;

                        // Ahora obtener los datos del usuario
                        db.collection("usuarios")
                                .document(idUsuario)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String nombrePasajero = userDoc.getString("nombre");
                                    String telefonoPasajero = userDoc.getString("telefono"); // si existe

                                    // Crear DTO y llenar datos
                                    SolicitudTaxi solicitud = new SolicitudTaxi(origen, origenDireccion, destino, destinoDireccion);
                                    solicitud.setNombrePasajero(nombrePasajero != null ? nombrePasajero : "Usuario desconocido");
                                    solicitud.setTelefonoPasajero(telefonoPasajero != null ? telefonoPasajero : "No disponible");
                                    solicitud.setNumeroPasajeros(totalPasajeros);
                                    solicitud.setTipoVehiculo("Estándar"); // o elige según reglas
                                    solicitud.setNotas("Solicitud generada automáticamente"); // si no hay campo notas

                                    solicitudes.add(solicitud);

                                    // Actualizar RecyclerView
                                    adapter.setListaSolicitudes(new ArrayList<>(solicitudes)); // copia para evitar duplicados
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Solicitudes", "Error al cargar reservas: " + e.getMessage());
                });
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