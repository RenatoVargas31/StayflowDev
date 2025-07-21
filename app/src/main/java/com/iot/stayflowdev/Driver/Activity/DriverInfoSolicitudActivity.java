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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.iot.stayflowdev.Driver.Dtos.SolicitudTaxi;
import com.iot.stayflowdev.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
public class DriverInfoSolicitudActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Declaración de vistas
    private MaterialToolbar toolbar;
    private Chip estadoChip;
    private MaterialTextView horaValor;
    private MaterialTextView nombrePasajero;
    private MaterialTextView telefonoPasajero;
    private MaterialTextView numeroPasajeros;
    private MaterialTextView tipoVehiculo;
    private MaterialTextView direccionOrigen;
    private MaterialTextView direccionDestino;
    private MaterialTextView distanciaValor;
    private MaterialTextView tiempoValor;
    private MaterialTextView notasContenido;
    private MaterialButton btnAceptar;
    private MaterialButton btnRechazar;
    private MaterialTextView origen;
    private MaterialTextView destino;
    private String solicitudId;

    // Variables para el mapa y coordenadas
    private GoogleMap mMap;
    private double origenLatitud = 0.0;
    private double origenLongitud = 0.0;
    private double destinoLatitud = -12.0219; // Aeropuerto Jorge Chávez
    private double destinoLongitud = -77.1143;
    private DistanceCalculator distanceCalculator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_info_solicitud);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar calculadora de distancia
        distanceCalculator = new DistanceCalculator(this);

        // Inicializar vistas
        inicializarVistas();

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

    private void inicializarVistas() {
        toolbar = findViewById(R.id.toolbar);
        estadoChip = findViewById(R.id.estadoChip);
        horaValor = findViewById(R.id.horaValor);
        nombrePasajero = findViewById(R.id.nombrePasajero);
        telefonoPasajero = findViewById(R.id.telefonoPasajero);
        numeroPasajeros = findViewById(R.id.numeroPasajeros);
        tipoVehiculo = findViewById(R.id.tipoVehiculo);
        direccionOrigen = findViewById(R.id.direccionOrigen);
        origen = findViewById(R.id.direccionOrigenName);
        destino = findViewById(R.id.direccionDestinoName);
        direccionDestino = findViewById(R.id.direccionDestino);
        distanciaValor = findViewById(R.id.distanciaValor);
        tiempoValor = findViewById(R.id.tiempoValor);
        notasContenido = findViewById(R.id.notasContenido);
        btnAceptar = findViewById(R.id.btnAceptar);
        btnRechazar = findViewById(R.id.btnRechazar);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
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

        cargarSolicitudDesdeFirebase();
    }

    private void cargarSolicitudDesdeFirebase() {
        Log.d("DEBUG", "Iniciando carga desde Firebase");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("solicitudesTaxi").document(solicitudId)
                .get()
                .addOnSuccessListener(document -> {
                    Log.d("DEBUG", "Firebase query exitosa");

                    if (document.exists()) {
                        Log.d("DEBUG", "Document data: " + document.getData());

                        try {
                            mostrarDatosDirectos(document);
                        } catch (Exception e) {
                            Log.e("ERROR", "Error al procesar documento: ", e);
                            Toast.makeText(this, "Error al procesar solicitud", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Solicitud no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR", "Error en query Firebase: ", e);
                    Toast.makeText(this, "Error al cargar solicitud", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void mostrarDatosDirectos(DocumentSnapshot document) {
        Log.d("DEBUG", "Iniciando mapeo manual");

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
            Long numPasajeros = document.getLong("numeroPasajeros");
            String estado = document.getString("estado");

            // Obtener coordenadas como GeoPoint
            GeoPoint origenCoordenadas = document.getGeoPoint("origenCoordenadas");
            GeoPoint destinoCoordenadas = document.getGeoPoint("destinoCoordenadas");

            Log.d("DEBUG", "Datos obtenidos:");
            Log.d("DEBUG", "- Nombre: " + nombrePas);
            Log.d("DEBUG", "- Origen: " + origenNombre);
            Log.d("DEBUG", "- Coordenadas origen: " + origenCoordenadas);

            // Configurar vistas
            nombrePasajero.setText(nombrePas != null ? nombrePas : "N/A");
            telefonoPasajero.setText(telefonoPas != null ? telefonoPas : "N/A");
            numeroPasajeros.setText((numPasajeros != null ? numPasajeros : 1) + " pasajeros");
            tipoVehiculo.setText(tipoVeh != null ? tipoVeh : "N/A");
            direccionOrigen.setText(origenDir != null ? origenDir : "N/A");
            origen.setText(origenNombre != null ? origenNombre : "N/A");
            destino.setText(destinoNombre != null ? destinoNombre : "Aeropuerto Internacional Jorge Chávez");
            direccionDestino.setText(destinoDir != null ? destinoDir : "Av. Elmer Faucett s/n, Callao 07031, Lima, Perú");
            notasContenido.setText(notas != null ? notas : "Solicitud generada automáticamente");

            // Configurar estado
            configurarEstadoChip(estado != null ? estado : "pendiente");

            // Guardar coordenadas para el mapa y cálculo de distancia
            if (origenCoordenadas != null) {
                origenLatitud = origenCoordenadas.getLatitude();
                origenLongitud = origenCoordenadas.getLongitude();

                // Mostrar ruta en mapa si ya está listo
                if (mMap != null) {
                    mostrarRutaEnMapa();
                }

                // Calcular distancia real
                calcularDistanciaReal();
            } else {
                Log.w("DEBUG", "No se encontraron coordenadas de origen");
                distanciaValor.setText("No disponible");
                tiempoValor.setText("No disponible");
            }

            Log.d("DEBUG", "Datos configurados correctamente");

        } catch (Exception e) {
            Log.e("ERROR", "Error en mapeo manual: ", e);
            Toast.makeText(this, "Error al mostrar datos", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void calcularDistanciaReal() {
        if (origenLatitud == 0.0 || origenLongitud == 0.0) {
            Log.w("DEBUG", "Coordenadas de origen no válidas");
            return;
        }

        Log.d("DEBUG", "Calculando distancia real...");

        distanceCalculator.calcularDistancia(
                origenLatitud, origenLongitud,
                destinoLatitud, destinoLongitud,
                new DistanceCalculator.DistanceCallback() {
                    @Override
                    public void onSuccess(double distanceKm, int timeMinutes, String distanceText, String timeText) {
                        Log.d("DEBUG", "Distancia calculada: " + distanceText + ", Tiempo: " + timeText);

                        runOnUiThread(() -> {
                            distanciaValor.setText(distanceText);
                            tiempoValor.setText(timeText);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("ERROR", "Error calculando distancia: " + error);

                        runOnUiThread(() -> {
                            distanciaValor.setText("Error al calcular");
                            tiempoValor.setText("Error al calcular");
                        });
                    }
                }
        );
    }

    private void configurarEstadoChip(String status) {
        estadoChip.setText(status);

        switch (status.toLowerCase()) {
            case "pendiente":
                estadoChip.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                estadoChip.setChipIconTintResource(android.R.color.holo_orange_dark);
                estadoChip.setChipStrokeColorResource(android.R.color.holo_orange_dark);
                break;
            case "aceptada":
                estadoChip.setChipBackgroundColorResource(android.R.color.holo_green_light);
                estadoChip.setChipIconTintResource(android.R.color.holo_green_dark);
                estadoChip.setChipStrokeColorResource(android.R.color.holo_green_dark);
                break;
            case "cancelada":
                estadoChip.setChipBackgroundColorResource(android.R.color.holo_red_light);
                estadoChip.setChipIconTintResource(android.R.color.holo_red_dark);
                estadoChip.setChipStrokeColorResource(android.R.color.holo_red_dark);
                break;
        }
    }

    private void configurarBotones() {
        btnAceptar.setOnClickListener(v -> {
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

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("solicitudesTaxi").document(solicitudId)
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        configurarEstadoChip("aceptada");
                        Snackbar.make(findViewById(R.id.main), "Solicitud aceptada", Snackbar.LENGTH_LONG).show();
                        btnAceptar.setEnabled(false);
                        btnRechazar.setEnabled(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Solicitud", "Error al actualizar solicitud", e);
                        Toast.makeText(this, "Error al aceptar solicitud", Toast.LENGTH_SHORT).show();
                    });
        });

        btnRechazar.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cancelar solicitud")
                    .setMessage("¿Estás seguro de que deseas cancelar esta solicitud?")
                    .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                        if (solicitudId == null) return;

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("solicitudesTaxi").document(solicitudId)
                                .update("estado", "cancelada")
                                .addOnSuccessListener(unused -> {
                                    configurarEstadoChip("cancelada");
                                    Snackbar.make(findViewById(R.id.main), "Solicitud cancelada", Snackbar.LENGTH_LONG).show();
                                    btnAceptar.setEnabled(false);
                                    btnRechazar.setEnabled(false);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Solicitud", "Error al cancelar solicitud", e);
                                    Toast.makeText(this, "Error al cancelar solicitud", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
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
}