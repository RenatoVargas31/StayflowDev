package com.iot.stayflowdev.Driver.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.iot.stayflowdev.Driver.Dtos.SolicitudReserva;
import com.iot.stayflowdev.Driver.Repository.TaxistaReservaRepository;
import com.iot.stayflowdev.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DriverReservaInfoActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String TAG = "DriverReservaInfoActivity";

    // UI Elements - Card Pasajero
    private ShapeableImageView iv_foto_perfil;
    private MaterialTextView nombrePasajero;
    private MaterialTextView telefonoPasajero;
    private MaterialTextView numeroPasajeros;
    private MaterialTextView tipoVehiculo;
    private MaterialTextView notasContenido;

    // UI Elements - Card Viaje
    private MaterialTextView nombreOrigen;
    private MaterialTextView direccionOrigen;
    private MaterialTextView nombreDestino;
    private MaterialTextView direccionDestino;
    private MaterialTextView fechaHora;
    private MaterialTextView distanciaViaje;
    private MaterialTextView tiempoViaje;

    // UI Elements - Card Hotel con Mapa
    private MapView mapView;
    private GoogleMap googleMap;
    private FloatingActionButton fabOpenMaps;
    private MaterialTextView tv_rating;
    private MaterialTextView tv_hotel_nombre;
    private MaterialTextView tv_hotel_direccion;

    // Data
    private String solicitudId;
    private SolicitudReserva solicitudActual;
    private TaxistaReservaRepository repository;
    private LatLng hotelLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_reserva_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar repositorio
        repository = new TaxistaReservaRepository();

        initToolbar();
        initViews();
        setupListeners();

        // Inicializar el mapa
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        cargarDatosDesdeRepositorio();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void initViews() {
        // Card Pasajero
        iv_foto_perfil = findViewById(R.id.iv_foto_perfil);
        nombrePasajero = findViewById(R.id.nombrePasajero);
        telefonoPasajero = findViewById(R.id.telefonoPasajero);
        numeroPasajeros = findViewById(R.id.numeroPasajeros);
        tipoVehiculo = findViewById(R.id.tipoVehiculo);
        notasContenido = findViewById(R.id.notasContenido);

        // Card Viaje
        nombreOrigen = findViewById(R.id.nombreOrigen);
        direccionOrigen = findViewById(R.id.direccionOrigen);
        nombreDestino = findViewById(R.id.nombreDestino);
        direccionDestino = findViewById(R.id.direccionDestino);
        fechaHora = findViewById(R.id.fechaHora);
        distanciaViaje = findViewById(R.id.distanciaViaje);
        tiempoViaje = findViewById(R.id.tiempoViaje);

        // Card Hotel con Mapa
        mapView = findViewById(R.id.map_hotel);
        fabOpenMaps = findViewById(R.id.fab_open_maps);
        tv_rating = findViewById(R.id.tv_rating);
        tv_hotel_nombre = findViewById(R.id.tv_hotel_nombre);
        tv_hotel_direccion = findViewById(R.id.tv_hotel_direccion);
    }

    private void setupListeners() {
        // Listener para llamar al pasajero
        telefonoPasajero.setOnClickListener(v -> {
            if (solicitudActual != null && solicitudActual.getTelefonoPasajero() != null) {
                String telefono = solicitudActual.getTelefonoPasajero();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + telefono));
                startActivity(intent);
            }
        });

        // Listener del FAB para abrir Google Maps
        fabOpenMaps.setOnClickListener(v -> {
            if (solicitudActual != null && solicitudActual.getOrigenCoordenadas() != null) {
                // Abrir DriverMapaActivity con los datos del hotel
                Intent intent = new Intent(DriverReservaInfoActivity.this, DriverMapaActivity.class);

                // Pasar las coordenadas del hotel
                GeoPoint coordenadas = solicitudActual.getOrigenCoordenadas();
                intent.putExtra("HOTEL_LAT", coordenadas.getLatitude());
                intent.putExtra("HOTEL_LNG", coordenadas.getLongitude());
                intent.putExtra("HOTEL_NOMBRE", solicitudActual.getOrigen());
                intent.putExtra("HOTEL_DIRECCION", solicitudActual.getOrigenDireccion());
                intent.putExtra("SOLICITUD_ID", solicitudActual.getReservaId());

                // Información adicional útil
                intent.putExtra("DESTINO_LAT", solicitudActual.getDestinoCoordenadas() != null ?
                        solicitudActual.getDestinoCoordenadas().getLatitude() : 0.0);
                intent.putExtra("DESTINO_LNG", solicitudActual.getDestinoCoordenadas() != null ?
                        solicitudActual.getDestinoCoordenadas().getLongitude() : 0.0);
                intent.putExtra("DESTINO_NOMBRE", solicitudActual.getDestino());

                startActivity(intent);
                Log.d(TAG, "Abriendo DriverMapaActivity con coordenadas: " +
                        coordenadas.getLatitude() + ", " + coordenadas.getLongitude());
            } else {
                Toast.makeText(this, "Ubicación del hotel no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Configurar el mapa
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        Log.d(TAG, "Mapa listo y configurado");

        // Si ya tenemos los datos de la solicitud, mostrar la ubicación
        if (solicitudActual != null && solicitudActual.getOrigenCoordenadas() != null) {
            mostrarUbicacionEnMapa();
        }
    }

    private void mostrarUbicacionEnMapa() {
        if (googleMap != null && solicitudActual != null && solicitudActual.getOrigenCoordenadas() != null) {
            GeoPoint geoPoint = solicitudActual.getOrigenCoordenadas();
            hotelLocation = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

            // Limpiar marcadores previos
            googleMap.clear();

            // Agregar marcador del hotel
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(hotelLocation)
                    .title(solicitudActual.getOrigen())
                    .snippet("Punto de recogida")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            googleMap.addMarker(markerOptions);

            // Mover la cámara a la ubicación del hotel
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hotelLocation, 15f));

            Log.d(TAG, "Mapa configurado con ubicación: " + hotelLocation.toString());
        } else {
            Log.w(TAG, "No se puede mostrar ubicación - mapa: " + (googleMap != null) +
                    ", solicitud: " + (solicitudActual != null) +
                    ", coordenadas: " + (solicitudActual != null && solicitudActual.getOrigenCoordenadas() != null));
        }
    }

    private void cargarDatosDesdeRepositorio() {
        // Obtener el ID del documento desde el Intent
        solicitudId = getIntent().getStringExtra("SOLICITUD_ID");
        Log.d(TAG, "Solicitud ID recibido: " + solicitudId);

        if (solicitudId == null || solicitudId.isEmpty()) {
            Log.e(TAG, "No se recibió SOLICITUD_ID");
            Toast.makeText(this, "Error: No se pudo cargar la información", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Usar el repositorio para obtener la solicitud
        repository.obtenerSolicitudPorReservaId(solicitudId, new TaxistaReservaRepository.OnSolicitudDetailListener() {
            @Override
            public void onSuccess(SolicitudReserva solicitud) {
                Log.d(TAG, "Solicitud cargada exitosamente: " + solicitud.getNombrePasajero());
                solicitudActual = solicitud;
                mostrarDatosEnUI();

                // Cargar datos del hotel si existe
                String idHotel = solicitudActual.getIdHotel();
                if (idHotel != null) {
                    cargarDatosHotel(idHotel.replace("hoteles/", ""));
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al cargar solicitud: " + error);
                Toast.makeText(DriverReservaInfoActivity.this,
                        "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDatosEnUI() {
        if (solicitudActual == null) return;

        // Datos del pasajero
        mostrarDatosPasajero();

        // Datos del viaje
        mostrarDatosViaje();

        // Mostrar ubicación en el mapa si está disponible
        mostrarUbicacionEnMapa();
    }

    private void mostrarDatosPasajero() {
        // Nombre del pasajero
        nombrePasajero.setText(solicitudActual.getNombrePasajero() != null ?
                solicitudActual.getNombrePasajero() : "Pasajero sin nombre");

        // Teléfono del pasajero
        String telefono = solicitudActual.getTelefonoPasajero();
        if (telefono != null) {
            if (!telefono.startsWith("+51")) {
                telefono = "+51 " + telefono;
            }
            telefonoPasajero.setText(telefono);
        } else {
            telefonoPasajero.setText("Sin teléfono");
        }

        // Número de pasajeros
        int numPasajeros = solicitudActual.getNumeroPasajeros();
        if (numPasajeros > 0) {
            if (numPasajeros == 1) {
                numeroPasajeros.setText("1 pasajero");
            } else {
                numeroPasajeros.setText(numPasajeros + " pasajeros");
            }
        } else {
            numeroPasajeros.setText("No especificado");
        }

        // Tipo de vehículo
        tipoVehiculo.setText(solicitudActual.getTipoVehiculo() != null ?
                solicitudActual.getTipoVehiculo() : "No especificado");

        // Notas
        String notas = solicitudActual.getNotas();
        if (notas != null && !notas.isEmpty() &&
                !notas.equals("Solicitud generada automáticamente")) {
            notasContenido.setText(notas);
        } else {
            notasContenido.setText("Sin notas adicionales");
        }

        // Cargar foto de perfil genérica
        Glide.with(this)
                .load(R.drawable.user_icon)
                .circleCrop()
                .into(iv_foto_perfil);
    }

    private void mostrarDatosViaje() {
        // Origen
        nombreOrigen.setText(solicitudActual.getOrigen() != null ?
                solicitudActual.getOrigen() : "Origen no especificado");

        direccionOrigen.setText(solicitudActual.getOrigenDireccion() != null ?
                solicitudActual.getOrigenDireccion() : "Dirección no disponible");

        // Destino
        nombreDestino.setText(solicitudActual.getDestino() != null ?
                solicitudActual.getDestino() : "Destino no especificado");

        direccionDestino.setText(solicitudActual.getDestinoDireccion() != null ?
                solicitudActual.getDestinoDireccion() : "Dirección no disponible");

        // Fecha y hora
        if (solicitudActual.getFechaSalida() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM yyyy - HH:mm",
                    new Locale("es", "ES"));
            fechaHora.setText(sdf.format(solicitudActual.getFechaSalida()));
        } else {
            fechaHora.setText("Fecha no especificada");
        }

        // Distancia
        if (solicitudActual.getDistanciaTexto() != null) {
            distanciaViaje.setText(solicitudActual.getDistanciaTexto());
        } else if (solicitudActual.getDistanciaKm() > 0) {
            distanciaViaje.setText(String.format("%.1f km", solicitudActual.getDistanciaKm()));
        } else {
            distanciaViaje.setText("Calculando...");
        }

        // Tiempo
        if (solicitudActual.getTiempoTexto() != null) {
            tiempoViaje.setText(solicitudActual.getTiempoTexto());
        } else if (solicitudActual.getTiempoEstimadoMin() > 0) {
            tiempoViaje.setText(solicitudActual.getTiempoEstimadoMin() + " min");
        } else {
            tiempoViaje.setText("Calculando...");
        }
    }

    private void cargarDatosHotel(String hotelId) {
        Log.d(TAG, "Cargando datos del hotel: " + hotelId);

        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Hotel encontrado");

                        // Nombre del hotel
                        String nombre = documentSnapshot.getString("nombre");
                        tv_hotel_nombre.setText(nombre != null ? nombre : "Hotel sin nombre");

                        // Dirección del hotel
                        String direccion = documentSnapshot.getString("ubicacion");
                        if (direccion == null) {
                            direccion = documentSnapshot.getString("direccion");
                        }
                        tv_hotel_direccion.setText(direccion != null ? direccion : "Dirección no disponible");

                        // Rating del hotel - primero intentar desde la solicitud, luego desde el hotel
                        String rating = solicitudActual.getCalificacionHotel();
                        if (rating == null || rating.isEmpty()) {
                            rating = documentSnapshot.getString("calificacion");
                        }
                        tv_rating.setText(rating != null ? rating : "4.0");

                    } else {
                        Log.w(TAG, "Hotel no encontrado con ID: " + hotelId);
                        mostrarDatosHotelPorDefecto();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos del hotel: " + e.getMessage());
                    mostrarDatosHotelPorDefecto();
                });
    }

    private void mostrarDatosHotelPorDefecto() {
        tv_hotel_nombre.setText("Hotel no disponible");
        tv_hotel_direccion.setText("Información del hotel no disponible");
        tv_rating.setText("4.0");
    }

    // Métodos del ciclo de vida del MapView
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
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