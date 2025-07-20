package com.iot.stayflowdev.Driver.Activity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.iot.stayflowdev.LoginActivity;
import com.iot.stayflowdev.R;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DriverMapaActivity extends AppCompatActivity implements OnMapReadyCallback, MapManager.MapEventListener {
    // ==================== VARIABLES PRINCIPALES ====================
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean isFirstLocation = true;
    private boolean mostroDialogoGps = false;
    private static final int LOCATION_REQUEST_CODE = 1;

    // MapManager - Optimiza toda la lógica del mapa
    private MapManager mapManager;

    // UI Components - Cards
    private CardView destinationCard;
    private CardView navigationCard;
    private CardView finalizeTripCard;

    // UI Components - Destination Card
    private LinearLayout collapsedContent, expandedContent;
    private TextView destinationName, direccionName, arrivalTime, arrivalTimeDetailed;
    private TextView distanceValue, estimatedCost, remainingDistance, routeText;
    private ImageButton btnCloseDestination;
    private MaterialButton btnStartTrip, btnContactPassenger;

    // UI Components - Navigation Card
    private ImageView navigationIcon;
    private TextView navigationInstruction, navigationDistance, navigationTime, navigationProgress;
    private MaterialButton btnFinalizeTrip;

    // UI Components - Finalize Card
    private TextView finalizeMessage;
    private MaterialButton btnScanQr, btnCancelFinalize;

    // Other UI Components
    private FloatingActionButton fabCurrentLocation;
    private BottomNavigationView bottomNavigation;
    private TextInputEditText searchInput;

    // States
    private boolean isCardExpanded = false;
    private boolean isNavigating = false;
    private boolean isTripStarted = false;

    // QR Scanner
    private ActivityResultLauncher<ScanOptions> qrLauncher;

    // Mock navigation data
    private Handler mockNavigationHandler = new Handler();
    private Runnable mockNavigationRunnable;
    private int mockProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_mapa);

        // Inicializar MapManager
        mapManager = new MapManager(this, this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeComponents();
        setupEventListeners();
        setupQRScanner();
        configureLocation();
        configurarBottomNavigation();
        initializeMap();
        setupWindowInsets();
        setupMockNavigation();
    }

    // ==================== INITIALIZATION ====================

    private void initializeComponents() {
        // Navigation & FAB
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabCurrentLocation = findViewById(R.id.fab_current_location);
        searchInput = findViewById(R.id.search_input);
        routeText = findViewById(R.id.route_text);

        // ==================== DESTINATION CARD ====================
        destinationCard = findViewById(R.id.destination_card);
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

        // ==================== NAVIGATION CARD ====================
        navigationCard = findViewById(R.id.navigation_card);
        navigationIcon = findViewById(R.id.navigation_icon);
        navigationInstruction = findViewById(R.id.navigation_instruction);
        navigationDistance = findViewById(R.id.navigation_distance);
        navigationTime = findViewById(R.id.navigation_time);
        navigationProgress = findViewById(R.id.navigation_progress);
        btnFinalizeTrip = findViewById(R.id.btn_finalize_trip);

        // ==================== FINALIZE TRIP CARD ====================
        finalizeTripCard = findViewById(R.id.finalize_trip_card);
        finalizeMessage = findViewById(R.id.finalize_message);
        btnScanQr = findViewById(R.id.btn_scan_qr);
        btnCancelFinalize = findViewById(R.id.btn_cancel_finalize);

        // ==================== ESTADO INICIAL - TODAS LAS CARDS OCULTAS ====================
        hideDestinationCard();
        hideNavigationCard();
        hideFinalizeTripCard();

        if (routeText != null) routeText.setVisibility(View.GONE);
    }

    private void setupEventListeners() {
        // Location FAB
        fabCurrentLocation.setOnClickListener(v -> {
            if (mapManager.getCurrentLocation() != null) {
                mapManager.centerOnCurrentLocation(18f);
            } else {
            }
        });

        // ==================== DESTINATION CARD LISTENERS ====================
        collapsedContent.setOnClickListener(v -> toggleCardExpansion());
        btnCloseDestination.setOnClickListener(v -> clearDestination());
        btnStartTrip.setOnClickListener(v -> startTrip());
        btnContactPassenger.setOnClickListener(v -> {
            Intent intent = new Intent(DriverMapaActivity.this, DriverChatActivity.class);
            startActivity(intent);
        });

        // ==================== NAVIGATION CARD LISTENERS ====================
        btnFinalizeTrip.setOnClickListener(v -> showFinalizeTripCard());

        // ==================== FINALIZE TRIP CARD LISTENERS ====================
        btnScanQr.setOnClickListener(v -> startQRScanning());
        btnCancelFinalize.setOnClickListener(v -> cancelFinalization());

        // Search Input
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(v.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void setupQRScanner() {
        qrLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
            } else {
                String qrContent = result.getContents();
                processQRResult(qrContent);
            }
        });
    }

    private void setupMockNavigation() {
        mockNavigationRunnable = new Runnable() {
            @Override
            public void run() {
                if (isNavigating && isTripStarted) {
                    updateMockNavigation();
                    mockNavigationHandler.postDelayed(this, 3000); // Actualizar cada 3 segundos
                }
            }
        };
    }

    // ==================== CARD MANAGEMENT ====================

    private void showDestinationCard() {
        // Solo mostrar si no hay un viaje en curso
        if (!isTripStarted && !isNavigating) {
            hideNavigationCard();
            hideFinalizeTripCard();

            destinationCard.setVisibility(View.VISIBLE);
            destinationCard.animate().alpha(1f).setDuration(300).start();
        }
    }

    private void hideDestinationCard() {
        destinationCard.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> destinationCard.setVisibility(View.GONE)).start();
        if (isCardExpanded) toggleCardExpansion();
    }

    private void showNavigationCard() {
        hideDestinationCard();
        hideFinalizeTripCard();

        navigationCard.setVisibility(View.VISIBLE);
        navigationCard.animate().alpha(1f).setDuration(300).start();
    }

    private void hideNavigationCard() {
        navigationCard.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> navigationCard.setVisibility(View.GONE)).start();
    }

    private void showFinalizeTripCard() {
        hideDestinationCard();
        hideNavigationCard();

        finalizeTripCard.setVisibility(View.VISIBLE);
        finalizeTripCard.animate().alpha(1f).setDuration(300).start();

    }

    private void hideFinalizeTripCard() {
        finalizeTripCard.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> finalizeTripCard.setVisibility(View.GONE)).start();
    }

    // ==================== TRIP FLOW MANAGEMENT ====================

    private void startTrip() {
        if (!mapManager.hasDestination()) {
            return;
        }

        // Cambiar estado
        isTripStarted = true;
        isNavigating = true;

        // Mostrar navigation card
        showNavigationCard();

        // Inicializar datos de navegación mock
        initializeMockNavigation();

        // Iniciar updates de navegación mock
        mockNavigationHandler.post(mockNavigationRunnable);

        // Habilitar tráfico
        mapManager.enableTraffic(true);

        // Mostrar mensaje

        // Ocultar elementos no necesarios durante navegación
        fabCurrentLocation.setVisibility(View.GONE);
        searchInput.setEnabled(false);
    }

    private void initializeMockNavigation() {
        mockProgress = 0;

        // Configurar navegación inicial
        navigationInstruction.setText("Dirígete hacia el norte por la Av. Principal");
        navigationDistance.setText("en 200 m");
        navigationTime.setText("12 min");
        navigationProgress.setText("0% completado");

        // Cambiar icono de navegación si es necesario
        // navigationIcon.setImageResource(R.drawable.ic_turn_right);
    }

    private void updateMockNavigation() {
        mockProgress += 15; // Incrementar progreso cada actualización

        // Simular diferentes instrucciones según el progreso
        if (mockProgress <= 25) {
            navigationInstruction.setText("Gira a la derecha en 150 metros");
            navigationDistance.setText("en 150 m");
            navigationTime.setText("10 min");
        } else if (mockProgress <= 50) {
            navigationInstruction.setText("Continúa recto por 500 metros");
            navigationDistance.setText("en 500 m");
            navigationTime.setText("8 min");
        } else if (mockProgress <= 75) {
            navigationInstruction.setText("Gira a la izquierda hacia tu destino");
            navigationDistance.setText("en 100 m");
            navigationTime.setText("3 min");
        } else {
            navigationInstruction.setText("Has llegado a tu destino");
            navigationDistance.setText("Destino alcanzado");
            navigationTime.setText("0 min");
            mockProgress = 100;
        }

        navigationProgress.setText(mockProgress + "% completado");

        // Si llegamos al 100%, mostrar opción de finalizar
        if (mockProgress >= 100) {
            mockNavigationHandler.removeCallbacks(mockNavigationRunnable);
        }
    }

    private void cancelFinalization() {
        showNavigationCard();
    }

    private void startQRScanning() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE); // SOLO códigos QR
        options.setPrompt("Escanea el código QR del pasajero");
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true); // Capturar imagen del QR
        options.setOrientationLocked(true);
        qrLauncher.launch(options);
    }

    private void processQRResult(String qrContent) {
        // Aquí procesarías el contenido del QR
        // Por ejemplo, verificar que es un código válido del pasajero


        // Simular verificación exitosa
        new Handler().postDelayed(() -> {
            completeTripFinalization();
        }, 1000);
    }

    private void completeTripFinalization() {
        // Limpiar estados
        isTripStarted = false;
        isNavigating = false;
        mockProgress = 0;

        // Detener navegación mock
        mockNavigationHandler.removeCallbacks(mockNavigationRunnable);

        // Limpiar destino
        clearDestination();

        // Restaurar UI
        fabCurrentLocation.setVisibility(View.VISIBLE);
        searchInput.setEnabled(true);
        mapManager.enableTraffic(false);

        // Mostrar mensaje de éxito
        showToast("¡Viaje finalizado exitosamente!");

        // Opcional: Mostrar dialog de resumen del viaje
        showTripSummaryDialog();
    }

    private void showTripSummaryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Viaje Completado")
                .setMessage("El viaje ha sido finalizado exitosamente.\n\n" +
                        "Duración: " + (100 - mockProgress + 15) + " minutos\n" +
                        "Estado: Completado")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==================== UI INTERACTIONS (EXISTING METHODS) ====================

    private String[] separatePlaceNameAndAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            return new String[]{"Destino", "Ubicación seleccionada"};
        }

        String[] parts = fullAddress.split(",");
        String placeName;
        String streetAddress;

        if (parts.length >= 2) {
            // Primer parte como nombre del lugar
            placeName = parts[0].trim();

            // Resto como dirección
            StringBuilder addressBuilder = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) addressBuilder.append(", ");
                addressBuilder.append(parts[i].trim());
            }
            streetAddress = addressBuilder.toString();

            // Validar que el nombre del lugar no sea solo números (probablemente sea dirección)
            if (placeName.matches("^[0-9\\s-]+$")) {
                // Si el primer elemento es solo números, es probablemente una dirección
                placeName = extractPlaceNameFromAddress(fullAddress);
                streetAddress = fullAddress;
            }
        } else {
            // Solo una parte, intentar extraer información útil
            placeName = extractPlaceNameFromAddress(fullAddress);
            streetAddress = fullAddress;
        }

        return new String[]{placeName, streetAddress};
    }

    private String extractPlaceNameFromAddress(String address) {
        // Buscar palabras clave que indiquen lugares importantes
        String[] keywords = {
                "Centro Comercial", "Mall", "Plaza", "Parque", "Universidad", "Hospital",
                "Aeropuerto", "Terminal", "Estación", "Mercado", "Iglesia", "Catedral",
                "Museo", "Teatro", "Cine", "Hotel", "Restaurant", "Banco", "Clínica",
                "Biblioteca", "Estadio", "Colegio", "Instituto", "Ministerio"
        };

        String lowerAddress = address.toLowerCase();
        for (String keyword : keywords) {
            if (lowerAddress.contains(keyword.toLowerCase())) {
                // Encontrar la palabra clave y extraer el nombre completo
                int index = lowerAddress.indexOf(keyword.toLowerCase());
                String[] words = address.substring(index).split(",")[0].split("\\d")[0].trim().split("\\s+");

                if (words.length <= 4) { // Limitar a nombres razonables
                    return address.substring(index).split(",")[0].trim();
                }
            }
        }

        // Si no encuentra palabras clave, usar la primera parte antes de números de casa
        String[] parts = address.split("\\d");
        if (parts.length > 0 && parts[0].trim().length() > 3) {
            return parts[0].trim().replaceAll(",$", "");
        }

        // Como último recurso, usar la primera parte antes de la primera coma
        String firstPart = address.split(",")[0].trim();
        if (firstPart.length() > 15) {
            // Si es muy largo, acortar
            String[] words = firstPart.split("\\s+");
            if (words.length > 3) {
                StringBuilder shortName = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    if (i > 0) shortName.append(" ");
                    shortName.append(words[i]);
                }
                return shortName.toString() + "...";
            }
        }

        return firstPart.isEmpty() ? "Destino" : firstPart;
    }

    private void updateDestinationCard(String placeName, String fullAddress) {
        destinationName.setText(placeName);
        direccionName.setText(fullAddress);

        // Reset values while calculating
        arrivalTime.setText("-- mins");
        arrivalTimeDetailed.setText("-- mins");
        distanceValue.setText("-- km");
        estimatedCost.setText("S/ --");
        remainingDistance.setText("Calculando ruta óptima...");
    }

    private void updateRouteInfo(MapManager.RouteInfo routeInfo) {
        distanceValue.setText(routeInfo.distance);
        arrivalTime.setText(routeInfo.durationMinutes + " mins");
        arrivalTimeDetailed.setText(routeInfo.durationMinutes + " mins");
        estimatedCost.setText(String.format(Locale.getDefault(), "S/ %.2f", routeInfo.estimatedCost));
        remainingDistance.setText("Ruta óptima calculada");
    }

    private void clearDestination() {
        mapManager.clearDestination();

        // Ocultar todas las cards
        hideDestinationCard();
        hideNavigationCard();
        hideFinalizeTripCard();

        searchInput.setText("");
        if (routeText != null) {
            routeText.setVisibility(View.GONE);
        }

        // Restaurar estados
        isTripStarted = false;
        isNavigating = false;
        mockProgress = 0;
        mockNavigationHandler.removeCallbacks(mockNavigationRunnable);

        // NO mostrar destination card automáticamente
        showToast("Destino eliminado");
    }

    private void toggleCardExpansion() {
        if (isCardExpanded) {
            expandedContent.animate().alpha(0f).setDuration(300)
                    .withEndAction(() -> {
                        expandedContent.setVisibility(View.GONE);
                        isCardExpanded = false;
                    }).start();
        } else {
            expandedContent.setVisibility(View.VISIBLE);
            expandedContent.animate().alpha(1f).setDuration(300).start();
            isCardExpanded = true;
        }
    }

    private void performSearch(String query) {
        if (!query.isEmpty()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            searchLocation(query);
        }
    }

    private void handleMapClick(LatLng latLng) {
        if (isTripStarted) {
            showToast("No puedes seleccionar un nuevo destino durante el viaje");
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String placeName = address.getFeatureName() != null ?
                                address.getFeatureName() : "Ubicación seleccionada";
                        String fullAddress = address.getAddressLine(0) != null ?
                                address.getAddressLine(0) : "Punto en el mapa";

                        mapManager.setDestination(latLng, placeName, fullAddress);
                    } else {
                        mapManager.setDestination(latLng, "Ubicación seleccionada",
                                String.format("%.6f, %.6f", latLng.latitude, latLng.longitude));
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> mapManager.setDestination(latLng, "Punto seleccionado",
                        String.format("%.6f, %.6f", latLng.latitude, latLng.longitude)));
            }
        }).start();
    }

    private void searchLocation(String query) {
        if (isTripStarted) {
            showToast("No puedes buscar un nuevo destino durante el viaje");
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 1);

                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                        String placeName = query;
                        String fullAddress = address.getAddressLine(0) != null ?
                                address.getAddressLine(0) : query;

                        mapManager.setDestination(location, placeName, fullAddress);
                        showToast("Ubicación encontrada");
                    } else {
                        showToast("No se encontró la ubicación");
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> showToast("Error de conexión"));
            }
        }).start();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ==================== MAPMANAGER EVENT LISTENERS ====================

    @Override
    public void onLocationUpdated(LatLng location) {
        Log.d("DriverMapa", "Ubicación actualizada: " + location.latitude + ", " + location.longitude);
    }

    @Override
    public void onDestinationSelected(LatLng destination, String address) {
        // Solo actualizar y mostrar si no hay viaje en curso
        if (!isTripStarted && !isNavigating) {
            // Separar nombre del lugar y dirección
            String[] parts = separatePlaceNameAndAddress(address);
            String placeName = parts[0];
            String fullAddress = parts[1];

            updateDestinationCard(placeName, fullAddress);
            showDestinationCard();
        }
    }

    @Override
    public void onRouteCalculated(MapManager.RouteInfo routeInfo) {
        runOnUiThread(() -> {
            updateRouteInfo(routeInfo);
            if (routeText != null) {
                routeText.setVisibility(View.VISIBLE);
                routeText.setText("Ruta óptima: " + routeInfo.distance + " en " + routeInfo.duration);
            }
        });
    }

    @Override
    public void onRouteCalculationFailed(String error) {
        runOnUiThread(() -> {
            showToast("Error: " + error);
            remainingDistance.setText("Error al calcular ruta");
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        handleMapClick(latLng);
    }

    // ==================== CONFIGURATION & LIFECYCLE (EXISTING METHODS) ====================

    private void configureLocation() {
        locationRequest = LocationRequest.create()
                .setInterval(3000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(3.0f);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result != null && result.getLastLocation() != null) {
                    Location location = result.getLastLocation();
                    LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    mapManager.updateCurrentLocation(newLocation);
                    mapManager.updateLocationPointAccuracy(location.getAccuracy());

                    if (isFirstLocation) {
                        mapManager.centerOnCurrentLocation(18f);
                        isFirstLocation = false;
                        showToast("Ubicación encontrada");
                    }
                }
            }
        };
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
        mapManager.initializeMap(googleMap);
        verificarUbicacionYGps();
    }

    private void configurarBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_mapa);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_mapa) return true;
            else if (id == R.id.nav_reservas) navegarSinAnimacion(DriverReservaActivity.class);
            else if (id == R.id.nav_inicio) navegarSinAnimacion(DriverInicioActivity.class);
            else if (id == R.id.nav_perfil) navegarSinAnimacion(DriverPerfilActivity.class);
            return true;
        });
    }

    private void navegarSinAnimacion(Class<?> cls) {
        if (isTripStarted) {
            showToast("No puedes navegar mientras hay un viaje en curso");
            return;
        }
        startActivity(new Intent(this, cls));
        overridePendingTransition(0, 0);
        finish();
    }

    private void verificarUbicacionYGps() {
        if (tienePermisosUbicacion()) {
            if (isGpsEnabled()) {
                startLocationUpdates();
            } else {
                showGpsDialog();
            }
        } else {
            requestLocationPermission();
        }
    }

    private boolean tienePermisosUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGpsDialog() {
        if (mostroDialogoGps) return;
        mostroDialogoGps = true;
        new AlertDialog.Builder(this)
                .setTitle("Activar GPS")
                .setMessage("Necesitamos GPS activo para calcular rutas óptimas")
                .setCancelable(false)
                .setPositiveButton("Activar", (d, w) ->
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Cancelar", null)
                .show();
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
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (tienePermisosUbicacion()) {
                startLocationUpdates();
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Permisos requeridos")
                    .setMessage("Necesitamos ubicación para calcular rutas")
                    .setPositiveButton("Reintentar", (d, w) -> requestLocationPermission())
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGpsEnabled() && tienePermisosUbicacion()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        mockNavigationHandler.removeCallbacksAndMessages(null);

        if (mapManager != null) {
            mapManager.onDestroy();
        }
    }

    public LatLng getCurrentCoordinates() {
        return mapManager != null ? mapManager.getCurrentLocation() : null;
    }

    public String getCurrentCoordinatesAsString() {
        LatLng location = getCurrentCoordinates();
        return location != null ?
                String.format(Locale.getDefault(), "%.6f,%.6f", location.latitude, location.longitude) :
                "Ubicación no disponible";
    }
}