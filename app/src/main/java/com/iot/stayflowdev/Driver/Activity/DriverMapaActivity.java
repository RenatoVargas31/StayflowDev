package com.iot.stayflowdev.Driver.Activity;

import android.content.Intent;
import android.location.LocationManager;
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
import com.iot.stayflowdev.Driver.Helper.LocationHelper;
import com.iot.stayflowdev.R;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


public class DriverMapaActivity extends AppCompatActivity implements LocationHelper.LocationUpdateListener{

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

    // ✅ NUEVA VARIABLE PARA EL HELPER DE UBICACIÓN
    private LocationHelper locationHelper;
    private boolean locationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean autoFollowLocation = true; // TRUE = sigue automáticamente tu ubicación
    private boolean isFirstLocationUpdate = true; // Para el zoom inicial

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

        locationHelper = new LocationHelper(this, this);
        // VERIFICAR PERMISOS
        verificarPermisosUbicacion();

        // Inicializar vistas
        inicializarVistas();
        configurarBottomNavigationConDebug();
        configurarDatosEjemplo();
        configurarBotones();
        configurarMapa();
    }

    private void verificarPermisosUbicacion() {
        if (locationHelper.hasLocationPermission()) {
            locationPermissionGranted = true;
            Log.d("DriverMapaActivity", "✅ Permisos de ubicación concedidos");
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                Log.d("DriverMapaActivity", "✅ Permisos concedidos por el usuario");
                // Iniciar ubicación después de obtener permisos
                iniciarUbicacionTiempoReal();
            } else {
                locationPermissionGranted = false;
                Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ✅ CONFIGURACIÓN DEL MAPA (IGUAL QUE ANTES)
    private void configurarMapa() {
        if (mapView == null) {
            Log.e("DriverMapaActivity", "❌ MapView es null");
            return;
        }

        Log.d("DriverMapaActivity", "🗺️ Configurando Mapbox...");

        try {
            CameraOptions initialCamera = new CameraOptions.Builder()
                    .center(Point.fromLngLat(-77.0428, -12.0464)) // Lima
                    .zoom(15.0)
                    .pitch(0.0)
                    .bearing(0.0)
                    .build();

            mapView.getMapboxMap().setCamera(initialCamera);

            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                Log.d("DriverMapaActivity", "✅ Estilo del mapa cargado");

                // INICIAR UBICACIÓN DESPUÉS DE CARGAR EL MAPA
                if (locationPermissionGranted) {
                    iniciarUbicacionTiempoReal();
                }

                configurarElementosDelMapa();
            });

        } catch (Exception e) {
            Log.e("DriverMapaActivity", "❌ Error configurando mapa: " + e.getMessage());
        }
    }

    // ✅ UBICACIÓN EN TIEMPO REAL - VERSIÓN NATIVA DE ANDROID
    private void iniciarUbicacionTiempoReal() {
        if (locationHelper == null) {
            Log.e("DriverMapaActivity", "❌ LocationHelper es null");
            return;
        }

        // Mostrar estado inicial
        mostrarEstadoInicial();

        // Iniciar con el helper
        boolean success = locationHelper.startLocationTracking();
        if (!success) {
            Log.e("DriverMapaActivity", "❌ No se pudo iniciar ubicación");
        }
    }

    @Override
    public void onLocationUpdate(Location location, String address, String timeString) {
        Log.d("DriverMapaActivity", "📍 Ubicación actualizada: " + address);

        // Actualizar el TextView con la información completa
        mostrarUbicacionConDireccion(location, address, timeString);

        // ✅ MOVER EL MAPA AUTOMÁTICAMENTE A TU UBICACIÓN
        if (autoFollowLocation) {
            moverMapaATuUbicacion(location);
        }
    }
    private void moverMapaATuUbicacion(Location location) {
        if (mapView == null || mapView.getMapboxMap() == null) {
            return;
        }

        try {
            Point userLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());

            // Configurar zoom según si es la primera vez o no
            double zoomLevel;
            if (isFirstLocationUpdate) {
                zoomLevel = 17.0; // Zoom más cercano la primera vez
                isFirstLocationUpdate = false;
            } else {
                // Mantener el zoom actual del usuario
                zoomLevel = mapView.getMapboxMap().getCameraState().getZoom();
                // Asegurar un zoom mínimo
                if (zoomLevel < 15.0) {
                    zoomLevel = 16.0;
                }
            }

            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(userLocation)
                    .zoom(zoomLevel)
                    .build();

            // ✅ MOVIMIENTO SUAVE Y AUTOMÁTICO
            mapView.getMapboxMap().setCamera(cameraOptions);

            Log.d("DriverMapaActivity", "✅ Mapa movido automáticamente a nueva ubicación");

        } catch (Exception e) {
            Log.e("DriverMapaActivity", "❌ Error moviendo mapa: " + e.getMessage());
        }
    }

    // ✅ IMPLEMENTAR INTERFACE LocationUpdateListener - ERROR DE UBICACIÓN
    @Override
    public void onLocationError(String error) {
        Log.e("DriverMapaActivity", "❌ Error de ubicación: " + error);

        if (routeText != null) {
            runOnUiThread(() -> {
                routeText.setText("❌ " + error + "\n🚗 Ruta hacia Hotel Monte Claro");
            });
        }

        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    // ✅ IMPLEMENTAR INTERFACE LocationUpdateListener - CAMBIO DE PROVEEDOR
    @Override
    public void onProviderStateChanged(String provider, boolean enabled) {
        String message = provider + (enabled ? " habilitado" : " deshabilitado");
        Log.d("DriverMapaActivity", "🔄 " + message);

        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            if (enabled) {
                mostrarEstadoInicial();
                Toast.makeText(this, "GPS habilitado", Toast.LENGTH_SHORT).show();
            } else {
                if (routeText != null) {
                    runOnUiThread(() -> {
                        routeText.setText("❌ GPS desactivado\nActiva el GPS para obtener ubicación");
                    });
                }
                Toast.makeText(this, "GPS deshabilitado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ MOSTRAR UBICACIÓN CON DIRECCIÓN REAL EN EL TEXTVIEW
    private void mostrarUbicacionConDireccion(Location location, String direccion, String hora) {
        if (routeText != null) {
            String ubicacionTexto = String.format(
                    "📍%s\n🎯 Precisión: %.0fm | 🕐 %s",
                    direccion,
                    location.getAccuracy(),
                    hora
            );

            runOnUiThread(() -> {
                routeText.setText(ubicacionTexto);
            });
        }
    }

    // ✅ MOSTRAR ESTADO INICIAL
    private void mostrarEstadoInicial() {
        if (routeText != null) {
            runOnUiThread(() -> {
                routeText.setText("📡 Obteniendo tu ubicación actual...");
            });
        }
    }

    // ✅ GESTIÓN DEL CICLO DE VIDA SIMPLIFICADA
    @Override
    protected void onResume() {
        super.onResume();

        // Tu código existente para bottomNavigation
        if (bottomNavigation != null) {
            bottomNavigation.post(() -> {
                bottomNavigation.setSelectedItemId(R.id.nav_mapa);
                Log.d("DriverMapaActivity", "✅ Selección verificada en onResume()");
            });
        }

        // Reanudar ubicación usando el helper
        if (locationPermissionGranted && locationHelper != null && !locationHelper.isTracking()) {
            Log.d("DriverMapaActivity", "🔄 Reanudando ubicación");
            locationHelper.startLocationTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Pausar ubicación usando el helper
        if (locationHelper != null) {
            locationHelper.stopLocationTracking();
            Log.d("DriverMapaActivity", "🔄 Ubicación pausada");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Limpiar recursos del helper
        if (locationHelper != null) {
            locationHelper.cleanup();
            locationHelper = null;
        }

        // Destruir mapa
        if (mapView != null) {
            mapView.onDestroy();
        }

        Log.d("DriverMapaActivity", "🗑️ Recursos liberados");
    }

    // ✅ RESTO DE TUS MÉTODOS EXISTENTES (SIN CAMBIOS)
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

    // ✅ CONFIGURACIÓN CON DEBUG COMPLETO (SIN CAMBIOS)
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

        // ✅ ACTUALIZAR EL BOTÓN FAB - AHORA CONTROLA EL SEGUIMIENTO AUTOMÁTICO
        if (fabCurrentLocation != null) {
            fabCurrentLocation.setOnClickListener(v -> {
                toggleAutoFollow();
            });
        }
    }
    private void toggleAutoFollow() {
        autoFollowLocation = !autoFollowLocation;

        if (autoFollowLocation) {
            // Activar seguimiento automático
            Toast.makeText(this, "🎯 Seguimiento automático activado", Toast.LENGTH_SHORT).show();

            // Inmediatamente centrar en ubicación actual
            if (locationHelper != null) {
                Point currentLocation = locationHelper.getCurrentLocation();
                if (currentLocation != null) {
                    CameraOptions cameraOptions = new CameraOptions.Builder()
                            .center(currentLocation)
                            .zoom(17.0)
                            .build();
                    mapView.getMapboxMap().setCamera(cameraOptions);
                }
            }

            // Cambiar ícono del FAB para mostrar que está activo
            // fabCurrentLocation.setImageResource(R.drawable.ic_gps_fixed); // Si tienes este ícono

        } else {
            // Desactivar seguimiento automático
            Toast.makeText(this, "📍 Seguimiento manual - mueve el mapa libremente", Toast.LENGTH_SHORT).show();

            // Cambiar ícono del FAB para mostrar que está inactivo
            // fabCurrentLocation.setImageResource(R.drawable.ic_location); // Ícono original
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