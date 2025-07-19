package com.iot.stayflowdev.Driver.Activity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.iot.stayflowdev.R;
import android.view.inputmethod.InputMethodManager;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DriverMapaActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LatLng currentLocation;
    private boolean isFirstLocation = true;
    private boolean mostroDialogoGps = false;
    private FloatingActionButton fabCurrentLocation;
    private BottomNavigationView bottomNavigation;
    private static final int LOCATION_REQUEST_CODE = 1;
    // ==================== NUEVAS VARIABLES PARA CARD DE DESTINO ====================
    private CardView destinationCard;
    private LinearLayout destinationCardContainer;
    private LinearLayout collapsedContent;
    private LinearLayout expandedContent;
    private TextView destinationName;
    private TextView direccionName;
    private TextView arrivalTime;
    private TextView arrivalTimeDetailed;
    private TextView distanceValue;
    private TextView estimatedCost;
    private TextView remainingDistance;
    private ImageButton btnCloseDestination;
    private MaterialButton btnStartTrip;
    private MaterialButton btnContactPassenger;
    private MaterialButton btnCallPassenger;
    private TextInputEditText searchInput;

    // Variables para manejo de destino
    private LatLng destinationLocation;
    private String destinationAddress;
    private boolean isCardExpanded = false;
    private Marker destinationMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_mapa);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeViews();
        setupClickListeners();
        createLocationRequest();
        createLocationCallback();
        configurarBottomNavigation();
        initializeMap();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabCurrentLocation = findViewById(R.id.fab_current_location);
        destinationCard = findViewById(R.id.destination_card);
        destinationCardContainer = findViewById(R.id.destination_card_container);
        collapsedContent = findViewById(R.id.collapsed_content);
        expandedContent = findViewById(R.id.expanded_content);
        destinationName = findViewById(R.id.destination_name);
        direccionName = findViewById(R.id.direccion_name);
        arrivalTime = findViewById(R.id.arrival_time);
        arrivalTimeDetailed = findViewById(R.id.arrival_time_detailed);
        distanceValue = findViewById(R.id.distance_value);
        estimatedCost = findViewById(R.id.estimated_cost);
        remainingDistance = findViewById(R.id.remaining_distance);
        btnCloseDestination = findViewById(R.id.btn_close_destination);
        btnStartTrip = findViewById(R.id.btn_start_trip);
        btnContactPassenger = findViewById(R.id.btn_contact_passenger);
        btnCallPassenger = findViewById(R.id.btn_call_passenger);
        searchInput = findViewById(R.id.search_input);

        // Inicialmente ocultar el card
        hideDestinationCard();
    }

    private void setupClickListeners() {
        fabCurrentLocation.setOnClickListener(v -> {
            if (currentLocation != null && mMap != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
            else
                showToast("Obteniendo ubicación...");
        });
        // Toggle expandir/colapsar card
        collapsedContent.setOnClickListener(v -> toggleCardExpansion());

        // Cerrar destino
        btnCloseDestination.setOnClickListener(v -> clearDestination());

        // Iniciar viaje
        btnStartTrip.setOnClickListener(v -> startTrip());

        // Contactar pasajero
        btnContactPassenger.setOnClickListener(v -> openChat());

        // Llamar pasajero
        btnCallPassenger.setOnClickListener(v -> callPassenger());

        // Búsqueda de destino
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                String searchText = v.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    // Ocultar teclado
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    // Buscar destino
                    searchByCoordinates(searchText);
                }
                return true;
            }
            return false;
        });

        // Click en el mapa para seleccionar destino
        setupMapClickListener();
    }
