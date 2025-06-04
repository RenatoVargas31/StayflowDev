package com.iot.stayflowdev.Driver.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.iot.stayflowdev.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;

public class DriverMapaActivity extends AppCompatActivity {

    private TextView destinationNameTextView;
    private TextView distanceValueTextView;
    private TextView remainingDistanceTextView;
    private TextView arrivalTimeTextView;
    private Button startTripButton;
    private Button contactPassengerButton;
    private BottomNavigationView bottomNavigation;

    private MapView mapView;
    private FloatingActionButton fabCurrentLocation;
    private TextView routeText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_mapa);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Inicializar vistas
        inicializarVistas();

        // Configurar navegación con debug
        configurarBottomNavigationConDebug();

        // Resto de configuraciones
        configurarDatosEjemplo();
        configurarBotones();
        configurarMapa();
    }

    private void inicializarVistas() {
        destinationNameTextView = findViewById(R.id.destination_name);
        distanceValueTextView = findViewById(R.id.distance_value);
        remainingDistanceTextView = findViewById(R.id.remaining_distance);
        arrivalTimeTextView = findViewById(R.id.arrival_time);
        startTripButton = findViewById(R.id.btn_start_trip);
        contactPassengerButton = findViewById(R.id.btn_contact_passenger);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Nuevas vistas para Mapbox
        mapView = findViewById(R.id.mapView);
        fabCurrentLocation = findViewById(R.id.fab_current_location);
        routeText = findViewById(R.id.route_text);

        // ✅ VERIFICAR QUE SE ENCONTRÓ
        if (bottomNavigation == null) {
            Log.e("DriverMapaActivity", "❌ ERROR: bottomNavigation es null!");
        } else {
            Log.d("DriverMapaActivity", "✅ bottomNavigation encontrado correctamente");
        }

        // Verificar MapView
        if (mapView == null) {
            Log.e("DriverMapaActivity", "❌ ERROR: mapView es null!");
        } else {
            Log.d("DriverMapaActivity", "✅ mapView encontrado correctamente");
        }
    }

    // ✅ CONFIGURACIÓN CON DEBUG COMPLETO
    private void configurarBottomNavigationConDebug() {
        if (bottomNavigation == null) {
            Log.e("DriverMapaActivity", "❌ No se puede configurar: bottomNavigation es null");
            return;
        }

        // ✅ VERIFICAR QUE EL MENÚ SE CARGÓ
        if (bottomNavigation.getMenu() == null || bottomNavigation.getMenu().size() == 0) {
            Log.e("DriverMapaActivity", "❌ ERROR: El menú no se cargó correctamente");
            return;
        }

        // ✅ DEBUG: Mostrar todos los IDs del menú
        Log.d("DriverMapaActivity", "📋 Ítems del menú encontrados:");
        for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
            MenuItem item = bottomNavigation.getMenu().getItem(i);
            Log.d("DriverMapaActivity", "- Ítem " + i + ": ID=" + item.getItemId() + ", Título=" + item.getTitle());
        }

        // ✅ INTENTAR SELECCIONAR CON MULTIPLE VERIFICACIONES
        try {
            // Método 1: Selección directa
            bottomNavigation.setSelectedItemId(R.id.nav_mapa);
            Log.d("DriverMapaActivity", "✅ Intentando seleccionar nav_mapa con ID: " + R.id.nav_mapa);

            // ✅ VERIFICAR SI SE SELECCIONÓ CORRECTAMENTE
            MenuItem selectedItem = bottomNavigation.getMenu().findItem(R.id.nav_mapa);
            if (selectedItem != null) {
                Log.d("DriverMapaActivity", "✅ Ítem nav_mapa encontrado: " + selectedItem.getTitle());
                selectedItem.setChecked(true); // ✅ FORZAR SELECCIÓN
            } else {
                Log.e("DriverMapaActivity", "❌ ERROR: No se encontró el ítem nav_mapa");

                // ✅ MÉTODO ALTERNATIVO: Buscar por posición
                if (bottomNavigation.getMenu().size() >= 3) {
                    bottomNavigation.getMenu().getItem(2).setChecked(true); // Posición 2 = Mapa
                    Log.d("DriverMapaActivity", "✅ Seleccionado por posición (ítem 2)");
                }
            }

        } catch (Exception e) {
            Log.e("DriverMapaActivity", "❌ ERROR al seleccionar ítem: " + e.getMessage());
        }

        // ✅ USAR POST PARA ASEGURAR QUE SE APLIQUE DESPUÉS DEL LAYOUT
        bottomNavigation.post(() -> {
            bottomNavigation.setSelectedItemId(R.id.nav_mapa);
            Log.d("DriverMapaActivity", "✅ Selección aplicada con post()");
        });

        // Configurar listener
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("DriverMapaActivity", "🔄 Ítem seleccionado: " + itemId);

            if (itemId == R.id.nav_mapa) {
                Log.d("DriverMapaActivity", "✅ Ya en Mapa");
                return true;
            } else if (itemId == R.id.nav_inicio) {
                Log.d("DriverMapaActivity", "🏠 Navegando a Inicio");
                navegarSinAnimacion(DriverInicioActivity.class);
                return true;
            } else if (itemId == R.id.nav_reservas) {
                Log.d("DriverMapaActivity", "📋 Navegando a Reservas");
                navegarSinAnimacion(DriverReservaActivity.class);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                Log.d("DriverMapaActivity", "👤 Navegando a Perfil");
                navegarSinAnimacion(DriverPerfilActivity.class);
                return true;
            }

            return false;
        });
    }

    private void configurarDatosEjemplo() {
        if (destinationNameTextView != null) {
            destinationNameTextView.setText("Hotel Monte Claro");
        }
        if (distanceValueTextView != null) {
            distanceValueTextView.setText("5 km");
        }
        if (remainingDistanceTextView != null) {
            remainingDistanceTextView.setText("-2 km");
        }
        if (arrivalTimeTextView != null) {
            arrivalTimeTextView.setText("15 mins");
        }
    }

    private void configurarBotones() {
        if (startTripButton != null) {
            startTripButton.setOnClickListener(v -> {
                Toast.makeText(this, "Iniciando viaje...", Toast.LENGTH_SHORT).show();
                // Aquí podrías iniciar el tracking del viaje en el mapa
                iniciarViaje();
            });
        }

        if (contactPassengerButton != null) {
            contactPassengerButton.setOnClickListener(v -> {
                Toast.makeText(this, "Contactando al pasajero...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, DriverChatActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        // Configurar botón de ubicación actual
        if (fabCurrentLocation != null) {
            fabCurrentLocation.setOnClickListener(v -> {
                centrarEnUbicacionActual();
            });
        }
    }

    // ✅ CONFIGURACIÓN COMPLETA DEL MAPA CON MAPBOX
    private void configurarMapa() {
        if (mapView == null) {
            Log.e("DriverMapaActivity", "❌ No se puede configurar: mapView es null");
            return;
        }

        Log.d("DriverMapaActivity", "🗺️ Configurando Mapbox...");

        try {
            // Configurar la cámara inicial centrada en Lima, Perú
            CameraOptions initialCamera = new CameraOptions.Builder()
                    .center(Point.fromLngLat(-77.0428, -12.0464)) // Lima, Perú
                    .zoom(12.0)
                    .pitch(0.0)
                    .bearing(0.0)
                    .build();

            // Aplicar la configuración de cámara
            mapView.getMapboxMap().setCamera(initialCamera);

            // Cargar el estilo del mapa
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                Log.d("DriverMapaActivity", "✅ Estilo del mapa cargado correctamente");
                // Aquí puedes agregar marcadores, rutas, etc.
                configurarElementosDelMapa();
            });

            Log.d("DriverMapaActivity", "✅ Mapa configurado exitosamente");

        } catch (Exception e) {
            Log.e("DriverMapaActivity", "❌ Error al configurar el mapa: " + e.getMessage());
        }
    }

    private void configurarElementosDelMapa() {
        // Aquí puedes agregar:
        // - Marcadores para origen y destino
        // - Ruta entre puntos
        // - Ubicación actual del conductor
        Log.d("DriverMapaActivity", "🎯 Configurando elementos del mapa (marcadores, rutas, etc.)");

        // Actualizar el texto de la ruta
        if (routeText != null) {
            routeText.setText("Ruta hacia Hotel Monte Claro");
        }
    }

    private void iniciarViaje() {
        Log.d("DriverMapaActivity", "🚗 Iniciando viaje - actualizando mapa");
        // Aquí puedes:
        // - Cambiar el estilo del mapa
        // - Iniciar tracking de ubicación
        // - Mostrar navegación paso a paso
        if (routeText != null) {
            routeText.setText("Viaje en progreso - Sigue la ruta");
        }
    }

    private void centrarEnUbicacionActual() {
        Log.d("DriverMapaActivity", "📍 Centrando en ubicación actual");
        // Aquí implementarías la lógica para obtener la ubicación actual
        // y centrar el mapa en esa posición
        Toast.makeText(this, "Centrando en ubicación actual...", Toast.LENGTH_SHORT).show();
    }

    // ✅ LLAMAR EN onResume PARA ASEGURAR SELECCIÓN
    @Override
    protected void onResume() {
        super.onResume();
        // Asegurar que el ítem correcto esté seleccionado cuando se regresa a la actividad
        if (bottomNavigation != null) {
            bottomNavigation.post(() -> {
                bottomNavigation.setSelectedItemId(R.id.nav_mapa);
                Log.d("DriverMapaActivity", "✅ Selección verificada en onResume()");
            });
        }
    }

    // ✅ CICLO DE VIDA DEL MAPA - MUY IMPORTANTE
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
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    private void navegarSinAnimacion(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navegarSinAnimacion(DriverInicioActivity.class);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navegarSinAnimacion(DriverInicioActivity.class);
        return true;
    }
}