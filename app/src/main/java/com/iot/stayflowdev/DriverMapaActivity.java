package com.iot.stayflowdev;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DriverMapaActivity extends BaseActivity {

    private TextView destinationNameTextView;
    private TextView distanceValueTextView;
    private TextView remainingDistanceTextView;
    private TextView arrivalTimeTextView;
    private Button startTripButton;
    private Button contactPassengerButton;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_driver_mapa;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_mapa;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Inicializar vistas
        initViews();

        // Configurar datos de ejemplo (en una app real, estos vendrían de tu backend)
        setupExampleData();

        // Configurar listeners de botones
        setupButtonListeners();

        // Aquí se cargaría el mapa real (Google Maps o similar)
        // En una implementación real se podría usar un MapFragment
        setupMap();


    }

    private void initViews() {
        destinationNameTextView = findViewById(R.id.destination_name);
        distanceValueTextView = findViewById(R.id.distance_value);
        remainingDistanceTextView = findViewById(R.id.remaining_distance);
        arrivalTimeTextView = findViewById(R.id.arrival_time);
        startTripButton = findViewById(R.id.btn_start_trip);
        contactPassengerButton = findViewById(R.id.btn_contact_passenger);
    }

    private void setupExampleData() {
        // En una app real, estos datos vendrían de tu backend o servicio
        destinationNameTextView.setText("Hotel Monte Claro");
        distanceValueTextView.setText("5 km");
        remainingDistanceTextView.setText("-2 km");
        arrivalTimeTextView.setText("15 mins");
    }

    private void setupButtonListeners() {
        startTripButton.setOnClickListener(v -> {
            // Lógica para iniciar el viaje
            Toast.makeText(this, "Iniciando viaje...", Toast.LENGTH_SHORT).show();
            // Aquí podrías iniciar un servicio de ubicación en tiempo real
        });

        contactPassengerButton.setOnClickListener(v -> {
            // Lógica para contactar al pasajero
            Toast.makeText(this, "Contactando al pasajero...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DriverChatActivity.class));
        });
    }

    private void setupMap() {
        // En una implementación real, aquí se configuraría el mapa
        // Por ejemplo, usando Google Maps o alguna otra biblioteca de mapas

        // Ejemplo con Google Maps (necesitarías agregar las dependencias apropiadas)
        /*
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        */
    }
}

// Si decides usar Google Maps, implementarías OnMapReadyCallback
    /*
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Configurar el mapa, agregar marcadores, dibujar ruta, etc.
        LatLng destination = new LatLng(-12.0464, -77.0428); // Coordenadas de ejemplo
        googleMap.addMarker(new MarkerOptions().position(destination).title("Hotel Monte Claro"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));

        // Dibujar ruta desde ubicación actual hasta destino
        // Esto requeriría usar la API de Directions de Google Maps
    }

}
}
     */