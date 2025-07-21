package com.iot.stayflowdev.Driver.Activity;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

public class DistanceCalculator {

    private static final String TAG = "DistanceCalculator";
    // ⚠️ REEMPLAZA CON TU API KEY
    private static final String API_KEY = "AIzaSyArgXmLe6Bk6km2wSEN8RDpk2qbPd73ryc";
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    private RequestQueue requestQueue;

    public DistanceCalculator(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    // Interface para recibir resultados
    public interface DistanceCallback {
        void onSuccess(double distanceKm, int timeMinutes, String distanceText, String timeText);
        void onError(String error);
    }

    // Método principal para calcular distancia y tiempo
    public void calcularDistancia(double origenLat, double origenLng,
                                  double destinoLat, double destinoLng,
                                  DistanceCallback callback) {

        // Construir URL
        String url = BASE_URL + "?" +
                "origins=" + origenLat + "," + origenLng +
                "&destinations=" + destinoLat + "," + destinoLng +
                "&mode=driving" +
                "&language=es" +
                "&units=metric" +
                "&key=" + API_KEY;

        Log.d(TAG, "🗺️ Calculando ruta:");
        Log.d(TAG, "   Origen: " + origenLat + ", " + origenLng);
        Log.d(TAG, "   Destino: " + destinoLat + ", " + destinoLng);
        Log.d(TAG, "   URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "✅ Respuesta recibida de Google Maps");
                    procesarRespuesta(response, callback);
                },
                error -> {
                    Log.e(TAG, "❌ Error en petición: " + error.getMessage());
                    callback.onError("Error de red: " + error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    private void procesarRespuesta(JSONObject response, DistanceCallback callback) {
        try {
            Log.d(TAG, "📄 Procesando respuesta JSON...");

            // Verificar status general
            String status = response.getString("status");
            Log.d(TAG, "Status general: " + status);

            if (!"OK".equals(status)) {
                callback.onError("Error de API: " + status);
                return;
            }

            // Obtener rows
            JSONArray rows = response.getJSONArray("rows");
            if (rows.length() == 0) {
                callback.onError("No se encontraron rutas");
                return;
            }

            // Obtener elements del primer row
            JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
            if (elements.length() == 0) {
                callback.onError("No hay elementos de ruta");
                return;
            }

            // Obtener primer element
            JSONObject element = elements.getJSONObject(0);
            String elementStatus = element.getString("status");
            Log.d(TAG, "Status del elemento: " + elementStatus);

            if (!"OK".equals(elementStatus)) {
                callback.onError("No se pudo calcular la ruta: " + elementStatus);
                return;
            }

            // Extraer distancia
            JSONObject distanceObj = element.getJSONObject("distance");
            int distanceMeters = distanceObj.getInt("value"); // en metros
            String distanceText = distanceObj.getString("text"); // texto formateado
            double distanceKm = distanceMeters / 1000.0; // convertir a km

            // Extraer tiempo
            JSONObject durationObj = element.getJSONObject("duration");
            int durationSeconds = durationObj.getInt("value"); // en segundos
            String durationText = durationObj.getString("text"); // texto formateado
            int timeMinutes = durationSeconds / 60; // convertir a minutos

            // Logs informativos
            Log.d(TAG, "📏 Distancia:");
            Log.d(TAG, "   Metros: " + distanceMeters);
            Log.d(TAG, "   Kilómetros: " + distanceKm);
            Log.d(TAG, "   Texto: " + distanceText);

            Log.d(TAG, "⏱️ Tiempo:");
            Log.d(TAG, "   Segundos: " + durationSeconds);
            Log.d(TAG, "   Minutos: " + timeMinutes);
            Log.d(TAG, "   Texto: " + durationText);

            // Llamar callback con resultados
            callback.onSuccess(distanceKm, timeMinutes, distanceText, durationText);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error procesando JSON", e);
            callback.onError("Error procesando respuesta: " + e.getMessage());
        }
    }

    // Método de prueba con coordenadas fijas
    public void pruebaDistancia(DistanceCallback callback) {
        // Coordenadas de ejemplo: Hotel Monasterio (Cusco) al Aeropuerto Lima
        double origenLat = -13.5153; // Hotel Monasterio, Cusco
        double origenLng = -71.9769;
        double destinoLat = -12.0219; // Aeropuerto Jorge Chávez, Lima
        double destinoLng = -77.1143;

        Log.d(TAG, "🧪 PRUEBA: Calculando distancia Hotel Monasterio → Aeropuerto Lima");
        calcularDistancia(origenLat, origenLng, destinoLat, destinoLng, callback);
    }
}

// Clase de ejemplo para probar
class DistanceTest {

    public static void probarCalculadora(Context context) {
        DistanceCalculator calculator = new DistanceCalculator(context);

        calculator.pruebaDistancia(new DistanceCalculator.DistanceCallback() {
            @Override
            public void onSuccess(double distanceKm, int timeMinutes, String distanceText, String timeText) {
                Log.i("DistanceTest", "🎉 ¡ÉXITO!");
                Log.i("DistanceTest", "Distancia: " + distanceKm + " km (" + distanceText + ")");
                Log.i("DistanceTest", "Tiempo: " + timeMinutes + " min (" + timeText + ")");
            }

            @Override
            public void onError(String error) {
                Log.e("DistanceTest", "💥 ERROR: " + error);
            }
        });
    }
}
