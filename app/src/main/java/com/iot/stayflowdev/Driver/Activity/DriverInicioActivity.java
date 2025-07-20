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
import androidx.appcompat.app.AlertDialog;
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

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
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
import com.iot.stayflowdev.Driver.Repository.SolicitudesRepository;
import com.iot.stayflowdev.Driver.Repository.TaxistaRepository;
import com.iot.stayflowdev.LoginActivity;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityDriverInicioBinding;
import com.iot.stayflowdev.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;


public class DriverInicioActivity extends AppCompatActivity {
    private ActivityDriverInicioBinding binding;
    private static final String TAG = "DriverInicioActivity";
    // Variables para adapter y lista
    private SolicitudesAdapter adapter;
    private List<SolicitudTaxi> solicitudes;
    // Variables para notificaciones
    private String channelId = "channelDefaultPri";
    private String gpsChannelId = "channelGPSAlert";
    // Repositories
    private TaxistaRepository taxistaRepository;
    private SolicitudesRepository solicitudesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Inicializar View Binding
        binding = ActivityDriverInicioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configurar márgenes para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Inicializar Repositories
        taxistaRepository = new TaxistaRepository();
        solicitudesRepository = new SolicitudesRepository();
        // Verificar autenticación y configurar usuario
        verificarAutenticacion();
        // Crear canales de notificación primero
        crearCanalesNotificacion();
        // Configurar todas las vistas
        configurarRecyclerView();
        configurarSwitch();
        configurarBottomNavigation();
        configurarNotificaciones();
        // Establecer estado inicial
        actualizarEstadoUI(binding.statusSwitch.isChecked());
        // Verificar GPS al iniciar la app
        verificarEstadoGPS();
        // Cargar solicitudes
        inicializarSolicitudes();
    }
    private void verificarAutenticacion() {
        if (!taxistaRepository.usuarioEstaAutenticado()) {
            Log.d(TAG, "No hay usuario autenticado.");
            redirigirALogin();
            return;
        }

        taxistaRepository.obtenerTaxistaConImagen(
                taxistaConImagen -> {
                    User usuario = taxistaConImagen.getUsuario();

                    // Configurar nombre
                    binding.title.setText(usuario.getName());

                    // Mostrar imagen
                    if (taxistaConImagen.tieneImagen()) {
                        Glide.with(this)
                                .load(taxistaConImagen.getUrlImagen())
                                .placeholder(R.drawable.taxista)
                                .error(R.drawable.taxista)
                                .into(binding.userIcon);
                    } else {
                        binding.userIcon.setImageResource(R.drawable.taxista);
                    }

                    if (!usuario.isEstado()) {
                        mostrarMensajeUsuarioDeshabilitado();
                    }
                },
                this::manejarErrorAutenticacion
        );
    }


    private void configurarUsuario(User usuario) {
        Log.d(TAG, "Usuario obtenido exitosamente");
        Log.d(TAG, "UID: " + usuario.getUid());
        Log.d(TAG, "Nombre: " + usuario.getName());
        Log.d(TAG, "Email: " + usuario.getEmail());
        Log.d(TAG, "Rol: " + usuario.getRol());

        // Mostrar el nombre en el título usando el método getName() de User
        String nombreCompleto = usuario.getName();
        binding.title.setText(nombreCompleto);

        // Opcional: Guardar usuario en variable de clase si lo necesitas después
        // this.usuarioActual = usuario;

        // Opcional: Verificar si el usuario está habilitado
        if (!usuario.isEstado()) {
            Log.w(TAG, "Usuario deshabilitado");
            mostrarMensajeUsuarioDeshabilitado();
        }
    }

    private void manejarErrorAutenticacion(Exception exception) {
        Log.e(TAG, "Error al obtener información del usuario: " + exception.getMessage());
        // AGREGAR ESTA LÍNEA:
        binding.userIcon.setImageResource(R.drawable.taxista);
        // Manejar diferentes tipos de errores
        String mensaje = exception.getMessage();
        if (mensaje != null) {
            if (mensaje.contains("rol de taxista")) {
                // Usuario no tiene permisos de taxista
                mostrarMensajeRolIncorrecto();
            } else if (mensaje.contains("Usuario no encontrado")) {
                // Usuario no existe en la base de datos
                mostrarMensajeUsuarioNoEncontrado();
            } else {
                // Otro tipo de error
                mostrarMensajeErrorGeneral(mensaje);
            }
        }

        // En caso de error crítico, redirigir al login
        redirigirALogin();
    }
    private void inicializarSolicitudes() {
        // Primero generar solicitudes desde reservas con quieroTaxi=true
        generarSolicitudesDesdeReservas();
    }
    private void generarSolicitudesDesdeReservas() {
        solicitudesRepository.generarSolicitudesDesdeReservas(
                cantidadGeneradas -> {
                    Log.d(TAG, "Solicitudes generadas desde reservas: " + cantidadGeneradas);
                    // Después de generar, cargar todas las solicitudes pendientes
                    cargarSolicitudesPendientes();
                },
                exception -> {
                    Log.e(TAG, "Error al generar solicitudes desde reservas: " + exception.getMessage());
                    // Aunque falle la generación, intentar cargar las existentes
                    cargarSolicitudesPendientes();
                }
        );
    }

    private void cargarSolicitudesPendientes() {
        solicitudesRepository.obtenerSolicitudesPendientes(
                this::configurarSolicitudes,
                this::manejarErrorSolicitudes
        );
    }

    private void configurarSolicitudes(List<SolicitudTaxi> solicitudesObtenidas) {
        Log.d(TAG, "Solicitudes pendientes obtenidas: " + solicitudesObtenidas.size());

        this.solicitudes = solicitudesObtenidas;

        if (adapter != null) {
            adapter.setListaSolicitudes(new ArrayList<>(solicitudes));
        }

        mostrarMensajeSiCorresponde();
    }

    private void manejarErrorSolicitudes(Exception exception) {
        Log.e(TAG, "Error al cargar solicitudes: " + exception.getMessage());

        // Inicializar lista vacía en caso de error
        this.solicitudes = new ArrayList<>();

        if (adapter != null) {
            adapter.setListaSolicitudes(solicitudes);
        }

        // Mostrar mensaje de error si el switch está activado
        if (binding.statusSwitch.isChecked()) {
            binding.tvSinSolicitudes.setVisibility(View.VISIBLE);
            binding.tvSinSolicitudes.setText("Error al cargar solicitudes");
        }

        Toast.makeText(this, "Error al cargar solicitudes: " + exception.getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    private void mostrarMensajeSiCorresponde() {
        if (binding.statusSwitch.isChecked() && (solicitudes == null || solicitudes.isEmpty())) {
            binding.tvSinSolicitudes.setVisibility(View.VISIBLE);
            binding.tvSinSolicitudes.setText("No hay solicitudes disponibles");
        } else {
            binding.tvSinSolicitudes.setVisibility(View.GONE);
        }
    }

    // Método para refrescar solicitudes (útil para pull-to-refresh)
    public void refrescarSolicitudes() {
        Log.d(TAG, "Refrescando solicitudes...");

        // Mostrar indicador de carga si lo tienes
        // binding.progressBar.setVisibility(View.VISIBLE);

        generarSolicitudesDesdeReservas();
    }

    /*// Método para aceptar una solicitud
    public void aceptarSolicitud(String solicitudId) {
        String taxistaId = taxistaRepository.obtenerUidActual();

        solicitudesRepository.aceptarSolicitud(solicitudId, taxistaId,
                aVoid -> {
                    Log.d(TAG, "Solicitud aceptada exitosamente");
                    Toast.makeText(this, "Solicitud aceptada", Toast.LENGTH_SHORT).show();

                    // Refrescar la lista para quitar la solicitud aceptada
                    cargarSolicitudesPendientes();

                    // Opcional: Navegar a la actividad del mapa o viaje
                    navegarAViaje(solicitudId);
                },
                exception -> {
                    Log.e(TAG, "Error al aceptar solicitud: " + exception.getMessage());
                    Toast.makeText(this, "Error al aceptar solicitud", Toast.LENGTH_SHORT).show();
                }
        );
    } */

    private void navegarAViaje(String solicitudId) {
        Intent intent = new Intent(this, DriverMapaActivity.class);
        intent.putExtra("solicitud_id", solicitudId);
        intent.putExtra("en_viaje", true);
        startActivity(intent);
    }

    // Métodos de configuración que permanecen igual...
    private void configurarRecyclerView() {
        binding.rvSolicitudesCercanas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SolicitudesAdapter();
        adapter.setContext(this);
        adapter.setListaSolicitudes(new ArrayList<>());

        // Opcional: Configurar callback para cuando se acepte una solicitud
        //adapter.setOnSolicitudAceptadaListener(this::aceptarSolicitud);

        binding.rvSolicitudesCercanas.setAdapter(adapter);
    }

    private void configurarSwitch() {
        binding.statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!verificarGPSParaServicio()) {
                    buttonView.setOnCheckedChangeListener(null);
                    buttonView.setChecked(false);
                    configurarSwitch();
                    return;
                }
                // Cuando se active, refrescar solicitudes
                refrescarSolicitudes();
            }
            actualizarEstadoUI(isChecked);
        });
    }

    private void actualizarEstadoUI(boolean disponible) {
        if (disponible) {
            binding.statusText.setText("Disponible");
            binding.statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            binding.rvSolicitudesCercanas.setVisibility(View.VISIBLE);
            binding.tvNoSolicitudes.setVisibility(View.GONE);
        } else {
            binding.statusText.setText("No disponible");
            binding.statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            binding.rvSolicitudesCercanas.setVisibility(View.GONE);
            binding.tvNoSolicitudes.setVisibility(View.VISIBLE);
        }
    }
    private void redirigirALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarMensajeUsuarioDeshabilitado() {
        new AlertDialog.Builder(this)
                .setTitle("Cuenta Deshabilitada")
                .setMessage("Tu cuenta ha sido deshabilitada. Contacta al administrador.")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    redirigirALogin();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarMensajeRolIncorrecto() {
        new AlertDialog.Builder(this)
                .setTitle("Acceso Denegado")
                .setMessage("No tienes permisos de taxista para acceder a esta aplicación.")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    redirigirALogin();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarMensajeUsuarioNoEncontrado() {
        new AlertDialog.Builder(this)
                .setTitle("Usuario No Encontrado")
                .setMessage("No se encontró información de usuario. Contacta al administrador.")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    dialog.dismiss();
                    redirigirALogin();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarMensajeErrorGeneral(String mensaje) {
        Toast.makeText(this, "Error al cargar información del usuario: " + mensaje,
                Toast.LENGTH_LONG).show();
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

    private void verificarEstadoGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGpsEnabled) {
            mostrarNotificacionGPS();
        }
    }

    private void mostrarNotificacionGPS() {
        Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent gpsPendingIntent = PendingIntent.getActivity(
                this, 100, gpsIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent mainIntent = new Intent(this, DriverInicioActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                this, 101, mainIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, gpsChannelId)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle("GPS Desactivado")
                .setContentText("Activa la ubicación para recibir solicitudes de viaje cercanas")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Para poder recibir solicitudes de viaje y mostrar tu ubicación a los pasajeros, necesitas activar el GPS en la configuración de tu dispositivo."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mainPendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 1000})
                .setLights(0xFFFF6B00, 300, 1000)
                .addAction(R.drawable.ic_exclamation, "Activar GPS", gpsPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(999, builder.build());
        }
    }

    private boolean verificarGPSParaServicio() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGpsEnabled) {
            mostrarNotificacionGPS();
            Toast.makeText(this, "Activa el GPS para ponerte disponible", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.statusSwitch.isChecked()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGpsEnabled) {
                binding.statusSwitch.setOnCheckedChangeListener(null);
                binding.statusSwitch.setChecked(false);
                configurarSwitch();
                actualizarEstadoUI(false);
                mostrarNotificacionGPS();
            }
        }
    }
    private void configurarNotificaciones() {
        binding.notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DriverInicioActivity.this, DriverNotificacionActivity.class);
            startActivity(intent);
        });
    }

    private void configurarBottomNavigation() {
        // ESTABLECER ÍTEM SELECCIONADO
        binding.bottomNavigation.setSelectedItemId(R.id.nav_inicio);

        // CONFIGURAR LISTENER
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
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
    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Liberar referencia del binding
    }
}