package com.iot.stayflowdev.Driver.Activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MapManager {
    private static final String TAG = "MapManager";

    // Configuración API
    private static final String API_KEY = "AIzaSyArgXmLe6Bk6km2wSEN8RDpk2qbPd73ryc";
    private static final String DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json";

    // Referencias principales
    private GoogleMap mMap;
    private Context context;
    private MapEventListener eventListener;

    // Elementos del mapa
    private Marker destinationMarker;
    private Polyline currentRoute;
    private Circle proximityCircle;

    // Ubicación actual como punto azul (no marcador)
    private Circle currentLocationCircle;
    private Circle currentLocationPulse;
    private ValueAnimator pulseAnimator;

    // Estado actual
    private LatLng currentLocation;
    private LatLng destinationLocation;
    private boolean isRouteCalculating = false;

    // Cache para optimización
    private RouteCache routeCache;
    private long lastRouteCalculation = 0;
    private static final long ROUTE_RECALCULATION_INTERVAL = 30000; // 30 segundos

    // ==================== INTERFACES ====================

    public interface MapEventListener {
        void onLocationUpdated(LatLng location);
        void onDestinationSelected(LatLng destination, String address);
        void onRouteCalculated(RouteInfo routeInfo);
        void onRouteCalculationFailed(String error);
        void onMapClick(LatLng latLng);
    }

    public static class RouteInfo {
        public String distance;
        public String duration;
        public int durationMinutes;
        public double distanceKm;
        public List<LatLng> routePoints;
        public double estimatedCost;

        public RouteInfo(String distance, String duration, int durationMinutes,
                         double distanceKm, List<LatLng> routePoints, double estimatedCost) {
            this.distance = distance;
            this.duration = duration;
            this.durationMinutes = durationMinutes;
            this.distanceKm = distanceKm;
            this.routePoints = routePoints;
            this.estimatedCost = estimatedCost;
        }
    }

    private static class RouteCache {
        private LatLng origin;
        private LatLng destination;
        private RouteInfo routeInfo;
        private long timestamp;

        public boolean isValid(LatLng newOrigin, LatLng newDestination) {
            if (origin == null || destination == null) return false;

            float distanceFromOrigin = getDistance(origin, newOrigin);
            float distanceFromDestination = getDistance(destination, newDestination);

            // Cache válido si no se movió más de 100 metros
            return distanceFromOrigin < 100 && distanceFromDestination < 100 &&
                    (System.currentTimeMillis() - timestamp) < 300000; // 5 minutos
        }

        private float getDistance(LatLng point1, LatLng point2) {
            float[] results = new float[1];
            Location.distanceBetween(point1.latitude, point1.longitude,
                    point2.latitude, point2.longitude, results);
            return results[0];
        }
    }

    // ==================== CONSTRUCTOR ====================

    public MapManager(Context context, MapEventListener listener) {
        this.context = context;
        this.eventListener = listener;
        this.routeCache = new RouteCache();
    }

    // ==================== CONFIGURACIÓN INICIAL ====================

    public void initializeMap(GoogleMap googleMap) {
        this.mMap = googleMap;
        configureMapSettings();
        setupMapListeners();
        Log.d(TAG, "Mapa inicializado y optimizado");
    }

    private void configureMapSettings() {
        if (mMap == null) return;

        // Configuración optimizada para rendimiento
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false); // Desactivar para mejor rendimiento
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Configurar tipo de mapa para mejor rendimiento
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false); // Activar solo cuando sea necesario

        // IMPORTANTE: NO activar MyLocationEnabled para usar punto azul personalizado
        // mMap.setMyLocationEnabled(false);
    }

    private void setupMapListeners() {
        mMap.setOnMapClickListener(latLng -> {
            if (eventListener != null) {
                eventListener.onMapClick(latLng);
            }
        });

        mMap.setOnCameraIdleListener(() -> {
            // Optimizar cuando la cámara deja de moverse
            optimizeMapElements();
        });
    }

    // ==================== GESTIÓN DE UBICACIÓN ACTUAL (PUNTO AZUL) ====================

    public void updateCurrentLocation(LatLng location) {
        this.currentLocation = location;
        updateCurrentLocationPoint(location);

        if (eventListener != null) {
            eventListener.onLocationUpdated(location);
        }

        // Recalcular ruta si es necesario
        if (destinationLocation != null && shouldRecalculateRoute()) {
            calculateOptimalRoute();
        }
    }

    private void updateCurrentLocationPoint(LatLng location) {
        if (mMap == null) return;

        if (currentLocationCircle != null) {
            // Animar el punto azul a la nueva posición
            animateLocationPointToPosition(location);
        } else {
            // Crear punto azul inicial
            createCurrentLocationPoint(location);
        }
    }

    private void createCurrentLocationPoint(LatLng location) {
        // Círculo principal (punto azul)
        currentLocationCircle = mMap.addCircle(new CircleOptions()
                .center(location)
                .radius(8) // Radio pequeño para el punto
                .strokeColor(Color.WHITE)
                .strokeWidth(3)
                .fillColor(Color.parseColor("#4285F4")) // Azul de Google
                .zIndex(1000)); // Asegurar que esté encima

        // Círculo de pulso (efecto de radar)
        currentLocationPulse = mMap.addCircle(new CircleOptions()
                .center(location)
                .radius(30)
                .strokeColor(Color.parseColor("#4285F4"))
                .strokeWidth(2)
                .fillColor(Color.parseColor("#304285F4")) // Semi-transparente
                .zIndex(999));

        // Iniciar animación de pulso
        startPulseAnimation();
    }

    private void animateLocationPointToPosition(LatLng targetPosition) {
        if (currentLocationCircle == null) {
            createCurrentLocationPoint(targetPosition);
            return;
        }

        LatLng startPosition = currentLocationCircle.getCenter();

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            double lat = startPosition.latitude + (targetPosition.latitude - startPosition.latitude) * fraction;
            double lng = startPosition.longitude + (targetPosition.longitude - startPosition.longitude) * fraction;
            LatLng newPosition = new LatLng(lat, lng);

            // Actualizar ambos círculos
            currentLocationCircle.setCenter(newPosition);
            if (currentLocationPulse != null) {
                currentLocationPulse.setCenter(newPosition);
            }
        });
        animator.start();
    }

    private void startPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }

        pulseAnimator = ValueAnimator.ofFloat(30f, 60f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        pulseAnimator.addUpdateListener(animation -> {
            if (currentLocationPulse != null) {
                float radius = (Float) animation.getAnimatedValue();
                currentLocationPulse.setRadius(radius);

                // Cambiar opacidad basada en el radio
                float alpha = 1.0f - ((radius - 30f) / 30f) * 0.7f;
                int alphaInt = (int) (alpha * 76); // 76 es 30% de 255
                int color = Color.argb(alphaInt, 66, 133, 244);
                currentLocationPulse.setFillColor(color);
            }
        });

        pulseAnimator.start();
    }

    private void stopPulseAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
            pulseAnimator = null;
        }
    }

    public void setLocationPointVisible(boolean visible) {
        if (currentLocationCircle != null) {
            currentLocationCircle.setVisible(visible);
        }
        if (currentLocationPulse != null) {
            currentLocationPulse.setVisible(visible);
        }

        if (visible) {
            startPulseAnimation();
        } else {
            stopPulseAnimation();
        }
    }

    public void updateLocationPointAccuracy(float accuracy) {
        if (currentLocationPulse != null) {
            // Ajustar el radio del pulso basado en la precisión del GPS
            float radius = Math.max(30f, Math.min(accuracy, 100f));
            currentLocationPulse.setRadius(radius);
        }
    }

    // ==================== GESTIÓN DE DESTINO ====================

    public void setDestination(LatLng destination, String placeName, String address) {
        this.destinationLocation = destination;

        updateDestinationMarker(destination, placeName, address);

        if (eventListener != null) {
            eventListener.onDestinationSelected(destination, address);
        }

        calculateOptimalRoute();
    }

    private void updateDestinationMarker(LatLng destination, String placeName, String address) {
        if (mMap == null) return;

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title(placeName)
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Agregar círculo de proximidad (opcional)
        addProximityCircle(destination);
    }

    private void addProximityCircle(LatLng center) {
        if (proximityCircle != null) {
            proximityCircle.remove();
        }

        proximityCircle = mMap.addCircle(new CircleOptions()
                .center(center)
                .radius(100) // 100 metros
                .strokeColor(Color.parseColor("#1976D2"))
                .strokeWidth(2)
                .fillColor(Color.parseColor("#301976D2")));
    }

    public void clearDestination() {
        destinationLocation = null;

        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }

        if (proximityCircle != null) {
            proximityCircle.remove();
            proximityCircle = null;
        }

        clearRoute();
    }

    // ==================== CÁLCULO DE RUTAS OPTIMIZADO ====================

    private boolean shouldRecalculateRoute() {
        return (System.currentTimeMillis() - lastRouteCalculation) > ROUTE_RECALCULATION_INTERVAL;
    }

    public void calculateOptimalRoute() {
        if (currentLocation == null || destinationLocation == null || isRouteCalculating) {
            return;
        }

        // Verificar cache primero
        if (routeCache.isValid(currentLocation, destinationLocation)) {
            Log.d(TAG, "Usando ruta desde cache");
            if (eventListener != null) {
                eventListener.onRouteCalculated(routeCache.routeInfo);
            }
            return;
        }

        isRouteCalculating = true;
        lastRouteCalculation = System.currentTimeMillis();

        CompletableFuture.supplyAsync(() -> {
            try {
                return calculateRouteAsync();
            } catch (Exception e) {
                Log.e(TAG, "Error calculando ruta", e);
                return null;
            }
        }).thenAccept(routeInfo -> {
            isRouteCalculating = false;
            if (routeInfo != null) {
                // Guardar en cache
                routeCache.origin = currentLocation;
                routeCache.destination = destinationLocation;
                routeCache.routeInfo = routeInfo;
                routeCache.timestamp = System.currentTimeMillis();

                // Notificar resultado
                if (eventListener != null) {
                    eventListener.onRouteCalculated(routeInfo);
                }

                // Dibujar ruta
                drawOptimalRoute(routeInfo.routePoints);
            } else {
                if (eventListener != null) {
                    eventListener.onRouteCalculationFailed("No se pudo calcular la ruta");
                }
            }
        });
    }

    private RouteInfo calculateRouteAsync() throws Exception {
        String origin = currentLocation.latitude + "," + currentLocation.longitude;
        String destination = destinationLocation.latitude + "," + destinationLocation.longitude;

        String url = DIRECTIONS_URL + "?" +
                "origin=" + origin +
                "&destination=" + destination +
                "&key=" + API_KEY +
                "&mode=driving" +
                "&avoid=tolls" +
                "&optimize=true" +
                "&alternatives=false" +
                "&language=es";

        String response = makeHttpRequest(url);
        return parseRouteResponse(response);
    }

    private String makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    private RouteInfo parseRouteResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        String status = jsonResponse.getString("status");

        if (!"OK".equals(status)) {
            throw new JSONException("API returned status: " + status);
        }

        JSONArray routes = jsonResponse.getJSONArray("routes");
        if (routes.length() == 0) {
            throw new JSONException("No routes found");
        }

        JSONObject route = routes.getJSONObject(0);
        JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

        String distance = leg.getJSONObject("distance").getString("text");
        String duration = leg.getJSONObject("duration").getString("text");
        int durationMinutes = leg.getJSONObject("duration").getInt("value") / 60;
        double distanceKm = leg.getJSONObject("distance").getInt("value") / 1000.0;

        String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");
        List<LatLng> routePoints = PolylineDecoder.decode(encodedPolyline);

        double estimatedCost = calculateCost(distanceKm);

        return new RouteInfo(distance, duration, durationMinutes, distanceKm, routePoints, estimatedCost);
    }

    private double calculateCost(double distanceKm) {
        return distanceKm * 1.5 + 3.0; // Tarifa base + por km
    }

    // ==================== VISUALIZACIÓN DE RUTA ====================

    private void drawOptimalRoute(List<LatLng> routePoints) {
        if (mMap == null || routePoints == null || routePoints.isEmpty()) return;

        // Ejecutar en UI thread
        ((Activity) context).runOnUiThread(() -> {
            clearRoute();

            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .width(8f)
                    .color(Color.parseColor("#1976D2"))
                    .geodesic(true)
                    .clickable(false); // Mejor rendimiento

            currentRoute = mMap.addPolyline(polylineOptions);
            optimizeCameraView(routePoints);
        });
    }

    private void clearRoute() {
        if (currentRoute != null) {
            currentRoute.remove();
            currentRoute = null;
        }
    }

    private void optimizeCameraView(List<LatLng> routePoints) {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // Incluir puntos clave en lugar de todos los puntos de la ruta
            if (currentLocation != null) builder.include(currentLocation);
            if (destinationLocation != null) builder.include(destinationLocation);

            // Incluir algunos puntos intermedios para mejor encuadre
            int pointsToInclude = Math.min(routePoints.size(), 10);
            for (int i = 0; i < pointsToInclude; i += pointsToInclude / 5) {
                builder.include(routePoints.get(i));
            }

            LatLngBounds bounds = builder.build();
            int padding = dpToPx(100);

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1500, null);
        } catch (Exception e) {
            Log.e(TAG, "Error ajustando cámara", e);
            // Fallback
            if (destinationLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
            }
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    private void optimizeMapElements() {
        // Optimizar elementos del mapa basado en zoom level
        float zoomLevel = mMap.getCameraPosition().zoom;

        // Ajustar visibilidad del círculo de proximidad
        if (proximityCircle != null) {
            proximityCircle.setVisible(zoomLevel > 14);
        }

        // Ajustar grosor de línea basado en zoom
        if (currentRoute != null) {
            float lineWidth = Math.max(4f, Math.min(12f, zoomLevel - 8));
            currentRoute.setWidth(lineWidth);
        }

        // Ajustar tamaño del punto de ubicación actual basado en zoom
        if (currentLocationCircle != null) {
            float pointRadius = Math.max(6f, Math.min(12f, zoomLevel - 10));
            currentLocationCircle.setRadius(pointRadius);

            // Ajustar pulso también
            if (currentLocationPulse != null) {
                float pulseRadius = pointRadius * 4;
                currentLocationPulse.setRadius(pulseRadius);
            }
        }
    }

    public void enableTraffic(boolean enable) {
        if (mMap != null) {
            mMap.setTrafficEnabled(enable);
        }
    }

    public void centerOnCurrentLocation(float zoom) {
        if (mMap != null && currentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom));
        }
    }

    public void centerOnDestination(float zoom) {
        if (mMap != null && destinationLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, zoom));
        }
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    public void onDestroy() {
        // Detener animaciones
        stopPulseAnimation();

        // Limpiar elementos del mapa
        clearRoute();

        if (currentLocationCircle != null) {
            currentLocationCircle.remove();
            currentLocationCircle = null;
        }

        if (currentLocationPulse != null) {
            currentLocationPulse.remove();
            currentLocationPulse = null;
        }

        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }

        if (proximityCircle != null) {
            proximityCircle.remove();
            proximityCircle = null;
        }

        mMap = null;
        eventListener = null;
    }

    // ==================== MÉTODOS ADICIONALES PARA PUNTO AZUL ====================

    public void setLocationPointStyle(int fillColor, int strokeColor, float strokeWidth) {
        if (currentLocationCircle != null) {
            currentLocationCircle.setFillColor(fillColor);
            currentLocationCircle.setStrokeColor(strokeColor);
            currentLocationCircle.setStrokeWidth(strokeWidth);
        }
    }

    public void setPulseStyle(int color, float minRadius, float maxRadius) {
        if (currentLocationPulse != null) {
            currentLocationPulse.setStrokeColor(color);
            currentLocationPulse.setFillColor(Color.argb(76, Color.red(color), Color.green(color), Color.blue(color)));

            // Reiniciar animación con nuevos parámetros
            if (pulseAnimator != null) {
                pulseAnimator.cancel();
                pulseAnimator = ValueAnimator.ofFloat(minRadius, maxRadius);
                pulseAnimator.setDuration(1500);
                pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
                pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
                pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                pulseAnimator.addUpdateListener(animation -> {
                    if (currentLocationPulse != null) {
                        float radius = (Float) animation.getAnimatedValue();
                        currentLocationPulse.setRadius(radius);
                    }
                });

                pulseAnimator.start();
            }
        }
    }

    public boolean isLocationPointVisible() {
        return currentLocationCircle != null && currentLocationCircle.isVisible();
    }

    // ==================== GETTERS ====================

    public LatLng getCurrentLocation() { return currentLocation; }
    public LatLng getDestinationLocation() { return destinationLocation; }
    public boolean isRouteCalculating() { return isRouteCalculating; }
    public boolean hasDestination() { return destinationLocation != null; }
    public boolean hasRoute() { return currentRoute != null; }
}