package com.iot.stayflowdev.Driver.Helper;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.mapbox.geojson.Point;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class LocationHelper implements LocationListener{

    private static final String TAG = "LocationHelper";

    // Interfaces para comunicación con la Activity
    public interface LocationUpdateListener {
        void onLocationUpdate(Location location, String address, String timeString);
        void onLocationError(String error);
        void onProviderStateChanged(String provider, boolean enabled);
    }

    // Variables privadas
    private Context context;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private LocationUpdateListener listener;
    private boolean isTrackingLocation = false;
    private Point currentUserLocation;
    private String lastKnownAddress = "Obteniendo ubicación...";

    // Configuración
    private static final long UPDATE_TIME_INTERVAL = 5000L; // 5 segundos
    private static final float UPDATE_DISTANCE_INTERVAL = 10F; // 10 metros

    // Constructor
    public LocationHelper(Context context, LocationUpdateListener listener) {
        this.context = context;
        this.listener = listener;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    // ✅ VERIFICAR SI HAY PERMISOS DE UBICACIÓN
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // ✅ INICIAR SEGUIMIENTO DE UBICACIÓN
    @SuppressLint("MissingPermission")
    public boolean startLocationTracking() {
        if (!hasLocationPermission()) {
            if (listener != null) {
                listener.onLocationError("No hay permisos de ubicación");
            }
            return false;
        }

        if (locationManager == null) {
            if (listener != null) {
                listener.onLocationError("LocationManager no disponible");
            }
            return false;
        }

        try {
            // Verificar proveedores disponibles
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                if (listener != null) {
                    listener.onLocationError("GPS y Red deshabilitados. Activa el GPS.");
                }
                return false;
            }

            Log.d(TAG, "📡 GPS habilitado: " + isGPSEnabled);
            Log.d(TAG, "🌐 Red habilitada: " + isNetworkEnabled);

            // Solicitar actualizaciones
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        UPDATE_TIME_INTERVAL,
                        UPDATE_DISTANCE_INTERVAL,
                        this
                );
                Log.d(TAG, "✅ Actualizaciones GPS solicitadas");
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        UPDATE_TIME_INTERVAL,
                        UPDATE_DISTANCE_INTERVAL,
                        this
                );
                Log.d(TAG, "✅ Actualizaciones de Red solicitadas");
            }

            // Obtener última ubicación conocida
            getLastKnownLocation();

            isTrackingLocation = true;
            Log.d(TAG, "✅ Seguimiento de ubicación iniciado");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error iniciando ubicación: " + e.getMessage());
            if (listener != null) {
                listener.onLocationError("Error iniciando ubicación: " + e.getMessage());
            }
            return false;
        }
    }

    // ✅ OBTENER ÚLTIMA UBICACIÓN CONOCIDA
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        if (!hasLocationPermission()) return;

        try {
            Location lastKnownGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastKnownNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location bestLocation = null;
            if (lastKnownGPS != null && lastKnownNetwork != null) {
                bestLocation = lastKnownGPS.getTime() > lastKnownNetwork.getTime() ?
                        lastKnownGPS : lastKnownNetwork;
            } else if (lastKnownGPS != null) {
                bestLocation = lastKnownGPS;
            } else if (lastKnownNetwork != null) {
                bestLocation = lastKnownNetwork;
            }

            if (bestLocation != null) {
                Log.d(TAG, "✅ Ubicación inicial encontrada");
                onLocationChanged(bestLocation);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error obteniendo última ubicación: " + e.getMessage());
        }
    }

    // ✅ DETENER SEGUIMIENTO DE UBICACIÓN
    public void stopLocationTracking() {
        if (locationManager != null && isTrackingLocation) {
            locationManager.removeUpdates(this);
            isTrackingLocation = false;
            Log.d(TAG, "🔄 Seguimiento de ubicación detenido");
        }
    }

    // ✅ IMPLEMENTACIÓN DE LocationListener - NUEVA UBICACIÓN RECIBIDA
    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, String.format("📍 Nueva ubicación: %.6f, %.6f (Precisión: %.1fm)",
                location.getLatitude(), location.getLongitude(), location.getAccuracy()));

        // Guardar ubicación actual
        currentUserLocation = Point.fromLngLat(location.getLongitude(), location.getLatitude());

        // Obtener dirección real de forma asíncrona
        getRealAddressAsync(location);
    }

    // ✅ OBTENER DIRECCIÓN REAL DE FORMA ASÍNCRONA
    private void getRealAddressAsync(Location location) {
        new AsyncTask<Location, Void, String>() {
            @Override
            protected String doInBackground(Location... locations) {
                if (locations == null || locations.length == 0) return null;
                return getAddressFromLocation(locations[0]);
            }

            @Override
            protected void onPostExecute(String address) {
                if (address != null) {
                    lastKnownAddress = address;
                }

                // Crear string de tiempo
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String timeString = sdf.format(new Date());

                // Notificar a la Activity
                if (listener != null) {
                    listener.onLocationUpdate(location, lastKnownAddress, timeString);
                }
            }
        }.execute(location);
    }

    // ✅ CONVERTIR COORDENADAS A DIRECCIÓN REAL
    private String getAddressFromLocation(Location location) {
        try {
            if (!Geocoder.isPresent()) {
                Log.e(TAG, "❌ Geocoder no disponible");
                return String.format("%.4f, %.4f", location.getLatitude(), location.getLongitude());
            }

            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder direccionCompleta = new StringBuilder();

                // Construir dirección legible
                if (address.getThoroughfare() != null) {
                    direccionCompleta.append(address.getThoroughfare());
                }

                if (address.getSubThoroughfare() != null) {
                    direccionCompleta.append(" ").append(address.getSubThoroughfare());
                }

                if (address.getSubLocality() != null) {
                    if (direccionCompleta.length() > 0) direccionCompleta.append(", ");
                    direccionCompleta.append(address.getSubLocality());
                }

                if (address.getLocality() != null) {
                    if (direccionCompleta.length() > 0) direccionCompleta.append(", ");
                    direccionCompleta.append(address.getLocality());
                }

                // Si no hay dirección específica
                if (direccionCompleta.length() == 0) {
                    if (address.getFeatureName() != null) {
                        direccionCompleta.append(address.getFeatureName());
                    } else if (address.getLocality() != null) {
                        direccionCompleta.append(address.getLocality());
                    } else if (address.getAdminArea() != null) {
                        direccionCompleta.append(address.getAdminArea());
                    } else {
                        return String.format("%.4f, %.4f",
                                location.getLatitude(), location.getLongitude());
                    }
                }

                String resultado = direccionCompleta.toString();
                Log.d(TAG, "📍 Dirección obtenida: " + resultado);
                return resultado;

            } else {
                Log.d(TAG, "❌ No se encontraron direcciones");
                return String.format("%.4f, %.4f",
                        location.getLatitude(), location.getLongitude());
            }

        } catch (IOException e) {
            Log.e(TAG, "❌ Error en Geocoding: " + e.getMessage());
            return String.format("Error - %.4f, %.4f",
                    location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error general en geocoding: " + e.getMessage());
            return "Error obteniendo dirección";
        }
    }

    // ✅ OBTENER UBICACIÓN ACTUAL (PARA CENTRAR MAPA)
    public Point getCurrentLocation() {
        return currentUserLocation;
    }

    // ✅ OBTENER ÚLTIMA UBICACIÓN DE FORMA INMEDIATA
    @SuppressLint("MissingPermission")
    public Location getLastLocation() {
        if (!hasLocationPermission() || locationManager == null) return null;

        try {
            Location lastKnownGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastKnownNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (lastKnownGPS != null && lastKnownNetwork != null) {
                return lastKnownGPS.getTime() > lastKnownNetwork.getTime() ?
                        lastKnownGPS : lastKnownNetwork;
            } else if (lastKnownGPS != null) {
                return lastKnownGPS;
            } else {
                return lastKnownNetwork;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error obteniendo última ubicación: " + e.getMessage());
            return null;
        }
    }

    // ✅ VERIFICAR SI ESTÁ RASTREANDO
    public boolean isTracking() {
        return isTrackingLocation;
    }

    // ✅ IMPLEMENTAR OTROS MÉTODOS DE LocationListener
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d(TAG, "✅ Proveedor habilitado: " + provider);
        if (listener != null) {
            listener.onProviderStateChanged(provider, true);
        }
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d(TAG, "❌ Proveedor deshabilitado: " + provider);
        if (listener != null) {
            listener.onProviderStateChanged(provider, false);
        }
    }

    // ✅ LIMPIAR RECURSOS
    public void cleanup() {
        stopLocationTracking();
        listener = null;
        context = null;
    }
}
