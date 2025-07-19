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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.iot.stayflowdev.R;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;



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

    private Polyline currentRoute; // Para almacenar la línea de ruta actual
    private TextView routeText; // El TextView que ya tienes para mostrar información de ruta


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

        // ==================== INICIALIZAR VISTAS DEL CARD ====================
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
        initializeRouteText();
    }

    private void setupClickListeners() {
        fabCurrentLocation.setOnClickListener(v -> {
            if (currentLocation != null && mMap != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
            else
                showToast("Obteniendo ubicación...");
        });

        // ==================== CLICK LISTENERS DEL CARD ====================

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

    // Sobrecarga del método para cuando tenemos nombre y dirección por separado
    private void setDestination(LatLng destination, String placeName, String fullAddress) {
        this.destinationLocation = destination;
        this.destinationAddress = fullAddress;

        // Agregar marcador en el mapa
        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title(placeName)
                .snippet(fullAddress)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Actualizar información del card con nombre y dirección separados
        updateDestinationInfo(placeName, fullAddress);

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
        // Separar nombre del lugar y dirección
        String[] parts = separatePlaceNameAndAddress(address);
        String placeName = parts[0];
        String streetAddress = parts[1];

        destinationName.setText(placeName);
        direccionName.setText(streetAddress);

        // Valores por defecto mientras se calculan
        arrivalTime.setText("-- mins");
        arrivalTimeDetailed.setText("-- mins");
        distanceValue.setText("-- km");
        estimatedCost.setText("S/ --");
        remainingDistance.setText("Calculando ruta...");
    }

    // Sobrecarga para cuando ya tenemos nombre y dirección separados
    private void updateDestinationInfo(String placeName, String fullAddress) {
        destinationName.setText(placeName);
        direccionName.setText(fullAddress);

        // Valores por defecto mientras se calculan
        arrivalTime.setText("-- mins");
        arrivalTimeDetailed.setText("-- mins");
        distanceValue.setText("-- km");
        estimatedCost.setText("S/ --");
        remainingDistance.setText("Calculando ruta...");
    }

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
                return String.join(" ", Arrays.copyOfRange(words, 0, 3)) + "...";
            }
        }

        return firstPart.isEmpty() ? "Destino" : firstPart;
    }
    private void calculateRoute() {
        if (currentLocation == null || destinationLocation == null) return;

        // Mostrar indicador de que se está calculando
        remainingDistance.setText("Calculando ruta...");

        // Mostrar el texto de ruta mientras se calcula
        if (routeText != null) {
            routeText.setVisibility(View.VISIBLE);
            routeText.setText("Calculando ruta óptima...");
        }

        // Calcular ruta usando Directions API
        calculateRouteWithDirectionsAPI();
    }
    private void calculateRouteWithDirectionsAPI() {
        String origin = currentLocation.latitude + "," + currentLocation.longitude;
        String destination = destinationLocation.latitude + "," + destinationLocation.longitude;
        String apiKey = "AIzaSyArgXmLe6Bk6km2wSEN8RDpk2qbPd73ryc"; // Reemplaza con tu API key

        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin +
                "&destination=" + destination +
                "&key=" + apiKey +
                "&mode=driving" +
                "&language=es";

        new Thread(() -> {
            try {
                String response = makeHttpRequest(url);
                parseDirectionsResponse(response);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showToast("Error al calcular la ruta");
                    // Fallback a cálculo básico
                    simulateRouteCalculation();
                });
            }
        }).start();
    }

    // ==================== MÉTODO PARA HACER REQUEST HTTP ====================
    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    // ==================== MÉTODO PARA PARSEAR RESPUESTA DE DIRECTIONS ====================
    private void parseDirectionsResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String status = jsonResponse.getString("status");

            if ("OK".equals(status)) {
                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);

                    // Obtener información de la ruta
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);

                    String distance = leg.getJSONObject("distance").getString("text");
                    String duration = leg.getJSONObject("duration").getString("text");
                    int durationValue = leg.getJSONObject("duration").getInt("value") / 60; // en minutos

                    // Obtener puntos de la ruta para dibujar
                    String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");
                    List<LatLng> routePoints = decodePolyline(encodedPolyline);

                    runOnUiThread(() -> {
                        // Actualizar UI con información real
                        updateRouteInfo(distance, durationValue);

                        // Dibujar ruta en el mapa
                        drawRoute(routePoints);

                        // Mostrar texto de ruta
                        if (routeText != null) {
                            routeText.setText("Ruta calculada - " + distance + " en " + duration);
                        }
                    });
                }
            } else {
                runOnUiThread(() -> {
                    showToast("No se pudo calcular la ruta");
                    simulateRouteCalculation();
                });
            }
        } catch (JSONException e) {
            runOnUiThread(() -> {
                showToast("Error al procesar la ruta");
                simulateRouteCalculation();
            });
        }
    }

    // ==================== MÉTODO PARA ACTUALIZAR INFO DE RUTA ====================
    private void updateRouteInfo(String distance, int durationMinutes) {
        // Calcular costo estimado basado en distancia real
        double distanceKm = extractDistanceInKm(distance);
        double estimatedCostValue = distanceKm * 1.5 + 3.0; // Tarifa base + km

        distanceValue.setText(distance);
        arrivalTime.setText(durationMinutes + " mins");
        arrivalTimeDetailed.setText(durationMinutes + " mins");
        estimatedCost.setText(String.format(Locale.getDefault(), "S/ %.2f", estimatedCostValue));
        remainingDistance.setText("Ruta calculada - Listo para iniciar");
    }

    // ==================== MÉTODO PARA EXTRAER DISTANCIA EN KM ====================
    private double extractDistanceInKm(String distanceText) {
        try {
            String numericPart = distanceText.replaceAll("[^0-9,.]", "").replace(",", ".");
            return Double.parseDouble(numericPart);
        } catch (NumberFormatException e) {
            return 10.0; // valor por defecto
        }
    }

    // ==================== MÉTODO PARA DIBUJAR RUTA EN EL MAPA ====================
    private void drawRoute(List<LatLng> routePoints) {
        if (mMap == null || routePoints == null || routePoints.isEmpty()) return;

        // Eliminar ruta anterior si existe
        if (currentRoute != null) {
            currentRoute.remove();
        }

        // Crear nueva polilínea para la ruta
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(8f)
                .color(Color.BLUE)
                .geodesic(true);

        currentRoute = mMap.addPolyline(polylineOptions);

        // Ajustar cámara para mostrar toda la ruta
        adjustCameraToShowRoute(routePoints);
    }

    // ==================== MÉTODO PARA DECODIFICAR POLYLINE ====================
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    // ==================== MÉTODO MEJORADO PARA AJUSTAR CÁMARA ====================
    private void adjustCameraToShowRoute(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // Incluir todos los puntos de la ruta
        for (LatLng point : routePoints) {
            builder.include(point);
        }

        // También incluir origen y destino
        if (currentLocation != null) {
            builder.include(currentLocation);
        }
        if (destinationLocation != null) {
            builder.include(destinationLocation);
        }

        LatLngBounds bounds = builder.build();
        int padding = 150; // Padding en pixels

        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            // Fallback si hay error con los bounds
            if (destinationLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
            }
        }
    }

    // ==================== MODIFICAR EL MÉTODO clearDestination() ====================
    private void clearDestination() {
        destinationLocation = null;
        destinationAddress = null;

        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }

        // *** NUEVO: Limpiar ruta dibujada ***
        if (currentRoute != null) {
            currentRoute.remove();
            currentRoute = null;
        }

        // *** NUEVO: Ocultar texto de ruta ***
        if (routeText != null) {
            routeText.setVisibility(View.GONE);
        }

        hideDestinationCard();
        searchInput.setText("");
        showToast("Destino eliminado");
    }
    private void initializeRouteText() {
        routeText = findViewById(R.id.route_text);
        if (routeText != null) {
            routeText.setVisibility(View.GONE); // Inicialmente oculto
        }
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
        showToast("Abriendo chat...");
    }

    private void callPassenger() {
        showToast("Llamando al pasajero...");
    }
   /* private void searchDestination(String query) {
        if (query.trim().isEmpty()) {
            showToast("Ingresa una dirección o lugar");
            return;
        }

        showToast("Buscando: " + query);
        remainingDistance.setText("Buscando ubicación...");

        searchWithGeocoder(query);
    }*/

    private void searchWithGeocoder(String query) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(query, 5);

                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                        String placeName = determinePlaceName(address, query);
                        String fullAddress = buildFullAddress(address);

                        setDestination(location, placeName, fullAddress);
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

    private String determinePlaceName(Address address, String originalQuery) {
        if (address.getFeatureName() != null &&
                !address.getFeatureName().equals(address.getSubThoroughfare()) &&
                !address.getFeatureName().matches("^[0-9\\s-]+$")) {
            return address.getFeatureName();
        }

        if (originalQuery != null && originalQuery.trim().length() > 0) {
            String cleanQuery = originalQuery.trim();
            if (!cleanQuery.matches("^[0-9\\s,.-]+$")) {
                return cleanQuery;
            }
        }

        String fullAddress = buildFullAddress(address);
        return extractPlaceNameFromAddress(fullAddress);
    }

    private String buildFullAddress(Address address) {
        StringBuilder addressText = new StringBuilder();

        if (address.getAddressLine(0) != null) {
            return address.getAddressLine(0);
        } else {
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
            if (address.getCountryName() != null) {
                addressText.append(", ").append(address.getCountryName());
            }
        }

        return addressText.toString();
    }

    private void searchByCoordinates(String query) {
        try {
            String[] parts = query.split(",");
            if (parts.length == 2) {
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());

                if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                    LatLng location = new LatLng(lat, lng);
                    getAddressFromLatLng(location);
                    return;
                }
            }
        } catch (NumberFormatException e) {
        }

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
                return true;
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
            getAddressFromLatLng(latLng);
        });

        verificarUbicacionYGps();
    }

    private void getAddressFromLatLng(LatLng latLng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String placeName = determinePlaceName(address, null);
                        String fullAddress = buildFullAddress(address);
                        setDestination(latLng, placeName, fullAddress);
                    } else {
                        setDestination(latLng, "Ubicación seleccionada",
                                "Lat: " + String.format("%.6f", latLng.latitude) +
                                        ", Lng: " + String.format("%.6f", latLng.longitude));
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setDestination(latLng, "Punto seleccionado",
                            "Lat: " + String.format("%.6f", latLng.latitude) +
                                    ", Lng: " + String.format("%.6f", latLng.longitude));
                });
            }
        }).start();
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
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}