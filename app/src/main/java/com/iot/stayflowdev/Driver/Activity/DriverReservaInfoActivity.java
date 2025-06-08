package com.iot.stayflowdev.Driver.Activity;
import android.content.Intent;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.iot.stayflowdev.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DriverReservaInfoActivity extends AppCompatActivity {
    // Elementos del UI - Card Pasajero
    private ShapeableImageView iv_foto_perfil;
    private MaterialTextView nombrePasajero;
    private MaterialTextView telefonoPasajero;
    private MaterialTextView numeroPasajeros;
    private MaterialTextView tipoVehiculo;
    private MaterialTextView notasContenido;

    // Elementos del UI - Card Viaje
    private MaterialTextView nombreOrigen;
    private MaterialTextView direccionOrigen;
    private MaterialTextView nombreDestino;
    private MaterialTextView direccionDestino;
    private MaterialTextView fechaHora;
    private MaterialTextView distanciaViaje;
    private MaterialTextView tiempoViaje;

    // Elementos del UI - Card Hotel
    private ShapeableImageView iv_hotel_image;
    private MaterialTextView tv_rating;
    private MaterialTextView tv_hotel_nombre;
    private MaterialTextView tv_hotel_direccion;
    private MaterialTextView tv_distancia;
    private MaterialButton btn_ver_mapa;

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

        // Inicializar toolbar
        initToolbar();

        initViews();
        setupListeners();
        cargarDatosDesdeFirebase();
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

        // Card Hotel
        iv_hotel_image = findViewById(R.id.iv_hotel_image);
        tv_rating = findViewById(R.id.tv_rating);
        tv_hotel_nombre = findViewById(R.id.tv_hotel_nombre);
        tv_hotel_direccion = findViewById(R.id.tv_hotel_direccion);
        tv_distancia = findViewById(R.id.tv_distancia);
        btn_ver_mapa = findViewById(R.id.btn_ver_mapa);
    }
    private void setupListeners() {
        // Listener del botón para abrir mapa
        btn_ver_mapa.setOnClickListener(v -> {
            Intent intent = new Intent(DriverReservaInfoActivity.this, DriverMapaActivity.class);
            // Puedes pasar datos adicionales aquí
            // intent.putExtra("HOTEL_LAT", hotelLatitud);
            // intent.putExtra("HOTEL_LNG", hotelLongitud);
            startActivity(intent);
        });
    }
    private void cargarDatosDesdeFirebase() {
        // Obtener el ID del documento desde el Intent
        String solicitudId = getIntent().getStringExtra("SOLICITUD_ID");
        if (solicitudId == null || solicitudId.isEmpty()) {
            Log.e("DriverReservaInfo", "No se recibió SOLICITUD_ID");
            Toast.makeText(this, "Error: No se pudo cargar la información", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("solicitudesTaxi")
                .document(solicitudId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Cargar datos del pasajero
                        cargarDatosPasajero(documentSnapshot);

                        // Cargar datos del viaje
                        cargarDatosViaje(documentSnapshot);

                        // Cargar datos del hotel usando idHotel
                        String idHotel = documentSnapshot.getString("idHotel");
                        if (idHotel != null) {
                            cargarDatosHotel(idHotel.replace("hoteles/", ""));
                        }
                    } else {
                        Log.w("DriverReservaInfo", "Documento no encontrado");
                        Toast.makeText(this, "No se encontró la información de la solicitud", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DriverReservaInfo", "Error al cargar datos: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarDatosPasajero(DocumentSnapshot doc) {
        // Nombre del pasajero
        String nombre = doc.getString("nombrePasajero");
        if (nombre != null) {
            nombrePasajero.setText(nombre);
        }

        // Teléfono del pasajero
        String telefono = doc.getString("telefonoPasajero");
        if (telefono != null) {
            // Agregar formato +51 si no lo tiene
            if (!telefono.startsWith("+51")) {
                telefono = "+51 " + telefono;
            }
            telefonoPasajero.setText(telefono);
        }

        // Número de pasajeros
        Long numPasajeros = doc.getLong("numeroPasajeros");
        if (numPasajeros != null) {
            numeroPasajeros.setText(numPasajeros + " pasajeros");
        }

        // Tipo de vehículo
        String tipoVeh = doc.getString("tipoVehiculo");
        if (tipoVeh != null) {
            tipoVehiculo.setText(tipoVeh);
        }

        // Notas
        String notas = doc.getString("notas");
        if (notas != null) {
            notasContenido.setText(notas);
        }
    }

    private void cargarDatosViaje(DocumentSnapshot doc) {
        // Origen
        String origen = doc.getString("origen");
        if (origen != null) {
            nombreOrigen.setText(origen);
        }

        String origenDir = doc.getString("origenDireccion");
        if (origenDir != null) {
            direccionOrigen.setText(origenDir);
        }

        // Destino
        String destino = doc.getString("destino");
        if (destino != null) {
            nombreDestino.setText(destino);
        }

        String destinoDir = doc.getString("destinoDireccion");
        if (destinoDir != null) {
            direccionDestino.setText(destinoDir);
        }

        // Fecha y hora
        Timestamp fechaSalida = doc.getTimestamp("fechaSalida");
        if (fechaSalida != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd 'de' MMMM yyyy - HH:mm", new Locale("es", "ES"));
            fechaHora.setText(sdf.format(fechaSalida.toDate()));
        }

        // Distancia y tiempo (valores por defecto ya que no están en Firebase)
        // Estos campos se pueden llenar cuando implementes cálculo de rutas
        distanciaViaje.setText("Calculando...");
        tiempoViaje.setText("Calculando...");
    }

    private void cargarDatosHotel(String hotelId) {
        FirebaseFirestore.getInstance()
                .collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Nombre del hotel
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null) {
                            tv_hotel_nombre.setText(nombre);
                        }

                        // Dirección del hotel
                        String direccion = documentSnapshot.getString("ubicacion");
                        if (direccion != null) {
                            tv_hotel_direccion.setText(direccion);
                        }

                        // Rating del hotel
                        String rating = documentSnapshot.getString("calificacion");
                        if (rating != null) {
                            tv_rating.setText(rating);
                        }

                        // Cargar imagen del hotel si existe
                        String urlImagen = documentSnapshot.getString("imagenUrl");
                        if (urlImagen != null && !urlImagen.isEmpty()) {
                            // TODO: Implementar carga de imagen con Glide
                            // Glide.with(this).load(urlImagen).into(iv_hotel_image);
                        }
                    } else {
                        Log.w("DriverReservaInfo", "Hotel no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DriverReservaInfo", "Error al cargar datos del hotel: " + e.getMessage());
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