// ==================== MÉTODOS PARA MANEJO DEL CARD DE DESTINO ====================

    private void showDestinationCard() {
        destinationCard.setVisibility(View.VISIBLE);
        destinationCard.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void hideDestinationCard() {
        destinationCard.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> destinationCard.setVisibility(View.GONE))
                .start();

        // Colapsar si estaba expandido
        if (isCardExpanded) {
            collapseCard();
        }
    }

    private void toggleCardExpansion() {
        if (isCardExpanded) {
            collapseCard();
        } else {
            expandCard();
        }
    }

    private void expandCard() {
        expandedContent.setVisibility(View.VISIBLE);
        expandedContent.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        isCardExpanded = true;
    }

    private void collapseCard() {
        expandedContent.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    expandedContent.setVisibility(View.GONE);
                    isCardExpanded = false;
                })
                .start();
    }

    private void setDestination(LatLng destination, String address) {
        this.destinationLocation = destination;
        this.destinationAddress = address;

        // Agregar marcador en el mapa
        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title("Destino")
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Actualizar información del card
        updateDestinationInfo(address);

        // Mostrar card
        showDestinationCard();

        // Calcular ruta si tenemos ubicación actual
        if (currentLocation != null) {
            calculateRoute();
        }

        // Ajustar cámara para mostrar origen y destino
        adjustCameraToShowBothPoints();
    }

    private void updateDestinationInfo(String address) {
        // Extraer nombre del lugar y dirección
        String[] addressParts = address.split(",");
        String placeName = addressParts.length > 0 ? addressParts[0].trim() : "Destino";

        destinationName.setText(placeName);
        direccionName.setText(address);

        // Valores por defecto mientras se calculan
        arrivalTime.setText("-- mins");
        arrivalTimeDetailed.setText("-- mins");
        distanceValue.setText("-- km");
        estimatedCost.setText("S/ --");
        remainingDistance.setText("Calculando ruta...");
    }

    private void calculateRoute() {
        if (currentLocation == null || destinationLocation == null) return;
        // Aquí implementarías la lógica para calcular la ruta
        // Por ahora, simularemos con datos de ejemplo
        simulateRouteCalculation();
    }

    private void simulateRouteCalculation() {
        // Simulación de cálculo de ruta
        new Handler().postDelayed(() -> {
            if (currentLocation != null && destinationLocation != null) {
                // Calcular distancia aproximada
                float[] results = new float[1];
                Location.distanceBetween(
                        currentLocation.latitude, currentLocation.longitude,
                        destinationLocation.latitude, destinationLocation.longitude,
                        results
                );

                float distanceKm = results[0] / 1000;
                int estimatedMinutes = Math.round(distanceKm * 2.5f); // Aproximación
                double estimatedCostValue = distanceKm * 1.5 + 3.0; // Tarifa base + km

                // Actualizar UI
                runOnUiThread(() -> {
                    distanceValue.setText(String.format(Locale.getDefault(), "%.1f km", distanceKm));
                    arrivalTime.setText(String.format(Locale.getDefault(), "%d mins", estimatedMinutes));
                    arrivalTimeDetailed.setText(String.format(Locale.getDefault(), "%d mins", estimatedMinutes));
                    estimatedCost.setText(String.format(Locale.getDefault(), "S/ %.2f", estimatedCostValue));
                    remainingDistance.setText("Ruta calculada - Listo para iniciar");
                });
            }
        }, 2000);
    }

    private void adjustCameraToShowBothPoints() {
        if (currentLocation != null && destinationLocation != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLocation);
            builder.include(destinationLocation);

            LatLngBounds bounds = builder.build();
            int padding = 200; // Padding en pixels

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    private void clearDestination() {
        destinationLocation = null;
        destinationAddress = null;

        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }

        hideDestinationCard();

        // Limpiar campo de búsqueda
        searchInput.setText("");

        showToast("Destino eliminado");
    }

    // ==================== MÉTODOS DE ACCIÓN DEL CARD ====================

    private void startTrip() {
        if (destinationLocation == null) {
            showToast("No hay destino seleccionado");
            return;
        }

        // Aquí implementarías la lógica para iniciar el viaje
        showToast("Iniciando viaje...");

        // Ejemplo: Cambiar el botón para indicar viaje en curso
        btnStartTrip.setText("Viaje en curso");
        btnStartTrip.setEnabled(false);
    }

    private void openChat() {
        // Implementar apertura del chat
        showToast("Abriendo chat...");
    }

    private void callPassenger() {
        // Implementar llamada al pasajero
        showToast("Llamando al pasajero...");
    }

    // ==================== BÚSQUEDA Y SELECCIÓN DE DESTINO ====================

    private void searchDestination(String query) {
        if (query.trim().isEmpty()) {
            showToast("Ingresa una dirección o lugar");
            return;
        }
        showToast("Buscando: " + query);
        remainingDistance.setText("Buscando ubicación...");
        // Usar Geocoder para buscar la dirección
        searchWithGeocoder(query);
    }
    private void searchWithGeocoder(String query) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 5);

                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        // Tomar la primera dirección encontrada
                        Address address = addresses.get(0);
                        LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                        // Construir dirección legible
                        StringBuilder addressText = new StringBuilder();

                        // Agregar nombre del lugar si existe
                        if (address.getFeatureName() != null && !address.getFeatureName().equals(address.getSubThoroughfare())) {
                            addressText.append(address.getFeatureName()).append(", ");
                        }

                        // Agregar dirección
                        if (address.getAddressLine(0) != null) {
                            addressText.append(address.getAddressLine(0));
                        } else {
                            // Construir dirección manualmente
                            if (address.getThoroughfare() != null) {
                                addressText.append(address.getThoroughfare());
                                if (address.getSubThoroughfare() != null) {
                                    addressText.append(" ").append(address.getSubThoroughfare());
                                }
                                addressText.append(", ");
                            }
                            if (address.getSubLocality() != null) {
                                addressText.append(address.getSubLocality()).append(", ");
                            }
                            if (address.getLocality() != null) {
                                addressText.append(address.getLocality());
                            }
                        }

                        setDestination(location, addressText.toString());
                        showToast("Ubicación encontrada");

                    } else {
                        showToast("No se encontró la ubicación. Intenta con una dirección más específica");
                        remainingDistance.setText("Ubicación no encontrada");
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    showToast("Error al buscar la ubicación. Verifica tu conexión");
                    remainingDistance.setText("Error en la búsqueda");
                });
            }
        }).start();
    }
    // Método alternativo usando coordenadas directas si el usuario ingresa lat,lng
    private void searchByCoordinates(String query) {
        try {
            String[] parts = query.split(",");
            if (parts.length == 2) {
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());

                // Validar que las coordenadas estén en un rango válido
                if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                    LatLng location = new LatLng(lat, lng);
                    getAddressFromLatLng(location);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            // No son coordenadas válidas, continuar con búsqueda normal
        }

        // Si no son coordenadas, buscar como dirección normal
        searchWithGeocoder(query);
    }
    private void setupMapClickListener() {
        // Este método se llamará después de que el mapa esté listo
    }
    private void configurarBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_mapa);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_mapa) {
                return true; // Ya estás en la vista actual
            } else if (id == R.id.nav_reservas) {
                navegarSinAnimacion(DriverReservaActivity.class);
                return true;
            } else if (id == R.id.nav_inicio) {
                navegarSinAnimacion(DriverInicioActivity.class);
                return true;
            } else if (id == R.id.nav_perfil) {
                navegarSinAnimacion(DriverPerfilActivity.class);
                return true;
            }
            return false;
        });
    }
    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGpsAlertDialog() {
        if (mostroDialogoGps) return;
        mostroDialogoGps = true;
        new AlertDialog.Builder(this)
                .setTitle("Activar GPS")
                .setMessage("Necesitamos que actives tu GPS para mostrar tu ubicación.")
                .setCancelable(false)
                .setPositiveButton("Activar", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }




    private void navegarSinAnimacion(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(0, 0);
        finish();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map_fragment);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setOnMapClickListener(latLng -> {
            // Obtener dirección del punto clickeado
            getAddressFromLatLng(latLng);
        });
        verificarUbicacionYGps();
    }
    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                setDestination(latLng, addressText);
            } else {
                setDestination(latLng, "Ubicación seleccionada");
            }
        } catch (IOException e) {
            setDestination(latLng, "Lat: " + String.format("%.6f", latLng.latitude) +
                    ", Lng: " + String.format("%.6f", latLng.longitude));
        }
    }
    private boolean tienePermisosUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
    private void verificarUbicacionYGps() {
        if (tienePermisosUbicacion()) {
            if (isGpsEnabled()) {
                mMap.setMyLocationEnabled(true);
                startLocationUpdates();
            } else {
                showGpsAlertDialog();
            }
        } else {
            requestLocationPermission();
        }
    }
    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(1.0f);
    }
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result != null && result.getLastLocation() != null) {
                    currentLocation = new LatLng(
                            result.getLastLocation().getLatitude(),
                            result.getLastLocation().getLongitude()
                    );
                    if (isFirstLocation) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                        isFirstLocation = false;
                        showToast("Ubicación encontrada");
                    }

                    // Si hay destino, recalcular ruta
                    if (destinationLocation != null) {
                        calculateRoute();
                    }
                }
            }
        };
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_REQUEST_CODE);
    }
    private void startLocationUpdates() {
        if (!tienePermisosUbicacion()) {
            requestLocationPermission();
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        showToast("Iniciando seguimiento de ubicación");
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        showToast("Seguimiento de ubicación pausado");
    }
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (tienePermisosUbicacion()) {
                mMap.setMyLocationEnabled(true);
                startLocationUpdates();
            }
            showToast("Permisos de ubicación concedidos");
        } else {
            showToast("Se requieren permisos de ubicación para funcionar");
            showPermissionExplanationDialog();
        }
    }
    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos de ubicación requeridos")
                .setMessage("Esta aplicación necesita acceso a tu ubicación para mostrarte tu posición actual.")
                .setPositiveButton("Intentar de nuevo", (dialog, which) -> requestLocationPermission())
                .setNegativeButton("Cancelar", null)
                .show();
    }
    public LatLng getCurrentCoordinates() {
        return currentLocation;
    }

    public String getCurrentCoordinatesAsString() {
        return currentLocation != null
                ? String.format(Locale.getDefault(), "%.6f,%.6f",
                currentLocation.latitude, currentLocation.longitude)
                : "Ubicación no disponible";
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null && isGpsEnabled() && tienePermisosUbicacion()) {
            mMap.setMyLocationEnabled(true);
            startLocationUpdates();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
