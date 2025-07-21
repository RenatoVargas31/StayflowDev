package com.iot.stayflowdev.Driver.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;
import com.iot.stayflowdev.Driver.Repository.SolicitudesRepository;
import com.iot.stayflowdev.R;
import com.iot.stayflowdev.databinding.ActivityDriverInfoSolicitudBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DriverInfoSolicitudActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityDriverInfoSolicitudBinding binding;
    private String solicitudId;
    private GoogleMap mMap;
    private SolicitudesRepository solicitudesRepository;

    // Variables para coordenadas
    private double origenLatitud = 0.0;
    private double origenLongitud = 0.0;
    private double destinoLatitud = -12.0219; // Aeropuerto Jorge Chávez
    private double destinoLongitud = -77.1143;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityDriverInfoSolicitudBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar repository
        solicitudesRepository = new SolicitudesRepository(); // Sin context, solo para cargar datos

        // Configurar toolbar
        configurarToolbar();

        // Inicializar mapa
        inicializarMapa();

        // Recuperar datos del intent
        recuperarDatosIntent();

        // Configurar botones
        configurarBotones();

        Log.d("DEBUG", "Activity iniciada correctamente");
    }

    private void configurarToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void inicializarMapa() {
        try {
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            Log.e("MapError", "Error inicializando mapa", e);
            findViewById(R.id.map_container).setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("DEBUG", "Mapa inicializado correctamente");

        // Configurar el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Si ya tenemos coordenadas, mostrar en el mapa
        if (origenLatitud != 0.0 && origenLongitud != 0.0) {
            mostrarRutaEnMapa();
        }
    }

    private void mostrarRutaEnMapa() {
        if (mMap == null) return;

        try {
            // Limpiar mapa
            mMap.clear();

            // Agregar marcador de origen (Hotel)
            LatLng origenPos = new LatLng(origenLatitud, origenLongitud);
            mMap.addMarker(new MarkerOptions()
                    .position(origenPos)
                    .title("Origen: Hotel")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            // Agregar marcador de destino (Aeropuerto)
            LatLng destinoPos = new LatLng(destinoLatitud, destinoLongitud);
            mMap.addMarker(new MarkerOptions()
                    .position(destinoPos)
                    .title("Destino: Aeropuerto Jorge Chávez")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            // Ajustar cámara para mostrar ambos puntos
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origenPos);
            builder.include(destinoPos);
            LatLngBounds bounds = builder.build();

            int padding = 100; // Padding en píxeles
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

            Log.d("DEBUG", "Ruta mostrada en mapa: " + origenPos + " → " + destinoPos);

        } catch (Exception e) {
            Log.e("MapError", "Error mostrando ruta en mapa", e);
        }
    }

    private void recuperarDatosIntent() {
        solicitudId = getIntent().getStringExtra("solicitudId");

        if (solicitudId == null) {
            Toast.makeText(this, "No se recibió la solicitud correctamente", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar datos desde Firebase
        cargarSolicitudDesdeFirebase();
    }

    private void cargarSolicitudDesdeFirebase() {
        Log.d("DEBUG", "Cargando solicitud desde Firebase: " + solicitudId);

        FirebaseFirestore.getInstance()
                .collection("solicitudesTaxi")
                .document(solicitudId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Log.d("DEBUG", "Documento encontrado, mostrando datos");
                        mostrarDatosSolicitud(document);
                    } else {
                        Toast.makeText(this, "Solicitud no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR", "Error cargando solicitud", e);
                    Toast.makeText(this, "Error al cargar solicitud", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void mostrarDatosSolicitud(DocumentSnapshot document) {
        try {
            // Obtener datos del documento
            String nombrePas = document.getString("nombrePasajero");
            String telefonoPas = document.getString("telefonoPasajero");
            String tipoVeh = document.getString("tipoVehiculo");
            String origenDir = document.getString("origenDireccion");
            String origenNombre = document.getString("origen");
            String destinoNombre = document.getString("destino");
            String destinoDir = document.getString("destinoDireccion");
            String notas = document.getString("notas");
            String estado = document.getString("estado");
            Long numPasajeros = document.getLong("numeroPasajeros");

            // Obtener coordenadas
            GeoPoint origenCoordenadas = document.getGeoPoint("origenCoordenadas");
            GeoPoint destinoCoordenadas = document.getGeoPoint("destinoCoordenadas");

            // Obtener distancia y tiempo YA calculados
            Double distanciaKm = document.getDouble("distanciaKm");
            Long tiempoMin = document.getLong("tiempoEstimadoMin");
            String distanciaTexto = document.getString("distanciaTexto");
            String tiempoTexto = document.getString("tiempoTexto");

            // CONFIGURAR DATOS DEL PASAJERO
            binding.nombrePasajero.setText(nombrePas != null ? nombrePas : "N/A");
            binding.telefonoPasajero.setText(telefonoPas != null ? telefonoPas : "N/A");
            binding.numeroPasajeros.setText((numPasajeros != null ? numPasajeros : 1) + " pasajeros");
            binding.tipoVehiculo.setText(tipoVeh != null ? tipoVeh : "N/A");

            // CONFIGURAR DATOS DEL RECORRIDO
            binding.direccionOrigenName.setText(origenNombre != null ? origenNombre : "N/A");
            binding.direccionOrigen.setText(origenDir != null ? origenDir : "N/A");
            binding.direccionDestinoName.setText(destinoNombre != null ? destinoNombre : "Aeropuerto Internacional Jorge Chávez");
            binding.direccionDestino.setText(destinoDir != null ? destinoDir : "Av. Elmer Faucett s/n, Callao 07031, Lima, Perú");

            // CONFIGURAR DISTANCIA Y TIEMPO (YA CALCULADOS)
            if (distanciaTexto != null && !distanciaTexto.isEmpty()) {
                binding.distanciaValor.setText(distanciaTexto);
            } else if (distanciaKm != null && distanciaKm > 0) {
                binding.distanciaValor.setText(String.format("%.1f km", distanciaKm));
            } else {
                binding.distanciaValor.setText("-- km");
            }

            if (tiempoTexto != null && !tiempoTexto.isEmpty()) {
                binding.tiempoValor.setText(tiempoTexto);
            } else if (tiempoMin != null && tiempoMin > 0) {
                binding.tiempoValor.setText(tiempoMin + " min");
            } else {
                binding.tiempoValor.setText("-- min");
            }

            // CONFIGURAR NOTAS
            binding.notasContenido.setText(notas != null ? notas : "Sin notas adicionales");

            // CONFIGURAR ESTADO
            configurarEstadoChip(estado != null ? estado : "pendiente");

            // CONFIGURAR HORA (usar fechaCreacion)
            Timestamp fechaCreacion = document.getTimestamp("fechaCreacion");
            if (fechaCreacion != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                binding.horaValor.setText(sdf.format(fechaCreacion.toDate()));
            } else {
                binding.horaValor.setText("--:--");
            }

            // GUARDAR COORDENADAS PARA EL MAPA
            if (origenCoordenadas != null) {
                origenLatitud = origenCoordenadas.getLatitude();
                origenLongitud = origenCoordenadas.getLongitude();

                // Si el mapa ya está listo, mostrar la ruta
                if (mMap != null) {
                    mostrarRutaEnMapa();
                }
            }

            if (destinoCoordenadas != null) {
                destinoLatitud = destinoCoordenadas.getLatitude();
                destinoLongitud = destinoCoordenadas.getLongitude();
            }

            Log.d("DEBUG", "Datos de solicitud configurados correctamente");

        } catch (Exception e) {
            Log.e("ERROR", "Error mostrando datos de solicitud", e);
            Toast.makeText(this, "Error al mostrar datos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configurarEstadoChip(String status) {
        binding.estadoChip.setText(status);

        switch (status.toLowerCase()) {
            case "pendiente":
                binding.estadoChip.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                binding.estadoChip.setChipIconTintResource(android.R.color.holo_orange_dark);
                binding.estadoChip.setChipStrokeColorResource(android.R.color.holo_orange_dark);
                break;
            case "aceptada":
                binding.estadoChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
                binding.estadoChip.setChipIconTintResource(android.R.color.holo_green_dark);
                binding.estadoChip.setChipStrokeColorResource(android.R.color.holo_green_dark);
                break;
            case "cancelada":
                binding.estadoChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
                binding.estadoChip.setChipIconTintResource(android.R.color.holo_red_dark);
                binding.estadoChip.setChipStrokeColorResource(android.R.color.holo_red_dark);
                break;
        }
    }

    private void configurarBotones() {
        binding.btnAceptar.setOnClickListener(v -> {
            mostrarDialogoConfirmacion();
        });
    }

    private void mostrarDialogoConfirmacion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar viaje")
                .setMessage("¿Estás seguro de que quieres aceptar este viaje?")
                .setPositiveButton("Aceptar viaje", (dialog, which) -> {
                    aceptarSolicitud();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private void aceptarSolicitud() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || solicitudId == null) {
            Toast.makeText(this, "No se pudo aceptar la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        String idDriver = currentUser.getUid();
        String horaActual = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "aceptada");
        updates.put("esAceptada", true);
        updates.put("horaAceptacion", horaActual);
        updates.put("idTaxista", idDriver);

        FirebaseFirestore.getInstance()
                .collection("solicitudesTaxi")
                .document(solicitudId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    configurarEstadoChip("aceptada");
                    Snackbar.make(binding.main, "Solicitud aceptada", Snackbar.LENGTH_LONG).show();
                    binding.btnAceptar.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR", "Error al actualizar solicitud", e);
                    Toast.makeText(this, "Error al aceptar solicitud", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}