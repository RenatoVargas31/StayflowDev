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

    private SolicitudesAdapter adapter;
    private List<SolicitudTaxi> solicitudes;
    private String channelId = "channelDefaultPri";
    private String gpsChannelId = "channelGPSAlert";

    private TaxistaRepository taxistaRepository;
    private SolicitudesRepository solicitudesRepository;
    private DistanceCalculator distanceCalculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityDriverInicioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        taxistaRepository = new TaxistaRepository();
        solicitudesRepository = new SolicitudesRepository();
        distanceCalculator = new DistanceCalculator(this);

        verificarAutenticacion();
        crearCanalesNotificacion();
        configurarRecyclerView();
        configurarSwitch();
        configurarBottomNavigation();
        configurarNotificaciones();
        actualizarEstadoUI(binding.statusSwitch.isChecked());
        verificarEstadoGPS();
        inicializarSolicitudes();
    }

    private void verificarAutenticacion() {
        if (!taxistaRepository.usuarioEstaAutenticado()) {
            redirigirALogin();
            return;
        }

        taxistaRepository.obtenerTaxistaConImagen(
                taxistaConImagen -> {
                    User usuario = taxistaConImagen.getUsuario();
                    binding.title.setText(usuario.getName());

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

    private void manejarErrorAutenticacion(Exception exception) {
        binding.userIcon.setImageResource(R.drawable.taxista);
        String mensaje = exception.getMessage();
        if (mensaje != null) {
            if (mensaje.contains("rol de taxista")) {
                mostrarMensajeRolIncorrecto();
            } else if (mensaje.contains("Usuario no encontrado")) {
                mostrarMensajeUsuarioNoEncontrado();
            } else {
                mostrarMensajeErrorGeneral(mensaje);
            }
        }
        redirigirALogin();
    }

    private void inicializarSolicitudes() {
        generarSolicitudesDesdeReservas();
    }

    private void generarSolicitudesDesdeReservas() {
        solicitudesRepository.generarSolicitudesDesdeReservas(
                cantidadGeneradas -> cargarSolicitudesPendientesConDistancia(),
                exception -> cargarSolicitudesPendientesConDistancia()
        );
    }

    private void cargarSolicitudesPendientesConDistancia() {
        solicitudesRepository.obtenerSolicitudesPendientes(
                solicitudesObtenidas -> {
                    if (solicitudesObtenidas.isEmpty()) {
                        configurarSolicitudes(solicitudesObtenidas);
                        return;
                    }
                    calcularDistanciasParaSolicitudes(solicitudesObtenidas);
                },
                this::manejarErrorSolicitudes
        );
    }

    private void calcularDistanciasParaSolicitudes(List<SolicitudTaxi> solicitudesObtenidas) {
        int[] procesadas = {0};
        int total = solicitudesObtenidas.size();

        for (int i = 0; i < solicitudesObtenidas.size(); i++) {
            SolicitudTaxi solicitud = solicitudesObtenidas.get(i);

            if (solicitud.tieneCoordenadasValidas()) {
                distanceCalculator.calcularDistancia(
                        solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud(),
                        solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud(),
                        new DistanceCalculator.DistanceCallback() {
                            @Override
                            public void onSuccess(double distanceKm, int timeMinutes, String distanceText, String timeText) {
                                solicitud.setDistanciaKm(distanceKm);
                                solicitud.setTiempoEstimadoMin(timeMinutes);
                                procesadas[0]++;

                                actualizarDistanciaEnFirestore(solicitud, distanceKm, timeMinutes);

                                if (procesadas[0] == total) {
                                    runOnUiThread(() -> configurarSolicitudes(solicitudesObtenidas));
                                }
                            }

                            @Override
                            public void onError(String error) {
                                double distanciaAprox = calcularDistanciaHaversine(
                                        solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud(),
                                        solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud()
                                );
                                solicitud.setDistanciaKm(distanciaAprox);
                                solicitud.setTiempoEstimadoMin((int)(distanciaAprox * 1.5));
                                procesadas[0]++;

                                if (procesadas[0] == total) {
                                    runOnUiThread(() -> configurarSolicitudes(solicitudesObtenidas));
                                }
                            }
                        }
                );
            } else {
                solicitud.setDistanciaKm(0);
                solicitud.setTiempoEstimadoMin(0);
                procesadas[0]++;

                if (procesadas[0] == total) {
                    runOnUiThread(() -> configurarSolicitudes(solicitudesObtenidas));
                }
            }
        }
    }

    private void actualizarDistanciaEnFirestore(SolicitudTaxi solicitud, double distanceKm, int timeMinutes) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("distanciaKm", distanceKm);
        updates.put("tiempoEstimadoMin", timeMinutes);
        updates.put("fechaCalculoDistancia", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("solicitudesTaxi")
                .document(solicitud.getSolicitudId())
                .update(updates);
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void cargarSolicitudesPendientes() {
        cargarSolicitudesPendientesConDistancia();
    }

    private void configurarSolicitudes(List<SolicitudTaxi> solicitudesObtenidas) {
        this.solicitudes = solicitudesObtenidas;
        if (adapter != null) {
            adapter.setListaSolicitudes(new ArrayList<>(solicitudes));
        }
        mostrarMensajeSiCorresponde();
    }

    private void manejarErrorSolicitudes(Exception exception) {
        this.solicitudes = new ArrayList<>();
        if (adapter != null) {
            adapter.setListaSolicitudes(solicitudes);
        }
        if (binding.statusSwitch.isChecked()) {
            binding.tvSinSolicitudes.setVisibility(View.VISIBLE);
            binding.tvSinSolicitudes.setText("Error al cargar solicitudes");
        }
        Toast.makeText(this, "Error al cargar solicitudes: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void mostrarMensajeSiCorresponde() {
        if (binding.statusSwitch.isChecked() && (solicitudes == null || solicitudes.isEmpty())) {
            binding.tvSinSolicitudes.setVisibility(View.VISIBLE);
            binding.tvSinSolicitudes.setText("No hay solicitudes disponibles");
        } else {
            binding.tvSinSolicitudes.setVisibility(View.GONE);
        }
    }

    public void refrescarSolicitudes() {
        generarSolicitudesDesdeReservas();
    }

    private void configurarRecyclerView() {
        binding.rvSolicitudesCercanas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SolicitudesAdapter();
        adapter.setContext(this);
        adapter.setListaSolicitudes(new ArrayList<>());
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
        Toast.makeText(this, "Error al cargar información del usuario: " + mensaje, Toast.LENGTH_LONG).show();
    }

    private void crearCanalesNotificacion() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channelDefault = new NotificationChannel(
                    channelId,
                    "Canal notificaciones default",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channelDefault.setDescription("Canal para notificaciones con prioridad default");

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
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(DriverInicioActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
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
        PendingIntent gpsPendingIntent = PendingIntent.getActivity(this, 100, gpsIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent mainIntent = new Intent(this, DriverInicioActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 101, mainIntent, PendingIntent.FLAG_IMMUTABLE);

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
        binding.bottomNavigation.setSelectedItemId(R.id.nav_inicio);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                return true;
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
        binding = null;
    }
}