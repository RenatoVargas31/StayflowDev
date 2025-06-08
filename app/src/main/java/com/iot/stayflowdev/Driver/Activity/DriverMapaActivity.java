package com.iot.stayflowdev.Driver.Activity;

import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
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
    // ==================== VARIABLES DE DATOS DEL VIAJE ====================
    private String solicitudId;
    private String nombreDestino;
    private String direccionDestino;
    private String nombrePasajero;
    private String telefonoPasajero;

    // ==================== VARIABLES DE VISTAS ====================
    // Card de destino
    private TextView destination_name;
    private TextView direccion_name;
    private TextView distance_value;
    private TextView arrival_time;
    private MaterialButton btn_start_trip;
    private MaterialButton btn_contact_passenger;

    // Navegación
    private BottomNavigationView bottomNavigation;

    // Mapa
    private MapView mapView;
    private FloatingActionButton fabCurrentLocation;
    private TextView routeText;

    // ==================== VARIABLES DE UBICACIÓN ====================
    private LocationHelper locationHelper;
    private boolean locationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean autoFollowLocation = true;
    private boolean isFirstLocationUpdate = true;


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
        // Inicialización en orden
        initLocationHelper();
        initViews();
        cargarDatosSolicitud(); // NUEVO: Cargar datos de Firebase
        setupListeners();
        setupBottomNavigation();
        setupMap();
        verificarPermisosUbicacion();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reseleccionar item de navegación
        if (bottomNavigation != null) {
            bottomNavigation.post(() -> {
                bottomNavigation.setSelectedItemId(R.id.nav_mapa);
                Log.d("DriverMapaActivity", "✅ Selección verificada en onResume()");
            });
        }

        // Reanudar ubicación
        if (locationPermissionGranted && locationHelper != null && !locationHelper.isTracking()) {
            Log.d("DriverMapaActivity", "🔄 Reanudando ubicación");
            locationHelper.startLocationTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationHelper != null) {
            locationHelper.stopLocationTracking();
            Log.d("DriverMapaActivity", "🔄 Ubicación pausada");
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
    protected void onDestroy() {
        super.onDestroy();

        // Limpiar recursos
        if (locationHelper != null) {
            locationHelper.cleanup();
            locationHelper = null;
        }

        if (mapView != null) {
            mapView.onDestroy();
        }

        Log.d("DriverMapaActivity", "🗑️ Recursos liberados");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

// ==================== INICIALIZACIÓN ====================

    private void initLocationHelper() {
        locationHelper = new LocationHelper(this, this);
    }
    private void initViews() {
        // Card de destino
        destination_name = findViewById(R.id.destination_name);
        direccion_name = findViewById(R.id.direccion_name);
        distance_value = findViewById(R.id.distance_value);
        arrival_time = findViewById(R.id.arrival_time);
        btn_start_trip = findViewById(R.id.btn_start_trip);
        btn_contact_passenger = findViewById(R.id.btn_contact_passenger);

        // Navegación
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Mapa
        mapView = findViewById(R.id.mapView);
        fabCurrentLocation = findViewById(R.id.fab_current_location);
        routeText = findViewById(R.id.route_text);

        // Validaciones
        if (bottomNavigation == null) {
            Log.e("DriverMapaActivity", "❌ ERROR: bottomNavigation es null!");
        } else {
            Log.d("DriverMapaActivity", "✅ bottomNavigation encontrado correctamente");
        }

        if (mapView == null) {
            Log.e("DriverMapaActivity", "❌ ERROR: mapView es null!");
        } else {
            Log.d("DriverMapaActivity", "✅ mapView encontrado correctamente");
        }
    }

    // ==================== CARGA DE DATOS DESDE FIREBASE ====================

    private void cargarDatosSolicitud() {
        solicitudId = getIntent().getStringExtra("SOLICITUD_ID");

        if (solicitudId == null || solicitudId.isEmpty()) {
            Log.w("DriverMapa", "No se recibió SOLICITUD_ID, usando datos de ejemplo");
            configurarDatosEjemplo();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("solicitudesTaxi")
                .document(solicitudId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Obtener datos del destino
                        nombreDestino = documentSnapshot.getString("destino");
                        direccionDestino = documentSnapshot.getString("destinoDireccion");

                        // Obtener datos del pasajero
                        nombrePasajero = documentSnapshot.getString("nombrePasajero");
                        telefonoPasajero = documentSnapshot.getString("telefonoPasajero");

                        // Actualizar las vistas
                        actualizarVistaDestino();

                        Log.d("DriverMapa", "✅ Datos cargados desde Firebase");
                    } else {
                        Log.w("DriverMapa", "Documento no encontrado, usando datos de ejemplo");
                        configurarDatosEjemplo();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DriverMapa", "Error al cargar datos: " + e.getMessage());
                    configurarDatosEjemplo();
                });
    }

    private void actualizarVistaDestino() {
        if (nombreDestino != null && destination_name != null) {
            destination_name.setText(nombreDestino);
        }

        if (direccionDestino != null && direccion_name != null) {
            direccion_name.setText(direccionDestino);
        }

        // Los valores de distancia y tiempo se calculan después
        if (distance_value != null) {
            distance_value.setText("Calculando...");
        }
        if (arrival_time != null) {
            arrival_time.setText("Calculando...");
        }
    }

    private void configurarDatosEjemplo() {
        // Datos por defecto si no se reciben del Intent
        nombreDestino = "Hotel Monte Claro";
        direccionDestino = "Av. Ejemplo 123, Lima";
        nombrePasajero = "Pasajero Ejemplo";
        telefonoPasajero = "999888777";

        // Actualizar vistas con datos de ejemplo
        if (destination_name != null) {
            destination_name.setText(nombreDestino);
        }
        if (direccion_name != null) {
            direccion_name.setText(direccionDestino);
        }
        if (distance_value != null) {
            distance_value.setText("5 km");
        }
        if (arrival_time != null) {
            arrival_time.setText("15 mins");
        }
    }
// ==================== CONFIGURACIÓN DE LISTENERS ====================

    private void setupListeners() {
        setupTripButtons();
        setupLocationButton();
    }

    private void setupTripButtons() {
        if (btn_start_trip != null) {
            btn_start_trip.setOnClickListener(v -> {
                Toast.makeText(this, "Iniciando viaje...", Toast.LENGTH_SHORT).show();
                iniciarViaje();
            });
        }

        if (btn_contact_passenger != null) {
            btn_contact_passenger.setOnClickListener(v -> {
                contactarPasajero();
            });
        }
    }

    private void setupLocationButton() {
        if (fabCurrentLocation != null) {
            fabCurrentLocation.setOnClickListener(v -> {
                toggleAutoFollow();
            });
        }
    }

    // ==================== FUNCIONALIDAD DE BOTONES ====================

    private void iniciarViaje() {
        Log.d("DriverMapaActivity", "🚗 Iniciando viaje - actualizando mapa");
        if (routeText != null) {
            routeText.setText("Viaje en progreso - Sigue la ruta");
        }
    }

    private void contactarPasajero() {
        Toast.makeText(this, "Abriendo chat con el pasajero...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DriverChatActivity.class);

        // Pasar ID de la solicitud
        if (solicitudId != null) {
            intent.putExtra("SOLICITUD_ID", solicitudId);
        }

        // Pasar datos del pasajero para mostrar en el chat
        if (nombrePasajero != null) {
            intent.putExtra("NOMBRE_PASAJERO", nombrePasajero);
        }

        if (telefonoPasajero != null) {
            intent.putExtra("TELEFONO_PASAJERO", telefonoPasajero);
        }

        // Pasar datos del destino para contexto
        if (nombreDestino != null) {
            intent.putExtra("DESTINO", nombreDestino);
        }

        if (direccionDestino != null) {
            intent.putExtra("DIRECCION_DESTINO", direccionDestino);
        }

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void toggleAutoFollow() {
        autoFollowLocation = !autoFollowLocation;

        if (autoFollowLocation) {
            Toast.makeText(this, "🎯 Seguimiento automático activado", Toast.LENGTH_SHORT).show();

            // Centrar inmediatamente en ubicación actual
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
        } else {
            Toast.makeText(this, "📍 Seguimiento manual - mueve el mapa libremente", Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== CONFIGURACIÓN DEL MAPA ====================

    private void setupMap() {
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

                if (locationPermissionGranted) {
                    iniciarUbicacionTiempoReal();
                }

                configurarElementosDelMapa();
            });

        } catch (Exception e) {
            Log.e("DriverMapaActivity", "❌ Error configurando mapa: " + e.getMessage());
        }
    }

    private void configurarElementosDelMapa() {
        Log.d("DriverMapaActivity", "🎯 Configurando elementos del mapa (marcadores, rutas, etc.)");

        if (routeText != null) {
            String textoRuta = nombreDestino != null ?
                    "Ruta hacia " + nombreDestino :
                    "Ruta hacia destino";
            routeText.setText(textoRuta);
        }
    }

    // ==================== GESTIÓN DE PERMISOS Y UBICACIÓN ====================

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
                iniciarUbicacionTiempoReal();
            } else {
                locationPermissionGranted = false;
                Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void iniciarUbicacionTiempoReal() {
        if (locationHelper == null) {
            Log.e("DriverMapaActivity", "❌ LocationHelper es null");
            return;
        }

        mostrarEstadoInicial();

        boolean success = locationHelper.startLocationTracking();
        if (!success) {
            Log.e("DriverMapaActivity", "❌ No se pudo iniciar ubicación");
        }
    }

    // ==================== IMPLEMENTACIÓN DE LocationUpdateListener ====================

    @Override
    public void onLocationUpdate(Location location, String address, String timeString) {
        Log.d("DriverMapaActivity", "📍 Ubicación actualizada: " + address);

        mostrarUbicacionConDireccion(location, address, timeString);

        if (autoFollowLocation) {
            moverMapaATuUbicacion(location);
        }
    }

    @Override
    public void onLocationError(String error) {
        Log.e("DriverMapaActivity", "❌ Error de ubicación: " + error);

        if (routeText != null) {
            runOnUiThread(() -> {
                String textoError = "❌ " + error + "\n🚗 Ruta hacia " +
                        (nombreDestino != null ? nombreDestino : "destino");
                routeText.setText(textoError);
            });
        }

        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

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

    // ==================== MÉTODOS DE UTILIDAD PARA UBICACIÓN ====================

    private void moverMapaATuUbicacion(Location location) {
        if (mapView == null || mapView.getMapboxMap() == null) {
            return;
        }

        try {
            Point userLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());

            double zoomLevel;
            if (isFirstLocationUpdate) {
                zoomLevel = 17.0;
                isFirstLocationUpdate = false;
            } else {
                zoomLevel = mapView.getMapboxMap().getCameraState().getZoom();
                if (zoomLevel < 15.0) {
                    zoomLevel = 16.0;
                }
            }

            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(userLocation)
                    .zoom(zoomLevel)
                    .build();

            mapView.getMapboxMap().setCamera(cameraOptions);

            Log.d("DriverMapaActivity", "✅ Mapa movido automáticamente a nueva ubicación");

        } catch (Exception e) {
            Log.e("DriverMapaActivity", "❌ Error moviendo mapa: " + e.getMessage());
        }
    }

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

    private void mostrarEstadoInicial() {
        if (routeText != null) {
            runOnUiThread(() -> {
                routeText.setText("📡 Obteniendo tu ubicación actual...");
            });
        }
    }

    // ==================== NAVEGACIÓN ====================

    private void setupBottomNavigation() {
        if (bottomNavigation == null) {
            Log.e("DriverMapaActivity", "❌ No se puede configurar: bottomNavigation es null");
            return;
        }

        if (bottomNavigation.getMenu() == null || bottomNavigation.getMenu().size() == 0) {
            Log.e("DriverMapaActivity", "❌ ERROR: El menú no se cargó correctamente");
            return;
        }

        // Debug del menú
        Log.d("DriverMapaActivity", "📋 Ítems del menú encontrados:");
        for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
            MenuItem item = bottomNavigation.getMenu().getItem(i);
            Log.d("DriverMapaActivity", "- Ítem " + i + ": ID=" + item.getItemId() + ", Título=" + item.getTitle());
        }

        // Seleccionar item actual
        try {
            bottomNavigation.setSelectedItemId(R.id.nav_mapa);
            Log.d("DriverMapaActivity", "✅ Intentando seleccionar nav_mapa con ID: " + R.id.nav_mapa);

            MenuItem selectedItem = bottomNavigation.getMenu().findItem(R.id.nav_mapa);
            if (selectedItem != null) {
                Log.d("DriverMapaActivity", "✅ Ítem nav_mapa encontrado: " + selectedItem.getTitle());
                selectedItem.setChecked(true);
            } else {
                Log.e("DriverMapaActivity", "❌ ERROR: No se encontró el ítem nav_mapa");
                if (bottomNavigation.getMenu().size() >= 3) {
                    bottomNavigation.getMenu().getItem(2).setChecked(true);
                    Log.d("DriverMapaActivity", "✅ Seleccionado por posición (ítem 2)");
                }
            }

        } catch (Exception e) {
            Log.e("DriverMapaActivity", "❌ ERROR al seleccionar ítem: " + e.getMessage());
        }

        bottomNavigation.post(() -> {
            bottomNavigation.setSelectedItemId(R.id.nav_mapa);
            Log.d("DriverMapaActivity", "✅ Selección aplicada con post()");
        });

        // Listener de navegación
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